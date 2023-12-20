package equalscale.SampleMethods;

/*
 * A fast algorithm to sample log. 
 * The original idea is referred to "The automatic creation of literature abstracts". 
 * 
 * rank a trace by its significance;
 * the significance of a trace is determined by the combination of its activity significance and dfr significance. 
 * 日志完整性采样
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

import equalscale.SampleEvaluation.SampleEvalutionPlugin;


public class SetCoverEqualScaleMethods {
	@Plugin(
			name = " (set cover)sampling Plugin",// plugin name
//			name="Similarity-based  Sample Log Evaluation Method Plugin",
			returnLabels = {"Sample Log"}, //return labels
			returnTypes = {XLog.class},//return class
//			returnLabels = {"Similarity Value"}, //return labels
//			returnTypes = {String.class},//return class
			//input parameter labels, corresponding with the second parameter of main function
			parameterLabels = {"Orginal Event Log"},
			userAccessible = true,
			help = "This plugin aims to the orginal  log and sample log,returns a similarity  value by measuring the log similarity." 
			)
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
	
	public static XLog DijkstraPlusSampling(UIPluginContext context, XLog originalLog) throws UserCancelledException
	{
		
		double startTime_total=0;
		double endTime_total=0;
		//set the sampling ratio
		double samplingRatio = ProMUIHelper.queryForDouble(context, "Select the sampling ratios", 0, 1,	0.3);		
		context.log("Interface Sampling Ratio is: "+samplingRatio, MessageLevel.NORMAL);	
		startTime_total=System.currentTimeMillis();
		XLog log=DijkstraPlus(originalLog,samplingRatio);
		endTime_total=System.currentTimeMillis();
		
		System.out.println("***********************");
		System.out.println("Set Cover Time:"+(endTime_total-startTime_total));
		System.out.println("***********************");
		context.log("Set Cover Time:"+(endTime_total-startTime_total));	
		return log;
	}
	//输入日志，输出样本日志
		public static XLog DijkstraPlus(XLog originalLog, double SampleRatio) {
			
			XLog sampleLog = new XLogImpl(originalLog.getAttributes());
			double currentCost=0.0;//当前代价
			HashMap<XTrace,Boolean> StatusVisited=new HashMap<>();//访问状态
			HashMap<XTrace,Double> traceWeight=new HashMap<>();//权重系数
			
			
			XLog newOriginalLog = new XLogImpl(originalLog.getAttributes());
			newOriginalLog=(XLog) originalLog.clone();//对象的克隆
			
			
			
			/**
			 * 1.计算原始日志的期望值
			 */
			//计算dfr的期望值
			HashMap<String,Integer> DfrNumber_original=SampleEvalutionPlugin.getDfrNumber(originalLog);
			System.out.println("DfrNumber_original:"+DfrNumber_original);
			//采样率自设，名称为SampleRatio,默认为30%
//			double SampleRatio=0.1;
			HashMap<String,Double> DfrNumber_except_original=new HashMap<>();
			HashMap<String,Double> DfrNumber_except_org=new HashMap<>();
			Map<String, Integer> map0 = DfrNumber_original;
			for(Map.Entry<String, Integer> entry : map0.entrySet()){
				DfrNumber_except_original.put(entry.getKey(), entry.getValue()*SampleRatio);
				DfrNumber_except_org.put(entry.getKey(), entry.getValue()*SampleRatio);
//				System.out.println("轨迹名： " + entry.getKey()+"值： " + entry.getValue()*SampleRatio);
			}
			
			/**
			 * 2.过滤值小于1的集合
			 */
			HashMap<String,Double> DfrNumber_except_original0=new HashMap<>();//保留df值大于1的集合
			HashMap<String,Double> DfrNumber_except_original1=new HashMap<>();//保留df值小于1的集合
			Map<String, Double> map1 = DfrNumber_except_original;
			for(Map.Entry<String, Double> entry : map1.entrySet()){
				if(entry.getValue()>0 && entry.getValue()<1) {
					DfrNumber_except_original1.put(entry.getKey(),entry.getValue());
				}else if(entry.getValue()>1) {
					DfrNumber_except_original0.put(entry.getKey(),entry.getValue());
				}
			}
			System.out.println("原始的DfrNumber_except_original:"+DfrNumber_except_original);
			System.out.println("原始的DfrNumber_except_original0:"+DfrNumber_except_original0);
			System.out.println("原始的DfrNumber_except_original1:"+DfrNumber_except_original1);
			System.out.println("原始的DfrNumber_except_original0.size():"+DfrNumber_except_original0.size());
			System.out.println("originalLog.size():"+originalLog.size());
			int orgSize=originalLog.size();
			int count=1;
			while(!isMinVisited(DfrNumber_except_original0) && count <=orgSize*SampleRatio) {
				
				
				Map<String, Double> map3 = DfrNumber_except_original;
				System.out.println("***************************");
				System.out.println("DfrNumber_except_original:"+DfrNumber_except_original);
				DfrNumber_except_original0.clear();//样本期望值大于1的df集合  清空
				for(Map.Entry<String, Double> entry : map3.entrySet()){
					if(entry.getValue()>0 &&entry.getValue()<1) {
						DfrNumber_except_original1.put(entry.getKey(),entry.getValue());
					}else if(entry.getValue()>= 1) {
						DfrNumber_except_original0.put(entry.getKey(),entry.getValue());
					}
				}
				System.out.println("第"+count+"次循环:");
				System.out.println("DfrNumber_except_original0:"+DfrNumber_except_original0);
				System.out.println("DfrNumber_except_original1:"+DfrNumber_except_original1);
				
				XLog log = new XLogImpl(originalLog.getAttributes());
				log=SetCover(originalLog,DfrNumber_except_original0);//集合覆盖算法
				//这个判定是因为经常出现集合为1个的情况，所以需要进行设定下 
				double allCurrentCost1=0.0;
				if(DfrNumber_except_original0.size()<2 ) {
					allCurrentCost1=CurrentCost(DfrNumber_except_org,sampleLog);
					System.out.println("先前的花费值为:" +allCurrentCost1);
				}//做个条件判定
				
				
				//输入参数意思：原始日志和样本期望的df大于1的集合
				//输出为样本覆盖日志
				//将轨迹添加到样本日志中
				System.out.println("第"+count+"次加入的轨迹:");
				for(XTrace trace:log)
				{	
					System.out.print(" " +trace.getAttributes().get("concept:name").toString());
					sampleLog.add(trace);
				}
				double allCurrentCost2=0.0;
				if(DfrNumber_except_original0.size()<2 ) {
					allCurrentCost2=CurrentCost(DfrNumber_except_org,sampleLog);
					System.out.println("最后的name值为:" +allCurrentCost2);
					
				}//做个条件判定
				if(allCurrentCost1 < allCurrentCost2) {
					for(XTrace trace:log)
					{	
						sampleLog.remove(trace);
					}
					break;
				}
//				//对其做减1处理
//				Map<String, Double> map2 = DfrNumber_except_original0;
//				for(Map.Entry<String, Double> entry : map2.entrySet()){
////					DfrNumber_except_original0.clear();
//					if(entry.getValue()-count>1) {
//						DfrNumber_except_original0.put(entry.getKey(),entry.getValue()-count);//其实有点重复了
////						DfrNumber_except_original.put(entry.getKey(), entry.getValue()-1);
//						number++;
//					}
//					else {
//						DfrNumber_except_original0.put(entry.getKey(),(double) -1000);
////						DfrNumber_except_original.remove(entry.getKey());
//					}
//				}
				System.out.println("2222DfrNumber_except_original0.size():"+DfrNumber_except_original0.size());

				
				//更新样本日志期望的df值集合
				DfrNumber_except_original.clear();
				//计算dfr的期望值
				HashMap<String,Integer> DfrNumber_originalNew=SampleEvalutionPlugin.getDfrNumber(originalLog);
				System.out.println("2222originalLog.size():"+originalLog.size());
				//采样率自设，名称为SampleRatio,默认为30%
				Map<String, Integer> map4 = DfrNumber_originalNew;
				
				
				System.out.println("2222DfrNumber_originalNew:"+DfrNumber_originalNew);
				for(Map.Entry<String, Integer> entry : map4.entrySet()){
					if(entry.getValue()*SampleRatio-count>0) {
    					DfrNumber_except_original.put(entry.getKey(), entry.getValue()*SampleRatio-count);
					}else {
						DfrNumber_except_original.put(entry.getKey(), (double) -1000);
					}
					
				}
				System.out.println("Finsh update:DfrNumber_except_original:"+DfrNumber_except_original);
				count++;
			}
			//最后这一步是什么意思？
		    System.out.println("sampleLog size： " + sampleLog.size());
			double allCurrentCost00=CurrentCost(DfrNumber_except_org,sampleLog);
			System.out.println("最后的name值为:" +allCurrentCost00);
			/*XLog log11 = new XLogImpl(originalLog.getAttributes());
			log11=SetCover(originalLog,DfrNumber_except_original1);
			for(XTrace trace:log11)
			{	
				sampleLog.add(trace);
			}
			System.out.println("111111sampleLog1 size： " + sampleLog.size());
			double allCurrentCost3=CurrentCost(DfrNumber_except_org,sampleLog);
			System.out.println("111111最后的name值为:" +allCurrentCost3);
			
			HashMap<String,Double> DfrNumber_except_original1_new=new HashMap<>();//保留df值大于1的集合
			System.out.println("DfrNumber_except_original1:" +DfrNumber_except_original1);
			System.out.println("DfrNumber_except_original1.size():" +DfrNumber_except_original1.size());*/
			
//			Map<String, Double> map5 = DfrNumber_except_original1;
//			for(Map.Entry<String, Double> entry : map1.entrySet()){
//				if(entry.getValue()>0 && entry.getValue()<1) {
//					DfrNumber_except_original1.put(entry.getKey(),entry.getValue());
//				}else if(entry.getValue()>1) {
//					DfrNumber_except_original0.put(entry.getKey(),entry.getValue());
//				}
//			}
	/*
			//1.初始化各系数：将轨迹权重第一次初始化traceWeight,将轨迹状态都设置为未访问
			for (XTrace trace: originalLog)	{   ///all trace size!=0
				XLog sampleLog1 = new XLogImpl(originalLog.getAttributes());
				sampleLog1=(XLog) sampleLog.clone();//对象的克隆
				sampleLog1.add(trace);
				currentCost=CurrentCost(DfrNumber_except_original1,sampleLog1);//当前越小越好
				StatusVisited.put(trace, false);//初始化全部的轨迹都为未访问状态
				traceWeight.put(trace, currentCost);
			}		
			//2.选择最小的mse值作为起始点
			Map<XTrace, Double> map3 = sortAscend(traceWeight);
//			int count=0;
			XTrace FirstTrace=null;
			for(Entry<XTrace, Double> entry : map3.entrySet()){
//				System.out.println("key = " + entry.getKey() + ", value = " + entry.getValue());
//				if(count ==0) {
					sampleLog.add(entry.getKey());//将最小值添加到样本日志中
					FirstTrace=entry.getKey();
					System.out.println("被选择的第一条轨迹 为= " + entry.getKey().getAttributes().get("concept:name").toString());
//					System.out.println("NAMPE值为： " + CurrentCost(DfrNumber_except_original,sampleLog));		
					break;
//				}
			}

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
			double PrecurrentCost=currentCost;//当前代价
			boolean flag=true;
			
			
//			Map<XTrace, Double> textmap3 = traceWeight;
			
			
			while(!isAllVisited(StatusVisited) && flag) {//当前的所有状态都被访问到，或者误差值小于一定的值(0.01)，终止循环	
//			while(flag) {//当前的所有状态都被访问到，或者误差值小于一定的值(0.01)，终止循环	
				// 将这个节点设置为已访问
				StatusVisited.put(trace00, true);
	            // 查看邻接矩阵中与指定节点邻接的节点
//		     	int value=0;
	            for (XTrace trace:originalLog) {
	            	if(!StatusVisited.get(trace)) {
	                    // 可能的新路径权值: 从最开始的指定起点到本轮起点到该节点的路径权值总和
	                    double newWeight;//轨迹的新权重
	                    // 如果节点未访问, 且是邻接节点  写错了
	                    newWeight=CurrentCost(DfrNumber_except_original1,sampleLog,trace);
	                    if(newWeight<traceWeight.get(trace)) {//大多数都不满足？
	                    	// 则更新该节点的最小路径值, 更新该节点的前驱为本轮起点
	                    	traceWeight.put(trace,newWeight);   //更新权重
	                    	
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
//	    				System.out.println("NAMPE值为： " + CurrentCost(DfrNumber_except_original,sampleLog));		
	    				break;
	    			}
	    		}
	    		double allCurrentCost=CurrentCost(DfrNumber_except_original1,sampleLog);
	    		double allCurrentCost2=CurrentCost(DfrNumber_except_org,sampleLog);
	    		System.out.println("当前值 allCurrentCost:" +allCurrentCost);
	    		System.out.println("先前值 PrecurrentCost:" +PrecurrentCost);
	    		System.out.println("整体上 allCurrentCost:" +allCurrentCost2);
//	    		if(PrecurrentCost > allCurrentCost && allCurrentCost >0.1) {
	    		if( PrecurrentCost > allCurrentCost) {//陷入局部最优解   判断条件是不是要改一下，不是单调递减的
	    			PrecurrentCost=allCurrentCost;
	    			System.out.println("111先前值大于当前值 ！！" );	
	    		}else {
	    			flag=false;
	    			System.out.println("最后的name值为:" +allCurrentCost);
	    			System.out.println("先前值小于当前值 ！！！" );	
	    		}
	    		
	    		
	            // 下轮起点from设置为: weights数组中数值最小的并且未访问的节点
	    		//更新权重
	    		for (XTrace trace: originalLog)	{   ///all trace size!=0
	    			if(!StatusVisited.containsKey(trace)) {
		    			XLog sampleLog3 = new XLogImpl(originalLog.getAttributes());
		    			sampleLog3=(XLog) sampleLog.clone();//对象的克隆
		    			sampleLog3.add(trace);
		    			currentCost=CurrentCost(DfrNumber_except_original1,sampleLog3);//当前越小越好
		    			traceWeight.put(trace, currentCost);
	    			}
	    		}
//	    		countValue++;
			}
		*/
//			return sampleLog;
			return sampleLog;
		}
		/**
	     * 检查是否全部被遍历(只要有一个是未被遍历返回false)
	     *
	     * @return boolean
	     */
	    public static boolean isMinVisited(HashMap<String,Double> StatusVisited) {
	    	Map<String, Double> map4 =StatusVisited ;
			for(Entry<String, Double> entry : map4.entrySet()){
				if(entry.getValue()>1) {
					return false;
				}
			}
	        return true;
	    }
		public static XLog SetCover(XLog originalLog,HashMap<String,Double> DfrNumber_except_original0)
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
//			for(int i=0;i<TraceIdList.size();i++) {
//				System.out.println("TraceIdList"+i+":"+TraceIdList.get(i));
//				System.out.println(TraceID2Trace.get(TraceIdList.get(i)));
//			}
			
			//trace to direct-follow relation set
			HashMap<String, HashSet<String>> traceIDToDFRSet = new HashMap<>();
			for(XTrace trace:originalLog)
			{
				HashSet<String> dfrSet = new HashSet<>();
				for(int i =0;i<trace.size()-1;i++)
				{
					//add directly follow pair
					dfrSet.add(XConceptExtension.instance().extractName(trace.get(i))+","+XConceptExtension.instance().extractName(trace.get(i+1)));
				}
				traceIDToDFRSet.put(XConceptExtension.instance().extractName(trace), dfrSet);
			}
			
			//direct-follow relation set of the log
			HashSet<String> dfrSetLog = new HashSet<>();
			Map<String, Double> map1 = DfrNumber_except_original0;
			for(Map.Entry<String, Double> entry : map1.entrySet()){
				if(entry.getValue()>1) {
					dfrSetLog.add(entry.getKey());
				}
				
			}
			

//			System.out.println("--------加强for循环遍历---------");
//			for (String item : dfrSetLog) {
//				System.out.println(item+",");
//			}

//				 创建广播电台,放入到Map
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
//	        System.out.println("232323 ");
	        //当剩余的地区不为0时,持续选择
	        int num=allAreas.size();
	        while (allAreas.size() != 0 && num > 0) {
	        	
	            //指针置空
	            maxKey = null;
	            //遍历所有的灯塔,找出最优解
//	           int count=0;
	            for (String key : broadcasts.keySet()) {
//	            	count++;
//	            	if(count == broadcasts.keySet().size()) break;
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
	            	originalLog.remove(TraceID2Trace.get(maxKey));
	            	//存放轨迹
	            	traceSet.add(TraceID2Trace.get(maxKey));
	            	//if()
	                selects.add(maxKey);
	                //从allAreas中移除已选择的
	                allAreas.removeAll(broadcasts.get(maxKey));
	            }
	            num--;
	        }
//	     System.out.println("originalLog.size():"+originalLog.size());	 
//	     System.out.println("sampleLog.size():"+sampleLog.size());	    
		//return the sample log.
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
//			System.out.println("sMAPE_value(指标2) = " + sMAPE_value);
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
//				fenmu+= entry.getValue();
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
//			System.out.println("sMAPE_value(指标2) = " + sMAPE_value);
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
