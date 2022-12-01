/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
package org.alfresco.rest.requests;

import java.util.List;

import org.alfresco.rest.core.JsonBodyGenerator;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestResponse;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.exception.JsonToModelConversionException;
import org.alfresco.rest.model.RestSiteContainerModel;
import org.alfresco.rest.model.RestSiteContainerModelsCollection;
import org.alfresco.rest.model.RestSiteMemberModel;
import org.alfresco.rest.model.RestSiteMemberModelsCollection;
import org.alfresco.rest.model.RestSiteGroupModel;
import org.alfresco.rest.model.RestSiteGroupModelsCollection;
import org.alfresco.rest.model.RestSiteModel;
import org.alfresco.rest.model.RestSiteModelsCollection;
import org.alfresco.rest.model.RestSitePersonMembershipRequestModelsCollection;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.UserModel;
import org.springframework.http.HttpMethod;

/**
 * Declares all Rest API under the /sites path
 *
 */
public class Site extends ModelRequest<Site>
{
  private SiteModel site;
  
  public Site(SiteModel site, RestWrapper restWrapper) 
  {
    super(restWrapper);
    this.site = site;    
  }
  
  /**
   * Retrieve one site using GET call on "sites/{siteId}"
   * 
   * @return
   * @throws JsonToModelConversionException
   */
  public RestSiteModel getSite()
  {
      RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "sites/{siteId}?{parameters}", this.site.getId(), restWrapper.getParameters());
      return restWrapper.processModel(RestSiteModel.class, request);
  }
  
  /**
   * Retrieve one site using GET call on "sites/{siteId}" along with relations parameter
   * 
   * @return
   * @throws JsonToModelConversionException
   */
  public List<Object> getSiteWithRelations()
  {
      RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "sites/{siteId}?{parameters}", this.site.getId(), restWrapper.getParameters());
      return restWrapper.processRelationsJson(request);
  }

  /**
   * Retrieve 100 sites (this is the default size when maxItems is not specified) from Alfresco using GET call on TestGroup.SITES
   * 
   * @return
   * @throws JsonToModelConversionException
   */
  public RestSiteModelsCollection getSites()
  {
      RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "sites?{parameters}", restWrapper.getParameters());
      return restWrapper.processModels(RestSiteModelsCollection.class, request);
  }
  
  /**
   * Retrieve sites using GET call on "sites/{siteId}" along with relations parameter
   * 
   * @return
   * @throws JsonToModelConversionException
   */
  public List<List<Object>> getSitesWithRelations()
  {
      RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "sites?{parameters}", restWrapper.getParameters());
      return restWrapper.processSitesRelationsJson(request);
  }
  
  /**
   * Add new site member using POST call on "/sites/{siteId}/members"
   * 
   * @param person {@link UserModel}
   * @return
   * @throws JsonToModelConversionException
   */
  public RestSiteMemberModel addPerson(UserModel person)
  {
      String siteMemberBody = JsonBodyGenerator.siteMember(person);
      RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, siteMemberBody, "sites/{siteId}/members?{parameters}", site.getId(), restWrapper.getParameters());
      return restWrapper.processModel(RestSiteMemberModel.class, request);
  }
  
  /**
   * Retrieve all members of a site using GET call on "sites/{siteId}/members"
   * 
   * @return
   * @throws JsonToModelConversionException
   */
  public RestSiteMemberModelsCollection getSiteMembers()
  {
      RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "sites/{siteId}/members?{parameters}", site.getId(), restWrapper.getParameters());
      return restWrapper.processModels(RestSiteMemberModelsCollection.class, request);
  }

  /**
   * Retrieve specific member of a site using GET call on "sites/{siteId}/members/{personId}"
   * 
   * @param user A model containing the username to look for.
   * @return
   * @throws JsonToModelConversionException
   */
  public RestSiteMemberModel getSiteMember(UserModel user)
  {
      RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "sites/{siteId}/members/{personId}?{parameters}", site.getId(), user.getUsername(), restWrapper.getParameters());
      return restWrapper.processModel(RestSiteMemberModel.class, request);
  }

  /**
   * Update site member role with PUT call on "sites/{siteId}/members/{personId}"
   * @param siteMember
   * @return
   * @throws JsonToModelConversionException
   */
  public RestSiteMemberModel updateSiteMember(UserModel siteMember)
  {
      String json = JsonBodyGenerator.keyValueJson("role", siteMember.getUserRole().toString());
      RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, json, "sites/{siteId}/members/{personId}", site.getId(), siteMember.getUsername());
      return restWrapper.processModel(RestSiteMemberModel.class, request);
  }

  /**
   * Delete site member with DELETE call on "sites/{siteId}/members/{personId}"
   * @param person
   */
  public void deleteSiteMember(UserModel person)
  {
      RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, "sites/{siteId}/members/{personId}", site.getId(), person.getUsername());
      restWrapper.processEmptyModel(request);
  }

  /**
   * Retrieve all containers of a site using GET call on "sites/{siteId}/containers"
   * 
   * @return
   * @throws JsonToModelConversionException
   */
  public RestSiteContainerModelsCollection getSiteContainers()
  {
      RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "sites/{siteId}/containers?{parameters}", site.getId(), restWrapper.getParameters());
      return restWrapper.processModels(RestSiteContainerModelsCollection.class, request);
  }
  
  /**
   * Retrieve specific container of a site using GET call on "sites/{siteId}/containers/{containerId}"
   * 
   * @param container A model containing the folderId to look for.
   * @return
   * @throws JsonToModelConversionException
   */
  public RestSiteContainerModel getSiteContainer(RestSiteContainerModel container)
  {
      RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "sites/{siteId}/containers/{containerId}?{parameters}", site.getId(), container.getFolderId(), restWrapper.getParameters());
      return restWrapper.processModel(RestSiteContainerModel.class, request);
  }   
  
  /**
   * Retrieve specific container of a site using GET call on "sites/{siteId}/containers/{containerId}"
   * 
   * @param containerFolder The id of the container to look for.
   * @return
   * @throws JsonToModelConversionException
   */
  public RestSiteContainerModel getSiteContainer(String containerFolder)
  {
      RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "sites/{siteId}/containers/{containerId}?{parameters}", site.getId(), containerFolder, restWrapper.getParameters());
      return restWrapper.processModel(RestSiteContainerModel.class, request);
  }

  /**
   * Create a collaboration site
   * 
   * @return the properties of the created site
   */
  public RestSiteModel createSite()
  {
      RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, site.toJson(), "sites?{parameters}", restWrapper.getParameters());
      return restWrapper.processModel(RestSiteModel.class, request);
  }

  /**
   * Update a site: Site title, description, visibility can be updated
   * Body:
   * {
   *   "title": "string",
   *   "description": "string",
   *   "visibility": "PRIVATE"
   * }
   * 
   * Response:
   * {
   *   "entry": {
   *     "id": "string",
   *     "guid": "string",
   *     "title": "string",
   *     "description": "string",
   *     "visibility": "PRIVATE",
   *     "preset": "string",
   *     "role": "SiteConsumer"
   *   }
   * }
   * 
   * @return the properties of an updated site
   */
  public RestSiteModel updateSite(SiteModel site)
  {     
      String siteBody = JsonBodyGenerator.updateSiteRequest(site);
      RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, siteBody, "sites/{siteId}", site.getId());
      return restWrapper.processModel(RestSiteModel.class, request);
  }

  /**
   * Get site membership requests by using GET /site-membership-requests
   * 
   * @return site memberships
   */
  public RestSitePersonMembershipRequestModelsCollection getSiteMemberships()
  {
      RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "site-membership-requests?{parameters}", restWrapper.getParameters());
      return restWrapper.processModels(RestSitePersonMembershipRequestModelsCollection.class, request);
  }

  /**
   * Approve a site membership request by using POST call on /sites/{siteId}/site-membership-requests/{inviteeId}/approve
   * 
   * @param siteMember
   */
  public RestResponse approveSiteMembership(UserModel siteMember)
  {
      String json = JsonBodyGenerator.keyValueJson("role", siteMember.getUserRole().toString());
      RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, json, "sites/{siteId}/site-membership-requests/{inviteeId}/approve", site.getId(), siteMember.getUsername());
      return restWrapper.process(request);
  }

  /**
   * Reject a site membership request by using POST call /sites/{siteId}/site-membership-requests/{inviteeId}/reject
   * 
   * @param siteMember
   */
  public RestResponse rejectSiteMembership(UserModel siteMember)
  {
      RestRequest request = RestRequest.simpleRequest(HttpMethod.POST, "sites/{siteId}/site-membership-requests/{inviteeId}/reject", site.getId(), siteMember.getUsername());
      return restWrapper.process(request);
  }

  /**
   * Retrieve all group membership of a site using GET call on "sites/{siteId}/group-members"
   *
   * @return RestSiteGroupModelsCollection
   * @throws JsonToModelConversionException
   */
  public RestSiteGroupModelsCollection getSiteGroups()
  {
      RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "sites/{siteId}/group-members?{parameters}", site.getId(), restWrapper.getParameters());
      return restWrapper.processModels(RestSiteGroupModelsCollection.class, request);
  }

  /**
   * Add new site group membership using POST call on "sites/{siteId}/group-members"
   *
   * @param authorityId The authorityId of the group
   * @param role    role to assign
   * @return
   * @throws JsonToModelConversionException
   */
  public RestSiteGroupModel addSiteGroup(String authorityId, UserRole role)
  {
      String siteMemberBody = JsonBodyGenerator.siteGroup(authorityId, role);
      RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, siteMemberBody, "sites/{siteId}/group-members?{parameters}", site.getId(), restWrapper.getParameters());
      return restWrapper.processModel(RestSiteGroupModel.class, request);
  }

  /**
   * Retrieve specific group membership of a site using GET call on "sites/{siteId}/group-members/{groupId}"
   *
   * @param groupId
   * @return RestSiteGroupModel
   */
  public RestSiteGroupModel getSiteGroup(String groupId)
  {
      RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "sites/{siteId}/group-members/{groupId}", site.getId(), groupId);
      return restWrapper.processModel(RestSiteGroupModel.class, request);
  }

  /**
   * Update site group membership role with PUT call on "sites/{siteId}/group-members/{groupId}"
   * @param groupId
   * @return RestSiteGroupModel
   * @throws JsonToModelConversionException
   */
  public RestSiteGroupModel updateSiteGroup(String groupId, UserRole role)
  {
      String json = JsonBodyGenerator.keyValueJson("role", role.toString());
      RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, json, "sites/{siteId}/group-members/{groupId}", site.getId(), groupId);
      return restWrapper.processModel(RestSiteGroupModel.class, request);
  }

  /**
   * Delete site group membership with DELETE call on "sites/{siteId}/group-members/{groupId}"
   * @param groupId
   */
  public void deleteSiteGroup(String groupId)
  {
      RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, "sites/{siteId}/group-members/{groupId}", site.getId(), groupId);
      restWrapper.processEmptyModel(request);
  }
}
