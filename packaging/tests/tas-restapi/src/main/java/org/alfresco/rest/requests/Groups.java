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

import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.RestGroupMember;
import org.alfresco.rest.model.RestGroupMemberModelsCollection;
import org.alfresco.rest.model.RestGroupsModel;
import org.alfresco.rest.model.RestGroupsModelsCollection;
import org.springframework.http.HttpMethod;

public class Groups extends ModelRequest<Groups>
{

    public Groups(RestWrapper restWrapper)
    {
        super(restWrapper);
    }

    /**
     * List existing groups using GET on '/groups
     */
    public RestGroupsModelsCollection listGroups()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "groups?{parameters}", restWrapper.getParameters());
        return restWrapper.processModels(RestGroupsModelsCollection.class, request);
    }

    /**
     * Create a group using POST on '/groups
     */
    public RestGroupsModel createGroup(String groupBodyCreate)
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, groupBodyCreate, "groups?{parameters}", restWrapper.getParameters());
        return restWrapper.processModel(RestGroupsModel.class, request);
    }

    /**
     * Retrieve group details using GET on '/groups/{groupId}
     */
    public RestGroupsModel getGroupDetail(String groupId)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "groups/{groupId}?{parameters}", groupId, restWrapper.getParameters());
        return restWrapper.processModel(RestGroupsModel.class, request);
    }

    /**
     * Delete a group using DELETE on '/groups/{groupId}
     */
    public void deleteGroup(String groupId)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, "groups/{groupId}?{parameters}", groupId, restWrapper.getParameters());
        restWrapper.processEmptyModel(request);
    }

    /**
     * Update group details using PUT on '/groups/{groupId}
     */
    public RestGroupsModel updateGroupDetails(String groupId, String groupBodyUpdate)
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, groupBodyUpdate, "groups/{groupId}?{parameters}", groupId, restWrapper.getParameters());
        return restWrapper.processModel(RestGroupsModel.class, request);
    }

    /**
     * List memberships of a group using GET on '/groups/{groupId}/members
     */
    public RestGroupMemberModelsCollection listGroupMemberships(String groupId)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "groups/{groupId}/members?{parameters}", groupId, restWrapper.getParameters());
        return restWrapper.processModels(RestGroupMemberModelsCollection.class, request);
    }

    /**
     * Create a group membership using POST on '/groups/{groupId}/members
     */
    public RestGroupMember createGroupMembership (String groupId, String groupMembershipBodyCreate)
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, groupMembershipBodyCreate, "groups/{groupId}/members?{parameters}", groupId,
                restWrapper.getParameters());
        return restWrapper.processModel(RestGroupMember.class, request);
    }

    /**
     * Delete a group membership using DELETE on '/groups/{groupId}/members/{groupMemberId}
     */
    public void deleteGroupMembership(String groupId, String groupMemberId)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, "groups/{groupId}/members/{groupMemberId}", groupId, groupMemberId);
        restWrapper.processEmptyModel(request);
    }
}
