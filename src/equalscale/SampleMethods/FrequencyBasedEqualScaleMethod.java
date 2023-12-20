package equalscale.SampleMethods;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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

@Plugin(
		name = "(Frequency-based) Equal Scale Event Log Sampling",// plugin name
		
		returnLabels = {"Sample Log"}, //return labels
		returnTypes = {XLog.class},//return class
		
		//input parameter labels, corresponding with the second parameter of main function
		parameterLabels = {"Large Event Log"},
		
		userAccessible = true,
		help = "This plugin aims to sample an input large-scale example log and returns a small sample log by measuring the significance of traces." 
		)
public class FrequencyBasedEqualScaleMethod {
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
		context.log("LogRank++ Time:"+(endTime_total-startTime_total));	
		return log1;
	}
	
	public static XLog SimRankSamplingTechnique(XLog originalLog, double samplingRatio)
	{
		double startTime_total=0;
		double endTime_total=0;
		startTime_total=System.currentTimeMillis();
		//create a new log with the same log-level attributes. 
		XLog sampleLog = new XLogImpl(originalLog.getAttributes());
		//keep an ordered list of traces names. 
		ArrayList<String> TraceIdList = new ArrayList<>();
//		****************************************获得轨迹变体******************************
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
		
		//traceFrequency中保存的是轨迹变体以及频次
		/********************************************************************************************/
		Map<XTrace, Integer> map = traceFrequency;
		Map<XTrace, Double> Fre_traceSet = new HashMap<>() ;
		XLog VariantLog = new XLogImpl(originalLog.getAttributes());
		for(Map.Entry<XTrace, Integer> entry : map.entrySet()){
			VariantLog.add(entry.getKey());
			System.out.println("key = " + entry.getKey() + ", value = " + entry.getValue());
			Fre_traceSet.put(entry.getKey(), entry.getValue()*samplingRatio);
		}
		
		Map<XTrace, Double> Selected_traceSet = new HashMap<>();
		Map<XTrace, Double> map1 =Fre_traceSet;
		for(Map.Entry<XTrace, Double> entry : map1.entrySet()){	
			if(Math.round(entry.getValue())>1) {
				for(int i=0;i<Math.round(entry.getValue());i++)
				{
					sampleLog.add(entry.getKey());
				}
				Selected_traceSet.put(entry.getKey(), entry.getValue());
			}
		}
	
		
		//return the sample log. 
		return sampleLog;	
	}
}
