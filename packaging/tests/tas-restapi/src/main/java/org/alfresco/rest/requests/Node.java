
package org.alfresco.rest.requests;

import io.restassured.http.ContentType;
import org.alfresco.rest.core.JsonBodyGenerator;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestResponse;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.exception.JsonToModelConversionException;
import org.alfresco.rest.model.*;
import org.alfresco.rest.model.body.RestNodeLockBodyModel;
import org.alfresco.rest.model.builder.NodesBuilder;
import org.alfresco.utility.Utility;
import org.alfresco.utility.model.RepoTestModel;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.reporters.Files;

import javax.json.JsonArrayBuilder;
import java.io.File;

/**
 * Declares all Rest API under the /nodes path
 *
 */
public class Node extends ModelRequest<Node>
{
    private RepoTestModel repoModel;

    public Node(RestWrapper restWrapper) throws Exception 
    {
      super(restWrapper);
    }

    public Node(RepoTestModel repoModel, RestWrapper restWrapper) throws Exception 
    {
      super(restWrapper);
      this.repoModel = repoModel; 
      Utility.checkObjectIsInitialized(this.repoModel.getNodeRef(), "repoModel.getNodeRef()");
    }
    
    /**
     * Retrieve details for a specific node using GET call on "nodes/{nodeId}"
     * 
     * @param nodeId
     * @return
     * @throws JsonToModelConversionException
     */
    public RestNodeModel getNode() throws Exception
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "nodes/{nodeId}?{parameters}", repoModel.getNodeRef(), restWrapper.getParameters());
        return restWrapper.processModel(RestNodeModel.class, request);
    }

    /**
     * Retrieve comments for a specific node using GET call on "nodes/{nodeId}/comments"
     * 
     * @param nodeId
     * @return
     * @throws JsonToModelConversionException
     */
    public RestCommentModelsCollection getNodeComments() throws Exception
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "nodes/{nodeId}/comments?{parameters}", repoModel.getNodeRef(), restWrapper.getParameters());
        return restWrapper.processModels(RestCommentModelsCollection.class, request);
    }

    /**
     * Publish one new comment on a specific node using POST call on "nodes/{nodeId}/comments"
     * 
     * @param node
     * @param commentContent
     * @return
     * @throws Exception
     */
    public RestCommentModel addComment(String commentContent) throws Exception
    {
        String postBody = JsonBodyGenerator.keyValueJson("content", commentContent);
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, postBody, "nodes/{nodeId}/comments", repoModel.getNodeRef());
        return restWrapper.processModel(RestCommentModel.class, request);
    }

    /**
     * Publish multiple comments on a specific node using POST call on "nodes/{nodeId}/comments"
     *
     * @param contentModel
     * @param comments
     * @return
     * @throws Exception
     */
    public RestCommentModelsCollection addComments(String... comments) throws Exception
    {
        JsonArrayBuilder array = JsonBodyGenerator.defineJSONArray();
        for(String comment: comments)
        {
            array.add(JsonBodyGenerator.defineJSON().add("content", comment));
        }
        String postBody = array.build().toString();
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, postBody, "nodes/{nodeId}/comments", repoModel.getNodeRef());
        return restWrapper.processModels(RestCommentModelsCollection.class, request);
    }

    /**
     * Update a comment for a specific node using PUT call on nodes/{nodeId}/comments/{commentId}
     * 
     * @param nodeId
     * @param commentId
     * @param commentContent
     * @return
     * @throws JsonToModelConversionException
     */
    public RestCommentModel updateComment(RestCommentModel commentModel, String commentContent) throws Exception
    {
        String postBody = JsonBodyGenerator.keyValueJson("content", commentContent);
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, postBody, "nodes/{nodeId}/comments/{commentId}?{parameters}", repoModel.getNodeRef(), commentModel.getId(), restWrapper.getParameters());
        request.setContentType("UTF-8");
        RestCommentModel response = restWrapper.processModel(RestCommentModel.class, request);
        return response;
    }

    /**
     * Delete a comment for a specific node using DELETE call on nodes/{nodeId}/comments/{commentId}
     * 
     * @param nodeId
     * @param commentId
     * @return
     * @throws JsonToModelConversionException
     */
    public void deleteComment(RestCommentModel comment) throws Exception
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, "nodes/{nodeId}/comments/{commentId}", repoModel.getNodeRef(), comment.getId());
        restWrapper.processEmptyModel(request);
    }

    /**
     * Like a document using POST call on "nodes/{nodeId}/ratings"
     * 
     * @return
     * @throws Exception
     */
    public RestRatingModel likeDocument() throws Exception {
      String postBody = JsonBodyGenerator.likeRating(true);
      RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, postBody, "nodes/{nodeId}/ratings", repoModel.getNodeRef());
      return restWrapper.processModel(RestRatingModel.class, request);
    }

    public RestRatingModel dislikeDocument() throws Exception {
      String postBody = JsonBodyGenerator.likeRating(false);
      RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, postBody, "nodes/{nodeId}/ratings", repoModel.getNodeRef());
      return restWrapper.processModel(RestRatingModel.class, request);
    }

    /**
     * POST call on "nodes/{nodeId}/ratings" using an invalid rating body
     * 
     * @return
     * @throws Exception
     */
    public RestRatingModel addInvalidRating(String jsonBody) throws Exception {
      RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, jsonBody, "nodes/{nodeId}/ratings", repoModel.getNodeRef());
      return restWrapper.processModel(RestRatingModel.class, request);
    }
    

    
    /**
     * Add five star rate to a document using POST call on "nodes/{nodeId}/ratings"
     * 
     * @param stars
     * @return
     * @throws Exception
     */
    public RestRatingModel rateStarsToDocument(int stars) throws Exception {
      String postBody = JsonBodyGenerator.fiveStarRating(stars);
      RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, postBody, "nodes/{nodeId}/ratings", repoModel.getNodeRef());
      return restWrapper.processModel(RestRatingModel.class, request);
    }

    /**
     * Retrieve node ratings using GET call on "nodes/{nodeId}/ratings"
     * 
     * @return
     * @throws Exception
     */
    public RestRatingModelsCollection getRatings() throws Exception {
      RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "nodes/{nodeId}/ratings?{parameters}", repoModel.getNodeRef(), restWrapper.getParameters());
      return restWrapper.processModels(RestRatingModelsCollection.class, request);
    }
    
    /**
     * Delete like rating using DELETE call on "nodes/{nodeId}/ratings/{ratingId}"
     * 
     * @return
     * @throws Exception
     */
    public void deleteLikeRating() throws Exception {
      RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, "nodes/{nodeId}/ratings/{ratingId}", repoModel.getNodeRef(), "likes");
      restWrapper.processEmptyModel(request);
    }

    /**
     * Try to delete invalid rating using DELETE call on "nodes/{nodeId}/ratings/{ratingId}"
     * 
     * @return
     * @throws Exception
     */
    public void deleteInvalidRating(String rating) throws Exception {
      RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, "nodes/{nodeId}/ratings/{ratingId}", repoModel.getNodeRef(), rating);
      restWrapper.processEmptyModel(request);
    }
    
    /**
     * 
     * Get like rating of a document using GET call on "nodes/{nodeId}/ratings/{ratingId}"
     */
    public RestRatingModel getLikeRating() throws Exception {
      RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "nodes/{nodeId}/ratings/{ratingId}?{parameters}", repoModel.getNodeRef(), "likes", restWrapper.getParameters());
      return restWrapper.processModel(RestRatingModel.class, request);
    }

    /**
     * Delete fivestar rating using DELETE call on "nodes/{nodeId}/ratings/{ratingId}"
     * 
     * @return
     * @throws Exception
     */
    public void deleteFiveStarRating() throws Exception {
      RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, "nodes/{nodeId}/ratings/{ratingId}", repoModel.getNodeRef(), "fiveStar");
      restWrapper.processEmptyModel(request);
    }

    /**
     * 
     * Get fivestar rating of a document using GET call on "nodes/{nodeId}/ratings/{ratingId}"
     * @return
     * @throws Exception
     */
    public RestRatingModel getFiveStarRating() throws Exception {
      RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "nodes/{nodeId}/ratings/{ratingId}?{parameters}", repoModel.getNodeRef(), "fiveStar", restWrapper.getParameters());
      return restWrapper.processModel(RestRatingModel.class, request);
    }

    /**
     * Adds a tag to the given content node
     *
     * @param contentModel
     * @param tag
     * @return
     * @throws Exception
     */
    public RestTagModel addTag(String tag) throws Exception
    {
        String postBody = JsonBodyGenerator.keyValueJson("tag", tag);
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, postBody, "nodes/{nodeId}/tags", repoModel.getNodeRef());
        return restWrapper.processModel(RestTagModel.class, request);
    }

    /**
     * Adds multiple tags to the given content node
     *
     * @param contentModel
     * @param tags
     * @return
     * @throws Exception
     */
    public RestTagModelsCollection addTags(String... tags) throws Exception
    {
        String postBody = "[";
        for(String tag: tags)
        {
            postBody += JsonBodyGenerator.keyValueJson("tag", tag) + ",";
        }
        postBody = postBody.substring(0, postBody.length()-1) + "]";

        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, postBody, "nodes/{nodeId}/tags", repoModel.getNodeRef());
        return restWrapper.processModels(RestTagModelsCollection.class, request);
    }

    
    /**
     * Deletes a tag for a specific content node using DELETE call on nodes/{nodeId}/tags/{tagId}
     *
     * @param content
     * @param tag
     * @return
     * @throws JsonToModelConversionException
     */
    public void deleteTag(RestTagModel tag) throws Exception
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, "nodes/{nodeId}/tags/{tagId}", repoModel.getNodeRef(), tag.getId());
        restWrapper.processEmptyModel(request);
    }

    /**
     * Get node tags using GET call on 'nodes/{nodeId}/tags'
     * 
     * @param tag
     * @return
     * @throws Exception
     */
    public RestTagModelsCollection getNodeTags() throws Exception
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "nodes/{nodeId}/tags?{parameters}", repoModel.getNodeRef(), restWrapper.getParameters());
        return restWrapper.processModels(RestTagModelsCollection.class, request);
    }

    /**
     * Create new nodes using POST call on 'nodes/{nodeId}/children
     * 
     * @param node
     * @return 
     * @throws Exception 
     */    
    public RestNodeModel createNode(RestNodeBodyModel node) throws Exception
    {        
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, node.toJson(), "nodes/{nodeId}/children?{parameters}", repoModel.getNodeRef(), restWrapper.getParameters());       
        return restWrapper.processModel(RestNodeModel.class, request);        
    }

    /**
     * Create new nodes using POST call on 'nodes/{nodeId}/children
     * 
     * You need to specify first the multipart call {@link RestWrapper#usingMultipartFile(java.io.File)}
     * 
     * <code>usingMultipartFile(new File("your-local-file.txt")).withCoreAPI().usingNode(ContentModel.my()).createNode();</code>
     * @return
     * @throws Exception
     */
    public RestNodeModel createNode() throws Exception
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.POST,  "nodes/{nodeId}/children", repoModel.getNodeRef());       
        return restWrapper.processModel(RestNodeModel.class, request);
    }

    /**
     * Retrieve content for a specific node using GET call on "nodes/{nodeId}/content"
     * 
     * @return
     * @throws Exception
     */
    public RestResponse getNodeContent() throws Exception
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "nodes/{nodeId}/content?{parameters}", repoModel.getNodeRef(), restWrapper.getParameters());
        return restWrapper.process(request);
    }

    /**
     * Retrieve content for a specific node using GET call on "nodes/{nodeId}/content"
     * 
     * @return
     * @param nodeId
     * @throws Exception
     */
    public RestResponse getNodeContent(String nodeId) throws Exception
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "nodes/{nodeId}/content?{parameters}", nodeId, restWrapper.getParameters());
        return restWrapper.process(request);
    }

    /**
     * Create node rendition using POST call on '/nodes/{nodeId}/renditions'
     * 
     * @param renditionId id of rendition to be created
     * @return
     * @throws Exception
     */
    public void createNodeRendition(String renditionId) throws Exception
    {
        String postBody = JsonBodyGenerator.keyValueJson("id", renditionId);
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, postBody, "nodes/{nodeId}/renditions", repoModel.getNodeRef());       
        restWrapper.processEmptyModel(request);
    }

    /**
     * Create node version rendition using POST call on '/nodes/{nodeId}/versions/{versionId}/renditions'
     *
     * @param renditionId id of rendition to be created
     * @param versionId version id of node
     * @return
     * @throws Exception
     */
    public void createNodeVersionRendition(String renditionId, String versionId) throws Exception
    {
        String postBody = JsonBodyGenerator.keyValueJson("id", renditionId);
        RestRequest request = RestRequest
                    .requestWithBody(HttpMethod.POST, postBody, "nodes/{nodeId}/versions/{versionId}/renditions",
                                repoModel.getNodeRef(), versionId);
        restWrapper.processEmptyModel(request);
    }

    /**
     * Check if specified rendition exists and if not
     * create node rendition using POST call on '/nodes/{nodeId}/renditions'
     *
     * @param renditionId id of rendition to be created
     * @return
     * @throws Exception
     */
    public void createNodeRenditionIfNotExists(String renditionId) throws Exception
    {
        getNodeRendition(renditionId);
        if (Integer.valueOf(restWrapper.getStatusCode()).equals(HttpStatus.OK.value()))
        {
            String postBody = JsonBodyGenerator.keyValueJson("id", renditionId);
            RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, postBody, "nodes/{nodeId}/renditions", repoModel.getNodeRef());
            restWrapper.processEmptyModel(request);
        }
    }

    /**
     * Get  node rendition using GET call on '/nodes/{nodeId}/renditions/{renditionId}
     * 
     * @param renditionId id of rendition to be retrieved
     * @return
     * @throws Exception
     */   
    public RestRenditionInfoModel getNodeRendition(String renditionId) throws Exception
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET,  "nodes/{nodeId}/renditions/{renditionId}", repoModel.getNodeRef(), renditionId);       
        return restWrapper.processModel(RestRenditionInfoModel.class, request);
    }

    /**
     * Get node version rendition using GET call on '/nodes/{nodeId}/versions/{versionId}renditions/{renditionId}
     *
     * @param renditionId id of rendition to be retrieved
     * @param versionId versionId of the node
     * @return
     * @throws Exception
     */
    public RestRenditionInfoModel getNodeVersionRendition(String renditionId, String versionId) throws Exception
    {
        RestRequest request = RestRequest
                    .simpleRequest(HttpMethod.GET, "nodes/{nodeId}/versions/{versionId}/renditions/{renditionId}",
                                repoModel.getNodeRef(), versionId, renditionId);
        return restWrapper.processModel(RestRenditionInfoModel.class, request);
    }

    /**
     * Get node rendition using GET call on 'nodes/{nodeId}/renditions/{renditionId} Please note that it retries to get
     * the renditions response several times because on the alfresco server the rendition can take a while to be created.
     * 
     * @return
     * @throws Exception
     */
    public RestRenditionInfoModel getNodeRenditionUntilIsCreated(String renditionId) throws Exception
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "nodes/{nodeId}/renditions/{renditionId}",repoModel.getNodeRef(), renditionId);
        RestRenditionInfoModel renditions = restWrapper.processModel(RestRenditionInfoModel.class, request);
        int retry = 0;
        if (Integer.valueOf(restWrapper.getStatusCode()).equals(HttpStatus.OK.value()))
        {
            while (renditions.getStatus().equals("NOT_CREATED") && retry < Utility.retryCountSeconds - 8)
            {
                Utility.waitToLoopTime(1);
                renditions = restWrapper.processModel(RestRenditionInfoModel.class, request);
                retry++;
            }
        }
        return renditions;
    }

    /**
     * Get node version rendition using GET call on 'nodes/{nodeId}/versions/{versionId}/renditions/{renditionId} Please note that it retries to get
     * the renditions response several times because on the alfresco server the rendition can take a while to be created.
     *
     * @return
     * @throws Exception
     */
    public RestRenditionInfoModel getNodeVersionRenditionUntilIsCreated(String renditionId, String versionId) throws Exception
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "nodes/{nodeId}/versions/{versionId}/renditions/{renditionId}",repoModel.getNodeRef(), versionId, renditionId);
        RestRenditionInfoModel renditions = restWrapper.processModel(RestRenditionInfoModel.class, request);
        int retry = 0;
        if (Integer.valueOf(restWrapper.getStatusCode()).equals(HttpStatus.OK.value()))
        {
            while (renditions.getStatus().equals("NOT_CREATED") && retry < Utility.retryCountSeconds - 8)
            {
                Utility.waitToLoopTime(1);
                renditions = restWrapper.processModel(RestRenditionInfoModel.class, request);
                retry++;
            }
        }
        return renditions;
    }
    
    /**
     * Get node rendition content using GET call on
     * 'nodes/{nodeId}/renditions/{renditionId}/content Please note that it
     * retries to get the renditions response several times because on the
     * alfresco server the rendition can take a while to be created.
     * 
     * @return
     * @throws Exception
     */
    public RestResponse getNodeRenditionContentUntilIsCreated(String renditionId) throws Exception
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "nodes/{nodeId}/renditions/{renditionId}/content", repoModel.getNodeRef(),
                renditionId);
        RestResponse response = restWrapper.process(request);
        int retry = 0;
        while (Integer.valueOf(response.getStatusCode()).equals(HttpStatus.NOT_FOUND.value()) && retry < Utility.retryCountSeconds)
        {
            Utility.waitToLoopTime(1);
            response = restWrapper.process(request);
            retry++;
        }

        return response;
    }

    /**
     * Get node version rendition content using GET call on
     * 'nodes/{nodeId}/versions/{versionId}/renditions/{renditionId}/content Please note that it
     * retries to get the renditions response several times because on the
     * alfresco server the rendition can take a while to be created.
     *
     * @return
     * @throws Exception
     */
    public RestResponse getNodeVersionRenditionContentUntilIsCreated(String renditionId, String versionId) throws Exception
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "nodes/{nodeId}/versions/{versionId}/renditions/{renditionId}/content", repoModel.getNodeRef(),
                    versionId, renditionId);
        RestResponse response = restWrapper.process(request);
        int retry = 0;
        while (Integer.valueOf(response.getStatusCode()).equals(HttpStatus.NOT_FOUND.value()) && retry < Utility.retryCountSeconds)
        {
            Utility.waitToLoopTime(1);
            response = restWrapper.process(request);
            retry++;
        }

        return response;
    }

    /**
     * Get node rendition content using GET call on
     * 'nodes/{nodeId}/renditions/{renditionId}/content
     *
     * @return
     * @throws Exception
     */
    public RestResponse getNodeRenditionContent(String renditionId) throws Exception
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "nodes/{nodeId}/renditions/{renditionId}/content", repoModel.getNodeRef(),
                renditionId);
        return restWrapper.process(request);
    }

    /**
     * Get node version rendition content using GET call on
     * 'nodes/{nodeId}/versions/{versionId}/renditions/{renditionId}/content
     *
     * @return
     * @throws Exception
     */
    public RestResponse getNodeVersionRenditionContent(String renditionId, String versionId) throws Exception
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "nodes/{nodeId}/versions/{versionId}/renditions/{renditionId}/content", repoModel.getNodeRef(),
                    versionId, renditionId);
        return restWrapper.process(request);
    }

    /**
     * Get rendition information for available renditions for the node using GET call on
     * 'nodes/{nodeId}/renditions'
     * @return
     * @throws Exception
     */
    public RestRenditionInfoModelCollection getNodeRenditionsInfo() throws Exception
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "nodes/{nodeId}/renditions?{parameters}", repoModel.getNodeRef(),
                restWrapper.getParameters());
        return restWrapper.processModels(RestRenditionInfoModelCollection.class, request);
    }

    /**
     * Get rendition information for available renditions for the node version using GET call on
     * 'nodes/{nodeId}/versions/{versionId}/renditions'
     * @return
     * @throws Exception
     */
    public RestRenditionInfoModelCollection getNodeVersionRenditionsInfo(String versionId) throws Exception
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "nodes/{nodeId}/versions/{versionId}/renditions?{parameters}", repoModel.getNodeRef(),
                    versionId, restWrapper.getParameters());
        return restWrapper.processModels(RestRenditionInfoModelCollection.class, request);
    }


    /**
     * Get a node's children using GET call 'nodes/{nodeId}/children
     * 
     * @return a collection of nodes
     * @throws Exception
     */
    public RestNodeModelsCollection listChildren() throws Exception
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET,  "nodes/{nodeId}/children?{parameters}", repoModel.getNodeRef(), restWrapper.getParameters());
        return restWrapper.processModels(RestNodeModelsCollection.class, request);
    }

    /**
     * Move a node to a target folder
     * 
     * @param moveBody a {@link RestNodeBodyMoveCopyModel} containing at least the target parent id
     * @return the moved node's new information
     * @throws Exception
     */
    public RestNodeModel move(RestNodeBodyMoveCopyModel moveBody) throws Exception
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, moveBody.toJson(), "nodes/{nodeId}/move?{parameters}", repoModel.getNodeRef(), restWrapper.getParameters());
        return restWrapper.processModel(RestNodeModel.class, request);
    }

    /**
     * Copy a node to a target folder
     *
     * @param copyBody a {@link RestNodeBodyMoveCopyModel} containing at least the target parent id
     * @return the moved node's new information
     * @throws Exception
     */
    public RestNodeModel copy(RestNodeBodyMoveCopyModel copyBody) throws Exception
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, copyBody.toJson(),
                "nodes/{nodeId}/copy?{parameters}", repoModel.getNodeRef(), restWrapper.getParameters());
        return restWrapper.processModel(RestNodeModel.class, request);
    }


    /**
     * Lock a specific node using POST call on "nodes/{nodeId}/lock"
     * 
     * @return
     * @throws Exception
     */
    public RestNodeModel lockNode(RestNodeLockBodyModel lockBody) throws Exception
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, lockBody.toJson(), "nodes/{nodeId}/lock?{parameters}", repoModel.getNodeRef(), restWrapper.getParameters());
        return restWrapper.processModel(RestNodeModel.class, request);
    }

    /**
     * Unlock a specific node using POST call on "nodes/{nodeId}/unlock"
     * 
     * @return
     * @throws Exception
     */
    public RestNodeModel unlockNode() throws Exception
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.POST, "nodes/{nodeId}/unlock?{parameters}", repoModel.getNodeRef(), restWrapper.getParameters());
        return restWrapper.processModel(RestNodeModel.class, request);
    }

    /**
     * @return {@link NodesBuilder} - help you define new nodes using rest API calls
     */
    public NodesBuilder defineNodes()
    {
        return new NodesBuilder(restWrapper, this.repoModel);
    }

    /**
     * Update a specific node using PUT call on "nodes/{nodeId}"
     * 
     * @param putBody
     * @return
     * @throws Exception
     */
    public RestNodeModel updateNode(String putBody) throws Exception
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, putBody, "nodes/{nodeId}?{parameters}", repoModel.getNodeRef(), restWrapper.getParameters());
        request.setContentType("UTF-8");
        return restWrapper.processModel(RestNodeModel.class, request);
    }
    
    /**
     * Retrieve targets for a specific node using GET call on "nodes/{nodeId}/targets
     * 
     * @return
     * @throws Exception
     */
    public RestNodeAssociationModelCollection getNodeTargets() throws Exception
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "nodes/{nodeId}/targets?{parameters}", repoModel.getNodeRef(), restWrapper.getParameters());
        return restWrapper.processModels(RestNodeAssociationModelCollection.class, request);
    }

    /**
     * Create new target nodes using POST call on '/nodes/{nodeId}/targets'
     * 
     * @param target
     * @return
     * @throws Exception
     */
    public RestNodeAssocTargetModel createTargetForNode(RestNodeAssocTargetModel target) throws Exception
    {        
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, target.toJson(), "nodes/{nodeId}/targets?{parameters}", repoModel.getNodeRef(), restWrapper.getParameters());       
        return restWrapper.processModel(RestNodeAssocTargetModel.class, request);        
    }

    /**
     * Delete a target for a specific node using DELETE call on
     * nodes/{nodeId}/targets/{targetId}
     * 
     * @param target
     * @throws Exception
     */
    public void deleteTarget(RestNodeAssocTargetModel target) throws Exception
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, "nodes/{nodeId}/targets/{targetId}", repoModel.getNodeRef(),
                target.getTargetId());
        restWrapper.processEmptyModel(request);
    }

    /**
     * Get sources for a specific node using GET call on GET /nodes/{nodeId}/sources
     * 
     * @return
     * @throws Exception
     */
    public RestNodeAssociationModelCollection getNodeSources() throws Exception
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "/nodes/{nodeId}/sources?{parameters}", repoModel.getNodeRef(), restWrapper.getParameters());
        return restWrapper.processModels(RestNodeAssociationModelCollection.class, request);
    }

    /**
     * Updates the content of the node with identifier nodeId using PUT call "/nodes/{nodeId}/content"
     * 
     * @param nodeContent
     * @return
     * @throws Exception
     */
    public RestNodeModel updateNodeContent(File nodeContent) throws Exception
    {
        restWrapper.usingContentType(ContentType.BINARY);
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT,  Files.readFile(nodeContent), "nodes/{nodeId}/content?{parameters}", repoModel.getNodeRef(), restWrapper.getParameters());
        request.setContentType("UTF-8");
        restWrapper.usingContentType(ContentType.JSON);
        return restWrapper.processModel(RestNodeModel.class, request);
    }

    /**
     * Copies the node nodeId to the parent folder node targetParentId using POST call "nodes/{nodeId}/copy"
     * 
     * @param postBody
     * @return
     */
    public RestNodeModel copyNode(String postBody)
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, postBody, "nodes/{nodeId}/copy?{parameters}", repoModel.getNodeRef(), restWrapper.getParameters());
        return restWrapper.processModel(RestNodeModel.class, request);
    }

     /** 
     * Get a node's parents using GET call 'nodes/{nodeId}/parents
     * 
     * @return a collection of nodes
     * @throws Exception
     */
    public RestNodeAssociationModelCollection getParents() throws Exception
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET,  "nodes/{nodeId}/parents?{parameters}", repoModel.getNodeRef(), restWrapper.getParameters());
        return restWrapper.processModels(RestNodeAssociationModelCollection.class, request);
    }

    /**
     * Get a node's secondary children using GET call 'nodes/{nodeId}/secondary-children
     * 
     * @return a collection of nodes
     * @throws Exception
     */
    public RestNodeAssociationModelCollection getSecondaryChildren() throws Exception
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET,  "nodes/{nodeId}/secondary-children?{parameters}", repoModel.getNodeRef(), restWrapper.getParameters());
        return restWrapper.processModels(RestNodeAssociationModelCollection.class, request);
    }

    /**
     * Create secondary children association using POST call 'nodes/{nodeId}/secondary-children
     * Use a list of secondary children nodes
     * 
     * @return a collection of nodes
     * @throws Exception
     */
    public RestNodeChildAssocModelCollection createSecondaryChildren(String secondaryChildren) throws Exception
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, secondaryChildren, "nodes/{nodeId}/secondary-children?{parameters}", repoModel.getNodeRef(), restWrapper.getParameters());
        return restWrapper.processModels(RestNodeChildAssocModelCollection.class, request);
    }

    /**
     * Delete secondary children using DELETE call 'nodes/{nodeId}/secondary-children/{childId}
     * 
     * @return a collection of nodes
     * @throws Exception
     */
    public void deleteSecondaryChild(RestNodeAssociationModel child) throws Exception
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, "nodes/{nodeId}/secondary-children/{childId}?{parameters}", repoModel.getNodeRef(), child.getId(), restWrapper.getParameters());
        restWrapper.processEmptyModel(request);
    }

    /**
     * Gets the version history as an ordered list for the specified nodeId using GET call 'nodes/{nodeId}/versions
     * 
     * @return
     */
    public RestVersionModelsCollection listVersionHistory()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "nodes/{nodeId}/versions?{parameters}", repoModel.getNodeRef(), restWrapper.getParameters());
        return restWrapper.processModels(RestVersionModelsCollection.class, request);
    }

    /**
     * Delete the version identified by versionId for nodeId using DELETE call 'nodes/{nodeId}versions/{versionId}
     * 
     * @param versionId
     */
    public void deleteNodeVersion(String versionId)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, "nodes/{nodeId}/versions/{versionId}", repoModel.getNodeRef(), versionId);
        restWrapper.processEmptyModel(request);
    }

    /**
     * Gets the version information versionId for node nodeId using GET call 'nodes/{nodeId}/versions/{versionId}
     * 
     * @param versionId
     * @return
     */
    public RestVersionModel getVersionInformation(String versionId)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "nodes/{nodeId}/versions/{versionId}", repoModel.getNodeRef(), versionId);
        return restWrapper.processModel(RestVersionModel.class, request);
    }

    /**
     * Gets the content for versionId of node nodeId using GET call 'nodes/{nodeId}/versions/{versionId}/content
     * 
     * @param versionId
     * @return
     */
    public RestResponse getVersionContent(String versionId)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "nodes/{nodeId}/versions/{versionId}/content?{parameters}", repoModel.getNodeRef(), versionId, restWrapper.getParameters());
        return restWrapper.process(request);
    }

    /**
     * Revert the version identified by versionId and nodeId to the node using POST call 'nodes/{nodeId}/versions/{versionId}/revert
     * 
     * @param versionId
     * @param postBody
     * @return
     */
    public RestVersionModel revertVersion(String versionId, String postBody)
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, postBody, "nodes/{nodeId}/versions/{versionId}/revert?{parameters}", repoModel.getNodeRef(), versionId, restWrapper.getParameters());
        return restWrapper.processModel(RestVersionModel.class, request);
    }

    /**
     * Delete a specific node using DELETE call on nodes/{nodeId}
     *
     * @param nodeModel
     * @return
     * @throws Exception
     */
    public void deleteNode(RestNodeModel nodeModel) throws Exception
    {
        deleteNode(nodeModel.getId());
    }


    /**
     * Delete a specific node using DELETE call on nodes/{nodeId}
     *
     * @param nodeId
     * @return
     * @throws Exception
     */
    public void deleteNode(String nodeId) throws Exception
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, "nodes/{nodeId}", nodeId);
        restWrapper.processEmptyModel(request);
    }

    public RestActionDefinitionModelsCollection getActionDefinitions()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET,  "nodes/{nodeId}/action-definitions?{parameters}", repoModel.getNodeRef(), restWrapper.getParameters());
        return restWrapper.processModels(RestActionDefinitionModelsCollection.class, request);
        
    }

    /**
     * Get Direct Access URL for a node
     * @return
     */
    public RestResponse createDirectAccessURL()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.POST, "nodes/{nodeId}/request-content-url", this.repoModel.getNodeRef());
        return this.restWrapper.process(request);
    }

    /**
     * Get Direct Access URL for a specific node version. E.g "1.1"
     * @param versionId
     * @return
     */
    public RestResponse createDirectAccessURLforVersion(String versionId)
    {
        RestRequest request = RestRequest
                .simpleRequest(HttpMethod.POST, "nodes/{nodeId}/versions/{versionId}/request-content-url", this.repoModel.getNodeRef(), versionId);
        return this.restWrapper.process(request);
    }

    /**
     * Get Direct Access URL for a specific node version rendition. E.g ("1.1", "pdf")
     * @param versionId
     * @param renditionId
     * @return
     */
    public RestResponse createDirectAccessURLforVersionAndRendition(String versionId, String renditionId)
    {
        RestRequest request = RestRequest
                .simpleRequest(HttpMethod.POST, "nodes/{nodeId}/versions/{versionId}/renditions/{renditionId}/request-content-url", this.repoModel.getNodeRef(), versionId, renditionId);
        return this.restWrapper.process(request);
    }

    /**
     * Get Direct Access URL for a specific node rendition E.g "pdf"
     * @param renditionId
     * @return
     */
    public RestResponse createDirectAccessURLforRendition(String renditionId)
    {
        RestRequest request = RestRequest
                .simpleRequest(HttpMethod.POST, "nodes/{nodeId}/renditions/{renditionId}/request-content-url", this.repoModel.getNodeRef(), renditionId);
        return this.restWrapper.process(request);
    }

}