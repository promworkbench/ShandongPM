//package PetriSimplify;
//
//import java.util.Collection;
//
//import org.processmining.framework.packages.PackageManager.Canceller;
//import org.processmining.models.graphbased.directed.petrinet.Petrinet;
//import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
//import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
//import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
//import org.processmining.models.graphbased.directed.petrinet.elements.Place;
//import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
//import org.processmining.models.semantics.petrinet.Marking;
//import org.processmining.plugins.petrinet.reduction.MurataUtils;
//
//import gnu.trove.set.hash.THashSet;
//
///**
// * 规则1：有一个token的库所只有一个入边和出边，入边和出边的连接变迁为同一个
// * @author ZSP
// *
// */
//public class PteriNetSimplificationRule6 {
//	public static boolean reduce(Petrinet net, Canceller canceller) {
//	
//		/*
//		 * Iterate over all transitions.
//		 */
//		for (Transition transitionA : net.getTransitions()) {
//			if (canceller.isCancelled()) {
//				return true;
//			}
//
//			/*
//			 * Check whether the transition is silent.
//			 */
//			if (!transitionA.isInvisible()) {
//				continue;
//			}
//
//			/*
//			 * Check input arc.
//			 */
//			Place placeX;
//			{
//				Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> arcs1 = net
//						.getInEdges(transitionA);
//				if (arcs1.size() != 1) {
//					continue;
//				}
//				PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> arc1 = arcs1.iterator().next();
//				if (!(arc1 instanceof Arc)) {
//					continue;
//				}
//
//				if (((Arc) arc1).getWeight() != 1) {
//					continue;
//				}
//
//				placeX = (Place) arc1.getSource();
//			}
//
//			Place placeY = getPlaceY(net, transitionA);
//			placeY.hashCode();
//			if (placeY == null) {
//				continue;
//			}
//
//			/*
//			 * Check that we're not dealing with a self loop
//			 */
//			if (placeX == placeY) {
//				continue;
//			}
//
//			//search for a second transition that links placeY to placeX
//			Transition transitionB = findTransitionB(net, placeX, placeY);
//			if (transitionB == null) {
//				continue;
//			}
//
//			/*
//			 * Target pattern identified. Proceed with reduction: remove place X
//			 */
//
//			/*
//			 * Relocate all input arcs from place X to place Y
//			 */
//			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : net.getInEdges(placeX)) {
//				Transition source = (Transition) edge.getSource();
//				int weight = ((Arc) edge).getWeight();
//				net.removeEdge(edge);
//				net.addArc(source, placeY, weight);
//			}
//
//			/*
//			 * Relocate all output arcs from place X to place Y
//			 */
//			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : net.getOutEdges(placeX)) {
//				Transition target = (Transition) edge.getTarget();
//				int weight = ((Arc) edge).getWeight();
//				net.removeEdge(edge);
//				net.addArc(placeY, target, weight);
//			}
//			
//			/*
//			 * Move tokens from siblingPlace to place in the initial and final
//			 * markings
//			 */
//			{
//				{
//					net.
//					int tokens = net.getInitialMarking().occurrences(placeX);
//					anet.getInitialMarking().add(placeY, tokens);
//					MurataUtils.updateLabel(placeY, anet.getInitialMarking());
//					MurataUtils.resetPlace(anet.getInitialMarking(), placeX);
//				}
//
//				for (Marking marking : anet.getFinalMarkings()) {
//					int tokens = marking.occurrences(placeX);
//					marking.add(placeY, tokens);
//					MurataUtils.updateLabel(placeY, marking);
//					MurataUtils.resetPlace(marking, placeX);
//				}
//
//				//the markings might have changed, so we need to re-index the final markings
//				anet.setFinalMarkings(new THashSet<>(anet.getFinalMarkings()));
//			}
//
//			//remove the place
//			net.removePlace(placeX);
//			net.removeTransition(transitionA);
//			net.removeTransition(transitionB);
//
//			return true;
//		}
//
//		return false;
//	}
//
//	public static Place getPlaceY(Petrinet net, Transition transitionA) {
//		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> postset = net.getOutEdges(transitionA);
//		if (postset.size() != 1) {
//			return null;
//		}
//		PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> arc2 = postset.iterator().next();
//		if (!(arc2 instanceof Arc)) {
//			return null;
//		}
//
//		if (((Arc) arc2).getWeight() != 1) {
//			return null;
//		}
//
//		return (Place) arc2.getTarget();
//	}
//
//	public static Transition findTransitionB(Petrinet net, Place placeX, Place placeY) {
//		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> arc4 : net.getInEdges(placeX)) {
//
//			if (((Arc) arc4).getWeight() == 1) {
//				Transition transitionB = (Transition) arc4.getSource();
//				
//				if (!transitionB.isInvisible()) {
//					continue;
//				}
//
//				/*
//				 * transition B may only have one incoming arc; from place Y
//				 */
//				Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> arcs3 = net
//						.getInEdges(transitionB);
//				if (arcs3.size() != 1) {
//					continue;
//				}
//
//				PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> arc3 = arcs3.iterator().next();
//
//				if (arc3.getSource() != placeY) {
//					continue;
//				}
//
//				if (((Arc) arc3).getWeight() != 1) {
//					continue;
//				}
//
//				return transitionB;
//			}
//		}
//		return null;
//	}
//}
