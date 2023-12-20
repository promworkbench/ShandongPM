package CrossOrgMining;

import java.util.HashMap;
import java.util.HashSet;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.graphviz.dot.DotCluster;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.graphviz.dot.DotNode;

public class VisualizeHPNandInteraction2Dot {
	
	// the input is a hpn, a dot cluster, and the interfaces 
	//输入是一个hpn,  一个点簇和接口
	/**
	 * 
	 * @param pn
	 * @param cluster
	 * @param tDotNodeSet: record all t node in the interaction models 
	 * @return
	 */
	
	public static DotCluster visualizePN2Dot(Petrinet pn, DotCluster cluster, HashSet<DotNode> tDotNodeSet)
	{
		// the mapping from transition(place) to dotNode
		HashMap<PetrinetNode, DotNode> mapPetrinet2Dot = new HashMap<PetrinetNode, DotNode>();

		//add transitions
		for (final Transition t : pn.getTransitions()) 		{
			DotNode tDot;
			if (t.isInvisible()) // invisible transition
			{
				tDot = new LocalDotTransition();
			} 
			else// normal transition, i.e., interfaces
			{	
				tDot = new LocalDotTransition(t.getLabel(), 2);	
				tDotNodeSet.add(tDot);
			}
			cluster.addNode(tDot);
			
			mapPetrinet2Dot.put(t, tDot);		
		}
		
		//add places
		for (final Place p : pn.getPlaces()) 
		{
			int startFlag=0;
			int endFlag=0;
			//check if the p is start or end. 
			for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : pn.getEdges())
			{
				//if there is no incoming arc, this place is a start place
				if(edge.getTarget().getId().equals(p.getId()))
				{
					startFlag=1;
					break;
				}
			}
			
			//check if the p is start or end. 
			for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : pn.getEdges())
			{
				//if there is no out-going arc, this place is a end place
				if(edge.getSource().getId().equals(p.getId()))
				{
					endFlag=1;
					break;
				}
			}
			
			//if the place is a start place
			DotNode pDot;
			if(startFlag==0)
			{
				pDot = new LocalDotPlace(0);
			}
			else if (endFlag ==0)
			{
				pDot = new LocalDotPlace(1);
			}
			else {
				pDot = new LocalDotPlace();
			}
//			DotNode pDot;
//			pDot = new LocalDotPlace();
			
			cluster.addNode(pDot);
			mapPetrinet2Dot.put(p, pDot);
		}
		
		//add arcs
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : pn.getEdges()) 
		{
			if (mapPetrinet2Dot.get(edge.getSource()) != null && mapPetrinet2Dot.get(edge.getTarget()) != null) 
			{
				cluster.addEdge(mapPetrinet2Dot.get(edge.getSource()), mapPetrinet2Dot.get(edge.getTarget()));
			}
		}
		
		return cluster;
	}
	
	//inner class for transition dot
	private static class LocalDotTransition extends DotNode 
	{
		//transition flag =0, normal transition flag=1, nested transition, flag=2 multi-instance transition
		public LocalDotTransition(String label, int flag) {
			super(label, null);
			if (flag==0)
			{
				
				setOption("shape", "box");
			}
			else if (flag==1)//nested transition
			{				
				setOption("shape", "box");
				setOption("style", "filled");
				//setOption("peripheries","2");//double line
			}
			else // multi-instance
			{
				setOption("shape", "box");
				setOption("fillcolor", "green");
				setOption("width", "0.45");
				setOption("height", "0.15");
				//setOption("style", "invis");
				//setOption("shape", "point");
			}
		}
		
		//tau transition
		public LocalDotTransition() {
			super("", null);
			setOption("style", "filled");
			setOption("fillcolor", "black");
			setOption("width", "0.45");
			setOption("height", "0.15");
			setOption("shape", "box");
		}
	}// inner class for transition dot
	
	
	//inner class for place dot
	private static class LocalDotPlace extends DotNode {
		public LocalDotPlace() {
			super("", null);
			setOption("shape", "circle");
		}
			public LocalDotPlace(int flag) {
				super("", null);
				if (flag==0) // the start place. with green.
				{
					setOption("shape", "doublecircle");
					setOption("style", "filled");
					setOption("fillcolor", "green");
//					setOption("image", "D:/triangle.svg");
					
				}
				else // the complete place with red
				{
					setOption("shape", "doublecircle");
					setOption("style", "filled");
					setOption("fillcolor", "red");
				}
			}
		
	}// inner class for place dot
	
	// inner class for edge dot, we have two types of edges, 
	//the normal one with arrows =1, and the double arrows with arrows =2.
	private static class LocalDotEdge extends DotEdge
	{
		public LocalDotEdge(DotNode source, DotNode target, int arrows)
		{
			super(source, target);
			if (arrows==1)
			{	
				setOption("arrowhead", "vee");
			}
			else
			{
				//startEdge =dot.addEdge(startTransitionDot,startPlaceDot);
				//setOption("penwidth","2");
				//startEdge.setOption("color", "black:black");
				//setOption("label", " N");
				setOption("arrowhead", "vee");//or to use normalnormal twice
				//startEdge.setOption("shape", "vee");
			}
		}
	}

	//here the input is the event class, showing the "class+package+method"
	public static String ActivityNameMethodClassPackage(String input)
	{
		
		String []parts = input.split("\\+");
		if (parts.length>2)
		{
			return parts[2]+"."+parts[1]+"."+parts[0]; 
		}
		else 
		{
			return input;
		}
		
	}
}
