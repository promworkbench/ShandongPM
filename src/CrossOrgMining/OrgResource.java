package CrossOrgMining;

import java.util.Objects;

//this class store the interaction activity. it contains the (1) organization (2) activity name
//此类存储交互活动,  它包含 组织 \活动名称
public class OrgResource {

	private String activity ="";
	private String organization ="";
	private String resource ="";
	
	
	public OrgResource(String act, String org, String reso)
	{
		activity=act;
		organization=org;
		resource=reso;
		
	}
	
	public String getActivity() {
		return activity;
	}
	public void setActivity(String activity) {
		this.activity = activity;
	}
	public String getOrganization() {
		return organization;
	}
	public void setOrganization(String organization) {
		this.organization = organization;
	}
	
	public String getResource() {
		return resource;
	}
	public void setResource(String resource) {
		this.resource = resource;
	}
	
	public int hashCode() {
		
        return Objects.hash(activity)+Objects.hash(organization)+Objects.hash(resource);
    }  
	
	public boolean equals(Object other)
	{
		if (this==other)
		{
			return true;
		}
		if (other==null)
		{
			return false;
		}
		if (!(other instanceof OrgActivity))
		{
			return false;
		}
		if (this.hashCode()==((OrgActivity)other).hashCode()) 
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public String toString() 
	{
		return this.activity+":"+this.organization+":"+this.resource;
		
	}
	
}
