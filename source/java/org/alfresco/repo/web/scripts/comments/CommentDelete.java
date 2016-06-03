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
package org.alfresco.repo.web.scripts.comments;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class is the controller for the comment.delete web script.
 * 
 * @author Ramona Popa
 * @since 4.2.6
 */

public class CommentDelete extends AbstractCommentsWebScript
{

    private static Log logger = LogFactory.getLog(CommentDelete.class);

    /**
     * Overrides AbstractCommentsWebScript to delete comment
     */
    @Override
    protected Map<String, Object> executeImpl(NodeRef nodeRef, WebScriptRequest req, Status status, Cache cache)
    {
        String pageParams = req.getParameter(JSON_KEY_PAGE_PARAMS);

        JSONObject jsonPageParams = parseJSONFromString(pageParams);

        String parentNodeRefStr = getOrNull(jsonPageParams, JSON_KEY_NODEREF);
        NodeRef parentNodeRef = null;
        if (parentNodeRefStr != null)
        {
            parentNodeRef = new NodeRef((String) getOrNull(jsonPageParams, JSON_KEY_NODEREF));
        }

        if (parentNodeRef != null)
        {
            this.behaviourFilter.disableBehaviour(parentNodeRef, ContentModel.ASPECT_AUDITABLE);
        }

        try
        {
            // delete node
            deleteComment(nodeRef);

            if (nodeService.exists(nodeRef))
            {
                // comment was not removed
                status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR, "Unable to delete node: " + nodeRef);
                return null;
            }

            // generate response model for a comment node
            Map<String, Object> model = generateModel(nodeRef);

            // post an activity item - it is ok to send json as null since the
            // infos that we need are as parameters on request
            postActivity(null, req, parentNodeRef, COMMENT_DELETED_ACTIVITY);

            status.setCode(Status.STATUS_OK);
            return model;

        }
        finally
        {
            if (parentNodeRef != null)
            {
                this.behaviourFilter.enableBehaviour(parentNodeRef, ContentModel.ASPECT_AUDITABLE);
            }
        }
    }

    /**
     * deletes comment node
     * 
     * @param commentNodeRef
     */
    private void deleteComment(NodeRef commentNodeRef)
    {
        QName nodeType = nodeService.getType(commentNodeRef);
        if (!nodeType.equals(ForumModel.TYPE_POST))
        {
            throw new IllegalArgumentException("Node to delete is not a comment node.");
        }

        nodeService.deleteNode(commentNodeRef);
    }

    /**
     * generates model for delete comment script
     * 
     * @param commentNodeRef
     * @return
     */
    private Map<String, Object> generateModel(NodeRef commentNodeRef)
    {
        Map<String, Object> model = new HashMap<String, Object>(2, 1.0f);

        model.put(PARAM_MESSAGE, "Node " + commentNodeRef + " deleted");

        return model;
    }
}
