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
 * 规则3-1：中间库所只有一个进边连接变迁A，只有一个出边连接静默变迁；
 * 变迁A有多个入边和出边，静默变迁只有一个入边和多个出边
 * @author ZSP
 *
 */
public class PteriNetSimplificationRule32 {
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
				if (arcs1.size() != 1) {
					continue;
				}
				PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> arc1 = arcs1.iterator().next();
				if (!(arc1 instanceof Arc)) {
					continue;
				}

				placeX = (Place) arc1.getSource();
				System.out.println("net.getTransitions():"+net.getTransitions());
				System.out.println("arcs1:"+arcs1);
				System.out.println("placeX:"+placeX);
			}


			/*
			 * Target pattern identified. Proceed with reduction: remove place X
			 */

			/*
			 * Relocate all input arcs from place X to place Y
			 */
			if(net.getInEdges(placeX).size()!=1) {
				continue;
			}
			if(net.getOutEdges(placeX).size()!=1) {
				continue;
			}
			Transition source=null;
			//只循环一次
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : net.getInEdges(placeX)) {
				source= (Transition) edge.getSource();
				System.out.println("库所X的入边的变迁:"+source);
//				int weight = ((Arc) edge).getWeight();
				net.removeEdge(edge);
//				net.addArc(source, placeY, weight);
//				net.addArc(source, placeY);
			}
			if(net.getOutEdges(source).size() ==1) {
				continue;
			}
			
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : net.getOutEdges(transitionA)) {
				Place place1= (Place) edge.getTarget();
				System.out.println("库所X的入边的变迁:"+source);
//				int weight = ((Arc) edge).getWeight();
				net.removeEdge(edge);
//				net.addArc(source, placeY, weight);
				net.addArc(source, place1);
			}
			
			/*
			 * Relocate all output arcs from place X to place Y
			 */
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : net.getOutEdges(placeX)) {
//				Transition target = (Transition) edge.getTarget();
//				int weight = ((Arc) edge).getWeight();
				net.removeEdge(edge);
//				net.addArc(placeY, target, weight);
			}


			//remove the place
			net.removePlace(placeX);
			net.removeTransition(transitionA);
//			net.removeTransition(transitionB);

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
