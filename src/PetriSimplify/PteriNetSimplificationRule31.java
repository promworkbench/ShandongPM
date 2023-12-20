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
 * 规则1：静默变迁有多个入边，只有一个出边，出边连接的库所单进单出
 * @author ZSP
 *
 */
public class PteriNetSimplificationRule31 {
	public static boolean reduce(Petrinet net, Canceller canceller) {
		/*
		 * Iterate over all transitions.
		 */
		for (Transition transitionA : net.getTransitions()) {
			if (canceller.isCancelled()) {
				return true;
			}

			/*
			 * Check whether the transition is silent.
			 */
			if (!transitionA.isInvisible()) {
				continue;
			}
			if(net.getOutEdges(transitionA).size() != 1) {//确保静默变迁的出边只有一个
				continue;
			}
			
			Place placeY = getPlaceY(net, transitionA);
			if(net.getOutEdges(placeY).size() != 1) {//
				continue;
			}
			if(net.getInEdges(placeY).size() != 1) {//
				continue;
			}
			System.out.println("placeY:"+placeY);
			if (placeY == null) {
				continue;
			}
			Transition target=null; 
			//只循环了一次
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : net.getOutEdges(placeY)) {
				target = (Transition) edge.getTarget();
				net.removeEdge(edge);
//				net.addArc(placeY, target, weight);
			}
			/////
			if(net.getInEdges(target).size() == 1) {//
				continue;
			}
			
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : net.getInEdges(transitionA)) {
				Place place1 = (Place) edge.getSource();
				net.removeEdge(edge);
				net.addArc(place1, target);
			}

			//remove the place
			net.removePlace(placeY);
			net.removeTransition(transitionA);

			return true;
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
