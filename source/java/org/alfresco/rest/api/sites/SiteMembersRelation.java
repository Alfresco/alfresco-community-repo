/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.rest.api.sites;

import info.bliki.wiki.template.If;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.rest.api.Sites;
import org.alfresco.rest.api.model.SiteMember;
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
 * @author steveglover
 *
 */
@RelationshipResource(name = "members", entityResource = SiteEntityResource.class, title = "Site Members")
public class SiteMembersRelation implements RelationshipResourceAction.Read<SiteMember>, RelationshipResourceAction.Delete, 
RelationshipResourceAction.Create<SiteMember>, RelationshipResourceAction.Update<SiteMember>, RelationshipResourceAction.ReadById<SiteMember>, InitializingBean
{
    private static final Log logger = LogFactory.getLog(SiteMembersRelation.class);

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
     * 
     * Returns a paged list of all the members of the site 'siteId'.
     * 
     * @throws If siteId does not exist, NotFoundException (status 404).
	 * 
	 * (non-Javadoc)
     * @see org.alfresco.rest.framework.resource.actions.interfaces.RelationshipCollectionResourceAction.Read#readAll(java.lang.String, org.alfresco.rest.framework.resource.parameters.Paging)
     */
    @Override
    @WebApiDescription(title="A paged list of all the members of the site 'siteId'.")
    public CollectionWithPagingInfo<SiteMember> readAll(String siteId, Parameters parameters)
    {
    	return sites.getSiteMembers(siteId, parameters);
    }

    /**
     * 
     * POST sites/<siteId>/members
     * 
     * Adds personId as a member of site siteId.
     * 
	 * @throws If personId does not exist, NotFoundException (status 404).
	 * @throws If siteMember. does not exist, NotFoundException (status 404).
	 * 
     * @see org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction.Create#create(java.io.Serializable)
     */
	@Override
    @WebApiDescription(title="Adds personId as a member of site siteId.")
	public List<SiteMember> create(String siteId, List<SiteMember> siteMembers, Parameters parameters)
	{
        List<SiteMember> result = new ArrayList<SiteMember>(siteMembers.size());
        for (SiteMember siteMember : siteMembers)
        {
           result.add(sites.addSiteMember(siteId, siteMember));
        }
        return result;
	}

	/**
	 * 
	 * DELETE sites/<siteId>/members/<personId>
	 * 
	 * Removes personId as a member of site siteId.
	 */
	@Override
    @WebApiDescription(title="Removes personId as a member of site siteId.")
	public void delete(String siteId, String personId, Parameters parameters)
	{
		sites.removeSiteMember(personId, siteId);
	}

	/**
	 * 
	 * PUT sites/<siteId>/members
	 * 
	 * Updates the membership of personId in the site (of which personId must be an existing member).
	 */
	@Override
    @WebApiDescription(title="Updates the membership of personId in the site (of which personId must be an existing member).")
	public SiteMember update(String siteId, SiteMember siteMember, Parameters parameters)
	{
		return sites.updateSiteMember(siteId, siteMember);
	}

	/**
	 * 
	 * Returns site membership information for personId in siteId.
	 * 
	 * GET sites/<siteId>/members/<personId>
	 */
	@Override
    @WebApiDescription(title="Returns site membership information for personId in siteId.")
	public SiteMember readById(String siteId, String personId, Parameters parameters)
	{
		return sites.getSiteMember(personId, siteId);
	}
}