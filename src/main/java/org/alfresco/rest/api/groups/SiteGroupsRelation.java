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
package org.alfresco.rest.api.groups;

import org.alfresco.rest.api.Sites;
import org.alfresco.rest.api.model.SiteGroup;
import org.alfresco.rest.api.sites.SiteEntityResource;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.util.ParameterCheck;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;
import java.util.stream.Collectors;

@RelationshipResource(name = "group-members", entityResource = SiteEntityResource.class, title = "Site Groups")
public class SiteGroupsRelation implements RelationshipResourceAction.Read<SiteGroup>,
        RelationshipResourceAction.Delete,
        RelationshipResourceAction.Create<SiteGroup>,
        RelationshipResourceAction.Update<SiteGroup>,
        RelationshipResourceAction.ReadById<SiteGroup>,
        InitializingBean
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
     * POST sites/<siteId>/group-members
     * <p>
     * Adds groups to site
     * <p>
     * If group does not exist throws NotFoundException (status 404).
     *
     * @see RelationshipResourceAction.Create#create(String, List, Parameters)
     */
    @Override
    @WebApiDescription(title = "Adds groups as a member of site siteId.")
    public List<SiteGroup> create(String siteId, List<SiteGroup> siteMembers, Parameters parameters)
    {
        return siteMembers.stream().map((group) -> sites.addSiteGroupMembership(siteId, group)).collect(Collectors.toList());
    }

    /**
     * Returns a paged list of all the groups of the site 'siteId'.
     * <p>
     * If siteId does not exist, throws NotFoundException (status 404).
     */
    @Override
    @WebApiDescription(title = "A paged list of all the groups of the site 'siteId'.")
    public CollectionWithPagingInfo<SiteGroup> readAll(String siteId, Parameters parameters)
    {
        return sites.getSiteGroupMemberships(siteId, parameters);
    }

    /**
     * Returns site membership information for groupId in siteId.
     * <p>
     * GET sites/<siteId>/group-members/<groupId>
     */
    @Override
    @WebApiDescription(title = "Returns site membership information for groupId in siteId.")
    public SiteGroup readById(String siteId, String groupId, Parameters parameters)
    {
        return sites.getSiteGroupMembership(siteId, groupId);
    }

    /**
     * PUT sites/<siteId>/group-members/<groupId>
     * <p>
     * Updates the membership of group in the site.
     */
    @Override
    @WebApiDescription(title = "Updates the membership of groupId in the site.")
    public SiteGroup update(String siteId, SiteGroup groupMember, Parameters parameters)
    {
        return sites.updateSiteGroupMembership(siteId, groupMember);
    }

    /**
     * DELETE sites/<siteId>/group-members/<groupId>
     * <p>
     * Remove a group from site.
     */
    @Override
    @WebApiDescription(title = "Removes groupId as a member of site siteId.")
    public void delete(String siteId, String groupId, Parameters parameters)
    {
        sites.removeSiteGroupMembership(siteId, groupId);
    }

}