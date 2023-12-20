package CrossOrgMining;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetImpl;
import org.processmining.models.semantics.petrinet.Marking;

/**
 * this plugin aims to transform a cross-organization business process model to a pn
 * @author cliu3
 *
 */

@Plugin(
		name = "Convert a Cross-organization Business Process Model to a Petri Net",// plugin name
		
		returnLabels = {"Flat Petri Net"}, //return labels
		returnTypes = {Petrinet.class},//return class
		
		//input parameter labels, corresponding with the second parameter of main function
		parameterLabels = {"cross-organization business process model"},
		
		userAccessible = true,
		help = "This plugin aims to convert a cross-organization business process model to a petri net." 
		)
public class TransformCrossOrganizationBusinessProcessModel2PetriNet {
	@UITopiaVariant(
	        affiliation = "TU/e", 
	        author = "Cong liu", 
	        email = "c.liu.3@tue.nl OR liucongchina@163.com"
	        )
	@PluginVariant(
			variantLabel = "Convert cross-organization business process model to a petri net, default",
			// the number of required parameters, {0} means one input parameter
			requiredParameterLabels = {0}
			)
	
	//transform a cross-organization business process model to a pn
	public static Petrinet Transform2PN(UIPluginContext context, CrossOrganizationBusinessProcessModel crossOrgModel)
	{
		final Petrinet newPN =new PetrinetImpl("Cloned PN");
		
		// add all intra organization pns to newPN
		for(String org:crossOrgModel.getAllOrganizations())
		{
			//add intra organization pn to the newPN
			ConvertPetriNet2PNwithLifeCycle.clonePetriNet(crossOrgModel.getOrganizationModel(org), newPN);
		}
		
		//add all interactions, and for each interaction, we add a place to connect the corresponding transitions.
		HashSet<CrossOrganizationInteraction> interactions = crossOrgModel.getAllInteractions();

		for(CrossOrganizationInteraction inte: interactions)
		{		
			//create a place for message
			Place messagePlace = newPN.addPlace(inte.toString(), null);

			//find the source transition and the target transition
			//find the two transitions
			for (Transition t : newPN.getTransitions()) 
			{
				for(OrgActivity sourceA: inte.getSourceActivities())
				{
					if(t.getLabel().equals(sourceA.getActivity()))
					{
						//add an arc from t to messagePlace
						newPN.addArc(t, messagePlace);
					}
				}
				
				for(OrgActivity targetA: inte.getTargetActivities())
				{
					if(t.getLabel().equals(targetA.getActivity()))
					{
						//add an arc from messagePlace to t
						newPN.addArc( messagePlace, t);

					}
				}
				
			}
		}
		return newPN;
	}
	
	
	//make sure the source place do not have input arcs, e.g., single entry,and the target place do not have outgoing arcs. 
	public static Petrinet addingArtifitialSouceandTargetPlaces(final Petrinet pn, Marking initialM, Marking finalM)
	{
		//get all places in the initial marking. 
		List<Place> Initialplaces = initialM.toList();
		List<Place> Finalplaces = finalM.toList();
//		HashSet<String> placeNames = new HashSet<>();
//		for(Place p: places)
//		{
//			placeNames.add(p.getLabel());
//		}
//		System.out.println(placeNames);

		
		int sourceFlag =1;
		int targetFlag =1;
		for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : pn.getEdges())
		{
			//System.out.println(edge.getTarget().getLabel());
			// if there exist an edge with a target place that included in the marking, then we need to add artificial source place and transition.  
			//if(placeNames.contains(edge.getTarget().getLabel()))
			if(Initialplaces.contains(edge.getTarget()))
			{
				sourceFlag=0;
				break;
			}
		}
		
		for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : pn.getEdges())
		{
			// if there exist an edge with a source place that included in the marking, then we need to add artificial target place and transition.  
			if(Finalplaces.contains(edge.getSource()))
			{
				targetFlag=0;
				break;
			}
		}
		
		if(sourceFlag==0)//
		{
			int random = ThreadLocalRandom.current().nextInt(1, 100 + 1);
			//create a new source place 
			Place sourceP= pn.addPlace("lc source"+random);
			Transition sourceT = pn.addTransition("");
			sourceT.setInvisible(true);
			
			pn.addArc(sourceP, sourceT);
			pn.addArc(sourceT, Initialplaces.get(0));// an implicit assumption that there is only one place
		}
		
		if(targetFlag==0)//
		{
			//create a new target place 
			int random = ThreadLocalRandom.current().nextInt(1, 100 + 1);

			Place targetP= pn.addPlace("lc target"+random);
			Transition targetT = pn.addTransition("");
			targetT.setInvisible(true);
			
			pn.addArc(Finalplaces.get(0), targetT);
			pn.addArc(targetT, targetP);// an implicit assumption that there is only one place
		}
		return pn;
	}
}
