package equalscale.SampleEvaluation;

public class VisualizeQualityMetrics {
	
	//visualize the SoftwareEventLogStatistics as a string
	public static String visualizeQualityMetrics(QualityMetrics metric)
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append("<html>"); 
		buffer.append("<body>");
		
		buffer.append("<h1 style=\"color:blue;\">"+"Quality Metrics of Sample Log"+"</h1>");  
		
		buffer.append("<h2 style=\"color:#2F8AA1;\">"+"Coverage: "+metric.getCoverage()+"</h2>");
		buffer.append("<h2 style=\"color:#2F8AA1;\">"+"NAME: "+metric.getNAME()+"</h2>");
		buffer.append("<h2 style=\"color:#2F8AA1;\">"+"sMPAE: "+metric.getSMAPE()+"</h2>");
		
		buffer.append("</body>");
		buffer.append("</html>");
		return buffer.toString();
	}		
}
