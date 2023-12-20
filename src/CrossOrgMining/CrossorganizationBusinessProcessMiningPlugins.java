package CrossOrgMining;

import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.util.ui.widgets.helper.UserCancelledException;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.plugins.IMPetriNet;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMMiningDialog;

//import extract.OrganizationConfig;
//import extract.OrganizationIdentification;


public class CrossorganizationBusinessProcessMiningPlugins {
//	@SuppressWarnings("null")
	@Plugin(
             name = "Cross-organization Business Process Mining", 
         			parameterLabels = { "Event Log" }, 
        			returnLabels = { "Cross-organization Business Process Models" },
//        			returnTypes = { XLog.class },
        					returnTypes = {CrossOrganizationBusinessProcessModel.class},
             help = "."
     )
     @UITopiaVariant(
             affiliation = "SDUT", 
             author = "HuiLing Li", 
             email = "17864309765@163.com"
     )
	
	public CrossOrganizationBusinessProcessModel log12(UIPluginContext context,  XLog originalLog) throws UserCancelledException  {
		 
		CrossOrganizationBusinessProcessModel crossOrgModel = 
				new CrossOrganizationBusinessProcessModel();
		
		OrganizationConfig orgConfig = 
				OrganizationIdentification.identifyOrganizationsFromLog(originalLog);
		
		XFactory factory = new XFactoryNaiveImpl();
		
		//create separate event log for each organization
		HashMap<String, XLog> org2Log = 
				OrganizationLogConstruction.contructOrganizationLog(originalLog, orgConfig);

		//we use existing discovery algorithm to discover process model for each organization. 
		HashMap<String, Petrinet> org2PN = new HashMap<String, Petrinet>();
		double startTime_total=0;
		double endTime_total=0;
		int count =1;//the inductive miner parameters are set only for the first time
		IMMiningDialog dialog;
		InteractionResult result;
		MiningParameters IMparameters=null;
		String regEx =",";
		String org1 = null;
		XLog Merge = null;
		XLog OrgMerge = null;
		for(String or: org2Log.keySet()){
			if(Pattern.compile(regEx).matcher(or).find()) {
			org1 = or;
			Merge = org2Log.get(or);
			continue;
			}}
//		for(String org: org2Log.keySet()){
//			if(Pattern.compile(regEx).matcher(org).find()) {
//				org1 = org;
//				Merge = org2Log.get(org);
//				break;
//				}}
 		for(String org: org2Log.keySet()){
			/****************************************找到组织混在一起的地方***********************************/
//			Pattern.compile(regEx).matcher(org).find(); //判断是否有共同活动, 输出为布尔值
			if(Pattern.compile(regEx).matcher(org).find()) {
				continue;
				}		
//			System.out.println("org1:"+org1);
//			System.out.println("org:"+org);			
			if((org1 != null)&&(org1.contains(org))) {
				OrgMerge = ORGMERGE(Merge, org2Log.get(org));
			  }
			else OrgMerge = org2Log.get(org);
//			if(org1.contains(org)) {
//				OrgMerge = ORGMERGE(Merge, org2Log.get(org));				
//			}
//			else {OrgMerge = org2Log.get(org); }
			/************************************************************************************/	
			//for the first time, we set the parameter
			if(count ==1){
				//set the inductive miner parameters, the original log is used to set the classifier
//				dialog = new IMMiningDialog(org2Log.get(org));
				dialog = new IMMiningDialog(OrgMerge);
				result = context.showWizard("Configure Parameters for "
						+ "Inductive Miner (used for all intra-organization models)", true, true, dialog);
				System.out.println("result:"+result);
				if (result != InteractionResult.FINISHED) {
					return null;
				}
				// the mining parameters are set here 
				IMparameters = dialog.getMiningParameters(); //IMparameters.getClassifier()
			}		
			
			startTime_total=System.currentTimeMillis();		
			//use the inductive miner to discover a pn for each organization. 
			Object[] objs =IMPetriNet.minePetriNet(org2Log.get(org), IMparameters, new Canceller() {
				public boolean isCancelled() {
					return false;
				}
			});		
			
			//check the single entry and single exist property //检查单个条目和单个存在属性
			Petrinet pn =TransformCrossOrganizationBusinessProcessModel2PetriNet.
					addingArtifitialSouceandTargetPlaces((Petrinet) objs[0], 
							(Marking)objs[1], (Marking)objs[2]);
					
			org2PN.put(org, pn);
//			System.out.println("count:"+count);
//			System.out.println("org:"+org);
			count++;
		}
		
	//	System.out.println("count:"+count);
		crossOrgModel.setOrganizationModels(org2PN); 								
				
/*******************************************************************************************************************/					
/*********************************************************************************************************************/					
		HashMap<HashSet<String>, XLog> ActivitySet2OrgLog = new HashMap<>();
		for(XTrace trace: originalLog){
			HashSet<String> activitySet =activitySetPerTrace(trace);
						
			if(!ActivitySet2OrgLog.containsKey(activitySet)){
				//create the sub log
				XLog subLog = factory.createLog();
				subLog.add(trace);							
				ActivitySet2OrgLog.put(activitySet, subLog);
				
			}
			else{
				ActivitySet2OrgLog.get(activitySet).add(trace);
		 	}
		 }
		
		System.out.println("<<<<<<<<<<<<<<< the number of subLog"+
				ActivitySet2OrgLog.keySet().size()+" >>>>>>>>>>>>>>>");
//		System.out.println("1111111111111111111111111111111111111111111111111111");
				
		//discovered interactions from the current log
		//从当前日志中发现交互
		HashSet<CrossOrganizationInteraction> interactions = CrossOrganizationInteractionRelationDiscovery.
								discoverCrossOrganizationInteractions(orgConfig, originalLog);

//	    System.out.println("2222222222222222222222222222222222222222222222222222");		
		for(CrossOrganizationInteraction i: interactions){
			System.out.println("+++++++++++++ discovered Interactions: "+i);
		 }
						
		//obtain the combined interactions, the combination may cause some inaccuracy. 
		//获得组合的交互,组合可能会导致一些不准确。
		crossOrgModel.setAllInteractions(interactions);	
//		}
						
		endTime_total=System.currentTimeMillis();			

        System.out.println("***************");
		System.out.println("Inductive 33333333333333:"+(endTime_total-startTime_total));
		System.out.println("***************");
		return crossOrgModel;
	}
			
	

			//get the activity set of a trace
			public static HashSet<String> activitySetPerTrace(XTrace trace)
			{
				HashSet<String> activitySet = new HashSet<>();
				for(XEvent event: trace)
				{
					activitySet.add(XConceptExtension.instance().extractName(event));
				}
				return activitySet;
			}
			
			/*
			 * reture the common elements of two hashset
			 */
			public static HashSet<CrossOrganizationInteraction> 
			getcommonInteraction(HashSet<CrossOrganizationInteraction> group1, 
					HashSet<CrossOrganizationInteraction> group2)
			{
				HashSet<CrossOrganizationInteraction> temp1 = new HashSet<CrossOrganizationInteraction>();
				temp1.addAll(group1);
				temp1.retainAll(group2);
				
				
				return temp1;
			}
/*******后加的****************************************************************************/			
			private XLog ORGMERGE(XLog merge, XLog xLog) {
				
				//trace.getAttributes().get("concept:name").toString();
				
				for (XTrace traceLog: xLog){//xLog.add(trace);
					//for(XEvent eventMerge: traceMerge ){
						for (XTrace traceMerge: merge){       //xLog.add(trace);
//							System.out.println("traceLog:"+traceMerge.getAttributes().get("concept:name").toString());
//							System.out.println("traceMerge:"+traceMerge.getAttributes().get("concept:name").toString());
							if(traceMerge.getAttributes().get("concept:name").toString() == traceLog.getAttributes().get("concept:name").toString()) {
								for(XEvent eventLog: traceMerge ){
									traceLog.add(eventLog);
//									System.out.println("eventLog:"+eventLog.getID());
								}
							}
						}
					//}
				}
				return xLog;
			}
/**********************************************************************************/			
			
		}
