package CrossOrgMining;

import java.util.Set;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

/**
 * this class defines a directed graph based on GraphT.
 * it is used to represent the activity precedence relation discovered from the declare miner. 
 * @author cliu3
 *
 */
public class ActivityPrecedenceGraph {

	//A directed weighted graph is a non-simple directed graph in which multiple edges between any two vertices are not permitted, 
	//but loops are. The graph has weights on its edges.
	private DefaultDirectedGraph<String, DefaultEdge> g = 
			new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
	
	public ActivityPrecedenceGraph(DefaultDirectedGraph<String, DefaultEdge> g)
	{
		this.g =g;
	}
	
	/**
	 * add vertex to the graph
	 * @param name
	 */
	public void addVertex(String node) {
			g.addVertex(node);
	}
	
	/**
	 * add edge to the vertex
	 * @param v1
	 * @param v2
	 * @return
	 */
	public DefaultEdge addEdge(String n1, String n2) {
		return g.addEdge(n1, n2);
	}
	
	public DefaultEdge addEdge(DefaultEdge edge)
	{
		return g.addEdge(g.getEdgeSource(edge), g.getEdgeTarget(edge));
	}
	
	/**
	 * return the current graph
	 * @return
	 */
	public DefaultDirectedGraph<String, DefaultEdge> getActivityPrecedenceGraph() {
		return g; 	
	}
	
	/**
	 * get the edge set
	 * @return
	 */
	public Set<DefaultEdge> getAllEdges()
	{
		return g.edgeSet();
	}
	
	/**
	 * get the vertext set
	 * @return
	 */
	public Set<String> getAllVertexes()
	{
		return g.vertexSet();
	}
}
