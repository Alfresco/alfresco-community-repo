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

package org.alfresco.rest.requests;

import static org.alfresco.rest.core.JsonBodyGenerator.arrayToJson;
import static org.alfresco.rest.requests.RuleSettings.IS_INHERITANCE_ENABLED;
import static org.springframework.http.HttpMethod.PUT;

import javax.json.JsonArrayBuilder;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import io.restassured.http.ContentType;
import org.alfresco.rest.core.JsonBodyGenerator;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestResponse;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.exception.JsonToModelConversionException;
import org.alfresco.rest.model.RestActionDefinitionModelsCollection;
import org.alfresco.rest.model.RestCategoryLinkBodyModel;
import org.alfresco.rest.model.RestCategoryModel;
import org.alfresco.rest.model.RestCategoryModelsCollection;
import org.alfresco.rest.model.RestCommentModel;
import org.alfresco.rest.model.RestCommentModelsCollection;
import org.alfresco.rest.model.RestNodeAssocTargetModel;
import org.alfresco.rest.model.RestNodeAssociationModel;
import org.alfresco.rest.model.RestNodeAssociationModelCollection;
import org.alfresco.rest.model.RestNodeBodyModel;
import org.alfresco.rest.model.RestNodeBodyMoveCopyModel;
import org.alfresco.rest.model.RestNodeChildAssocModelCollection;
import org.alfresco.rest.model.RestNodeModel;
import org.alfresco.rest.model.RestNodeModelsCollection;
import org.alfresco.rest.model.RestRatingModel;
import org.alfresco.rest.model.RestRatingModelsCollection;
import org.alfresco.rest.model.RestRenditionInfoModel;
import org.alfresco.rest.model.RestRenditionInfoModelCollection;
import org.alfresco.rest.model.RestRuleExecutionModel;
import org.alfresco.rest.model.RestRuleSetLinkModel;
import org.alfresco.rest.model.RestRuleSetModel;
import org.alfresco.rest.model.RestRuleSetModelsCollection;
import org.alfresco.rest.model.RestTagModel;
import org.alfresco.rest.model.RestTagModelsCollection;
import org.alfresco.rest.model.RestVersionModel;
import org.alfresco.rest.model.RestVersionModelsCollection;
import org.alfresco.rest.model.body.RestNodeLockBodyModel;
import org.alfresco.rest.model.builder.NodesBuilder;
import org.alfresco.utility.Utility;
import org.alfresco.utility.model.RepoTestModel;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.reporters.Files;

/**
 * Declares all Rest API under the /nodes path
 *
 */
public class Node extends ModelRequest<Node>
{
    private static final String RULE_SETS_URI = "nodes/{nodeId}/rule-sets";
    private static final String RULE_SET_BY_ID = RULE_SETS_URI + "/{ruleSetId}";

    private RepoTestModel repoModel;

    public Node(RestWrapper restWrapper)
    {
        super(restWrapper);
    }

    public Node(RepoTestModel repoModel, RestWrapper restWrapper)
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
    public RestNodeModel getNode()
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
    public RestCommentModelsCollection getNodeComments()
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
     */
    public RestCommentModel addComment(String commentContent)
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
     */
    public RestCommentModelsCollection addComments(String... comments)
    {
        JsonArrayBuilder array = JsonBodyGenerator.defineJSONArray();
        for (String comment : comments)
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
    public RestCommentModel updateComment(RestCommentModel commentModel, String commentContent)
    {
        String postBody = JsonBodyGenerator.keyValueJson("content", commentContent);
        RestRequest request = RestRequest.requestWithBody(PUT, postBody, "nodes/{nodeId}/comments/{commentId}?{parameters}", repoModel.getNodeRef(), commentModel.getId(), restWrapper.getParameters());
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
    public void deleteComment(RestCommentModel comment)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, "nodes/{nodeId}/comments/{commentId}", repoModel.getNodeRef(), comment.getId());
        restWrapper.processEmptyModel(request);
    }

    /**
     * Like a document using POST call on "nodes/{nodeId}/ratings"
     *
     * @return
     */
    public RestRatingModel likeDocument()
    {
        String postBody = JsonBodyGenerator.likeRating(true);
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, postBody, "nodes/{nodeId}/ratings", repoModel.getNodeRef());
        return restWrapper.processModel(RestRatingModel.class, request);
    }

    public RestRatingModel dislikeDocument()
    {
        String postBody = JsonBodyGenerator.likeRating(false);
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, postBody, "nodes/{nodeId}/ratings", repoModel.getNodeRef());
        return restWrapper.processModel(RestRatingModel.class, request);
    }

    /**
     * POST call on "nodes/{nodeId}/ratings" using an invalid rating body
     *
     * @return
     */
    public RestRatingModel addInvalidRating(String jsonBody)
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, jsonBody, "nodes/{nodeId}/ratings", repoModel.getNodeRef());
        return restWrapper.processModel(RestRatingModel.class, request);
    }

    /**
     * Add five star rate to a document using POST call on "nodes/{nodeId}/ratings"
     *
     * @param stars
     * @return
     */
    public RestRatingModel rateStarsToDocument(int stars)
    {
        String postBody = JsonBodyGenerator.fiveStarRating(stars);
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, postBody, "nodes/{nodeId}/ratings", repoModel.getNodeRef());
        return restWrapper.processModel(RestRatingModel.class, request);
    }

    /**
     * Retrieve node ratings using GET call on "nodes/{nodeId}/ratings"
     *
     * @return
     */
    public RestRatingModelsCollection getRatings()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "nodes/{nodeId}/ratings?{parameters}", repoModel.getNodeRef(), restWrapper.getParameters());
        return restWrapper.processModels(RestRatingModelsCollection.class, request);
    }

    /**
     * Delete like rating using DELETE call on "nodes/{nodeId}/ratings/{ratingId}"
     *
     * @return
     */
    public void deleteLikeRating()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, "nodes/{nodeId}/ratings/{ratingId}", repoModel.getNodeRef(), "likes");
        restWrapper.processEmptyModel(request);
    }

    /**
     * Try to delete invalid rating using DELETE call on "nodes/{nodeId}/ratings/{ratingId}"
     *
     * @return
     */
    public void deleteInvalidRating(String rating)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, "nodes/{nodeId}/ratings/{ratingId}", repoModel.getNodeRef(), rating);
        restWrapper.processEmptyModel(request);
    }

    /**
     * 
     * Get like rating of a document using GET call on "nodes/{nodeId}/ratings/{ratingId}"
     */
    public RestRatingModel getLikeRating()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "nodes/{nodeId}/ratings/{ratingId}?{parameters}", repoModel.getNodeRef(), "likes", restWrapper.getParameters());
        return restWrapper.processModel(RestRatingModel.class, request);
    }

    /**
     * Delete fivestar rating using DELETE call on "nodes/{nodeId}/ratings/{ratingId}"
     *
     * @return
     */
    public void deleteFiveStarRating()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, "nodes/{nodeId}/ratings/{ratingId}", repoModel.getNodeRef(), "fiveStar");
        restWrapper.processEmptyModel(request);
    }

    /**
     * 
     * Get fivestar rating of a document using GET call on "nodes/{nodeId}/ratings/{ratingId}"
     * @return
     */
    public RestRatingModel getFiveStarRating()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "nodes/{nodeId}/ratings/{ratingId}?{parameters}", repoModel.getNodeRef(), "fiveStar", restWrapper.getParameters());
        return restWrapper.processModel(RestRatingModel.class, request);
    }

    /**
     * Adds a tag to the given content node
     *
     * @param contentModel
     * @param tag
     * @return
     */
    public RestTagModel addTag(String tag)
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
     */
    public RestTagModelsCollection addTags(String... tags)
    {
        String postBody = "[";
        for (String tag : tags)
        {
            postBody += JsonBodyGenerator.keyValueJson("tag", tag) + ",";
        }
        postBody = postBody.substring(0, postBody.length() - 1) + "]";

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
    public void deleteTag(RestTagModel tag)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, "nodes/{nodeId}/tags/{tagId}", repoModel.getNodeRef(), tag.getId());
        restWrapper.processEmptyModel(request);
    }

    /**
     * Get node tags using GET call on 'nodes/{nodeId}/tags'
     *
     * @param tag
     * @return
     */
    public RestTagModelsCollection getNodeTags()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "nodes/{nodeId}/tags?{parameters}", repoModel.getNodeRef(), restWrapper.getParameters());
        return restWrapper.processModels(RestTagModelsCollection.class, request);
    }

    /**
     * Create new nodes using POST call on 'nodes/{nodeId}/children
     *
     * @param node
     * @return
     */
    public RestNodeModel createNode(RestNodeBodyModel node)
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
     */
    public RestNodeModel createNode()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.POST, "nodes/{nodeId}/children", repoModel.getNodeRef());
        return restWrapper.processModel(RestNodeModel.class, request);
    }

    /**
     * Retrieve content for a specific node using GET call on "nodes/{nodeId}/content"
     *
     * @return
     */
    public RestResponse getNodeContent()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "nodes/{nodeId}/content?{parameters}", repoModel.getNodeRef(), restWrapper.getParameters());
        return restWrapper.process(request);
    }

    /**
     * Retrieve content for a specific node using GET call on "nodes/{nodeId}/content"
     *
     * @return
     * @param nodeId
     */
    public RestResponse getNodeContent(String nodeId)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "nodes/{nodeId}/content?{parameters}", nodeId, restWrapper.getParameters());
        return restWrapper.process(request);
    }

    /**
     * Create node rendition using POST call on '/nodes/{nodeId}/renditions'
     *
     * @param renditionId id of rendition to be created
     * @return
     */
    public void createNodeRendition(String renditionId)
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
     */
    public void createNodeVersionRendition(String renditionId, String versionId)
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
     */
    public void createNodeRenditionIfNotExists(String renditionId)
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
     */
    public RestRenditionInfoModel getNodeRendition(String renditionId)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "nodes/{nodeId}/renditions/{renditionId}", repoModel.getNodeRef(), renditionId);
        return restWrapper.processModel(RestRenditionInfoModel.class, request);
    }

    /**
     * Get node version rendition using GET call on '/nodes/{nodeId}/versions/{versionId}renditions/{renditionId}
     *
     * @param renditionId id of rendition to be retrieved
     * @param versionId versionId of the node
     * @return
     */
    public RestRenditionInfoModel getNodeVersionRendition(String renditionId, String versionId)
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
     */
    public RestRenditionInfoModel getNodeRenditionUntilIsCreated(String renditionId)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "nodes/{nodeId}/renditions/{renditionId}", repoModel.getNodeRef(), renditionId);
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
     */
    public RestRenditionInfoModel getNodeVersionRenditionUntilIsCreated(String renditionId, String versionId)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "nodes/{nodeId}/versions/{versionId}/renditions/{renditionId}", repoModel.getNodeRef(), versionId, renditionId);
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
     */
    public RestResponse getNodeRenditionContentUntilIsCreated(String renditionId)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "nodes/{nodeId}/renditions/{renditionId}/content", repoModel.getNodeRef(),
                renditionId);
        RestResponse response = restWrapper.process(request);
        int retry = 0;
        //Multiplied by '8' because AI rendition test cases need more time (~30 seconds) - see ACS-2158
        while (!Integer.valueOf(response.getStatusCode()).equals(HttpStatus.OK.value()) && retry < (8 * Utility.retryCountSeconds))
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
     */
    public RestResponse getNodeVersionRenditionContentUntilIsCreated(String renditionId, String versionId)
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
     */
    public RestResponse getNodeRenditionContent(String renditionId)
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
     */
    public RestResponse getNodeVersionRenditionContent(String renditionId, String versionId)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "nodes/{nodeId}/versions/{versionId}/renditions/{renditionId}/content", repoModel.getNodeRef(),
                versionId, renditionId);
        return restWrapper.process(request);
    }

    /**
     * Get rendition information for available renditions for the node using GET call on
     * 'nodes/{nodeId}/renditions'
     * @return
     */
    public RestRenditionInfoModelCollection getNodeRenditionsInfo()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "nodes/{nodeId}/renditions?{parameters}", repoModel.getNodeRef(),
                restWrapper.getParameters());
        return restWrapper.processModels(RestRenditionInfoModelCollection.class, request);
    }

    /**
     * Get rendition information for available renditions for the node version using GET call on
     * 'nodes/{nodeId}/versions/{versionId}/renditions'
     * @return
     */
    public RestRenditionInfoModelCollection getNodeVersionRenditionsInfo(String versionId)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "nodes/{nodeId}/versions/{versionId}/renditions?{parameters}", repoModel.getNodeRef(),
                versionId, restWrapper.getParameters());
        return restWrapper.processModels(RestRenditionInfoModelCollection.class, request);
    }


    /**
     * Delete the rendition identified by renditionId using DELETE call on "/nodes/{nodeId}/renditions/{renditionId}"
     *
     * @param renditionId id of rendition to delete
     */
    public void deleteNodeRendition(String renditionId)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, "nodes/{nodeId}/renditions/{renditionId}", repoModel.getNodeRef(), renditionId);
        restWrapper.processEmptyModel(request);
    }

    /**
     * Get a node's children using GET call 'nodes/{nodeId}/children
     *
     * @return a collection of nodes
     */
    public RestNodeModelsCollection listChildren()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "nodes/{nodeId}/children?{parameters}", repoModel.getNodeRef(), restWrapper.getParameters());
        return restWrapper.processModels(RestNodeModelsCollection.class, request);
    }

    /**
     * Move a node to a target folder
     *
     * @param moveBody a {@link RestNodeBodyMoveCopyModel} containing at least the target parent id
     * @return the moved node's new information
     */
    public RestNodeModel move(RestNodeBodyMoveCopyModel moveBody)
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, moveBody.toJson(), "nodes/{nodeId}/move?{parameters}", repoModel.getNodeRef(), restWrapper.getParameters());
        return restWrapper.processModel(RestNodeModel.class, request);
    }

    /**
     * Copy a node to a target folder
     *
     * @param copyBody a {@link RestNodeBodyMoveCopyModel} containing at least the target parent id
     * @return the moved node's new information
     */
    public RestNodeModel copy(RestNodeBodyMoveCopyModel copyBody)
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, copyBody.toJson(),
                "nodes/{nodeId}/copy?{parameters}", repoModel.getNodeRef(), restWrapper.getParameters());
        return restWrapper.processModel(RestNodeModel.class, request);
    }


    /**
     * Lock a specific node using POST call on "nodes/{nodeId}/lock"
     *
     * @return
     */
    public RestNodeModel lockNode(RestNodeLockBodyModel lockBody)
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, lockBody.toJson(), "nodes/{nodeId}/lock?{parameters}", repoModel.getNodeRef(), restWrapper.getParameters());
        return restWrapper.processModel(RestNodeModel.class, request);
    }

    /**
     * Unlock a specific node using POST call on "nodes/{nodeId}/unlock"
     *
     * @return
     */
    public RestNodeModel unlockNode()
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
     */
    public RestNodeModel updateNode(String putBody)
    {
        RestRequest request = RestRequest.requestWithBody(PUT, putBody, "nodes/{nodeId}?{parameters}", repoModel.getNodeRef(), restWrapper.getParameters());
        request.setContentType("UTF-8");
        return restWrapper.processModel(RestNodeModel.class, request);
    }

    /**
     * Retrieve targets for a specific node using GET call on "nodes/{nodeId}/targets
     *
     * @return
     */
    public RestNodeAssociationModelCollection getNodeTargets()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "nodes/{nodeId}/targets?{parameters}", repoModel.getNodeRef(), restWrapper.getParameters());
        return restWrapper.processModels(RestNodeAssociationModelCollection.class, request);
    }

    /**
     * Create new target nodes using POST call on '/nodes/{nodeId}/targets'
     *
     * @param target
     * @return
     */
    public RestNodeAssocTargetModel createTargetForNode(RestNodeAssocTargetModel target)
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, target.toJson(), "nodes/{nodeId}/targets?{parameters}", repoModel.getNodeRef(), restWrapper.getParameters());
        return restWrapper.processModel(RestNodeAssocTargetModel.class, request);
    }

    /**
     * Delete a target for a specific node using DELETE call on
     * nodes/{nodeId}/targets/{targetId}
     *
     * @param target
     */
    public void deleteTarget(RestNodeAssocTargetModel target)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, "nodes/{nodeId}/targets/{targetId}", repoModel.getNodeRef(),
                target.getTargetId());
        restWrapper.processEmptyModel(request);
    }

    /**
     * Get sources for a specific node using GET call on GET /nodes/{nodeId}/sources
     *
     * @return
     */
    public RestNodeAssociationModelCollection getNodeSources()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "/nodes/{nodeId}/sources?{parameters}", repoModel.getNodeRef(), restWrapper.getParameters());
        return restWrapper.processModels(RestNodeAssociationModelCollection.class, request);
    }

    /**
     * Updates the content of the node with identifier nodeId using PUT call "/nodes/{nodeId}/content"
     *
     * @param nodeContent
     * @return
     */
    public RestNodeModel updateNodeContent(File nodeContent)
    {
        try
        {
            restWrapper.usingContentType(ContentType.BINARY);
            String body = Files.readFile(new FileInputStream(nodeContent));
            RestRequest request = RestRequest.requestWithBody(PUT, body, "nodes/{nodeId}/content?{parameters}", repoModel.getNodeRef(), restWrapper.getParameters());
            request.setContentType("UTF-8");
            restWrapper.usingContentType(ContentType.JSON);
            return restWrapper.processModel(RestNodeModel.class, request);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Unexpected error when reading content file.", e);
        }
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
     */
    public RestNodeAssociationModelCollection getParents()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "nodes/{nodeId}/parents?{parameters}", repoModel.getNodeRef(), restWrapper.getParameters());
        return restWrapper.processModels(RestNodeAssociationModelCollection.class, request);
    }

    /**
     * Get a node's secondary children using GET call 'nodes/{nodeId}/secondary-children
     *
     * @return a collection of nodes
     */
    public RestNodeAssociationModelCollection getSecondaryChildren()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "nodes/{nodeId}/secondary-children?{parameters}", repoModel.getNodeRef(), restWrapper.getParameters());
        return restWrapper.processModels(RestNodeAssociationModelCollection.class, request);
    }

    /**
     * Create secondary children association using POST call 'nodes/{nodeId}/secondary-children
     * Use a list of secondary children nodes
     *
     * @return a collection of nodes
     */
    public RestNodeChildAssocModelCollection createSecondaryChildren(String secondaryChildren)
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, secondaryChildren, "nodes/{nodeId}/secondary-children?{parameters}", repoModel.getNodeRef(), restWrapper.getParameters());
        return restWrapper.processModels(RestNodeChildAssocModelCollection.class, request);
    }

    /**
     * Delete secondary children using DELETE call 'nodes/{nodeId}/secondary-children/{childId}
     *
     * @return a collection of nodes
     */
    public void deleteSecondaryChild(RestNodeAssociationModel child)
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
     */
    public void deleteNode(RestNodeModel nodeModel)
    {
        deleteNode(nodeModel.getId());
    }


    /**
     * Delete a specific node using DELETE call on nodes/{nodeId}
     *
     * @param nodeId
     * @return
     */
    public void deleteNode(String nodeId)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, "nodes/{nodeId}", nodeId);
        restWrapper.processEmptyModel(request);
    }

    public RestActionDefinitionModelsCollection getActionDefinitions()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "nodes/{nodeId}/action-definitions?{parameters}", repoModel.getNodeRef(), restWrapper.getParameters());
        return restWrapper.processModels(RestActionDefinitionModelsCollection.class, request);

    }

    /**
     * Get Direct Access URL for a node
     * @param postBody
     * @return
     */
    public RestResponse createDirectAccessURL(String postBody)
    {
        RestRequest request;
        if (postBody == null)
        {
            request = RestRequest.simpleRequest(HttpMethod.POST, "nodes/{nodeId}/request-direct-access-url", this.repoModel.getNodeRef());
        }
        else
        {
            request = RestRequest.requestWithBody(HttpMethod.POST, postBody, "nodes/{nodeId}/request-direct-access-url", this.repoModel.getNodeRef());
        }

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
                .simpleRequest(HttpMethod.POST, "nodes/{nodeId}/renditions/{renditionId}/request-direct-access-url", this.repoModel.getNodeRef(), renditionId);
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
                .simpleRequest(HttpMethod.POST, "nodes/{nodeId}/versions/{versionId}/request-direct-access-url", this.repoModel.getNodeRef(), versionId);
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
                .simpleRequest(HttpMethod.POST, "nodes/{nodeId}/versions/{versionId}/renditions/{renditionId}/request-direct-access-url", this.repoModel.getNodeRef(), versionId, renditionId);
        return this.restWrapper.process(request);
    }

    public ContentStorageInformation usingStorageInfo(String contentPropName)
    {
        return new ContentStorageInformation(restWrapper)
                .withNodeId(repoModel.getNodeRef())
                .withContentPropName(contentPropName);
    }

    public ContentStorageInformation usingVersionStorageInfo(String contentPropName, String versionId)
    {
        return new ContentStorageInformation(restWrapper)
                .withNodeId(repoModel.getNodeRef())
                .withContentPropName(contentPropName)
                .withVersionId(versionId);
    }

    public FolderRules usingDefaultRuleSet()
    {
        return usingRuleSet("-default-");
    }

    public FolderRules usingRuleSet(String ruleSetId)
    {
        return new FolderRules(restWrapper)
                .withNodeId(repoModel.getNodeRef())
                .withRuleSetId(ruleSetId);
    }

    /**
     * Get the rule sets defined on a folder.
     *
     * @return The list of rule sets.
     */
    public RestRuleSetModelsCollection getListOfRuleSets()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, RULE_SETS_URI,
                repoModel.getNodeRef());
        return restWrapper.processModels(RestRuleSetModelsCollection.class, request);
    }

    /**
     * Get the specified rule set from a folder.
     *
     * @param ruleSetId The id of the rule set.
     * @return The specified rule set.
     */
    public RestRuleSetModel getRuleSet(String ruleSetId)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, RULE_SET_BY_ID,
                repoModel.getNodeRef(), ruleSetId);
        return restWrapper.processModel(RestRuleSetModel.class, request);
    }

    /**
     * Update a rule set on this folder - for example to reorder the rules.
     *
     * @param ruleSet The updated rule set.
     * @return The updated rule set returned by the server.
     */
    public RestRuleSetModel updateRuleSet(RestRuleSetModel ruleSet)
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, ruleSet.toJson(), RULE_SET_BY_ID,
                repoModel.getNodeRef(), ruleSet.getId());
        return restWrapper.processModel(RestRuleSetModel.class, request);
    }

    /**
     * Get the default rule set from a folder.
     *
     * @return The specified rule set.
     */
    public RestRuleSetModel getDefaultRuleSet()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, RULE_SET_BY_ID,
                repoModel.getNodeRef(), "-default-");
        return restWrapper.processModel(RestRuleSetModel.class, request);
    }

    public RuleSettings usingRuleSetting(String ruleSettingKey)
    {
        return new RuleSettings(restWrapper)
                .withNodeId(repoModel.getNodeRef())
                .withRuleSettingKey(ruleSettingKey);
    }

    public RuleSettings usingIsInheritanceEnabledRuleSetting()
    {
        return usingRuleSetting(IS_INHERITANCE_ENABLED);
    }

    public RestRuleSetLinkModel createRuleLink(RestRuleSetLinkModel body)
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, body.toJson(), "nodes/{nodeId}/rule-set-links", repoModel.getNodeRef());
        return restWrapper.processModel(RestRuleSetLinkModel.class, request);
    }

    /**
     * Try to delete a ruleset link performing a DELETE call on "/nodes/{folderNodeId}/rule-set-links/{rulesetId}"
     *
     * @param ruleSetId the id of the ruleset to be unlinked from the folder
     * @return
     */
    public void unlinkRuleSet(String ruleSetId)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, "nodes/{nodeId}/rule-set-links/{ruleSetId}", repoModel.getNodeRef(), ruleSetId);
        restWrapper.processEmptyModel(request);
    }

    /**
     * Trigger rules on a folder performing POST call on "/nodes/{folderNodeId}/rule-executions"
     *
     * @param body - rules execution request
     * @return execution result
     */
    public RestRuleExecutionModel executeRules(RestRuleExecutionModel body)
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, body.toJson(), "nodes/{nodeId}/rule-executions", repoModel.getNodeRef());
        return restWrapper.processModel(RestRuleExecutionModel.class, request);
    }

    /**
     * Get linked categories performing GET cal on "/nodes/{nodeId}/category-links"
     *
     * @return categories which are linked from content
     */
    public RestCategoryModelsCollection getLinkedCategories()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "nodes/{nodeId}/category-links", repoModel.getNodeRef());
        return restWrapper.processModels(RestCategoryModelsCollection.class, request);
    }

    /**
     * Link content to category performing POST call on "/nodes/{nodeId}/category-links"
     *
     * @param categoryLink - contains category ID
     * @return linked to category
     */
    public RestCategoryModel linkToCategory(RestCategoryLinkBodyModel categoryLink)
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, categoryLink.toJson(), "nodes/{nodeId}/category-links", repoModel.getNodeRef());
        return restWrapper.processModel(RestCategoryModel.class, request);
    }

    /**
     * Link content to many categories performing POST call on "/nodes/{nodeId}/category-links"
     *
     * @param categoryLinks - contains categories IDs
     * @return linked to categories
     */
    public RestCategoryModelsCollection linkToCategories(List<RestCategoryLinkBodyModel> categoryLinks)
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, arrayToJson(categoryLinks), "nodes/{nodeId}/category-links", repoModel.getNodeRef());
        return restWrapper.processModels(RestCategoryModelsCollection.class, request);
    }

    /**
     * Unlink content from a category performing a DELETE call on "nodes/{nodeId}/category-links/{categoryId}"
     *
     * @param categoryId the id of the category to be unlinked from content
     */
    public void unlinkFromCategory(String categoryId)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, "nodes/{nodeId}/category-links/{categoryId}", repoModel.getNodeRef(), categoryId);
        restWrapper.processEmptyModel(request);
    }
}
