package kmean;


import java.util.HashMap;
import java.util.HashSet;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.model.XTrace;

public class ConvertTraceToVector {

	/*
	 * Given a trace, we construct a hashmap to represent it, the key is activity set + relation
	 * e.g., <a, b, c>==>{(a, 1), (b, 1), (c, 1), ((a,b), 1), ((b, c),)}
	 * 
	 */
	
	public HashMap<String, Boolean> Trace2FeatureMap(XTrace trace)
	{
		HashMap<String, Boolean> alphabet2value = new HashMap<>();
		
		if(trace.size()==0)
		{
			return alphabet2value;
		}
		else if(trace.size()==1)
		{
			alphabet2value.put(XConceptExtension.instance().extractName(trace.get(0)), true);

		}
		else {
			for(int i =0;i<trace.size()-1;i++)
			{
				//add activity
				alphabet2value.put(XConceptExtension.instance().extractName(trace.get(i)), true);
				//add directly follow pair
				alphabet2value.put(XConceptExtension.instance().extractName(trace.get(i))+","+XConceptExtension.instance().extractName(trace.get(i+1)), true);
			}
			alphabet2value.put(XConceptExtension.instance().extractName(trace.get(trace.size()-1)), true);

		}
			
		//System.out.println(alphabet2value);
		return alphabet2value;
	}
	
	
	public HashMap<String, Boolean> Trace2FeatureMapStartComplete(XTrace trace)
	{
		HashMap<String, Boolean> alphabet2value = new HashMap<>();
		
		if(trace.size()==0)
		{
			return alphabet2value;
		}
		else if(trace.size()==1)
		{
			alphabet2value.put(XConceptExtension.instance().extractName(trace.get(0)), true);

		}
		else {
			for(int i =0;i<trace.size()-1;i++)
			{
				//add activity
				alphabet2value.put(XConceptExtension.instance().extractName(trace.get(i)), true);
				//add directly follow pair
				
				alphabet2value.put(XConceptExtension.instance().extractName(trace.get(i))+"_"+
						XLifecycleExtension.instance().extractTransition(trace.get(i))+","+
						XConceptExtension.instance().extractName(trace.get(i+1))+"_"+XLifecycleExtension.instance().extractTransition(trace.get(i+1)), true);
			}
			alphabet2value.put(XConceptExtension.instance().extractName(trace.get(trace.size()-1)), true);

		}
			
		//System.out.println(alphabet2value);
		return alphabet2value;
	}
	
	/*
	 * Given a trace, we construct its hashset, the key is activity set + relation
	 * e.g., <a, b, c>==>{a, b, c, (a,b), (b, c)}
	 * 
	 */
	
	public HashSet<String> Trace2FeatureSet(XTrace trace)
	{
		HashSet<String> alphabetSet = new HashSet<>();
		
		if(trace.size()==0)
		{
			return alphabetSet;
		}
		else if(trace.size()==1)
		{
			alphabetSet.add(XConceptExtension.instance().extractName(trace.get(0)));

		}
		else {
			for(int i =0;i<trace.size()-1;i++)
			{
				//add activity
				alphabetSet.add(XConceptExtension.instance().extractName(trace.get(i)));
				//add directly follow pair
				alphabetSet.add(XConceptExtension.instance().extractName(trace.get(i))+","+XConceptExtension.instance().extractName(trace.get(i+1)));
			}
			alphabetSet.add(XConceptExtension.instance().extractName(trace.get(trace.size()-1)));

		}
			
		//System.out.println(alphabet2value);
		return alphabetSet;
	}
	
	public HashSet<String> Trace2FeatureSetStartComplete(XTrace trace)
	{
		HashSet<String> alphabetSet = new HashSet<String>();
		
		if(trace.size()==0)
		{
			return alphabetSet;
		}
		else if(trace.size()==1)
		{
			alphabetSet.add(XConceptExtension.instance().extractName(trace.get(0)));

		}
		else {
			for(int i =0;i<trace.size()-1;i++)
			{
				//add activity
				alphabetSet.add(XConceptExtension.instance().extractName(trace.get(i)));
				//add directly follow pair
				
				alphabetSet.add(XConceptExtension.instance().extractName(trace.get(i))+"_"+
						XLifecycleExtension.instance().extractTransition(trace.get(i))+","+
						XConceptExtension.instance().extractName(trace.get(i+1))+"_"+XLifecycleExtension.instance().extractTransition(trace.get(i+1)));
			}
			alphabetSet.add(XConceptExtension.instance().extractName(trace.get(trace.size()-1)));

		}
			
		//System.out.println(alphabet2value);
		return alphabetSet;
	}
	
	/*
	 * compute the similarity of two traces () using cosine. 
	 */
	public double CosineSimilarity(HashMap<String, Boolean> featuremap1, HashMap<String, Boolean> featuremap2)
	{
		//get the feature number of the two feature map
		HashSet<String> temp= new HashSet<>();
		temp.addAll(featuremap1.keySet());
		temp.addAll(featuremap2.keySet());
		
		//System.out.println(temp);
		
		CaseVector vector1 = new CaseVector(temp.size());
		CaseVector vector2 = new CaseVector(temp.size());
		for(int i=0;i<temp.toArray().length;i++)
		{
			if(featuremap1.keySet().contains(temp.toArray()[i]))
			{
				vector1.add(i, 1);
				//System.out.println("A"+i);
			}
			
			if(featuremap2.keySet().contains(temp.toArray()[i]))
			{
				vector2.add(i, 1);
				//System.out.println("B"+i);
			}
		}
		
//		System.out.println(vector1);
//		System.out.println(vector2);
		
		
		return computeSimilarity(vector1,vector2);
		
	}
	
	/*
	 * this is from TraceClustering package @Bart. 
	 */
	public double computeSimilarity(CaseVector firstVector, CaseVector secondVector) {
		// If the vectors are of different sizes we cannot compute cosine similarity.
		if (firstVector.size() != secondVector.size())
			return 0.0;

		// If both vectors are zero vectors, return 1 (exactly similar)
		if (firstVector.isZeroVector() && secondVector.isZeroVector())
			return 1.0;

		// If only one of the vectors are zero vectors, return 0 (cannot compute actually)
		if (firstVector.isZeroVector() || secondVector.isZeroVector())
			return 0.0;

		// Cosine similarity is the dot product divided by the product of the first norms of the two vectors
		// If vectors are huge they might overflow, so then use TraceVector.Norm.TwoRobust. Otherwise use the faster Norm.Two.
		// We might also first normalize the vectors, to weigh their respective perspectives.

		// Initialize variables
		Double dot = 0.0;

		// Compute the dot product
		//		dot = firstVector.dot(secondVector);
		//		return dot / (firstVector.norm(TraceVector.Norm.TwoRobust) * secondVector.norm(TraceVector.Norm.TwoRobust));

		dot = firstVector.toUnitVector().dot(secondVector.toUnitVector());
		return dot
				/ (firstVector.toUnitVector().norm(CaseVector.Norm.TwoRobust) * secondVector.toUnitVector().norm(
						CaseVector.Norm.TwoRobust));
	}
	
}
