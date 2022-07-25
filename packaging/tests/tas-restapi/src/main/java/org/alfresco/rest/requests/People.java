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

import static io.restassured.RestAssured.given;

import java.io.File;

import org.alfresco.rest.core.JsonBodyGenerator;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestResponse;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.exception.EmptyJsonResponseException;
import org.alfresco.rest.exception.JsonToModelConversionException;
import org.alfresco.rest.model.RestActivityModelsCollection;
import org.alfresco.rest.model.RestFavoriteSiteModel;
import org.alfresco.rest.model.RestGroupsModelsCollection;
import org.alfresco.rest.model.RestNetworkModel;
import org.alfresco.rest.model.RestNetworkModelsCollection;
import org.alfresco.rest.model.RestPersonFavoritesModel;
import org.alfresco.rest.model.RestPersonFavoritesModelsCollection;
import org.alfresco.rest.model.RestPersonModel;
import org.alfresco.rest.model.RestPreferenceModel;
import org.alfresco.rest.model.RestPreferenceModelsCollection;
import org.alfresco.rest.model.RestSiteEntry;
import org.alfresco.rest.model.RestSiteMembershipModelsCollection;
import org.alfresco.rest.model.RestSiteMembershipRequestModel;
import org.alfresco.rest.model.RestSiteMembershipRequestModelsCollection;
import org.alfresco.rest.model.RestSiteModel;
import org.alfresco.rest.model.RestSiteModelsCollection;
import org.alfresco.utility.Utility;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.UserModel;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;

/**
 * Declares all Rest API under the /people path
 */

public class People extends ModelRequest<People>
{
    UserModel person;

    public People(UserModel person, RestWrapper restWrapper)
    {
        super(restWrapper);
        this.person = person;
        Utility.checkObjectIsInitialized(this.person, "person");
    }

    /**
     * Retrieve details of a specific person using GET call on "people/{personId}"
     */
    public RestPersonModel getPerson()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "people/{personId}?{parameters}", this.person.getUsername(), restWrapper.getParameters());
        return restWrapper.processModel(RestPersonModel.class, request);
    }

    /**
     * Update a person properties using PUT call on "people/{personId}"
     */
    public RestPersonModel updatePerson(String putBody)
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, putBody, "people/{personId}", this.person.getUsername());
        return restWrapper.processModel(RestPersonModel.class, request);
    }

    /**
     * Retrieve list of activities for a specific person using GET call on "people/{personId}/activities"
     * Please note that it retries to get the list of activities several times before returning the empty list. The list of activities are not displayed as
     * they are created.
     */
    public RestActivityModelsCollection getPersonActivitiesUntilEntriesCountIs(int expectedNoOfEntries)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "people/{personId}/activities?{parameters}", this.person.getUsername(), restWrapper.getParameters());
        RestActivityModelsCollection activityCollection = restWrapper.processModels(RestActivityModelsCollection.class, request);
        int retry = 0;
        if (Integer.valueOf(restWrapper.getStatusCode()).equals(HttpStatus.OK.value()))
        {
            while ((activityCollection.isEmpty() || activityCollection.getPagination().getCount() != expectedNoOfEntries) && retry < Utility.retryCountSeconds + 20)
            {
                Utility.waitToLoopTime(2);
                activityCollection = restWrapper.processModels(RestActivityModelsCollection.class, request);
                retry++;
            }
        }
        return activityCollection;
    }

    /**
     * Retrieve list of activities for a specific person using GET call on "people/{personId}/activities" without retry
     */
    public RestActivityModelsCollection getPersonActivities()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "people/{personId}/activities?{parameters}", this.person.getUsername(), restWrapper.getParameters());
        return restWrapper.processModels(RestActivityModelsCollection.class, request);
    }

    /**
     * Retrieve preferences of a specific person using GET call on "people/{personId}/preferences"
     */
    public RestPreferenceModelsCollection getPersonPreferences()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "people/{personId}/preferences?{parameters}", this.person.getUsername(), restWrapper.getParameters());
        return restWrapper.processModels(RestPreferenceModelsCollection.class, request);
    }

    /**
     * Retrieve the current site membership requests for a specific person using GET call on "/people/{personId}/site-membership-requests"
     */
    public RestSiteMembershipRequestModelsCollection getSiteMembershipRequests()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "people/{personId}/site-membership-requests?{parameters}", this.person.getUsername(), restWrapper.getParameters());
        return restWrapper.processModels(RestSiteMembershipRequestModelsCollection.class, request);
    }

    /**
     * Retrieve a specific person's favorite sites using GET call on "people/{personId}/favorite-sites"
     */
    public RestSiteModelsCollection getFavoriteSites()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "people/{personId}/favorite-sites?{parameters}", this.person.getUsername(), restWrapper.getParameters());
        return restWrapper.processModels(RestSiteModelsCollection.class, request);
    }

    /**
     * Add a favorite site for a specific person using POST call on "people/{personId}/favorite-sites"
     */
    public RestFavoriteSiteModel addFavoriteSite(SiteModel site)
    {
        String postBody = JsonBodyGenerator.keyValueJson("id", site.getId());
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, postBody, "people/{personId}/favorite-sites", this.person.getUsername());
        return restWrapper.processModel(RestFavoriteSiteModel.class, request);
    }

    /**
     * Retrieve a specific preference of a specific person using GET call on "people/{personId}/preferences/{preferenceName}"
     */
    public RestPreferenceModel getPersonPreferenceInformation(String preferenceName)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "people/{personId}/preferences/{preferenceName}?{parameters}", this.person.getUsername(), preferenceName, restWrapper.getParameters());
        return restWrapper.processModel(RestPreferenceModel.class, request);
    }

    /**
     * Remove a specific site from favorite sites list of a person using DELETE call on "people/{personId}/favorite-sites/{siteId}"
     */
    public void removeFavoriteSite(SiteModel site)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, "people/{personId}/favorite-sites/{siteId}", this.person.getUsername(), site.getId());
        restWrapper.processEmptyModel(request);
    }

    /**
     * Returns information on favorite site siteId of person personId. GET call on "people/{personId}/favorite-sites/{siteId}"
     */
    public RestSiteModel getFavoriteSite(SiteModel site)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "people/{personId}/favorite-sites/{siteId}?{parameters}", this.person.getUsername(), site.getId(), restWrapper.getParameters());
        return restWrapper.processModel(RestSiteModel.class, request);
    }

    /**
     * Delete site member with DELETE call on "people/{personId}/sites/{siteId}"
     */
    public void deleteSiteMember(SiteModel site)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, "people/{personId}/sites/{siteId}", person.getUsername(), site.getId());
        restWrapper.processEmptyModel(request);
    }

    /**
     * Add new site membership request using POST call on "people/{personId}/site-membership-requests"
     */
    public RestSiteMembershipRequestModel addSiteMembershipRequest(String siteMembershipRequest)
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, siteMembershipRequest, "people/{personId}/site-membership-requests", this.person.getUsername());
        return restWrapper.processModel(RestSiteMembershipRequestModel.class, request);
    }

    /**
     * Add new site membership request using POST call on "people/{personId}/site-membership-requests"
     */
    public RestSiteMembershipRequestModel addSiteMembershipRequest(SiteModel siteModel)
    {
        String json = JsonBodyGenerator.siteMemberhipRequest("Please accept me", siteModel, "New request");
        return addSiteMembershipRequest(json);
    }

    /**
     * Add new site membership request using POST call on "people/{personId}/site-membership-requests"
     */
    public RestSiteMembershipRequestModel addSiteMembershipRequest(String message, SiteModel siteModel, String title)
    {
        String json = JsonBodyGenerator.siteMemberhipRequest(message, siteModel, title);
        return addSiteMembershipRequest(json);
    }

    /**
     * Get site membership information using GET call on "/people/{personId}/sites"
     */

    public RestSiteMembershipModelsCollection getSitesMembershipInformation()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "people/{personId}/sites?{parameters}", person.getUsername(), restWrapper.getParameters());
        return restWrapper.processModels(RestSiteMembershipModelsCollection.class, request);
    }

    /**
     * Retrieve site membership information for a person using GET call on "people/{personId}/sites/{siteId}"
     */
    public RestSiteEntry getSiteMembership(SiteModel site)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "people/{personId}/sites/{siteId}?{parameters}", person.getUsername(), site.getId(), restWrapper.getParameters());
        return restWrapper.processModel(RestSiteEntry.class, request);
    }

    /**
     * Retrieve site membership request details for a person/site using GET call on "people/{personId}/site-membership-requests/{siteId}"
     */
    public RestSiteMembershipRequestModel getSiteMembershipRequest(SiteModel site)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "people/{personId}/site-membership-requests/{siteId}?{parameters}", person.getUsername(),
                site.getId(), restWrapper.getParameters());
        return restWrapper.processModel(RestSiteMembershipRequestModel.class, request);
    }

    /**
     * Delete site membership request with DELETE call on "/people/{personId}/site-membership-requests/{siteId}"
     */
    public void deleteSiteMembershipRequest(SiteModel site) throws JsonToModelConversionException, EmptyJsonResponseException
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, "people/{personId}/site-membership-requests/{siteId}", person.getUsername(),
                site.getId());
        restWrapper.processEmptyModel(request);
    }

    /**
     * Update site membership request using PUT call on "people/{personId}/site-membership-requests/{siteId}"
     */
    public RestSiteMembershipRequestModel updateSiteMembershipRequest(SiteModel siteModel, String message)
    {
        String json = JsonBodyGenerator.siteMemberhipRequest(message, siteModel, "New request");
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, json, "people/{personId}/site-membership-requests/{siteId}", person.getUsername(),
                siteModel.getId());
        return restWrapper.processModel(RestSiteMembershipRequestModel.class, request);
    }

    /**
     * Retrieve favorite site for a specific user using GET call on "people/{personId}/favorites/{favoriteId}"
     */
    public RestPersonFavoritesModel getFavorite(String favoriteId)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "people/{personId}/favorites/{favoriteId}?{parameters}", this.person.getUsername(), favoriteId, restWrapper.getParameters());
        return restWrapper.processModel(RestPersonFavoritesModel.class, request);
    }

    /**
     * Retrieve specific favorites for a specific user using GET call on "people/{personId}/favorites"
     */
    public RestPersonFavoritesModelsCollection getFavorites()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "people/{personId}/favorites?{parameters}", this.person.getUsername(), restWrapper.getParameters());
        return restWrapper.processModels(RestPersonFavoritesModelsCollection.class, request);
    }

    /**
     * Add a folder to favorites for a specific user using POST call on "people/{personId}/favorites"
     */
    public RestPersonFavoritesModel addFolderToFavorites(FolderModel folderModel)
    {
        String jsonPost = JsonBodyGenerator.targetFolderWithGuid(folderModel);
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, jsonPost, "people/{personId}/favorites?{parameters}", this.person.getUsername(),
                restWrapper.getParameters());
        return restWrapper.processModel(RestPersonFavoritesModel.class, request);
    }

    /**
     * Add a folder to favorites for a specific user using POST call on "people/{personId}/favorites"
     */
    public RestPersonFavoritesModel addFileToFavorites(FileModel fileModel)
    {
        String jsonPost = JsonBodyGenerator.targetFileWithGuid(fileModel);
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, jsonPost, "people/{personId}/favorites?{parameters}", this.person.getUsername(),
                restWrapper.getParameters());
        return restWrapper.processModel(RestPersonFavoritesModel.class, request);
    }

    /**
     * Add a site to favorites for a specific user using POST call on "people/{personId}/favorites"
     */
    public RestPersonFavoritesModel addSiteToFavorites(SiteModel siteModel)
    {
        String jsonPost = JsonBodyGenerator.targetSiteWithGuid(siteModel);

        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, jsonPost, "people/{personId}/favorites?{parameters}", this.person.getUsername(),
                restWrapper.getParameters());
        return restWrapper.processModel(RestPersonFavoritesModel.class, request);
    }

    /**
     * Delete site from favorites for a specific user using DELETE call on "people/{personId}/favorites/{favoriteId}"
     */
    public RestWrapper deleteSiteFromFavorites(SiteModel site)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, "people/{personId}/favorites/{favoriteId}", this.person.getUsername(), site.getGuid()); 
        restWrapper.processEmptyModel(request);
        return restWrapper;
    }

    /**
     * Delete a folder from favorites for a specific user using DELETE call on "people/{personId}/favorites/{favoriteId}"
     */
    public RestWrapper deleteFolderFromFavorites(FolderModel folderModel)
    {
        String jsonPost = JsonBodyGenerator.targetFolderWithGuid(folderModel);

        RestRequest request = RestRequest.requestWithBody(HttpMethod.DELETE, jsonPost, "people/{personId}/favorites/{favoriteId}", this.person.getUsername(),
                folderModel.getNodeRef());
        restWrapper.processEmptyModel(request);
        return restWrapper;
    }

    /**
     * Delete a file from favorites for a specific user using DELETE call on "people/{personId}/favorites/{favoriteId}"
     */
    public RestWrapper deleteFileFromFavorites(FileModel fileModel)
    {
        String jsonPost = JsonBodyGenerator.targetFileWithGuid(fileModel);

        RestRequest request = RestRequest.requestWithBody(HttpMethod.DELETE, jsonPost, "people/{personId}/favorites/{favoriteId}", this.person.getUsername(),
                fileModel.getNodeRef());
        restWrapper.processEmptyModel(request);
        return restWrapper;
    }

    /**
     * Retrieve details of the current user network using GET call on "people/{personId}/networks/{networkId}"
     */
    public RestNetworkModel getNetwork()
    {
        return getNetwork(person);
    }

    /**
     * Retrieve details of a specific network using GET call on "people/{personId}/networks/{networkId}"
     */
    public RestNetworkModel getNetwork(UserModel tenant)
    {
        Utility.checkObjectIsInitialized(tenant.getDomain(), "tenant.getDomain()");
        String personId = tenant.getUsername().contains("-me-@")? "-me-" : tenant.getUsername();
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "people/{personId}/networks/{networkId}?{parameters}", personId, tenant.getDomain(), restWrapper.getParameters());
        return restWrapper.processModel(RestNetworkModel.class, request);
    }

    /**
     * Retrieve details of all networks related to the current person using GET call on "people/{personId}/networks"
     */
    public RestNetworkModelsCollection getNetworks()
    {
        return getNetworks(person);
    }

    /**
     * Retrieve details of all networks related to a specific person using GET call on "people/{personId}/networks"
     */
    public RestNetworkModelsCollection getNetworks(UserModel tenant)
    {
        String personId = tenant.getUsername().contains("-me-@") ? "-me-" : tenant.getUsername();
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "people/{personId}/networks?{parameters}", personId, restWrapper.getParameters());
        return restWrapper.processModels(RestNetworkModelsCollection.class, request);
    }

    /**
     * Create new person with given newPerson details using POST call on "people"
     */
    public RestPersonModel createPerson(RestPersonModel newPerson)
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, newPerson.toJson(), "people");
        return restWrapper.processModel(RestPersonModel.class, request);
    }
    
    /**
     * Get people avatar image using GET call on '/people/{personId}/avatar Please note that it retries to get the
     * renditions response several times because on the alfresco server the rendition can take a while to be created.
     */
    public RestResponse downloadAvatarContent()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "people/{personId}/avatar?{parameters}",
                this.person.getUsername(), restWrapper.getParameters());
        return restWrapper.process(request);
    }

    /**
     * Delete avatar image with DELETE call on "/people/{personId}/avatar}"
     */
    public void resetAvatarImageRequest() throws JsonToModelConversionException, EmptyJsonResponseException
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, "people/{personId}/avatar", this.person.getUsername());
        restWrapper.processEmptyModel(request);
    }

    /**
     * Update avatar image PUT call on 'people/{nodeId}/children
     */
    public ValidatableResponse uploadAvatarContent(String fullServerUrL, File avatarFile)
    {
        return given().auth().preemptive().basic(person.getUsername(), person.getPassword()).contentType(ContentType.BINARY)
                .body(avatarFile).when()
                .put(String.format("%s/%s/people/%s/avatar", fullServerUrL, RestAssured.basePath, person.getUsername()))
                .then();
    }

    /**
     * List group memberships for a person using GET on '/people/{personId}/groups
     */
    public RestGroupsModelsCollection listGroupMemberships()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "people/{personId}/groups?{parameters}", this.person.getUsername(), restWrapper.getParameters());
        return restWrapper.processModels(RestGroupsModelsCollection.class, request);
    }

    public WhereClause where()
    {
        return new WhereClause(this);
    }

/**
     * Construct the Where clause of getFavorites
     * You can use the where parameter to restrict the list in the response to entries of a specific kind. The where parameter takes a value. The value is a
     * single predicate that can include one or more EXISTS conditions. The EXISTS condition uses a single operand to limit the list to include entries that
     * include that one property. The property values are:-
     * target/file
     * target/folder
     * target/site
     * Usage:
     * where.. targetFileExist().or().targetSiteExist().filterAnd().getFavorites(...)
     * At this point this method is working only with @link {@link RestFavoritesApi#getFavorites(UserModel) method.
     * 
     * @author paul.brodner
     */
    public class WhereClause
    {
        String whereClause = "where=(%s))";
        StringBuilder expression = new StringBuilder();
        private People people;

        public WhereClause(People people)
        {
            this.people = people;
        }

        public WhereClause targetFileExist()
        {
            expression.append("EXISTS(target/file)").append(" ");
            return this;
        }

        public WhereClause targetFolderExist()
        {
            expression.append("EXISTS(target/folder)").append(" ");
            return this;
        }

        public WhereClause targetSiteExist()
        {
            expression.append(" EXISTS(target/site)").append(" ");
            return this;
        }

        public WhereClause invalidWhereParameter(String invalidParameter)
        {
            expression.append(invalidParameter).append(" ");
            return this;
        }

        public WhereClause or()
        {
            expression.append("OR").append(" ");
            return this;
        }

        public RestPersonFavoritesModelsCollection getFavorites()
        {
            restWrapper.withParams(String.format(whereClause, expression));
            return people.getFavorites();
        }
    }
}
