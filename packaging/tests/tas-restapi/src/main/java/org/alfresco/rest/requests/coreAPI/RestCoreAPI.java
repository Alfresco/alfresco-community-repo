package org.alfresco.rest.requests.coreAPI;

import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.RestDownloadsModel;
import org.alfresco.rest.model.RestSiteModelsCollection;
import org.alfresco.rest.model.RestTagModel;
import org.alfresco.rest.model.RestTagModelsCollection;
import org.alfresco.rest.requests.Actions;
import org.alfresco.rest.requests.Audit;
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

import io.restassured.RestAssured;

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
     * @throws Exception
     */
    public RestSiteModelsCollection getSites() throws Exception
    {
        return new Site(null, restWrapper).getSites();
    }

    /**
     * Provides DSL on all REST calls under <code>/nodes</code> API path
     * 
     * @return {@link Node}
     * @throws Exception
     */
    public Node usingResource(RepoTestModel node) throws Exception
    {
        return new Node(node, restWrapper);
    }

    /**
     * synonym for {@link #usingResource(RepoTestModel)}
     * 
     * @param node
     * @return
     * @throws Exception
     */
    public Node usingNode(RepoTestModel node) throws Exception
    {
        return new Node(node, restWrapper);
    }

    public Node usingNode() throws Exception
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
     * @throws Exception
     */
    public People usingUser(UserModel person) throws Exception
    {
        return new People(person, restWrapper);
    }

    /**
     * Make REST calls using current authenticated user, but using -me- instead of username
     * 
     * @return {@link People}
     * @throws Exception
     */
    public People usingMe() throws Exception
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
     * @throws Exception
     */
    public People usingAuthUser() throws Exception
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

    public Tags usingTag(RestTagModel tag)
    {
        return new Tags(tag, restWrapper);
    }

    public RestTagModelsCollection getTags() throws Exception
    {
        return new Tags(null, restWrapper).getTags();
    }

    public RestTagModel getTag(RestTagModel tag) throws Exception
    {
        return new Tags(tag, restWrapper).getTag();
    }
    
    public Queries usingQueries()
    {
        return new Queries(restWrapper);
    }

    public Audit usingAudit() throws Exception
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

    public Downloads usingDownloads(RestDownloadsModel downloadsModel) throws Exception
    {
        return new Downloads(downloadsModel, restWrapper);
    }
}
