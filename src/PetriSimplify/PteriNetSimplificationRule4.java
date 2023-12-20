package PetriSimplify;

import java.util.Collection;

import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

/**
 * 规则1：并发库所，变迁A和变迁B之间有多个直达库所，这时候可以进行合并
 * @author ZSP
 *
 */
public class PteriNetSimplificationRule4 {
	public static boolean reduce(Petrinet net, Canceller canceller) {
		/*
		 * Iterate over all transitions.
		 */
		for (Transition transitionA : net.getTransitions()) {
			if (canceller.isCancelled()) {
				return true;
			}
			System.out.println("transitionA:"+transitionA);
//			System.out.println("11111net.getOutEdges(transitionA).size():"+net.getOutEdges(transitionA).size());
			/*
			 * Check whether the transition is silent.
			 */
			if (transitionA.isInvisible()) {
				continue;
			}
			if(net.getOutEdges(transitionA).size() <= 2) {
				continue;
			}
			
			/*
			 * Target pattern identified. Proceed with reduction: remove place X
			 */

			/*
			 * Relocate all input arcs from place X to place Y
			 */
//			Place place1=null;//p1的预设
			int count=0;
//			System.out.println("net.getOutEdges(transitionA).size():"+net.getOutEdges(transitionA).size());
//			System.out.println("net.getOutEdges(transitionA):"+net.getOutEdges(transitionA));
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : net.getOutEdges(transitionA)) {
				Place place1 = (Place) edge.getTarget();
				Place place2=null;
				for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge_new : net.getOutEdges(transitionA)) {
					if( ((Place) edge_new.getTarget())== place1) continue;
					place2 = (Place) edge_new.getTarget();
					if(net.getInEdges(place1).size() !=1|| net.getOutEdges(place1).size() !=1 
							|| net.getInEdges(place2).size() !=1 || net.getOutEdges(place2).size() !=1){
						continue;
					}
					{
						//只循坏一次
						for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge1 : net.getOutEdges(place1)) {
							Transition transition1=(Transition) edge1.getTarget();
//							if(net.getInEdges(transition1).size() <=2) {
//								continue;
//							}
							System.out.println("transition1:"+transition1);
							if (transition1.isInvisible()) {
								continue;
							}
							for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge2 : net.getOutEdges(place2)) {
								Transition transition2=(Transition) edge2.getTarget();
//								if(net.getInEdges(transition2).size() <=2) {
//									continue;
//								}
								System.out.println("transition2:"+transition2);
								if (transition2.isInvisible()) {
									continue;
								}
								if(transition1 == transition2) {
									System.out.println("successssss!!!!!!!!!!");
									net.removeEdge(edge2);
									net.removePlace(place2);
									return true;
									
								}
							}
						}
					}
				}			
			}
			System.out.println("**********************###############");
			
		}

		return false;
	}

	public static Place getPlaceY(Petrinet net, Transition transitionA) {
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> postset = net.getOutEdges(transitionA);
		if (postset.size() != 1) {
			return null;
		}
		PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> arc2 = postset.iterator().next();
		if (!(arc2 instanceof Arc)) {
			return null;
		}

//		if (((Arc) arc2).getWeight() != 1) {
//			return null;
//		}

		return (Place) arc2.getTarget();
	}

	public static Transition findTransitionB(Petrinet net, Place placeX, Place placeY) {
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> arc4 : net.getInEdges(placeX)) {

			if (((Arc) arc4).getWeight() == 1) {
				Transition transitionB = (Transition) arc4.getSource();
				
				if (!transitionB.isInvisible()) {
					continue;
				}

				/*
				 * transition B may only have one incoming arc; from place Y
				 */
				Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> arcs3 = net
						.getInEdges(transitionB);
				if (arcs3.size() != 1) {
					continue;
				}

				PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> arc3 = arcs3.iterator().next();

				if (arc3.getSource() != placeY) {
					continue;
				}

				if (((Arc) arc3).getWeight() != 1) {
					continue;
				}

				return transitionB;
			}
		}
		return null;
	}
}
