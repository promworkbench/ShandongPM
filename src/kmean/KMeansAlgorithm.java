package kmean;



/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XLogImpl;
//import org.processmining.analysis.traceclustering.distance.DistanceMetric;

import cern.colt.matrix.DoubleMatrix2D;

/**
 * @author Minseok Song
 */
public class KMeansAlgorithm   {



	protected ArrayList<Integer> traceList;
	protected int clusterSize;//初始化聚类大小为4
	protected int traceSize;
	protected ArrayList<Double> currentDistanceSum;
	protected ArrayList<ClusterSet> clustersList;
	protected ArrayList<Integer> frequencyList;
	protected ClusterSet clusters;
	protected ClusterSet clustersforOthers;
	//protected DistanceMetric distanceMeasures;
	protected DoubleMatrix2D distanceMatrix;
	protected DoubleMatrix2D distances;
	protected static String ST_FREQUENCY = "Frequency";
	protected static String ST_DISTANCE = "Distance";
	protected ArrayList<Integer> TraceIdList1;
	protected ArrayList<String> traceIdList0;

	protected KMeansAlgorithm() {
		
		// TODO 自动生成的构造函数存根
	}
	/**
	 * logRank++中有三个参数：
	 * TraceIdList:可以说存储的trace id
	 * nameToTrace：存储的是trace  带事件
	 * matrix：存储的是相似性
	 * double[][] matrix = new double[TraceIdList.size()][TraceIdList.size()];
	 * 存放的是第i条轨迹到第j条轨迹的相似性，也可以说是距离
	 * 这里与distence相对应
	 * @throws FileNotFoundException 
	 */
	
	public ClusterSet runKmean(XLog log,HashMap<String, XTrace> nameToTrace,ArrayList<String> TraceIdList,double[][] matrix,int clusterSize) 
	{
		ClusterSet clusters = new ClusterSet(log);
		//把N换成轨迹的大小
		/**
		 *1.初始化
		 * (1).初始化中心点
		 * 聚类的大小可以通过先设置，随后的话再传参
		 */	
		
	    // initialize clusters
		ClusterSet oldClusterSet = new ClusterSet(log);
		System.out.println("oldClusterSet:"+oldClusterSet.getClusters());
		//映射轨迹名
	    HashMap<String, Integer> mapTraceName=new HashMap<String, Integer>();
	    for(int i=0;i<TraceIdList.size();i++) {
	    	mapTraceName.put(TraceIdList.get(i), i);
	    }
		//初始化聚类 并添加了聚类的中心点属性分别为0，1，2，3

		/**
		 * 2.开始聚类，聚类的终止条件为达到最大迭代次数以及新聚类等于旧聚类
		 * iteration_number：表示最大迭代次数，默认设置为50
		 */
		/**
		 * (2).初始化聚类和大聚类集合，将聚类设置为空的日志
		 */
		for (int i = 0; i < clusterSize; i++) {
			XLog clusterLog = new XLogImpl(log.getAttributes());
			Cluster cluster = new Cluster(clusterLog, "Cluster " + i,i);
			clusters.addCluster(cluster);
		}
		int iteration_number=100;
		HashMap<Integer, Integer> center1= new HashMap<>();
//		ArrayList<Integer> list1=new ArrayList<Integer>();
		for (int k = 0; k < iteration_number; k++) {
			clusters.clear();//清空一下聚类集合
			System.out.println("第"+(k+1)+"次迭代！");
			   //添加聚类
			   //初始化 分为Cluster1 Cluster2 ...
	        //	calculate distance between each center point and each instance
			/**
			 * （1）计算距离，这里直接用前面传过来的相似性矩阵来计算
			 * int iteration_number=50;
			 * matrix1[i][j]表示轨迹i和轨迹j的距离，应为对称矩阵
			 * 	clusterSize:聚类大小，可以说聚类的数量		 
			 * **/
			 for (int i = 0; i < clusterSize; i++) {
				XLog clusterLog = new XLogImpl(log.getAttributes());
				if(k==0) {
					Cluster cluster = new Cluster(clusterLog, "Cluster " + i,i);
					clusters.addCluster(cluster);
				}else {
					Cluster cluster = new Cluster(clusterLog, "Cluster " + i,center1.get(i));
					clusters.addCluster(cluster);
				}
				
			}
			/**
			 * (3).初始化每个聚类的中心
			 */
			System.out.println("初始化clusters:"+clusters);
			for (int i = 0; i < clusterSize; i++) {
				System.out.println("第"+i+"个聚类名为："+clusters.getClusters().get(i).getName());
				System.out.println("第"+i+"个聚类的中心为："+clusters.getClusters().get(i).getClusterCenter());
				//clusters.getClusters().get(i).getClusterCenter();
			}
			/** int array[][];
           		数组宽度（列数）
               	int lenX = array[0].length;
          		数组高度（行数）
              	int lenY = array.length;
			 */
			for (int i = 0; i < matrix.length; i++) {
				int index = -1;
				double value = Double.MAX_VALUE;
				for (int j = 0; j < clusterSize; j++) {
					if(i != clusters.getClusters().get(j).getClusterCenter()) {
						if (value > matrix[i][clusters.getClusters().get(j).getClusterCenter()]) {
							index = j;
							value = matrix[i][clusters.getClusters().get(j).getClusterCenter()];
						}
					}
				}
//				clusters.getClusters().get(index).setCurrentDistanceSum(clusters.getClusters().get(index).getCurrentDistanceSum()+value);
//				System.out.println("第"+i+"个聚类");
				//算法：让每个轨迹和聚类中心点比较，取最小的那个
				//System.out.println("聚类数："+clusters.getClusters());//Cluster0, Cluster1, Cluster2, Cluster3
//				System.out.println("clusters.getClusters().get(index):"+clusters.getClusters().get(index));
				
//				clusters.getClusters().get(index).addTrace(traceList.get(i));
				//TraceIdList
//				for(int k=0;k<TraceIdList.size();k++){
//					if((clusters.getClusters().get(i).getTraceIndices().get(j)).toString().equals(TraceIdList.get(k))) {
//						XTrace clusterTrace=nameToTrace.get(TraceIdList.get(k));
//						clusters.getClusters().get(i).getLog().add(clusterTrace);
//					}
//				}
				//如果轨迹名不为数字的话，会出现问题
				//TraceIdList.get(i)：这里得到trace0
				
				//clusters.getClusters().get(index).getLog().add(e);
				//找到对应的值
				clusters.getClusters().get(index).addTrace(mapTraceName.get(TraceIdList.get(i)));
				//clusters.getClusters().get(index).addTrace(Integer.parseInt(TraceIdList.get(i)));
//				System.out.println("getTraceIndices："+clusters.getClusters().get(index).getTraceIndices());	
			}
//			System.out.println("999clusters:"+clusters);
			/**
			 * 4.重复计算中心
			 * 直到新类等于旧类，说明中心点不再改变
			 */
			// recalculate center
			center1.clear();
			center1=calculateCenter(clusters,matrix,clusterSize);
			try {
				if (k == 0) {
					for (int i = 0; i < clusterSize; i++)
					oldClusterSet = (ClusterSet) clusters.clone();
//					System.out.println("oldClusterSet:"+oldClusterSet.getClusters());
				} else {
					if (clusters.equals(oldClusterSet)) {
//						System.out.println("退出退出退出");
						break;
					} else {
						oldClusterSet = (ClusterSet) clusters.clone();
					}
				}
			} catch (Exception e) {}
			
	   }
		System.out.println("最后啦 哈哈"+clusters);
		ArrayList<String>  centerName=new ArrayList<String>();
		//这段的意思是说，将轨迹添加到所聚好的类的日志中
		for (int i = 0; i < clusterSize; i++) {
			//clusters.getClusters().get(i) 大聚类的集合
			System.out.println("开始聚类存放==================================");
			int center11=clusters.getClusters().get(i).getClusterCenter();
			System.out.println("第"+i+"个聚类的中心为："+clusters.getClusters().get(i).getClusterCenter());
			System.out.println("TraceIdList的中心为："+TraceIdList.size());
//			System.out.println("dasdasdadsa："+clusters.getClusters().get(i).getTraceIndices().get(center11));
			System.out.println("聚类中心轨迹名："+TraceIdList.get(center11));
			centerName.add(TraceIdList.get(center11));
//			System.out.println("中心轨迹坐标为："+mapTraceName.get(TraceIdList.get(clusters.getClusters().get(i).clusterCenter)));
//			System.out.println("聚类的值为："+clusters.getClusters().get(i).getTraceIndices());
			System.out.println("clusters.getClusters().get(i).getTraceIndices().size():"+clusters.getClusters().get(i).getTraceIndices().size());
			for(int j=0;j<clusters.getClusters().get(i).getTraceIndices().size();j++) {
//				System.out.println("j="+j);
//				System.out.println("clusters.getClusters().get(i).getTraceIndices().get(j)：["+clusters.getClusters().get(i).getTraceIndices().get(j)+"]");
				for(int k=0;k<TraceIdList.size();k++){
//					System.out.println("22["+mapTraceName.get(TraceIdList.get(k))+"]");
					if((clusters.getClusters().get(i).getTraceIndices().get(j)).equals(mapTraceName.get(TraceIdList.get(k)))) {
//						System.out.println("111111111111"+mapTraceName.get(TraceIdList.get(k)));
//						System.out.println("eeeee"+nameToTrace.get(TraceIdList.get(k)));
						XTrace clusterTrace=nameToTrace.get(TraceIdList.get(k));
						clusters.getClusters().get(i).getLog().add(clusterTrace);
						break;
					}
				}
				
			}
		}
		System.out.println("聚类中心轨迹名："+centerName);
		return clusters;
    }


	
	private HashMap<Integer, Integer> calculateCenter(ClusterSet clusters2,double[][] matrix,int clusterSize)
	{//中心的计算等于各cluster中的中心点到各点的距离的平均值
		/**
		 * 1.首先获得各聚类中心点和各聚类中元素的个数i
		 * 2.算的平均值，等于currentDistanceSum/i
		 * 3。更新聚类中心点就等于求得的平均值
		 */
		System.out.println("0000000");
//		ArrayList<Integer> list1=new ArrayList<Integer>();
		HashMap<Integer, Integer> center=new HashMap<Integer, Integer>();
		for(int i=0;i<clusterSize;i++) {
			int index=0;//记录下标
//			System.out.println("clusterSize:"+clusterSize);
			double value = Double.MAX_VALUE;;//记录最小的值
			for(int j=0;j<clusters2.getClusters().get(i).getTraceIndices().size();j++) {
				//聚类中多于两个点的话也就是说至少要3个点
//				System.out.println("2222222222200"+j+"  "+clusters2.getClusters().get(i).getTraceIndices().size());
				if(clusters2.getClusters().get(i).getTraceIndices().size() >2 ) {
					//计算每个点到其他点的距离，最近的距离取为中心点
					int points=clusters2.getClusters().get(i).getTraceIndices().get(j);
					double current_instance=0;
					for(int k=0;k<clusters2.getClusters().get(i).getTraceIndices().size();k++) {
						int otherPoint=clusters2.getClusters().get(i).getTraceIndices().get(k);
						current_instance+=matrix[points][otherPoint];
					}
					if (value > current_instance ) {//如果最终距离大于value；则记录下标和值
						index = j;
						value = current_instance;
					}
				}else {
					break;
				}			
			}
			//更新中心点坐标
			if(clusters2.getClusters().get(i).getTraceIndices().size() >2) clusters2.getClusters().get(i).setClusterCenter(index);
			center.put(i,clusters2.getClusters().get(i).getClusterCenter());
//			list1.add(clusters2.getClusters().get(i).getClusterCenter());
//			System.out.println("nengbuneng聚类的中心为11111："+clusters2.getClusters().get(i).getClusterCenter());
		}
		return center;
	}
}


