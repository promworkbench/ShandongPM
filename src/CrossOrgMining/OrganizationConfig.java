package CrossOrgMining;

import java.util.HashMap;
import java.util.Set;

/*
 * organization is a string and activity is represented as a string.  
 * An organization involves a set of activities. 
 * *组织是一个字符串,活动表示为一个字符串。
   *   *组织涉及一系列活动。
 * 
 */
public class OrganizationConfig {
	HashMap<String, Set<String>> org2acticities;
	
	public OrganizationConfig()
	{
		//an organization --> a set of activities that belongs to the current organization
		org2acticities =new HashMap<String, Set<String>>();
	}
	
	
	public void add(String org, Set<String> activities)
	{
		org2acticities.put(org, activities);
	}
	
	public Set<String> getAllOrganizations()
	{
		return org2acticities.keySet();
	}
	
	public Set<String> getActivities(String org)
	{
		return org2acticities.get(org);
	}
	
	public void removeOrganization(String org)
	{
		org2acticities.remove(org);
	}
	
	//get the organization of an activity, if this activity is included in the configuration //获取活动的组织(如果此活动包含在配置中)
	public String getOrganization4Activity(String activity)
	{
		for(String org: org2acticities.keySet())
		{
			if(org2acticities.get(org).contains(activity))
				return org;
		}
		
		//if not found, then return null
		return null;
	}
}
