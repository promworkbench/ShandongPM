package CrossOrgMining;

import java.util.HashMap;
import java.util.Set;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

//import extract.OrganizationConfig;

public class OrganizationLogConstruction {

	/*
	 * this class construct the event log for each organization. 
	 */
	public static HashMap<String, XLog> contructOrganizationLog(XLog originalLog, OrganizationConfig orgConfig) 
	{
		//boolean flag
		
		HashMap<String, XLog> org2Log = new HashMap<>();
		XFactory factory = new XFactoryNaiveImpl();
		for(String org: orgConfig.getAllOrganizations())
		{			
			//the activity set of the current organization //当前组织的活动集
/**********************************************************************************************************/			
//            //判断是否是多个组织公共的活动
//			Pattern p = Pattern.compile(",", Pattern.CASE_INSENSITIVE);
//			Matcher m = p.matcher(org);
//			boolean flag = m.find();
////			System.out.println(b);
//			if (flag) {//如果是公共的活动,则将含有此公共活动的组织都分开,将公共活动添加到使用该活动的组织中
//
//				String[] spiltOrg=org.split(",");
//				
//				for(int k=0;k<spiltOrg.length;k++) {
//					String publicOrg = spiltOrg[k].trim();
//					System.out.println("publicOrg:"+publicOrg);
//					
//					}
//				
//				
//				String regEx="[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
//				Pattern pattern=Pattern.compile(regEx);
//				Matcher match =pattern.matcher(org);
//				String aa = "";
//				String newString = m.replaceAll(aa).trim(); 
//				 
//			   System.out.println("activitySet:"+orgConfig.getActivities(org)); //////可以找到, 在这里分开
//			}
	/************************************************************************************************************/		
			Set<String> activitySet = orgConfig.getActivities(org);				
			XLog orgLog = InitializeEventLog.initializeEventLog(factory, org);
			
			for(XTrace trace: originalLog)
			{
				XTrace orgTrace = factory.createTrace();
				XConceptExtension.instance().assignName(orgTrace, XConceptExtension.instance().extractName(trace));//assign the original name
				for(XEvent event: trace)
				{
					if(activitySet.contains(XConceptExtension.instance().extractName(event)))//if the activity belong to the org
					{						
//						System.out.println("******************************************");
						orgTrace.insertOrdered(event);
					}
				}
				if(orgTrace.size()!=0)
				{
					orgLog.add(orgTrace);
				}
			}
			if(orgLog.size()!=0)
			{
				org2Log.put(org, orgLog);
			}
		}//String org: orgConfig.getAllOrganizations()
		return org2Log;
		
	}
}
