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
    public RestGroupsModelsCollection listGroups() throws Exception
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "groups?{parameters}", restWrapper.getParameters());
        return restWrapper.processModels(RestGroupsModelsCollection.class, request);
    }

    /**
     * Create a group using POST on '/groups
     */
    public RestGroupsModel createGroup(String groupBodyCreate) throws Exception
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, groupBodyCreate, "groups?{parameters}", restWrapper.getParameters());
        return restWrapper.processModel(RestGroupsModel.class, request);
    }

    /**
     * Retrieve group details using GET on '/groups/{groupId}
     */
    public RestGroupsModel getGroupDetail(String groupId) throws Exception
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "groups/{groupId}?{parameters}", groupId, restWrapper.getParameters());
        return restWrapper.processModel(RestGroupsModel.class, request);
    }

    /**
     * Delete a group using DELETE on '/groups/{groupId}
     */
    public void deleteGroup(String groupId) throws Exception
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, "groups/{groupId}?{parameters}", groupId, restWrapper.getParameters());
        restWrapper.processEmptyModel(request);
    }

    /**
     * Update group details using PUT on '/groups/{groupId}
     */
    public RestGroupsModel updateGroupDetails(String groupId, String groupBodyUpdate) throws Exception
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, groupBodyUpdate, "groups/{groupId}?{parameters}", groupId, restWrapper.getParameters());
        return restWrapper.processModel(RestGroupsModel.class, request);
    }

    /**
     * List memberships of a group using GET on '/groups/{groupId}/members
     */
    public RestGroupMemberModelsCollection listGroupMemberships(String groupId) throws Exception
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "groups/{groupId}/members?{parameters}", groupId, restWrapper.getParameters());
        return restWrapper.processModels(RestGroupMemberModelsCollection.class, request);
    }

    /**
     * Create a group membership using POST on '/groups/{groupId}/members
     */
    public RestGroupMember createGroupMembership (String groupId, String groupMembershipBodyCreate) throws Exception
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, groupMembershipBodyCreate, "groups/{groupId}/members?{parameters}", groupId,
                restWrapper.getParameters());
        return restWrapper.processModel(RestGroupMember.class, request);
    }

    /**
     * Delete a group membership using DELETE on '/groups/{groupId}/members/{groupMemberId}
     */
    public void deleteGroupMembership(String groupId, String groupMemberId) throws Exception
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, "groups/{groupId}/members/{groupMemberId}", groupId, groupMemberId);
        restWrapper.processEmptyModel(request);
    }
}
