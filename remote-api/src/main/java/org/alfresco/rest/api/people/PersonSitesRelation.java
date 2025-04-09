/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.rest.api.people;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import org.alfresco.rest.api.Sites;
import org.alfresco.rest.api.model.MemberOfSite;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.util.ParameterCheck;

@RelationshipResource(name = "sites", entityResource = PeopleEntityResource.class, title = "Person Sites")
public class PersonSitesRelation implements RelationshipResourceAction.Read<MemberOfSite>, RelationshipResourceAction.ReadById<MemberOfSite>,
        RelationshipResourceAction.Delete, InitializingBean
{
    private static final Log logger = LogFactory.getLog(PersonSitesRelation.class);

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
     * List all the sites that the specified user has a explicit membership of.
     * 
     * THOR-1151: “F312: For a user, get the list of sites they are a member of”
     * 
     * If personId does not exist, NotFoundException (status 404).
     * 
     * @param personId
     *            the id (email) of the person
     * 
     *            (non-Javadoc)
     * @see org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction.Read#readAll(String, org.alfresco.rest.framework.resource.parameters.Parameters)
     */
    @Override
    @WebApiDescription(title = "A paged list of the person's site memberships.")
    public CollectionWithPagingInfo<MemberOfSite> readAll(String personId, Parameters parameters)
    {
        return sites.getSites(personId, parameters);
    }

    /**
     * Returns site membership information for personId in siteId.
     * 
     * @see org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction.ReadById#readById(String, String, org.alfresco.rest.framework.resource.parameters.Parameters)
     */
    @Override
    @WebApiDescription(title = "Site membership information for 'personId' in 'siteId'.")
    public MemberOfSite readById(String personId, String siteId, Parameters parameters)
    {
        return sites.getMemberOfSite(personId, siteId);
    }

    @Override
    public void delete(String personId, String siteId, Parameters parameters)
    {
        sites.removeSiteMember(personId, siteId);
    }
}
