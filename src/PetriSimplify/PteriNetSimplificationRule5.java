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
 * 规则1：并发库所，库所A和库所B有多个直达的静默变迁
 * @author ZSP
 *
 */
public class PteriNetSimplificationRule5 {
	public static boolean reduce(Petrinet net, Canceller canceller) {
		/*
		 * Iterate over all transitions.
		 */
		for (Place placeA : net.getPlaces()) {
			if (canceller.isCancelled()) {
				return true;
			}
			System.out.println("net.getPlaces():"+net.getPlaces());
			System.out.println("placeA:"+placeA);
			System.out.println("net.getOutEdges(transitionA).size():"+net.getOutEdges(placeA).size());
			if(net.getOutEdges(placeA).size() <=2) {
				continue;
			}
			
			System.out.println("net.getOutEdges(transitionA):"+net.getOutEdges(placeA));
			
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : net.getOutEdges(placeA)) {
				if(!((Transition) edge.getTarget()).isInvisible()) {
					continue;
				}
				Transition transitionA=(Transition) edge.getTarget();
				Transition transitionB=null;
				for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge_new : net.getOutEdges(placeA)) {
					if(((Transition) edge_new.getTarget())==transitionA) continue;
					transitionB=(Transition) edge_new.getTarget();
					System.out.println("333333333333");
					if(net.getInEdges(transitionA).size() !=1 || net.getOutEdges(transitionA).size() !=1 
							|| net.getInEdges(transitionB).size() !=1 || net.getOutEdges(transitionB).size() !=1) {
						continue;
					}
					System.out.println("44444444444444444");
					for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge1 : net.getOutEdges(transitionA)) {
						Place place1 = (Place) edge1.getTarget();
						if(net.getInEdges(place1).size() <=2) {
							continue;
						}
						for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge2 : net.getOutEdges(transitionB)) {
							Place place2 = (Place) edge2.getTarget();
							if(net.getInEdges(place2).size() <=2) {
								continue;
							}
							if(place1 == place2) {
								net.removeEdge(edge2);
								net.removeTransition(transitionB);
								return true;
							}
							
						}
					}
				}
			}
				
			System.out.println("**********************###############");
//			return true;
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
