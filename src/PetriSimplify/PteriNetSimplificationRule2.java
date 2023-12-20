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
 * 规则2：前驱库所有多个进边和出边，其中有一个出边连接静默变迁；静默变迁的后继库所只有一个静默变迁入边和多个出边
 * 方法：把后继库所所有出边加入到前驱库所中
 * @author ZSP
 *
 */
public class PteriNetSimplificationRule2 {
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
				if (arcs1.size() != 1) {//保证静默变迁只有一个入边
					continue;
				}
				PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> arc1 = arcs1.iterator().next();
				if (!(arc1 instanceof Arc)) {
					continue;
				}

				placeX = (Place) arc1.getSource();
//				System.out.println("net.getTransitions():"+net.getTransitions());
//				System.out.println("arcs1:"+arcs1);
//				System.out.println("placeX:"+placeX);
			}

			Place placeY = getPlaceY(net, transitionA);//保证静默变迁只有一个出边
			System.out.println("placeY:"+placeY);
			if (placeY == null) {
				continue;
			}

			/*
			 * Check that we're not dealing with a self loop
			 */
			if (placeX == placeY) {
				continue;
			}
			

			/*
			 * Target pattern identified. Proceed with reduction: remove place X
			 */

			/*
			 * Relocate all input arcs from place X to place Y
			 */
//			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : net.getInEdges(placeY)) {
//				Transition source = (Transition) edge.getSource();
//				System.out.println("库所Y的入边的变迁:"+source);
////				int weight = ((Arc) edge).getWeight();
//				net.removeEdge(edge);
////				net.addArc(source, placeY, weight);
////				net.addArc(source, placeY);
//			}
			
			if(net.getOutEdges(placeX).size() ==1) {
				continue;
			}
			if(net.getInEdges(placeY).size() !=1) {
				continue;
			}
			/*
			 * Relocate all output arcs from place X to place Y
			 * 保证后继库所Y所有的出边都连接到前驱库所所有的出边上
			 */
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : net.getOutEdges(placeY)) {
				Transition target = (Transition) edge.getTarget();
//				int weight = ((Arc) edge).getWeight();
				net.removeEdge(edge);
//				net.addArc(placeY, target, weight);
				net.addArc(placeX, target);
			}


			//remove the place
			net.removePlace(placeY);//移除后继库所Y
			net.removeTransition(transitionA);//移除静默变迁

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
