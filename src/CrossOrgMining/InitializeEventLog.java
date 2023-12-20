package CrossOrgMining;

import org.deckfour.xes.classification.XEventAttributeClassifier;
import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;

public class InitializeEventLog {
	// initialize main software event loglog
	public static XLog initializeEventLog(XFactory factory, String logName)
	{
		//add the log name		
		XLog log = factory.createLog();
		log.getAttributes().put(XConceptExtension.KEY_NAME, new XAttributeLiteralImpl(XConceptExtension.KEY_NAME, logName));
		
		//create standard extension
		XExtension conceptExtension = XConceptExtension.instance();
		XExtension organizationalExtension = XOrganizationalExtension.instance();
		XExtension timeExtension = XTimeExtension.instance();
		XExtension lifecycleExtension=XLifecycleExtension.instance();
		
		// create extensions
		log.getExtensions().add(conceptExtension);
		log.getExtensions().add(organizationalExtension);
		log.getExtensions().add(lifecycleExtension);
		log.getExtensions().add(timeExtension);
		
		// create trace level global attributes
		XAttribute xtrace = new XAttributeLiteralImpl(XConceptExtension.KEY_NAME, "DEFAULT"); 
		log.getGlobalTraceAttributes().add(xtrace);
		
		// create event level global attributes		

		log.getGlobalEventAttributes().add(XConceptExtension.ATTR_NAME);
		log.getGlobalEventAttributes().add(XLifecycleExtension.ATTR_TRANSITION);
		log.getGlobalEventAttributes().add(XOrganizationalExtension.ATTR_RESOURCE);
		log.getGlobalEventAttributes().add(XTimeExtension.ATTR_TIMESTAMP);
		
		// create classifiers based on global attribute		

		XEventAttributeClassifier classifierActivity = new XEventAttributeClassifier("Activity Name Identifier", 
				 XConceptExtension.KEY_NAME);
		log.getClassifiers().add(classifierActivity);
		
		return log;
	}
}
