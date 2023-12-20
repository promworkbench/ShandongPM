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
 * 规则1：静默变迁只有一个入边和一个出边，入边和出边连接的是同一个库所
 * @author ZSP
 *
 */
public class PteriNetSimplificationRule7 {
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

			/*
			 * Check input arc.
			 */
			Place placeX;
			{
				Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> arcs1 = net
						.getInEdges(transitionA);
				if (arcs1.size() != 1) {//入边为1
					continue;
				}
				PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> arc1 = arcs1.iterator().next();
				if (!(arc1 instanceof Arc)) {
					continue;
				}
				
				Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> arcs2 = net
						.getInEdges(transitionA);
				if (arcs2.size() != 1) {//出边为1
					continue;
				}
				PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> arc2 = arcs2.iterator().next();
				if (!(arc2 instanceof Arc)) {
					continue;
				}
				
				placeX = (Place) arc1.getSource();
				System.out.println("net.getTransitions():"+net.getTransitions());
				System.out.println("arcs1:"+arcs1);
				System.out.println("placeX:"+placeX);
			}

			Place placeY = getPlaceY(net, transitionA);
			System.out.println("placeY:"+placeY);
			if (placeY == null) {
				continue;
			}

			/*
			 * Check that we're not dealing with a self loop
			 */
			if (placeX == placeY) {
				for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : net.getInEdges(transitionA)) {	
					net.removeEdge(edge);
				
				}
				for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : net.getOutEdges(transitionA)) {
					net.removeEdge(edge);
				}				
				net.removeTransition(transitionA);
				return true;
			}

			
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
