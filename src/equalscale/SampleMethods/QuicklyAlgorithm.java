
package equalscale.SampleMethods;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XLogImpl;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

import equalscale.SampleEvaluation.SampleEvalutionPlugin;
@Plugin(
		name = "1111111--(Quickly Sampling)A Novel Euqal Scale Event Log Sampling",// plugin name
		returnLabels = {"Sample Log"}, //return labels
		returnTypes = {XLog.class},//return class
		//input parameter labels, corresponding with the second parameter of main function
		parameterLabels = {"Large Event Log"},
		userAccessible = true,
		help = "This plugin aims to sample an input large-scale example log and returns a small sample log by measuring the significance of traces." 
		)
public class QuicklyAlgorithm {
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
	public static XLog DijkstraPlusSampling(UIPluginContext context, XLog originalLog)
	{
		
		double startTime_total=0;
		double endTime_total=0;
		//set the sampling ratio
//		double samplingRatio = ProMUIHelper.queryForDouble(context, "Select the sampling ratios", 0, 1,	0.3);		
//		context.log("Interface Sampling Ratio is: "+samplingRatio, MessageLevel.NORMAL);	
		startTime_total=System.currentTimeMillis();
		XLog log=DijkstraPlus(originalLog);
		endTime_total=System.currentTimeMillis();

		System.out.println("***********************");
		System.out.println("DijkstraPlus Time:"+(endTime_total-startTime_total));
		System.out.println("***********************");
		context.log("DijkstraPlus Time:"+(endTime_total-startTime_total));	
		return log;
	}
	
	//输入日志，输出样本日志
	public static XLog DijkstraPlus(XLog originalLog) {
		
		XLog sampleLog = new XLogImpl(originalLog.getAttributes());
		double currentCost=0.0;//当前代价
		HashMap<XTrace,Boolean> StatusVisited=new HashMap<>();//访问状态
		HashMap<XTrace,Double> traceWeight=new HashMap<>();//权重系数
		
		
		XLog newOriginalLog = new XLogImpl(originalLog.getAttributes());
		newOriginalLog=(XLog) originalLog.clone();//对象的克隆
		
		//计算dfr的期望值
		HashMap<String,Integer> DfrNumber_original=SampleEvalutionPlugin.getDfrNumber(originalLog);
		//采样率自设，名称为SampleRatio,默认为30%
		double SampleRatio=0.3;
		HashMap<String,Double> DfrNumber_except_original=new HashMap<>();
		Map<String, Integer> map0 = DfrNumber_original;
		for(Map.Entry<String, Integer> entry : map0.entrySet()){
			DfrNumber_except_original.put(entry.getKey(), entry.getValue()*SampleRatio);
//			System.out.println("轨迹名： " + entry.getKey()+"值： " + entry.getValue()*SampleRatio);
		}
		
		//预处理MAX
		
		for(XTrace trace:originalLog) {
			int count=0;
			Map<String, Double> map00 = sortDescend(DfrNumber_except_original);
			String firstall=null;
			
			for(Entry<String, Double> entry : map00.entrySet()){
				firstall=entry.getKey();
				System.out.println("1111轨迹名： " + entry.getKey()+";值： " + entry.getValue());
			}
		}
		Map<String, Double> map00 = sortDescend(DfrNumber_except_original);
		String firstall=null;
		
		for(Entry<String, Double> entry : map00.entrySet()){
			firstall=entry.getKey();
			System.out.println("轨迹名： " + entry.getKey()+";值： " + entry.getValue());
			break;
		}
		//大致处理  包含最多的直接跟随活动关系的轨迹都加入到样本日志中
		//保证不会加入的太多
		double Allcount=originalLog.size()*0.2;
		double count00=0.0;
		HashMap<XTrace,Boolean> StatusVisited000=new HashMap<>();//访问状态
		for(XTrace trace :originalLog){
			HashSet<String> DfrNumber_trace=SampleEvalutionPlugin.getDfrNumber(trace);
//			System.out.println("1111111 "+trace.getAttributes().get("concept:name").toString()+";;;firstall:"+firstall);
			if(DfrNumber_trace.contains(firstall) ) {
				System.out.println("1111111 ");
				sampleLog.add(trace);
				newOriginalLog.remove(trace);
				count00++;
			}
			if(count00 >Allcount) break;
		}
		//预处理    加到多少是合适的
		
		
		
		
		//1.初始化各系数：将轨迹权重第一次初始化traceWeight,将轨迹状态都设置为未访问
		for (XTrace trace: newOriginalLog)	{   ///all trace size!=0
			XLog sampleLog1 = new XLogImpl(originalLog.getAttributes());
			sampleLog1=(XLog) sampleLog.clone();//对象的克隆
			sampleLog1.add(trace);
			currentCost=CurrentCost(DfrNumber_except_original,sampleLog1);//当前越小越好
			StatusVisited.put(trace, false);//初始化全部的轨迹都为未访问状态
			traceWeight.put(trace, currentCost);
		}		
		//2.选择最小的mse值作为起始点
		Map<XTrace, Double> map3 = sortAscend(traceWeight);
		int count=0;
		XTrace FirstTrace=null;
		for(Entry<XTrace, Double> entry : map3.entrySet()){
//			System.out.println("key = " + entry.getKey() + ", value = " + entry.getValue());
//			if(count ==0) {
				sampleLog.add(entry.getKey());//将最小值添加到样本日志中
				FirstTrace=entry.getKey();
				System.out.println("被选择的第一条轨迹 为= " + entry.getKey().getAttributes().get("concept:name").toString());
//				System.out.println("NAMPE值为： " + CurrentCost(DfrNumber_except_original,sampleLog));		
				break;
//			}
		}
////		 XTrace minKey = getMapMinOrMaxValueKey(traceWeight, "min");
//		//////////////////////////////////////
//		
//		//3.初始化轨迹的前缀组合PreTrace  记录轨迹前驱，key：轨迹，value:key的前驱
//		HashMap<XTrace,XTrace> PreTrace=new HashMap<>();
//		for (XTrace trace: originalLog)	{
//			PreTrace.put(trace, FirstTrace);//初始化轨迹前驱
//		}
//		
		//思路。。。
		
		//trace_pearson数组 key：轨迹，value：weight值
		//status key:trace  value: boolean
		//整理，整一个weight hashmap  key:trace value:name(double)
		//path:记录前驱， trace   trace
		//判断条件：weight.get(trace0).value + currentcost(tarce1) <= weight.get(trace1)
		
		
		//4.遍历
		//令初识的轨迹为第一个轨迹
		XTrace trace00=FirstTrace;
//		double mse=0.0;
//		int countValue=1;
		double PrecurrentCost=currentCost;//当前代价
		boolean flag=true;
		
		
//		Map<XTrace, Double> textmap3 = traceWeight;
		
		
		while(!isAllVisited(StatusVisited) && flag) {//当前的所有状态都被访问到，或者误差值小于一定的值(0.01)，终止循环	
//		while(flag) {//当前的所有状态都被访问到，或者误差值小于一定的值(0.01)，终止循环	
			// 将这个节点设置为已访问
			StatusVisited.put(trace00, true);
            // 查看邻接矩阵中与指定节点邻接的节点
//	     	int value=0;
            for (XTrace trace:newOriginalLog) {
//            	value++;
            	//当前轨迹不在样本日志中时进行处理
//            	 System.out.println("value= " + value);
//            	if(!sampleLog.contains(trace)) {
            	if(!StatusVisited.get(trace)) {
                    // 可能的新路径权值: 从最开始的指定起点到本轮起点到该节点的路径权值总和
                    double newWeight;//轨迹的新权重
                    // 如果节点未访问, 且是邻接节点  写错了
//                  newWeight=traceWeight.get(trace00)+CurrentCost(originalLog,sampleLog,trace00,trace);
                    newWeight=CurrentCost(DfrNumber_except_original,sampleLog,trace);
                    
                    //如果新权重小于原来的权重而且节点未被访问过
                    //traceWeight.get(trace):原来的权重
                    //newWeight:加入这条轨迹后的权重
//                    System.out.println("newOriginalLog= " + originalLog.size());
//                    System.out.println("被选择的轨迹 为= " + trace.getAttributes().get("concept:name").toString());
//                    System.out.println("newWeight:"+newWeight);
//                    System.out.println("traceWeight.get(trace):"+traceWeight.get(trace));
                    if(newWeight<traceWeight.get(trace)) {//大多数都不满足？
                    	// 则更新该节点的最小路径值, 更新该节点的前驱为本轮起点
                    	traceWeight.put(trace,newWeight);   //更新权重
                    	
                    	
//                    	PreTrace.put(trace, trace00);   //更新前缀节点
                    	sampleLog.add(trace);//更新样本日志
                    	StatusVisited.put(trace, true);
//                    	originalLog.remove(trace);//将那条轨迹移除
                    	//被选择的轨迹为
                    	System.out.println("11111被选择的轨迹 为= " +trace.getAttributes().get("concept:name").toString());
                    }
            	}
            }
          //1.选择下一轮中值最小的节点且未被访问过的,此时轨迹权重需要更新
    		Map<XTrace, Double> map4 = sortAscend(traceWeight);
    		int count1=0;
    		for(Entry<XTrace, Double> entry : map4.entrySet()){
    			if(count1 ==0 && !StatusVisited.get(entry.getKey()) ) {
    				sampleLog.add(entry.getKey());//更新样本日志
    				trace00=entry.getKey();
    				originalLog.remove(entry.getKey());//将那条轨迹移除
    				System.out.println("*******************************");
    				System.out.println("被选择的轨迹 为= " + entry.getKey().getAttributes().get("concept:name").toString());
//    				System.out.println("NAMPE值为： " + CurrentCost(DfrNumber_except_original,sampleLog));		
    				break;
    			}
    		}
    		double allCurrentCost=CurrentCost(DfrNumber_except_original,sampleLog);
    		
    		System.out.println("当前值 allCurrentCost:" +allCurrentCost);
    		System.out.println("先前值 PrecurrentCost:" +PrecurrentCost);
//    		if(PrecurrentCost > allCurrentCost && allCurrentCost >0.1) {
    		if( PrecurrentCost > allCurrentCost) {//陷入局部最优解   判断条件是不是要改一下，不是单调递减的
    			PrecurrentCost=allCurrentCost;
    			System.out.println("111先前值大于当前值 ！！" );	
//    		}else if(allCurrentCost> 0.3){
//    			PrecurrentCost=allCurrentCost;
//    			System.out.println("222先前值大于当前值 ！！" );	
//    		}
    		}else {
    			flag=false;
    			System.out.println("最后的name值为:" +allCurrentCost);
    			System.out.println("先前值小于当前值 ！！！" );	
    		}
    		
    		
            // 下轮起点from设置为: weights数组中数值最小的并且未访问的节点
    		//更新权重
    		for (XTrace trace: newOriginalLog)	{   ///all trace size!=0
    			if(!StatusVisited.containsKey(trace)) {
	    			XLog sampleLog3 = new XLogImpl(originalLog.getAttributes());
	    			sampleLog3=(XLog) sampleLog.clone();//对象的克隆
	    			sampleLog3.add(trace);
	    			currentCost=CurrentCost(DfrNumber_except_original,sampleLog3);//当前越小越好
	    			traceWeight.put(trace, currentCost);
    			}
    		}
//    		countValue++;
		}
		
		return sampleLog;
	}
	
	/**
     * 检查是否全部被遍历(只要有一个是未被遍历返回false)
     *
     * @return boolean
     */
    public static boolean isAllVisited(HashMap<XTrace,Boolean> StatusVisited) {
    	Map<XTrace, Boolean> map4 =StatusVisited ;
		for(Entry<XTrace, Boolean> entry : map4.entrySet()){
			if(!entry.getValue()) {
				return false;
			}
		}
        return true;
    }
	/**
	 * 
	 * @param originalLog
	 * @param sampleLog
	 * @return 当前代价的值
	 */
	public static double CurrentCost(HashMap<String,Double> DfrNumber_except_original,XLog sampleLog) {
		HashMap<String,Integer> DfrNumber_sample=SampleEvalutionPlugin.getDfrNumber(sampleLog);
		//2.对称平均误差 sMAPE
		Map<String, Double> map2 = DfrNumber_except_original;

		double abs_sum=0;
		for(Map.Entry<String, Double> entry : map2.entrySet()){
			double fenzi1=0;
			double fenmu1=0;
			if(DfrNumber_sample.containsKey(entry.getKey())) {
				fenzi1+=Math.abs(entry.getValue()-(double)DfrNumber_sample.get(entry.getKey()));
				fenmu1+=entry.getValue()+(double)DfrNumber_sample.get(entry.getKey());
				abs_sum+=fenzi1/fenmu1;
			}else {
				abs_sum+=1;
			}
		}
		double sMAPE_value=abs_sum/ DfrNumber_except_original.size();
//		System.out.println("sMAPE_value(指标2) = " + sMAPE_value);
		return sMAPE_value;
	}
	
	
	/**
	 * 
	 * @param originalLog
	 * @param sampleLog
	 * @return 当前代价的值
	 */
	public static double CurrentCost(HashMap<String,Double> DfrNumber_except_original,XLog sampleLog,XTrace trace) {
		XLog sampleLog2 = new XLogImpl(sampleLog.getAttributes());
		sampleLog2=(XLog) sampleLog.clone();//对象的克隆
		sampleLog2.add(trace);
		HashMap<String,Integer> DfrNumber_sample=SampleEvalutionPlugin.getDfrNumber(sampleLog2);


		//2.对称平均误差 sMAPE
		Map<String, Double> map2 = DfrNumber_except_original;

		double abs_sum=0;
		for(Map.Entry<String, Double> entry : map2.entrySet()){
//			fenmu+= entry.getValue();
			double fenzi1=0;
			double fenmu1=0;
			if(DfrNumber_sample.containsKey(entry.getKey())) {
				fenzi1+=Math.abs(entry.getValue()-(double)DfrNumber_sample.get(entry.getKey()));
				fenmu1+=entry.getValue()+(double)DfrNumber_sample.get(entry.getKey());
				abs_sum+=fenzi1/fenmu1;
			}else {
				abs_sum+=1;
			}
		}
		double sMAPE_value=abs_sum/ DfrNumber_except_original.size();
//		System.out.println("sMAPE_value(指标2) = " + sMAPE_value);
		return sMAPE_value;
	}
	
	/************************************************排序算法***************************************/
    // Map的value值降序排序
    public static <K, V extends Comparable<? super V>> Map<K, V> sortDescend(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                int compare = (o1.getValue()).compareTo(o2.getValue());
                return -compare;
            }
        });
 
        Map<K, V> returnMap = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            returnMap.put(entry.getKey(), entry.getValue());
        }
        return returnMap;
    }
 
    // Map的value值升序排序
    public static <K, V extends Comparable<? super V>> Map<K, V> sortAscend(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                int compare = (o1.getValue()).compareTo(o2.getValue());
                return compare;
            }
        });
 
        Map<K, V> returnMap = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            returnMap.put(entry.getKey(), entry.getValue());
        }
        return returnMap;
    }
}
