package CrossOrgMining;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;

/*
 * this class defines the cross-organization business process model
 * It involves 
 * (1) a set of organizational models that each is represented as a flat Petri net.
 * (2) a set of interactions (message-based) that each is represented as a sender activity and a receiver activity.
 */
public class CrossOrganizationBusinessProcessModel {

	// the organization models 
	private HashMap<String, Petrinet> org2PN = new HashMap<String, Petrinet>();
	
	// cross organization interactions
	private HashSet<CrossOrganizationInteraction> interactions = new HashSet<>();
	
	//get all interactions
	public HashSet<CrossOrganizationInteraction> getAllInteractions() {
		return interactions;
	}

	//set all interactions
	public void setAllInteractions(HashSet<CrossOrganizationInteraction> interactions) {
		this.interactions = interactions;
	}
	
	//add single interaction
	public void addSingleInteraction(CrossOrganizationInteraction interaction)
	{
		interactions.add(interaction);
	}

	//set organization models
	public void setOrganizationModels(HashMap<String, Petrinet> org2pns)
	{
		this.org2PN=org2pns;
	}
	
	//get all organizations
	public Set<String> getAllOrganizations()
	{
		return org2PN.keySet();
	}
	
	//get the pn for an organization
	public Petrinet getOrganizationModel(String org) 
	{
		return org2PN.get(org);
	}
}
