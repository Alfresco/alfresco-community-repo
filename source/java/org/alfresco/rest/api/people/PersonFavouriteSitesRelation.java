package org.alfresco.rest.api.people;

import java.util.List;

import org.alfresco.rest.api.Sites;
import org.alfresco.rest.api.model.FavouriteSite;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * 
 * @author steveglover
 *
 */
@RelationshipResource(name = "favorite-sites", entityResource = PeopleEntityResource.class, title = "Person Favorite Sites")
public class PersonFavouriteSitesRelation implements RelationshipResourceAction.Read<FavouriteSite>, RelationshipResourceAction.ReadById<FavouriteSite>,
RelationshipResourceAction.Create<FavouriteSite>,  RelationshipResourceAction.Delete, InitializingBean
{
    private static final Log logger = LogFactory.getLog(PersonFavouriteSitesRelation.class);

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
     * List the user's favourite sites.
     * 
     * @see org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction.Read#readAll(java.lang.String, org.alfresco.rest.framework.resource.parameters.Parameters)
     */
    @Override
    @WebApiDescription(title = "Get Person Favorite Sites", description = "Get a paged list of the person's favorite sites")
    public CollectionWithPagingInfo<FavouriteSite> readAll(String personId, Parameters parameters)
    {
        return sites.getFavouriteSites(personId, parameters);
    }

    /**
     * List the favourite site information for a specific site.
     * 
     * @see org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction.ReadById#readById(String, String, org.alfresco.rest.framework.resource.parameters.Parameters)
     */
	@Override
    @WebApiDescription(title = "Get Person Favorite Site", description = "Get information on a person's specific favorite site")
	public FavouriteSite readById(String personId, String siteShortName, Parameters parameters)
	{
		return sites.getFavouriteSite(personId, siteShortName);
	}

	/**
     * Adds the given site as a favourite site for the user.
     * 
     * @see org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction.Create#create(String, java.util.List, org.alfresco.rest.framework.resource.parameters.Parameters)
	 */
	@Override
    @WebApiDescription(title = "Add Person Favorite Site", description = "Favorite a site")
	public List<FavouriteSite> create(String personId, List<FavouriteSite> entity, Parameters parameters)
	{
        for (FavouriteSite favSite : entity)
        {
           sites.addFavouriteSite(personId, favSite);
        }
        return entity;
	}

	@Override
    @WebApiDescription(title = "Remove Person Favorite Site", description = "Un-favorite a site")
	public void delete(String personId, String siteId, Parameters parameters)
	{
		sites.removeFavouriteSite(personId, siteId);
	}

}
