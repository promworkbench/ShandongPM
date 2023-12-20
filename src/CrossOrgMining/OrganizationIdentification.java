package CrossOrgMining;

import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

/*
 *identifies all organizations and its mapping to activities.  //识别所有组织及其对活动的映射。
 */
public class OrganizationIdentification {

	public static OrganizationConfig identifyOrganizationsFromLog(XLog log)
	{
		OrganizationConfig orgConfig = new OrganizationConfig();
		
		for(XTrace trace: log)
		{
			for(XEvent event: trace)
			{
				//the current organization mapping does not include the organization of current event //当前组织映射不包括当前事件的组织
				if(!orgConfig.getAllOrganizations().contains(XOrganizationalExtension.instance().extractResource(event)))
				{
					Set<String> activitySet = new HashSet<>();
					activitySet.add(XConceptExtension.instance().extractName(event));
					orgConfig.add(XOrganizationalExtension.instance().extractResource(event), activitySet);
					
				}
				else{//add the activity to the organization mapping
					orgConfig.getActivities(XOrganizationalExtension.instance().extractResource(event)).add(XConceptExtension.instance().extractName(event));
				}
			}
		}
		return orgConfig;
	}
}
