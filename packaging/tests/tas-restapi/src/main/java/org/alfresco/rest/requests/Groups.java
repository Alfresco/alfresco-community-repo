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
