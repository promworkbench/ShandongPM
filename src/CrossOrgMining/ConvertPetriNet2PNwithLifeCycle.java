package CrossOrgMining;
/*
 * this plugin aims to transform a hierarchical petri net to a flat one. 
 */

import java.util.HashMap;
import java.util.HashSet;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.DirectedGraphElement;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetImpl;

@Plugin(
		name = "Convert a Petri Net to a Petri Net with Lifecycle Transitions",// plugin name
		
		returnLabels = {"Flat Petri Net"}, //return labels
		returnTypes = {Petrinet.class},//return class
		
		//input parameter labels, corresponding with the second parameter of main function
		parameterLabels = {"Lifecycle Petri Net"},
		
		userAccessible = true,
		help = "This plugin aims to convert a petri net to a lifecycle one." 
		)
public class ConvertPetriNet2PNwithLifeCycle {
	@UITopiaVariant(
	        affiliation = "TU/e", 
	        author = "Cong liu", 
	        email = "c.liu.3@tue.nl OR liucongchina@163.com"
	        )
	@PluginVariant(
			variantLabel = "Convert Hierarchical Petri Net to Flat Petri Net, default",
			// the number of required parameters, {0} means one input parameter
			requiredParameterLabels = {0}
			)
	public Petrinet convertPNtoLifecyclePN(UIPluginContext context, Petrinet pn) 
	{
		//add all transitions, normal, silient, nested. 
		//for normal transitions that are not nested transitions. 
		final Petrinet clonedPN =new PetrinetImpl("Cloned PN");
		clonePetriNet(pn, clonedPN);
		
		HashSet<Transition> transitions = new HashSet<>();
		
		for(Transition t :clonedPN.getTransitions())
		{
			if(!t.isInvisible()) // not invisible transition
			transitions.add(t);
		}
		
		for(Transition t: transitions)
		{
			convertNormalTransition(clonedPN, t);
		}
				
		return clonedPN;

	}
	
	public static HashMap<DirectedGraphElement, DirectedGraphElement> clonePetriNet(Petrinet pn, Petrinet clonedPN)
	{
		//keep the mapping from old to new. 
		HashMap<DirectedGraphElement, DirectedGraphElement> mapping = new HashMap<DirectedGraphElement, DirectedGraphElement>();

		//copy transitions. 
		for (Transition t : pn.getTransitions()) {
			Transition copy = clonedPN.addTransition(t.getLabel(), null);
			copy.setInvisible(t.isInvisible());
			mapping.put(t, copy);
		}
		
		//copy places. 
		for (Place p : pn.getPlaces()) {
			Place copy = clonedPN.addPlace(p.getLabel(), null);
			mapping.put(p, copy);
		}

		//add arcs
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> a : pn.getEdges()) {
			if(a.getSource() instanceof Transition)
			{
				org.processmining.models.graphbased.directed.petrinet.elements.Arc clonedArc = clonedPN.addArc((Transition) mapping.get(a.getSource()), (Place) mapping.get(a.getTarget()));
				mapping.put(a, clonedArc);
			}
			else{
				org.processmining.models.graphbased.directed.petrinet.elements.Arc clonedArc = clonedPN.addArc((Place) mapping.get(a.getSource()), (Transition) mapping.get(a.getTarget()));
				mapping.put(a, clonedArc);
			}
		}
		
		return mapping;
	}
	
	public static void convertNormalTransition(Petrinet pn, Transition t)
	{
		//get the pre place set of t
		HashSet<Place> prePlaceSet = new HashSet<>();
		for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge :pn.getInEdges(t))
		{
			prePlaceSet.add((Place)edge.getSource());
			//remove the current edge
			pn.removeArc((Place)edge.getSource(), t);
		}
		
		//get the post place set of t
		HashSet<Place> postPlaceSet = new HashSet<>();
		for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge :pn.getOutEdges(t))
		{
			postPlaceSet.add((Place)edge.getTarget());
			//remove the current edge
			pn.removeArc(t, (Place)edge.getTarget());
		}
		
		//create start and end transition
		Transition startTransition = pn.addTransition(t.getLabel()+"+Start");
		Transition endTransition= pn.addTransition(t.getLabel()+"+Complete");
		
		//remove t, 
		pn.removeTransition(t);
		
		//add arcs from startTransition to each pre place
		for(Place preP: prePlaceSet)
		{
			pn.addArc(preP, startTransition);
		}
		
		//add arcs from endTransition to each post place
		for(Place postP: postPlaceSet)
		{
			pn.addArc(endTransition, postP);
		}
		
		//create the subset, i.e., ts->p->tc
		//add a place to connect startTransition and endTransition
		Place connectionP = pn.addPlace("connect");
		pn.addArc(startTransition, connectionP);
		pn.addArc(connectionP, endTransition);

	}
}
