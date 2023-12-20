package sigrank;
/*
 * A fast algorithm to sample log. 
 * The original idea is referred to "The automatic creation of literature abstracts". 
 * 
 * rank a trace by its significance;
 * the significance of a trace is determined by the combination of its activity significance and dfr significance. 
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.deckfour.xes.extension.std.XConceptExtension;
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

import simrank.SortingHashMapByValues;

@Plugin(
		name = "LogRank++-based Event Log Sampling",// plugin name
		returnLabels = {"Sample Log"}, //return labels
		returnTypes = {XLog.class},//return class
		
		//input parameter labels, corresponding with the second parameter of main function
		parameterLabels = {"Large Event Log"},
		
		userAccessible = true,
		help = "This plugin aims to the orginal log returs a sample log that ensures that the evnet log directlt follows relation of the frequency consistency." 
		)
public class SigRankSamplingPlugin {
	@UITopiaVariant(
	        affiliation = "TU/e", 
	        author = "Shuaipeng Zhang", 
	        email = "c.liu.3@tue.nl"
	        )
	@PluginVariant(
			variantLabel = "Sampling Big Event Log, default",
			// the number of required parameters, {0} means one input parameter
			requiredParameterLabels = {0}
			)
	public static XLog SimRankSampling(UIPluginContext context, XLog originalLog) throws UserCancelledException
	{
		
		
		//set the sampling ratio
		double samplingRatio = ProMUIHelper.queryForDouble(context, "Select the sampling ratios", 0, 1,	0.3);		
		context.log("Interface Sampling Ratio is: "+samplingRatio, MessageLevel.NORMAL);	
		double startTime_total=0;
		double endTime_total=0;

		startTime_total=System.currentTimeMillis();
		XLog log1=SimRankSamplingTechnique(originalLog, samplingRatio);
		endTime_total=System.currentTimeMillis();

		System.out.println("***************");
		System.out.println("LogRank++ Time:"+(endTime_total-startTime_total));
		System.out.println("***************");
		return log1;
	}
	
	public static XLog SimRankSamplingTechnique(XLog originalLog, double samplingRatio)
	{
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
				//add directly follow pair
//				dfrSet.add(XConceptExtension.instance().extractName(trace.get(i))+","+XConceptExtension.instance().extractName(trace.get(i+1)));
			}
			for(int i =0;i<trace.size()-1;i++)
			{
				//add activity
//				activitySet.add(XConceptExtension.instance().extractName(trace.get(i)));
				//add directly follow pair
				dfrSet.add(XConceptExtension.instance().extractName(trace.get(i))+","+XConceptExtension.instance().extractName(trace.get(i+1)));
			}
			traceIDToActivitySet.put(XConceptExtension.instance().extractName(trace), activitySet);
			traceIDToDFRSet.put(XConceptExtension.instance().extractName(trace), dfrSet);
		}
				
		//the number of traces
		int traceNumber = originalLog.size();
		
		//activity set of the log
		HashSet<String> activitySetLog = new HashSet<>();
		for(String traceID: traceIDToActivitySet.keySet())
		{
			activitySetLog.addAll(traceIDToActivitySet.get(traceID));
		}
		
		//direct-follow relation set of the log
		HashSet<String> dfrSetLog = new HashSet<>();
		for(String traceID: traceIDToDFRSet.keySet())
		{
			dfrSetLog.addAll(traceIDToDFRSet.get(traceID));
		}
		
		
		//activity to significance
		HashMap<String, Double> activity2Sig = new HashMap<>();
		for(String act: activitySetLog)
		{
			//count the number of traces that contains act
			int count =0;
			for(XTrace trace: originalLog)
			{
				for(int i =0;i<trace.size();i++)
				{
					if(act.equals(XConceptExtension.instance().extractName(trace.get(i))))
					{
						count++;
						break;
					}
				}
			}
			
			activity2Sig.put(act,(double) count/traceNumber);
		}
		
		//direct-follow relation to significance. the number of traces contain the drf divided by the total number of traces. 
		HashMap<String, Double> dfr2Sig = new HashMap<>();
		for(String dfr: dfrSetLog)
		{
			//count the number of traces that contains dfr
			int count =0;
			for(XTrace trace: originalLog)
			{
				for(int i =0;i<trace.size()-1;i++)
				{
					if(dfr.equals(XConceptExtension.instance().extractName(trace.get(i))+","+XConceptExtension.instance().extractName(trace.get(i+1))))
					{
						count++;
						break;
					}
				}
			}
			dfr2Sig.put(dfr, (double) count/traceNumber);
		}
		
		
		//trace to significance	
		HashMap<String, Double> traceToSignificance = new HashMap<>();
		for(String traceID: TraceID2Trace.keySet())
		{
			//activities significance
			// number of activities in this trace
			int ActNumber= traceIDToActivitySet.get(traceID).size();
			double ActSigSum = 0;
			for(String act: traceIDToActivitySet.get(traceID))
			{
				ActSigSum=ActSigSum+activity2Sig.get(act);
			}
			// the average activity significance of current trace
			double AverageActSig = ActSigSum/ActNumber;
			
			//dfrs significance
			// number of dfr in this trace
			int DFRNumber= ActNumber-1;
			double DFRSigSum =0;
			for(String dfr: traceIDToDFRSet.get(traceID))
			{
				DFRSigSum=DFRSigSum+dfr2Sig.get(dfr);
			}
			// the average dfr significance of current trace
			double AverageDFRSig = DFRSigSum/DFRNumber;
			
			
			//set the significance of trace as: the average of activity and dfr significance
			
			traceToSignificance.put(traceID, 1-(AverageActSig+AverageDFRSig)/2);//1- the insignificance... used for ordering 
		}
		
		//select the top n traces. 
		int topN=(int)Math.round(samplingRatio*originalLog.size());
		System.out.println("Sample Size: "+ topN);
		
		//order traces based on the weight
		HashSet<String> sampleTraceNameSet=SortingHashMapByValues.sortMapByValues(traceToSignificance,topN);
		
		System.out.println("Sample Trace Names: "+sampleTraceNameSet);
		//construct the sample log based on the selected top n traces. 
		for(XTrace trace: originalLog)
		{
			if(sampleTraceNameSet.contains(trace.getAttributes().get("concept:name").toString()))
			{
				sampleLog.add(trace);
			}
		}
		
		//return the sample log. 
		return sampleLog;	
	}
}
