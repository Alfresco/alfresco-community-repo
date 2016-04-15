package org.alfresco.rest.api.sites;

import org.alfresco.rest.api.Sites;
import org.alfresco.rest.api.model.Site;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.util.ParameterCheck;
import org.springframework.beans.factory.InitializingBean;

/**
 * An implementation of an Entity Resource for a Site
 *
 * @author Gethin James
 * @author steveglover
 */
@EntityResource(name="sites", title = "Sites")
public class SiteEntityResource implements EntityResourceAction.Read<Site>, EntityResourceAction.ReadById<Site>, InitializingBean
{
    private Sites sites;

	public void setSites(Sites sites)
	{
		this.sites = sites;
	}

	@Override
    public void afterPropertiesSet()
    {
        ParameterCheck.mandatory("sites", this.sites);
    }

    /**
     * Returns a paged list of all sites in the current tenant.
     * 
     */
    @Override
    @WebApiDescription(title="A paged list of visible sites in the network.", description="A site is visible if it is public or if the person is a member")
    public CollectionWithPagingInfo<Site> readAll(Parameters parameters)
    { 
        return sites.getSites(parameters);
    }
    
    /**
     * Returns information regarding the site 'siteId'.
     * 
     */
    @Override
    @WebApiDescription(title="Returns site information for site siteId.")
    public Site readById(String siteId, Parameters parameters)
    {
        return sites.getSite(siteId);
    }

}
