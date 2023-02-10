/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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
package org.alfresco.rest.requests.coreAPI;

import static org.alfresco.rest.core.JsonBodyGenerator.arrayToJson;

import java.util.List;

import io.restassured.RestAssured;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.RestCategoryModel;
import org.alfresco.rest.model.RestDownloadsModel;
import org.alfresco.rest.model.RestSiteModelsCollection;
import org.alfresco.rest.model.RestTagModel;
import org.alfresco.rest.model.RestTagModelsCollection;
import org.alfresco.rest.requests.Actions;
import org.alfresco.rest.requests.Audit;
import org.alfresco.rest.requests.Categories;
import org.alfresco.rest.requests.ContentStorageInformation;
import org.alfresco.rest.requests.Downloads;
import org.alfresco.rest.requests.Groups;
import org.alfresco.rest.requests.ModelRequest;
import org.alfresco.rest.requests.Networks;
import org.alfresco.rest.requests.Node;
import org.alfresco.rest.requests.People;
import org.alfresco.rest.requests.Queries;
import org.alfresco.rest.requests.SharedLinks;
import org.alfresco.rest.requests.Site;
import org.alfresco.rest.requests.Tags;
import org.alfresco.rest.requests.Trashcan;
import org.alfresco.utility.model.RepoTestModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.UserModel;
import org.springframework.http.HttpMethod;

/**
 * Defines the entire Rest Core API
 * {@link https://api-explorer.alfresco.com/api-explorer/} select "Core API"
 */
public class RestCoreAPI extends ModelRequest<RestCoreAPI>
{    
    public RestCoreAPI(RestWrapper restWrapper)
    {
        super(restWrapper);
        RestAssured.basePath = "alfresco/api/-default-/public/alfresco/versions/1";
        restWrapper.configureRequestSpec().setBasePath(RestAssured.basePath);
    }

    /**
     * Provides DSL on all REST calls under <code>sites/{siteId}/...</code> API path
     * 
     * @param siteId
     * @return {@link Site}
     */
    public Site usingSite(String siteId)
    {
        return new Site(new SiteModel(siteId), restWrapper);
    }

    /**
     * Provides DSL on all REST calls under <code>sites/{siteId}/...</code> API path
     * 
     * @param SiteModel
     * @return {@link Site}
     */
    public Site usingSite(SiteModel siteModel)
    {
        return new Site(siteModel, restWrapper);
    }

    /**
     * Provides DSL on all REST calls under <code>/sites</code> API path
     * 
     * @return {@link Site}
     */
    public RestSiteModelsCollection getSites()
    {
        return new Site(null, restWrapper).getSites();
    }

    /**
     * Provides DSL on all REST calls under <code>/nodes</code> API path
     * 
     * @return {@link Node}
     */
    public Node usingResource(RepoTestModel node)
    {
        return new Node(node, restWrapper);
    }

    /**
     * synonym for {@link #usingResource(RepoTestModel)}
     * 
     * @param node
     * @return
     */
    public Node usingNode(RepoTestModel node)
    {
        return new Node(node, restWrapper);
    }

    public Node usingNode()
    {
        return new Node(restWrapper);
    }

    public ContentStorageInformation usingStorageInfo()
    {
        return new ContentStorageInformation(restWrapper);
    }

    /**
     * Provides DSL of all REST calls under <code>/people</code> API path
     * 
     * @return {@link People}
     */
    public People usingUser(UserModel person)
    {
        return new People(person, restWrapper);
    }

    /**
     * Make REST calls using current authenticated user, but using -me- instead of username
     * 
     * @return {@link People}
     */
    public People usingMe()
    {
        UserModel userModel = new UserModel("-me-", restWrapper.getTestUser().getPassword());
        userModel.setDomain(restWrapper.getTestUser().getDomain());
        userModel.setUserRole(restWrapper.getTestUser().getUserRole());
        return new People(userModel, restWrapper);
    }

    /**
     * Make REST calls using current authenticated user.
     * This is set on the {@link #authenticateUser(UserModel)} call
     * 
     * @return {@link People}
     */
    public People usingAuthUser()
    {
        return new People(restWrapper.getTestUser(), restWrapper);
    }

    /**
     * Provides DSL on all REST calls under <code>/network</code> API path
     * 
     * @return {@link Networks}
     */
    public Networks usingNetworks()
    {
        return new Networks(restWrapper);
    }

    /**
     * Create a single orphan tag.
     *
     * @param tag Tag model to create.
     * @return Created tag.
     */
    public RestTagModel createSingleTag(RestTagModel tag)
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, tag.toJson(), "tags/");
        return restWrapper.processModel(RestTagModel.class, request);
    }

    /**
     * Create several orphan tags in one request.
     *
     * @param tags Tags models to create.
     * @return Created tags.
     */
    public RestTagModelsCollection createTags(List<RestTagModel> tags)
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, arrayToJson(tags), "tags/");
        return restWrapper.processModels(RestTagModelsCollection.class, request);
    }

    public Tags usingTag(RestTagModel tag)
    {
        return new Tags(tag, restWrapper);
    }

    public RestTagModelsCollection getTags()
    {
        return new Tags(null, restWrapper).getTags();
    }

    public RestTagModel getTag(RestTagModel tag)
    {
        return new Tags(tag, restWrapper).getTag();
    }


    public Categories usingCategory(RestCategoryModel categoryModel)
    {
        return new Categories(restWrapper, categoryModel);
    }
    
    public Queries usingQueries()
    {
        return new Queries(restWrapper);
    }

    public Audit usingAudit()
    {
        return new Audit(restWrapper);
    }

    public Trashcan usingTrashcan()
    {
        return new Trashcan(restWrapper);
    }
    
    /**
     * SharedLinks api endpoint
     * 
     * @author meenal bhave
     * @return SharedLinks api endpoint
     */

    public SharedLinks usingSharedLinks()
    {
        return new SharedLinks(restWrapper);
    }

    public Groups usingGroups()
    {
        return new Groups(restWrapper);
    }
    
    
    public Actions usingActions()
    {
        return new Actions(restWrapper);
    }

    public Downloads usingDownloads()
    {
        return new Downloads(restWrapper);
    }

    public Downloads usingDownloads(RestDownloadsModel downloadsModel)
    {
        return new Downloads(downloadsModel, restWrapper);
    }
}
