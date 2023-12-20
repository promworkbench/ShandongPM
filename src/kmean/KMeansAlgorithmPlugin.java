package kmean;


import java.io.FileNotFoundException;

/*
 * This is the implementation of LogRank plugin. 
 * It aims to sample an input large-scale example log and returns a small sample log using PageRank algorithm. 
 * Step 1: each trace is transformed to a featured vertor, we implement multiple approaches to the transformation.
 * Step 2: we compute the similarity for each two traces (vectors) using the cosine similarity metric. 
 * Step 3: using text ranking /PageRank algorithm to get a sub-set of representative event logs. 
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
//import org.processmining.plugins.guidetreeminer.ClusterLogOutput;

@Plugin(
		name = "Kmean-based Trace Clustering",// plugin name
		
		returnLabels = {"ClusterLogOutput Log"}, //return labels
		returnTypes = {ClusterLogOutput.class},//return class
		
		//input parameter labels, corresponding with the second parameter of main function
		parameterLabels = {"Large Event Log"},
		
		//userAccessible = true,
		help = "This plugin aims to cluster an input large-scale example log and returns some small cluster logs using the  Kmean-based Trace Clustering model." 
		)
public class KMeansAlgorithmPlugin {
	@UITopiaVariant(
	        affiliation = "TU/e", 
	        author = "Shuaipeng Zhang", 
	        email = "15994069715@163.com"
	        )
	@PluginVariant(
			variantLabel = "Clustering Big Event Log, default",
			// the number of required parameters, {0} means one input parameter
			requiredParameterLabels = {0}
			)
	public static ClusterLogOutput KMeansAlgorithm(UIPluginContext context, XLog originalLog) throws UserCancelledException, FileNotFoundException
	{
			
//		//create a new log with the same log-level attributes. 
//		XLog sampleLog = new XLogImpl(originalLog.getAttributes());
				
		//select two types of event logs, lifecycle event log and normal event log.  
		String [] logType = new String[2];
		logType[0]="Normal Event Log";
		logType[1]="Lifecycle Event Log";
		String selectedType =ProMUIHelper.queryForObject(context, "Select the type of event log for sampling", logType);
		context.log("The selected log type is: "+selectedType, MessageLevel.NORMAL);	
		System.out.println("Selected log type is: "+selectedType);
				
		
		//set the sampling ratio
		int clusterNumber = (int) ProMUIHelper.queryForDouble(context, "Select the cluster number", 0, 10,	4);		
		context.log("kmean cluster number: "+clusterNumber, MessageLevel.NORMAL);	
//		 PrintStream print=new PrintStream("C:\\Users\\张帅鹏\\Desktop\\2.基于XXX的事件日志聚类方法评估\\a.txt");  //写好输出位置文件，注意windows文件路径用/隔开了，直接复制时需要把/改成//,最后需要加上自己要创建的文件名
//         System.setOut(print); 
		return KMeansAlgorithmTechnique(context,originalLog,  selectedType, clusterNumber);
	}	 
	
	
	public static ClusterLogOutput KMeansAlgorithmTechnique(UIPluginContext context,XLog originalLog, String selectedType, int clusterNumber)
	{
		long startTime=0;
		long endTime=0;
		startTime =System.currentTimeMillis();
		//create a new log with the same log-level attributes. 
		XLog sampleLog = new XLogImpl(originalLog.getAttributes());
		ConvertTraceToVector ctv = new ConvertTraceToVector();

		//keep an ordered list of traces names. 
		ArrayList<String> TraceIdList = new ArrayList<>();
		
		//convert the log to a map, the key is the name of the trace, and the value is the trace. 
		HashMap<String, XTrace> nameToTrace = new HashMap<>();
		//originalLog.
		for(XTrace trace: originalLog)
		{
			TraceIdList.add(trace.getAttributes().get("concept:name").toString());
			nameToTrace.put(trace.getAttributes().get("concept:name").toString(), trace);
		}
		System.out.println("TraceIdList:"+TraceIdList);
		System.out.println("nameToTrace:"+nameToTrace);
		// the similarity matrix of the log
		double[][] matrix = new double[TraceIdList.size()][TraceIdList.size()];
		if(selectedType=="Normal Event Log")// different trace 2 feature mapping
		{
			for(int i=0;i<TraceIdList.size();i++)
			{
				for(int j =0;j<TraceIdList.size();j++)
				{
					//get the trace similarity	
					matrix[i][j]=ctv.CosineSimilarity(ctv.Trace2FeatureMap(nameToTrace.get(TraceIdList.get(i))),
							ctv.Trace2FeatureMap(nameToTrace.get(TraceIdList.get(j))));
				}
			}
		}
		else{
			for(int i=0;i<TraceIdList.size();i++)
			{
				for(int j =0;j<TraceIdList.size();j++)
				{
					//get the trace similarity	
					matrix[i][j]=ctv.CosineSimilarity(ctv.Trace2FeatureMapStartComplete(nameToTrace.get(TraceIdList.get(i))),
							ctv.Trace2FeatureMapStartComplete(nameToTrace.get(TraceIdList.get(j))));
					System.out.println("matrix:"+matrix[i][j]);
				}
			}
		}
//		System.out.println("matrix:"+matrix);
		
		KMeansAlgorithm kma= new KMeansAlgorithm();
		ClusterSet cs=kma.runKmean(originalLog,nameToTrace,TraceIdList,matrix,clusterNumber);
		endTime =System.currentTimeMillis();
		System.out.println("**********************************");
		System.out.println("kmean cluster Time:"+(endTime-startTime)+"ms");
		System.out.println("**********************************");
//		int clusterSize=4;
		List<XLog> clusterLogList = new ArrayList<XLog>();
		for (int i = 0; i < clusterNumber; i++) {
			clusterLogList.add(cs.getClusters().get(i).getLog());
//			System.out.println("ClusterSet:"+cs.getClusters().get(i).getLog() );
		}	
		
		XLog currentClusterLog;
		
		for (int i = 0; i < clusterNumber; i++){
			currentClusterLog = new XLogImpl(originalLog.getAttributes());
//			context.getResourceManager().getResourceForInstance(clusterLog[i]).setFavorite(
//								true);
			currentClusterLog=cs.getClusters().get(i).getLog();
			context.getProvidedObjectManager().createProvidedObject(
					"ClusterLog " + i, currentClusterLog,
					XLog.class, context);
		}
		ClusterLogOutput  cop=new ClusterLogOutput(0,clusterLogList);
//		context.getFutureResult(0).setLabel("Cluster Log Output");
		//context.getFutureResult(1).setLabel("cs.log");
	   return  cop;
}
}


