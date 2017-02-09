package com.cqupt.util;

import java.util.HashMap;
import java.util.List;

import org.ksoap2.serialization.SoapObject;

import android.os.AsyncTask;

import com.cqupt.db.DBManager;
import com.cqupt.model.Plan;
import com.cqupt.model.PlanChoice;
import com.cqupt.model.PlanTestArrange;
import com.cqupt.net.WebService;

/** 
 * ������Ҫ����ͬ���ƻ�(�ϴ�������)�ĺ�̨�߳�
 * */
 
public class SyncPlanAsyncTask {
	
	private final static int SUCCESS = 1;
	private final static int FAIL = 0;
	
	public static interface OnProcessChangeListener{
		public void onTaskStart();
		public void onTaskFinished();
		public void onTaskFailed();
	}
	
	public static class SyncFromWebToLocalThread extends AsyncTask<Void, Void, Integer>{
		
		private OnProcessChangeListener listener;
		private DBManager db;
		private String userID;
		private int planID;
		
		/**
		 * @param planID < 0 ͬ��ȫ���ƻ��� >= 0 ͬ��һ���ƻ�
		 */
		public SyncFromWebToLocalThread(DBManager db,String userID,int planID,OnProcessChangeListener listener) {
			this.db = db;
			this.userID = userID;
			this.listener = listener;
			this.planID = planID;
		}
		
		@Override
		protected void onPreExecute() {
			
			if(listener != null){
				listener.onTaskStart();
			}
			
		}

		@Override
		protected Integer doInBackground(Void... params) {
			
			WebService web = new WebService();
			HashMap<String, String> p = new HashMap<String, String>();
			SoapObject result = null;
			List<Plan> localPlanList = null;
			List<Plan> webPlanList = null;
			
			if(planID < 0){//ͬ��ȫ���ƻ�
				p.put("userID", userID);
				result = web.CallWebService("getPlanList", p);
				
				if(result == null){
					return FAIL;
				}
							
				localPlanList = db.getPlanListForWebSync(userID);
				
			}else{//ͬ��һ���ƻ�
				p.put("createTime", db.getPlanListForWebSync(userID, planID).get(0).getCreateTime());
				result = web.CallWebService("getPlanByPlanCreateTime", p);
				
				if(result == null){
					return FAIL;
				}
				
				localPlanList = db.getPlanListForWebSync(userID, planID);
			}
			
			//ɾ�����غͷ�������ͬ��plan
			webPlanList = XMLParser.parsePlanList(result);
			localPlanList.removeAll(webPlanList);
			
			for(Plan plan : localPlanList){
				db.deletePlan(plan.getPlanID());
				db.deletePlanChoice(plan.getPlanID());
			}
			
			//�Է�����Ϊ׼ ����planList
			db.updatePlan(webPlanList);
			
			//���¼ƻ���ҵ���ű�
			List<PlanTestArrange> webPlanTestArrangeList = null;
			List<PlanTestArrange> localPlanTestArrangeList = null;
			
			if(planID < 0){//ͬ��ȫ���ƻ�
				result = web.CallWebService("getPlanTestArrange", p);
				
				if(result == null){
					return FAIL;
				}
				
				localPlanTestArrangeList = db.getPlanTestArrange(userID);
			}else{//ͬ��һ���ƻ�
				result = web.CallWebService("getPlanTestArrangeForSync", p);
				
				if(result == null){
					return FAIL;
				}
				
				localPlanTestArrangeList = db.getPlanTestArrange(planID);
			}
			
			//ɾ�����ض���ļƻ���ҵ����
			webPlanTestArrangeList = XMLParser.parsePlanTestArrange(result);
			localPlanTestArrangeList.removeAll(webPlanTestArrangeList);
			
			for(PlanTestArrange pta : localPlanTestArrangeList){
				planID = db.getPlanID(pta.getCreateTime());
				db.deletePlanTestArrange(planID);
			}
			
			//�Է�����Ϊ׼ ���¼ƻ���ҵ���ű�
			int tempID = -1;
			String tempCreateTime = "";
			for(PlanTestArrange pta : webPlanTestArrangeList){
				
				if(!pta.getCreateTime().equalsIgnoreCase(tempCreateTime)){
					tempID = db.getPlanID(pta.getCreateTime());
					tempCreateTime = pta.getCreateTime();
				}
				
				//���ز����мƻ��е���Ŀ������Ҫ��web������
				if(!db.checkTestID(pta.getTestID())){
					HashMap<String,String> map = new HashMap<String, String>();
					map.put("testID", String.valueOf(pta.getTestID()));
					
					result = web.CallWebService("getTest", map);
					
					if(result != null){
						new DownloadTest(db).downloadTest(XMLParser.parseTestList(result));
					}
				}
				
				db.addPlanTestArrange(tempID, pta.getTestID());
				
			}
				
			//���¼ƻ��𰸱�
			
			if(planID < 0){
				result = web.CallWebService("getPlanChoice", p);
			}else{
				result = web.CallWebService("getPlanChoiceByPlanCreateTime", p);
			}
			
			if(result != null){
			
				for(PlanChoice pc : XMLParser.parsePlanChoice(result)){
					
					if(!pc.getCreateTime().equalsIgnoreCase(tempCreateTime)){
						tempID = db.getPlanID(pc.getCreateTime());
						tempCreateTime = pc.getCreateTime();
					}
					
					pc.setPlanID(tempID);
					db.addPlanChoice(pc);
					
				}
				
			}
			
			return SUCCESS;
			
		}

		@Override
		protected void onPostExecute(Integer result) {
					
			if(result == FAIL){
				if(listener != null){
					listener.onTaskFailed();;
				}
			}else{
				if(listener != null){
					listener.onTaskFinished();
				}
			}
			
		}
		
	}

	public static class SyncFromLocalToWebThread extends AsyncTask<Void, Void, Integer>{
		
		private OnProcessChangeListener listener;
		private List<Plan> planList;
		private List<PlanTestArrange> planTestArrangeList;
		private List<PlanChoice> planChoiceList;
		private String userID;
		private int type;
		
		/**
		 * @param type 0 ��ʾ����plan�б�һ��ͬ�� ��1 ��ʾͬ��ĳһ��plan
		 */
		public SyncFromLocalToWebThread(List<Plan> planList,List<PlanTestArrange> planTestArrangeList
				,List<PlanChoice> planChoiceList,OnProcessChangeListener listener,
				String userID,int type){
			this.planList = planList;
			this.planTestArrangeList = planTestArrangeList;
			this.planChoiceList = planChoiceList;
			this.listener = listener;
			this.userID = userID;
			this.type = type;
		}
		
		@Override
		protected void onPreExecute() {
			
			if(listener != null){
				listener.onTaskStart();
			}
			
		}


		@Override
		protected Integer doInBackground(Void... params) {
			WebService web = new WebService();
			HashMap<String, String> p = new HashMap<String, String>();
			
			StringBuilder planCreateTimeSB = new StringBuilder();
			StringBuilder planTitleSB = new StringBuilder();		
			StringBuilder planStartDateSB = new StringBuilder();
			StringBuilder planEndDateSB = new StringBuilder();
			
			p.put("planUserID", userID);
			p.put("type", String.valueOf(type));
			
			if(planList.size() != 0){
				
				for(Plan plan : planList){
					planCreateTimeSB.append(plan.getCreateTime() + ",");
					planTitleSB.append(plan.getTitle() + ",");			
					planStartDateSB.append(plan.getsDate() + ",");
					planEndDateSB.append(plan.geteDate() + ",");
				}
				
			}
			
			p.put("planCreateTime", planCreateTimeSB.toString());
			p.put("planTitle", planTitleSB.toString());
			p.put("planStartDate", planStartDateSB.toString());
			p.put("planEndDate", planEndDateSB.toString());
			
			if(planTestArrangeList.size() == 0){
				p.put("planTestArrangeCreateTime", "");
				p.put("planTestArrangeTestID", "");
				p.put("planChoiceCreateTime", "");
				p.put("planChoiceItemID", "");
				p.put("planChoiceAnswer", "");
				p.put("planChoiceSubmitTime", "");
			}else{
				
				StringBuilder planTestArrangeCreateTimeSB = new StringBuilder();
				StringBuilder planTestArrangeTestIDSB = new StringBuilder();
				
				for(PlanTestArrange pta : planTestArrangeList){
					planTestArrangeCreateTimeSB.append(pta.getCreateTime() + ",");
					planTestArrangeTestIDSB.append(pta.getTestID() + ",");
				}
				
				p.put("planTestArrangeCreateTime", planTestArrangeCreateTimeSB.toString());
				p.put("planTestArrangeTestID", planTestArrangeTestIDSB.toString());
				
				if(planChoiceList.size() == 0){
					p.put("planChoiceCreateTime", "");
					p.put("planChoiceItemID", "");
					p.put("planChoiceAnswer", "");
					p.put("planChoiceSubmitTime", "");
				}else{
					StringBuilder planChoiceCreateTimeSB = new StringBuilder();
					StringBuilder planChoiceItemIDSB = new StringBuilder();
					StringBuilder planChoiceAnswerSB = new StringBuilder();
					StringBuilder planChoiceSubmitTimeSB = new StringBuilder();
					
					for(PlanChoice pc : planChoiceList){
						planChoiceCreateTimeSB.append(pc.getCreateTime() + ",");
						planChoiceItemIDSB.append(pc.getItemID() + ",");
						planChoiceAnswerSB.append(pc.getAnswer() + "eXiT");
						planChoiceSubmitTimeSB.append(pc.getSubmitTime() + ",");
					}
					
					p.put("planChoiceCreateTime", planChoiceCreateTimeSB.toString());
					p.put("planChoiceItemID", planChoiceItemIDSB.toString());
					p.put("planChoiceAnswer", planChoiceAnswerSB.toString());
					p.put("planChoiceSubmitTime", planChoiceSubmitTimeSB.toString());
				}
			}
			
			SoapObject result = web.CallWebService("uploadPlanList", p);
			
			if(result == null){
				return FAIL;
			}
			
			return XMLParser.parseBoolean(result).equals("true") ? SUCCESS : FAIL;

		}

		
		@Override
		protected void onPostExecute(Integer result) {
			
			if(result == FAIL){
				if(listener != null){
					listener.onTaskFailed();
				}
			}else{
				if(listener != null){
					listener.onTaskFinished();
				}
			}
		}
		
	}  

}
