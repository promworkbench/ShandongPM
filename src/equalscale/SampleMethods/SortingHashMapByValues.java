package equalscale.SampleMethods;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class SortingHashMapByValues {

	
	public static void main(String args[])
	{
		HashMap<String, Double> map = new HashMap<>();
		map.put("trace4", 0.8);
		map.put("trace2", 0.3);
		map.put("trace1", 0.2);
		map.put("trace3", 0.5);
		sortMapByValues(map,  2);
		
	}
	
	//get the top n keys...
	 public static HashSet<String> sortMapByValues(HashMap<String, Double> aMap, int n) {
	        
		 HashSet<String> topNKeys = new HashSet<>();
	        Set<Entry<String,Double>> mapEntries = aMap.entrySet();
	        
//	        System.out.println("Values and Keys before sorting ");
//	        for(Entry<String,Double> entry : mapEntries) {
//	            System.out.println(entry.getValue() + " - "+ entry.getKey());
//	        }
	        
	        // used linked list to sort, because insertion of elements in linked list is faster than an array list. 
	        List<Entry<String,Double>> aList = new LinkedList<Entry<String,Double>>(mapEntries);

	        // sorting the List
	        Collections.sort(aList, new Comparator<Entry<String,Double>>() {

	            @Override
	            public int compare(Entry<String, Double> ele1,
	                    Entry<String, Double> ele2) {
	                
	                return ele1.getValue().compareTo(ele2.getValue());
	            }
	        });
	        
	        // Storing the list into Linked HashMap to preserve the order of insertion. 
	        HashMap<String,Double> aMap2 = new LinkedHashMap<String, Double>();
	        for(Entry<String,Double> entry: aList) {
	            aMap2.put(entry.getKey(), entry.getValue());
	        }
	        
	        // printing values after soring of map
//	        System.out.println("Value " + " - " + "Key");
	        int count =0;
	        for(Entry<String,Double> entry : aMap2.entrySet()) {
//	            System.out.println(entry.getValue() + " - " + entry.getKey());
	           
	            topNKeys.add(entry.getKey());
	            count++;
	            if(count >= n)
	            	break;
	        }
	        
	        return topNKeys;
	        
	    }
}
