package simrank;
/*
 * This is the implementation of SimRank plugin. 
 * It aims to sample an input large-scale example log and returns a small sample log using.
 * The main difference with LogRank is that it tries to reduce the similarity computation time.
 
 * Step 1: each trace is transformed to a featured vertor, we implement multiple approaches for the transformation, for both lifecycle log and normal one.
 * Step 2: we compute the similarity for each trace (vector) with the rest of traces using the cosine similarity metric. 
 * Step 3: find the most similar traces to get a sub-set of representative event logs. 
 */

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XLogImpl;
import org.deckfour.xes.out.XesXmlSerializer;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.plugin.events.Logger.MessageLevel;
import org.processmining.framework.util.ui.widgets.helper.ProMUIHelper;
import org.processmining.framework.util.ui.widgets.helper.UserCancelledException;

import kmean.ConvertTraceToVector;
@Plugin(
		name = "LogRank+-based Event Log Sampling",// plugin name
		
		returnLabels = {"Sample Log"}, //return labels
		returnTypes = {XLog.class},//return class
		
		//input parameter labels, corresponding with the second parameter of main function
		parameterLabels = {"Large Event Log"},
		
		userAccessible = true,
		help = "This plugin aims to sample an input large-scale example log and returns a small sample log using similarity." 
		)
public class SimRankSamplingPlugin {

	@UITopiaVariant(
	        affiliation = "TU/e", 
	        author = "Cong liu", 
	        email = "c.liu.3@tue.nl"
	        )
	@PluginVariant(
			variantLabel = "Sampling Big Event Log, default",
			// the number of required parameters, {0} means one input parameter
			requiredParameterLabels = {0}
			)
	public static XLog SimRankSampling(UIPluginContext context, XLog originalLog) throws UserCancelledException
	{
		//select two types of event logs, lifecycle event log and normal event log.  
		String [] logType = new String[2];
		logType[0]="Normal Event Log";
		logType[1]="Lifecycle Event Log";
		String selectedType =ProMUIHelper.queryForObject(context, "Select the type of event log for sampling", logType);
		context.log("The selected log type is: "+selectedType, MessageLevel.NORMAL);	
		System.out.println("Selected log type is: "+selectedType);
				
		
		//set the sampling ratio
		double samplingRatio = ProMUIHelper.queryForDouble(context, "Select the sampling ratios", 0, 1,	0.3);		
		context.log("Interface Sampling Ratio is: "+samplingRatio, MessageLevel.NORMAL);	
		
		
		double startTime_total=0;
		double endTime_total=0;

		startTime_total=System.currentTimeMillis();
		XLog log1=null;
		try {
			log1 = SimRankSamplingTechnique(originalLog,selectedType,samplingRatio);
		} catch (FileNotFoundException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		endTime_total=System.currentTimeMillis();

		System.out.println("***************");
		System.out.println("LogRank+ Time:"+(endTime_total-startTime_total));
		System.out.println("***************");
		context.log("LogRank+ Time:"+(endTime_total-startTime_total));
		//return the sample log. 
		return log1;
	}
	
	public static XLog SimRankSamplingTechnique(XLog originalLog, String selectedType, double samplingRatio) throws FileNotFoundException
	{
		double startTime_total=0;
		double endTime_total=0;
		startTime_total=System.currentTimeMillis();
		//create a new log with the same log-level attributes. 
		XLog sampleLog = new XLogImpl(originalLog.getAttributes());
		ConvertTraceToVector ctv = new ConvertTraceToVector();

		//keep an ordered list of traces names. 
		ArrayList<String> TraceIdList = new ArrayList<>();
		
		//convert the log to a map, the key is the name of the trace, and the value is the trace. 
		HashMap<String, XTrace> nameToTrace = new HashMap<>();
		for(XTrace trace: originalLog)
		{
			TraceIdList.add(trace.getAttributes().get("concept:name").toString());
			nameToTrace.put(trace.getAttributes().get("concept:name").toString(), trace);
		}
		
		//trace name to vector
		HashMap<String, HashSet<String>> nameToFeatureSet = new HashMap<>();

		if(selectedType=="Normal Event Log")// different trace 2 feature mapping
		{
			for(int i=0;i<TraceIdList.size();i++)
			{
				//get the trace feature vector	
				nameToFeatureSet.put(TraceIdList.get(i), ctv.Trace2FeatureSet(nameToTrace.get(TraceIdList.get(i))));
			}
		}
		else{
			for(int i=0;i<TraceIdList.size();i++)
			{
				//get the trace feature vector	
				nameToFeatureSet.put(TraceIdList.get(i), ctv.Trace2FeatureSetStartComplete(nameToTrace.get(TraceIdList.get(i))));
			}
		}
		
		//trace name to weight, the weight represents the difference, rather than similarity. 
		HashMap<String, Double> nameToWeight = new HashMap<>();
		for(int i=0;i<TraceIdList.size();i++)
		{
			//for i=0, we compute the similarity between 0 and {1...n}
			nameToWeight.put(TraceIdList.get(i), 
					1-ctv.CosineSimilarity(convertHashSet2HashMap(nameToFeatureSet.get(TraceIdList.get(i))),computeFeatureUnion(TraceIdList.get(i), nameToFeatureSet)));
		}
		endTime_total=System.currentTimeMillis();
		double preTime=endTime_total-startTime_total;
		

		
//		XLog log1=LogRankSamplingTechnique(originalLog,  selectedType, samplingRatio);
		

		System.out.println("***************");
		System.out.println("precost Time:"+(endTime_total-startTime_total));
		System.out.println("***************");
		ArrayList<Double> performance = new ArrayList<>();
		for(int i=5;i<31;i+=5) {
			double startTime_total1=0;
			double endTime_total1=0;
			XLog sampleLog2 = new XLogImpl(originalLog.getAttributes());
			startTime_total1=System.currentTimeMillis();
			//select the top n traces. 
//			int topN=(int)Math.round(samplingRatio*originalLog.size());
			int topN=(int)Math.round((double)i/100*originalLog.size());
			System.out.println("Sample Size: "+ topN);
			//order traces based on the weight
			HashSet<String> sampleTraceNameSet=SortingHashMapByValues.sortMapByValues(nameToWeight,topN);
			System.out.println("Sample Trace Names: "+sampleTraceNameSet);
			//construct the sample log based on the selected top n traces. 
			for(XTrace trace: originalLog)
			{
				if(sampleTraceNameSet.contains(trace.getAttributes().get("concept:name").toString()))
				{
					sampleLog2.add(trace);
				}
			}
			endTime_total1=System.currentTimeMillis();
			double rankTime=endTime_total1-startTime_total1;
			System.out.println("***************");
			System.out.println("sim  rank Time:"+(endTime_total1-startTime_total1));
			System.out.println("***************");
			performance.add(preTime+rankTime);
			
	        XesXmlSerializer xesSerializer2 = new XesXmlSerializer();
	         java.io.File xesFile2 = new java.io.File("C:\\Users\\张帅鹏\\Desktop\\111\\BPI2012O_simlog_"+i+".xes");
	         java.io.OutputStream xesStream2;
			try {
				xesStream2 = new java.io.FileOutputStream(xesFile2);
			    xesSerializer2.serialize(sampleLog2, xesStream2); 
			    sampleLog2.clear();
			} catch (IOException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}

			System.out.println("LogRank+ Final Time:"+performance);
		}

		
		return sampleLog;
				
	}
	
	public static HashMap<String, Boolean> computeFeatureUnion(String currentTraceName, HashMap<String, HashSet<String>> nameToFeatureSet)
	{
		HashSet<String> unionFeatureSet = new HashSet<>();
		for(String traceName : nameToFeatureSet.keySet())
		{
			if(!traceName.equals(currentTraceName))
			{
				unionFeatureSet.addAll(nameToFeatureSet.get(traceName));
			}
		}
		
		//transform feature set to 
		return convertHashSet2HashMap(unionFeatureSet);
	}
	
	public static HashMap<String, Boolean> convertHashSet2HashMap(HashSet<String> hashSet)
	{
		HashMap<String, Boolean> featureMap = new HashMap<>();
		for(String s: hashSet)
		{
			featureMap.put(s, true);
		}
		
		return featureMap;

	}
	
}
