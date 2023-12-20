package kmean;

import java.util.List;

import org.deckfour.xes.model.XLog;

/**
 * @author R.P. Jagadeesh Chandra 'JC' Bose
 * @date 14 July 2010 
 * @since 01 July 2010
 * @version 1.0
 * @email j.c.b.rantham.prabhakara@tue.nl
 * @copyright R.P. Jagadeesh Chandra 'JC' Bose
 * 			  Architecture of Information Systems Group (AIS) 
 * 			  Department of Mathematics and Computer Science
 * 			  University of Technology, Eindhoven, The Netherlands
 */

/*
 * This class stores the sub-logs for each cluster
 * 该类存储每个集群的子日志
 */
public class ClusterLogOutput {
	int noClusters;
	List<XLog> clusterLogList;
	
	public ClusterLogOutput(int noClusters, List<XLog> clusterLogList){
		this.noClusters = noClusters;
		this.clusterLogList = clusterLogList;
	}
	
	public List<XLog> clusterLogList(){
		return clusterLogList;
	}
	
	public XLog getClusterLog(int clusterIndex){
		if(clusterIndex < noClusters)
			return clusterLogList.get(clusterIndex);
		else
			return null;
	}
	
	public int getNoClusters(){
		return noClusters;
	}
}