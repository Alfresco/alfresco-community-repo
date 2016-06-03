package org.alfresco.repo.version;

import org.alfresco.repo.version.VersionServicePolicies.CalculateVersionLabelPolicy;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;

/**
 * Utility class to register a version label policy version service.
 * 
 * Used to configure the version service via spring.
 * 
 */
public class VersionServiceVersionLabelRegistrationBean 
{
	private String typeQName;
	
	private CalculateVersionLabelPolicy policy;
	
	private VersionService versionService;
	
	private NamespacePrefixResolver prefixResolver;
	
	/**
	 * Register the deployment target with the deployment target registry
	 */
	public void register()
	{
		PropertyCheck.mandatory(this, "typeQName", typeQName);
		PropertyCheck.mandatory(this, "versionService", getVersionService());
		PropertyCheck.mandatory(this, "policy", policy);
	    PropertyCheck.mandatory(this, "prefixResolver", prefixResolver);
		
		/**
		 * Go ahead and register the version label policy with the 
		 * versionService
		 */
		QName qName = QName.createQName(typeQName, prefixResolver);
		getVersionService().registerVersionLabelPolicy(qName, policy);
	    
	}
	
	public void setTypeQName(String typeQName) 
	{
		this.typeQName = typeQName;
	}
	
	public String getTypeQName() 
	{
		return typeQName;
	}
	
	public void setPolicy(CalculateVersionLabelPolicy policy)
	{
	    this.policy = policy;
	}
	
	public CalculateVersionLabelPolicy getPolicy()
	{
	    return policy;
	}

    public void setNamespacePrefixResolver(NamespacePrefixResolver prefixResolver)
    {
        this.prefixResolver = prefixResolver;
    }
    public NamespacePrefixResolver getNamespacePrefixResolver()
    {
        return prefixResolver;
    }

    public void setVersionService(VersionService versionService)
    {
        this.versionService = versionService;
    }

    public VersionService getVersionService()
    {
        return versionService;
    }
	
}
