package PetriSimplify;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;



@Plugin(name = "0000Petri Net Simplify", parameterLabels = { "Name of your first Log"}, 
	    returnLabels = { "object" }, returnTypes = {Petrinet.class }, help = "")
public class PetriSimplify {
	/**
	 * The plug-in variant that runs in any context and requires a parameters.
	 * 
	 * @param context The context to run in.
	 * @param input1 The first input.
	 * @param input2 The second input.
	 * @param parameters The parameters to use.
	 * @return The output.
	 * @throws InterruptedException 
	 */
	@UITopiaVariant(
	        affiliation = "TU/e", 
	        author = "ShuaiPeng Zhang", 
	        email = "15994069715@163.coml"
	        )
	@PluginVariant(
			variantLabel = "1111111",
			// the number of required parameters, {0} means one input parameter
			requiredParameterLabels = {0}
			)
	public static Petrinet ComputerEMDValue(UIPluginContext context, Petrinet net) throws InterruptedException
	{
//		 List<Place> places = new ArrayList<Place>();
//		 System.out.println("place........");
//	        for(Place place : net.getPlaces()) {
//	        	System.out.print(place+";");
//	            int input = 0;
//	            int output = 0;
//	            for(PetrinetEdge edge : net.getEdges()) {
//	                if(edge.getSource().equals(place)) {
//	                    output++;
//	                }else if(edge.getTarget().equals(place)) {
//	                    input++;
//	                }
//	            }
//	            if(input > 1 && output > 1) {
//	                places.add(place);
//	            }
//	        }
//	       
//	        System.out.println("\ntransition:");
//	        for(Transition transition1 : net.getTransitions()) {
////	        	t.setInvisible(transition.isInvisible());
//	        	System.out.println("******************************");
//	        	Collection<Transition> t1=transition1.getVisiblePredecessors();
//	        	ExpandableSubNet  t2=transition1.getParent();
//	        	System.out.println("±äÇ¨Ãû×Ö:"+transition1);
//	        	System.out.println("transition.getVisiblePredecessors:"+t1);
//	        	System.out.println("transition getparent:"+t2);
//	        	if(transition1.isInvisible()) {
//	        		System.out.print("Invisible transition1");
//	        	}
//	        	System.out.println("......");
//            }
//	        
//	        System.out.println("edge Set:");
//	        for(PetrinetEdge edge : net.getEdges()) {
//	        	System.out.print("edge:"+edge);
//	        	 System.out.println("edge.getSource():"+edge.getSource());
//	        	 System.out.println("edge.getTarget():"+edge.getTarget());
////	        	 if(edge.getSource() instanceof Place) {
////	                 net.addArc(places.get(edge.getSource()), transitions.get(edge.getTarget()));
////	             }
//	        	
//            }
	        
	        System.out.println("*****************************************************");
	        ReducePetriNet.reduce(net, new Canceller() {
				public boolean isCancelled() {
					return false;
				}
			});

	        return net;
		
	}
}
