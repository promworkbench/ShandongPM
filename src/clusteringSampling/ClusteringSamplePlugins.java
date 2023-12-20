package clusteringSampling;


/*
 * A fast algorithm to sample log. 
 * The original idea is referred to "The automatic creation of literature abstracts". 
 * 
 * rank a trace by its significance;
 * the significance of a trace is determined by the combination of its activity significance and dfr significance. 
 * 日志完整性采样
 */

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XLogImpl;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.plugin.events.Logger.MessageLevel;
import org.processmining.framework.util.ui.widgets.helper.ProMUIHelper;
import org.processmining.framework.util.ui.widgets.helper.UserCancelledException;


public class ClusteringSamplePlugins {
	@Plugin(
			name = "Clustering-based Event Log Sampling",// plugin name
			returnLabels = {"Sample Log"}, //return labels
			returnTypes = {XLog.class},//return class
			parameterLabels = {"Orginal Event Log"},
			userAccessible = true,
			help = "This plugin aims to the orginal log ,returns a sample log  by clustering-based sampling methods." 
			)
	@UITopiaVariant(
	        affiliation = "TU/e", 
	        author = "XXX", 
	        email = "XXX"
	        )
	@PluginVariant(
			variantLabel = "Sampling Big Event Log, default",
			// the number of required parameters, {0} means one input parameter
			requiredParameterLabels = {0}
			)
	public static XLog GraphSampling(UIPluginContext context,XLog originalLog) throws UserCancelledException
	{
		//UI
		//select two types of event logs, lifecycle event log and normal event log.  
		String [] GraphSamplingTechnique = new String[4];
		GraphSamplingTechnique[0]="K-means";
		GraphSamplingTechnique[1]="ActiTrac";
		GraphSamplingTechnique[2]="Guide Miner Tree";
		String selectedTechnique =ProMUIHelper.queryForObject(context, "Select the clustering techniques", GraphSamplingTechnique);
		context.log("The selected graph sampling technique is: "+selectedTechnique, MessageLevel.NORMAL);	
		System.out.println("Selected selected graph sampling technique is: "+selectedTechnique);
		
		//select two types of event logs, lifecycle event log and normal event log.  
		String [] logType = new String[2];
		logType[0]="LogRank-based Sampling";
		logType[1]="LogRank+-based Sampling";
		String selectedLogType =ProMUIHelper.queryForObject(context, "Select the sampling techniques", logType);
		context.log("The selected log type is: "+selectedLogType, MessageLevel.NORMAL);	
		System.out.println("Selected log type is: "+selectedLogType);
				
		

		
		//create a new log with the same log-level attributes. 
		XLog sampleLog = new XLogImpl(originalLog.getAttributes());
		//keep an ordered list of traces names. 
		ArrayList<String> TraceIdList = new ArrayList<>();
		
		//convert the log to a map, the key is the name of the trace, and the value is the trace. 
		HashMap<String, XTrace> TraceID2Trace = new HashMap<>();
		for(XTrace trace: originalLog)
		{
			TraceIdList.add(trace.getAttributes().get("concept:name").toString());
			TraceID2Trace.put(trace.getAttributes().get("concept:name").toString(), trace);
		}
		for(int i=0;i<TraceIdList.size();i++) {
			System.out.println("TraceIdList"+i+":"+TraceIdList.get(i));
			System.out.println(TraceID2Trace.get(TraceIdList.get(i)));
		}
		//trace to activity set
		HashMap<String, HashSet<String>> traceIDToActivitySet = new HashMap<>();
		
		//trace to direct-follow relation set
		HashMap<String, HashSet<String>> traceIDToDFRSet = new HashMap<>();
		for(XTrace trace:originalLog)
		{
			HashSet<String> activitySet = new HashSet<>();
			HashSet<String> dfrSet = new HashSet<>();
			for(int i =0;i<trace.size();i++)
			{
				//add activity
				activitySet.add(XConceptExtension.instance().extractName(trace.get(i)));
			}
			for(int i =0;i<trace.size()-1;i++)
			{
				//add directly follow pair
				dfrSet.add(XConceptExtension.instance().extractName(trace.get(i))+","+XConceptExtension.instance().extractName(trace.get(i+1)));
			}
			traceIDToActivitySet.put(XConceptExtension.instance().extractName(trace), activitySet);
			traceIDToDFRSet.put(XConceptExtension.instance().extractName(trace), dfrSet);
		}
		
		//direct-follow relation set of the log
		HashSet<String> dfrSetLog = new HashSet<>();
		for(String traceID: traceIDToDFRSet.keySet())
		{
			dfrSetLog.addAll(traceIDToDFRSet.get(traceID));
		}
		//得到日志的开始-结束集合 遍历日志轨迹
		//trace to direct-follow relation set
//		HashSet<String> initialSet=
//		HashMap<String, HashSet<String>> traceIDToDFRSet = new HashMap<>();
		for(XTrace trace:originalLog)
		{
			HashSet<String> activitySet = new HashSet<>();
			HashSet<String> dfrSet = new HashSet<>();
			for(int i =0;i<trace.size();i++)
			{
				//add activity
				activitySet.add(XConceptExtension.instance().extractName(trace.get(i)));
			}
		}
		 /**
		  * 0.策略类
		  * （1）基于长度的
		  * （2）基于频率的
		  */
		 /*****************************策略1：基于长度的******************/
		if(selectedTechnique.equals("Trace Length-based Graph Sampling"))
		{
			HashMap<XTrace,String> traceLabel=new HashMap<>();
			HashSet<String> traceVariantLabel=new HashSet<>();
			HashMap<XTrace,Integer> traceLength=new HashMap<>();
			//step0:获得每个轨迹对应的长度,对轨迹长度进行存储
			for (XTrace trace: originalLog)	{   ///all trace size!=0
				String mergeTransition="";
				for(XEvent event: trace){  
					String transition = event.getAttributes().get("concept:name").toString();       //Resource
					mergeTransition=mergeTransition+transition;
				}
				traceVariantLabel.add(mergeTransition);
				traceLabel.put(trace, mergeTransition);
				traceLength.put(trace, mergeTransition.length());
			}
			
			//step1-0:获得单一变体 variant
			for(XTrace trace:originalLog) {
				String m1=traceLabel.get(trace);
				if(traceVariantLabel.contains(m1)) {
					traceVariantLabel.remove(m1);
				}else {
					traceLength.remove(trace);//删除轨迹相同的轨迹
				}
			}
			//step2-1:排序，降序   根据值对键排序 traceFrequency
		      List<Map.Entry<XTrace,Integer>> list1 = new ArrayList<Map.Entry<XTrace,Integer>>(traceLength.entrySet());
		      list1.sort(new Comparator<Map.Entry<XTrace,Integer>>() {
		            @Override
		            public int compare(Map.Entry<XTrace,Integer> o1, Map.Entry<XTrace,Integer> o2) {
		                return o2.getValue().compareTo(o1.getValue());
		            }
		       });
		       //step3:填充
		       for (int i = 0; i < list1.size(); i++) {
		           System.out.println("第i次： " + i);
		           System.out.println(list1.get(i).getValue()+ ": " + list1.get(i).getKey() ); 
		           System.out.println( "name: " + XConceptExtension.instance().extractName(list1.get(i).getKey()));
		           System.out.println( "dfrSet: " + traceIDToDFRSet.get(XConceptExtension.instance().extractName(list1.get(i).getKey())));
		           System.out.println( "总的dfrSetLog: " + dfrSetLog);
		           //取差集
		         if(dfrSetLog.removeAll(traceIDToDFRSet.get(XConceptExtension.instance().extractName(list1.get(i).getKey())))) {
						sampleLog.add(list1.get(i).getKey());
				 }
	//	          System.out.println( "操作后的dfrSetLog: " + dfrSetLog);
				  if(dfrSetLog.size()==0) break;	  
		        }
		}
		/********************************策略2：基于轨迹频数的******************************/
		if(selectedTechnique.equals("Trace Frequency-based Graph Sampling")) {
			HashMap<XTrace,String> compareString=new HashMap<>();
			HashSet<String> compareString1=new HashSet<>();
			for (XTrace trace: originalLog)	{   ///all trace size!=0
				String mergeTransition="";
				for(XEvent event: trace){  
					String transition = event.getAttributes().get("concept:name").toString();       //Resource
					mergeTransition=mergeTransition+transition;
				}
				compareString1.add(mergeTransition);
				compareString.put(trace, mergeTransition);
			}
	        //step1:对轨迹频数进行计数
			HashMap<XTrace,Integer> traceFrequency=new HashMap<>();
			for(XTrace trace1:originalLog)
			{
				int count=0;
				for(XTrace trace2:originalLog)
				{
					if(compareString.get(trace1).equals(compareString.get(trace2))) {
						count++;
					}
				}
				traceFrequency.put(trace1, count);
			}
			//step2-0:获得单一变体 variant
			for(XTrace trace:originalLog) {
				String m1=compareString.get(trace);
				if(compareString1.contains(m1)) {//hashset
					compareString1.remove(m1);
				}else {
					traceFrequency.remove(trace);//删除轨迹相同的轨迹
				}
			}
//			 for (XTrace key : traceFrequency.keySet()){
//					System.out.println("traceFrequency key: "+ XConceptExtension.instance().extractName(key) + "; traceFrequency value: " + traceFrequency.get(key));
//			 }
			//step2-1:排序，降序   根据值对键排序 traceFrequency
		      List<Map.Entry<XTrace,Integer>> list1 = new ArrayList<Map.Entry<XTrace,Integer>>(traceFrequency.entrySet());
		      list1.sort(new Comparator<Map.Entry<XTrace,Integer>>() {
		            @Override
		            public int compare(Map.Entry<XTrace,Integer> o1, Map.Entry<XTrace,Integer> o2) {
		                return o2.getValue().compareTo(o1.getValue());
		            }
		       });
		       //step3:填充
		       for (int i = 0; i < list1.size(); i++) {
		           System.out.println("第i次： " + i);
		           System.out.println(list1.get(i).getValue()+ ": " + list1.get(i).getKey() ); 
		           System.out.println( "name: " + XConceptExtension.instance().extractName(list1.get(i).getKey()));
		           System.out.println( "dfrSet: " + traceIDToDFRSet.get(XConceptExtension.instance().extractName(list1.get(i).getKey())));
		           System.out.println( "总的dfrSetLog: " + dfrSetLog);
		           //取差集
		         if(dfrSetLog.removeAll(traceIDToDFRSet.get(XConceptExtension.instance().extractName(list1.get(i).getKey())))) {
						sampleLog.add(list1.get(i).getKey());
				 }
//		          System.out.println( "操作后的dfrSetLog: " + dfrSetLog);
				  if(dfrSetLog.size()==0) break;	  
		        }
		}	
		
		/**
		  * 方法2.暴力法 常规做法 有不确定性
		  * dfrSetLog:表示日志中所有的直接跟随活动关系数    HashSet<String>
		  * traceIDToDFRSet ：key：轨迹名；value:直接跟随活动 HashMap<String, HashSet<String>>
		  */
		HashSet<String> StartSet = new HashSet<>();
		HashSet<String> EndSet = new HashSet<>();
		HashSet<String> StartAndEndSet = new HashSet<>();
		for(XTrace trace:originalLog)
		{
//			HashSet<String> activitySet = new HashSet<>();
//			HashSet<String> dfrSet = new HashSet<>();
			StartSet.add(XConceptExtension.instance().extractName(trace.get(0)));
			EndSet.add(XConceptExtension.instance().extractName(trace.get(trace.size()-1)));
			StartAndEndSet.add(XConceptExtension.instance().extractName(trace.get(0))+","+
			XConceptExtension.instance().extractName(trace.get(trace.size()-1)));
		}
		System.out.println("StartSet-------------------------->"+StartSet);
		System.out.println("EndSet---------------------------->"+EndSet);
		System.out.println("StartSet.size()--------------+++++++++++>"+StartSet.size());
		System.out.println("EndSet.size()--------------+++++++++++>"+EndSet.size());
		if(selectedTechnique.equals("Brute Force Graph Sampling")) {
			 
//			 for(int i=0;i<TraceIdList.size();i++) {
////				 System.out.println("第i次遍历:"+i+"dfrSetLog:"+dfrSetLog);
//				 //差集 dfrSetLog与新轨迹有交集那么将其加入到采样日志中
//				 if(dfrSetLog.removeAll(traceIDToDFRSet.get(TraceIdList.get(i)))) {
//					 sampleLog.add(TraceID2Trace.get(TraceIdList.get(i)));
//				 }
//				 if(dfrSetLog.size()==0) break;	 
//				 
//			 }
			 
			 
//			 for(XTrace trace:originalLog)
//				{
//					
//					if(dfrSetLog.removeAll(traceIDToDFRSet.get(XConceptExtension.instance().extractName(trace))))
//					{
//						sampleLog.add(trace);
//					}
//					//dfrSetLog.remove(traceIDToDFRSet);
//					//System.out.println("//////dfrSetLog//////////");	
//					 if(dfrSetLog.size()==0) break;	 
//				}
//			System.out.println("/////第一个//////");
			for(XTrace trace:originalLog)
			{	
				System.out.println("////////////////////////////");
				String StartEvent = XConceptExtension.instance().extractName(trace.get(0));
				String EndEvent = XConceptExtension.instance().extractName(trace.get(trace.size()-1));
				String StartAndEndEvent = StartEvent+","+EndEvent;
				System.out.println("11111111111111111111");
//				if((StartAndEndSet.size() != 0) || (dfrSetLog.size() !=0)){
//					int flag1=0;
//					int flag2=0;
//					System.out.println("222处理前：2222");
//					System.out.println("StartAndEndEvent："+StartAndEndEvent);
//					System.out.println("StartAndEndSet："+StartAndEndSet);
//					System.out.println("dfrSetLog："+dfrSetLog);
//					if(StartAndEndSet.remove(StartAndEndEvent)) {
//						flag1=1;
//					}
//					if(dfrSetLog.removeAll(traceIDToDFRSet.get(XConceptExtension.instance().extractName(trace))))
//					{
//						flag2=1;
//					}
//					if((flag1 ==1) || (flag2 ==1)) {
//						System.out.println("true!!!!!!!!!!!!!!!");
//						sampleLog.add(trace);
//					}
//				}else {
//					System.out.println("333333333333333333333333");
//					break;
//				}
//				System.out.println("******************************");
				if((StartSet.size()!=0)||(EndSet.size()!=0)||(dfrSetLog.size()!=0)) {
					
					int flag1=0;
					int flag2=0;
					int flag3=0;
					System.out.println("222处理前：2222");
					System.out.println("StartSet："+StartSet);
					System.out.println("EndSet："+EndSet);
					System.out.println("StartEvent:"+StartEvent);
					System.out.println("EndEvent:"+EndEvent);
					if(StartSet.remove(StartEvent)) {
						flag1=1;
					}
					if(EndSet.remove(EndEvent))
					{
						flag2=1;
					}
					if(dfrSetLog.removeAll(traceIDToDFRSet.get(XConceptExtension.instance().extractName(trace))))
					{
						flag3=1;
					}
					if((flag1==1)||(flag2==1)||(flag3==1))
					{
						System.out.println("true!!!!!!!!!!!!!!!");
						sampleLog.add(trace);
					}
					System.out.println("StartSet.size():"+StartSet.size());
					System.out.println("EndSet.size():"+EndSet.size());
					System.out.println("dfrSetLog.size():"+dfrSetLog.size());
				}else{
					break;
				}
				System.out.println("******************************");
			}
		}
		
		/**
		 * 方法3.集合覆盖算法
		 */	
		if(selectedTechnique.equals("Simple Set covering Graph Sampling")) {

//			 创建广播电台,放入到Map
	        HashMap<String,HashSet<String>> broadcasts = new HashMap<String, HashSet<String>>();
	        broadcasts=traceIDToDFRSet;//存放所有的轨迹
	        //allAreas 存放所有的地区
	        HashSet<String> allAreas = new HashSet<String>();
	        allAreas=dfrSetLog;
	        //已选择的地区
	        ArrayList<String> selects = new ArrayList<String>();
	        //临时集合,存放临时选择的电塔包含的地区
	        HashSet<String> tempSet = new HashSet<String>();
	        //存放轨迹
	        HashSet<XTrace> traceSet = new HashSet<XTrace>();
	        //指向本轮最优解的指针
	        String maxKey = null;
	        //当剩余的地区不为0时,持续选择
	        while (allAreas.size() != 0) {
	            //指针置空
	            maxKey = null;
	            //遍历所有的灯塔,找出最优解
	            for (String key : broadcasts.keySet()) {
	                //临时集合置空
	                tempSet.clear();
	                //从broadcast的一个value中取出所有地区,加入tempSet
	                HashSet<String> areas = broadcasts.get(key);
	                tempSet.addAll(areas);
	                //求tempSet和剩余未选地区的子集并赋值给tempSet
	                tempSet.retainAll(allAreas);
	                //如果tempSet不为0,且maxKey为空或者当前tempSet的地区数量大于maxKey的地区数量时,更新maxKey
	                if (tempSet.size() > 0 && (maxKey == null || tempSet.size() > broadcasts.get(maxKey).size())) {
	                    maxKey = key;
	                }
	            }
	            //如果maxKey不为空
	            if(maxKey != null){
	                //循环结束时,maxKey是本轮最优解
	                //更新选择的灯塔
	            	//TraceID2Trace
	            	sampleLog.add(TraceID2Trace.get(maxKey));
	            	//存放轨迹
	            	traceSet.add(TraceID2Trace.get(maxKey));
	            	//if()
	                selects.add(maxKey);
	                //从allAreas中移除已选择的
	                allAreas.removeAll(broadcasts.get(maxKey));
	            }
	        }
	        //移除上一个算法中的开始事件和结束事件
	      //增强for循环
	        for(XTrace trace : traceSet){
	        	String StartEvent = XConceptExtension.instance().extractName(trace.get(0));
				String EndEvent = XConceptExtension.instance().extractName(trace.get(trace.size()-1));
				StartSet.remove(StartEvent);
				EndSet.remove(EndEvent);
	        }
	        
			for(XTrace trace:originalLog)
			{	
				if(traceSet.contains(trace)) {
					continue;
				}
				System.out.println("////////////////////////////");
				String StartEvent = XConceptExtension.instance().extractName(trace.get(0));
				String EndEvent = XConceptExtension.instance().extractName(trace.get(trace.size()-1));
				System.out.println("11111111111111111111");
				if((StartSet.size()!=0)||(EndSet.size()!=0)) {
					
					int flag1=0;
					int flag2=0;
					System.out.println("222处理前：2222");
					System.out.println("StartSet："+StartSet);
					System.out.println("EndSet："+EndSet);
					System.out.println("StartEvent:"+StartEvent);
					System.out.println("EndEvent:"+EndEvent);
					if(StartSet.remove(StartEvent)) {
						flag1=1;
					}
					if(EndSet.remove(EndEvent))
					{
						flag2=1;
					}
					if((flag1==1)||(flag2==1))
					{
						System.out.println("true!!!!!!!!!!!!!!!");
						sampleLog.add(trace);
					}
					System.out.println("StartSet.size():"+StartSet.size());
					System.out.println("EndSet.size():"+EndSet.size());
					System.out.println("dfrSetLog.size():"+dfrSetLog.size());
				}else{
					break;
				}
				System.out.println("******************************");
			}
		}
		
		 /**
		  * 2,背包问题		不可行
		  * 背包有容量 这里可以是整个日志的轨迹的直接跟随活动数  价值对应什么
		  * 每个物品两个属性：物品对应轨迹，重量对应轨迹中的直接跟随关系，价值对应重量
		  * dfrSetLog:表示日志中所有的直接跟随活动关系数 大小就相当于容量 HashSet<String>
		  * traceIDToDFRSet ：key：轨迹名；value:直接跟随活动 HashMap<String, HashSet<String>>
		  * 重量等于价值 都一样
		  * 相当于一次装入多个
		  * 1个轨迹包含多个直接跟随活动 一次装入这么多直接跟随活动
		  * 将价值和权重随时更新
		  */
//		 int V= dfrSetLog.size();//初始化背包的容量，在这里为整个事件日志的直接跟随活动关系数
//		 int N=TraceIdList.size();//初始化物体的个数，在这里为轨迹的个数；不过这里sh的轨迹可以是重复
//		 //用于存储每个物体的重量，下标从1开始
//		 //还需要初始化每个物体的重量与价值
//		 //重量的话用每条轨迹包含的直接跟随活动数
//		 //初始化重量与价值
//		 int[] weight= {0};
//		 int[] value= {0};
//		 for(int i=0;i<N+1;i++)//第一个不储存
//		 {
//			 weight[i+1]=traceIDToDFRSet.get(TraceIdList.get(0)).size();
//			 value[i+1]=traceIDToDFRSet.get(TraceIdList.get(0)).size();
//			 System.out.println(" weight[i+1]:"+i+" "+weight[i+1]);
//		 }
//		 System.out.println(" value: "+value);
//		 //如果放入就更新原来的V和value
//		 //不放入的话就不更新
//		 //V为差集
//		 //V=V-dfrSetLog与加入的轨迹的直接跟随活动的交集的大小
//		 //value=dfrSetLog与该轨迹的直接跟随活动的交集的大小
//		 for(int i=0;i<N+1;i++) {
//			 boolean notContains1=dfrSetLog.removeAll(traceIDToDFRSet.get(TraceIdList.get(i)));
////			 N=N-dfrSetLog.retainAll(traceIDToDFRSet.get(TraceIdList.get(0))).size();
//			 System.out.println(" notContains1: "+notContains1);
//			 N=dfrSetLog.size();
//			 HashSet<String> newDfrSetLog=dfrSetLog;
//			 boolean isContains=newDfrSetLog.retainAll(traceIDToDFRSet.get(TraceIdList.get(i)));
//			 System.out.println(isContains);
//			 value[i+1]=newDfrSetLog.size();
//		 }
//		    
		//return the sample log.
		return sampleLog;
//		return "";	
		}                                                                                                                                                                                          
	
}
