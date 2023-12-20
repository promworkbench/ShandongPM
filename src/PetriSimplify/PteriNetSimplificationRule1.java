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
 * 规则1：前驱库所有多个进边，只有一个出边连接静默变迁；静默变迁的后继库所有多个入边和出边
 * 方式：将前驱库所的入边加到后继库所的入边
 * @author ZSP
 *
 */
public class PteriNetSimplificationRule1 {
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
				if (arcs1.size() != 1) {//满足静默变迁的入边只有一个
					continue;
				}
				PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> arc1 = arcs1.iterator().next();
				if (!(arc1 instanceof Arc)) {
					continue;
				}
				if(net.getInEdges(transitionA).size() !=1) {
					continue;
				};
				placeX = (Place) arc1.getSource();
//				System.out.println("net.getTransitions():"+net.getTransitions());
//				System.out.println("arcs1:"+arcs1);
//				System.out.println("placeX:"+placeX);
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
				continue;
			}

			if(net.getOutEdges(placeX).size() !=1) {//前驱库所的出边只有一条
				continue;
			}
			if(net.getInEdges(placeY).size() ==1) {//后继库所的入边不止一条
				continue;
			}
			/*
			 * Relocate all input arcs from place X to place Y
			 * 删除所有静默变迁前驱库所的入边，将其加到静默变迁后继库所上
			 */
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : net.getInEdges(placeX)) {
				Transition source = (Transition) edge.getSource();
				System.out.println("库所X的入边的变迁:"+source);
//				int weight = ((Arc) edge).getWeight();
				net.removeEdge(edge);
//				net.addArc(source, placeY, weight);
				net.addArc(source, placeY);
			}
			
			//细节部分：如果静默变迁没有了，那么连接它的边也就没有了，所以无需进行处理
			/*
			 * Relocate all output arcs from place X to place Y
			 * 移除静默变迁前驱库所之间的边
			 */
//			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : net.getOutEdges(placeX)) {
//				net.removeEdge(edge);
////				net.addArc(placeY, target, weight);
//			}

			//remove the place
			net.removePlace(placeX);//移除前驱库所
			net.removeTransition(transitionA);//移除静默变迁

			return true;//对一个静默变迁操作，不过外面有do  while循环，所以不影响
		}

		return false;
	}

	public static Place getPlaceY(Petrinet net, Transition transitionA) {
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> postset = net.getOutEdges(transitionA);
		if (postset.size() != 1) {//满足静默变迁的出边只有一个
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
