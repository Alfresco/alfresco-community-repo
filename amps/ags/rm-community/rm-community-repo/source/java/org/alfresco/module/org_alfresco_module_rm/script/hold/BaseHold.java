/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.script.hold;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.hold.HoldService;
import org.alfresco.module.org_alfresco_module_rm.recordfolder.RecordFolderService;
import org.alfresco.module.org_alfresco_module_rm.util.NodeTypeUtility;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Base class for the hold webscripts
 *
 * @author Tuna Aksoy
 * @since 2.2
 */
public abstract class BaseHold extends DeclarativeWebScript
{
    /** Hold Service */
    private HoldService holdService;

    /** record folder service */
    private RecordFolderService recordFolderService;

    /** node service */
    private NodeService nodeService;

    /** Node type utility */
    private NodeTypeUtility nodeTypeUtility;

    /**
     * Set the hold service
     *
     * @param holdService the hold service
     */
    public void setHoldService(HoldService holdService)
    {
        this.holdService = holdService;
    }

    /**
     * @param recordFolderService   record folder service
     */
    public void setRecordFolderService(RecordFolderService recordFolderService)
    {
        this.recordFolderService = recordFolderService;
    }

    /**
     * @param nodeService node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param nodeTypeUtility node type utility
     */
    public void setNodeTypeUtility(NodeTypeUtility nodeTypeUtility)
    {
        this.nodeTypeUtility = nodeTypeUtility;
    }

    /**
     * Returns the hold service
     *
     * @return Returns the hold service
     */
    protected HoldService getHoldService()
    {
        return this.holdService;
    }

    /**
     * @see org.springframework.extensions.webscripts.DeclarativeWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.Status, org.springframework.extensions.webscripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        final JSONObject json = getJSONFromContent(req);
        final List<NodeRef> holds = getHolds(json);
        final List<NodeRef> nodeRefs = getItemNodeRefs(json);
        doAction(holds, nodeRefs);
        return new HashMap<>();
    }

    /**
     * Abstract method which will be implemented in the subclasses.
     * It will either add the item(s) to the hold(s) or remove it/them from the hold(s)
     *
     * @param holds List of hold {@link NodeRef}(s)
     * @param nodeRefs List of item {@link NodeRef}(s) (record(s) / record folder(s) / active content(s)) which will be
     *                 either added to the hold(s) or removed from the hold(s)
     */
    abstract void doAction(List<NodeRef> holds, List<NodeRef> nodeRefs);

    /**
     * Helper method the get the json object from the request
     *
     * @param req The webscript request
     * @return The json object which was sent in the request body
     */
    protected JSONObject getJSONFromContent(WebScriptRequest req)
    {
        JSONObject json = null;
        try
        {
            final String content = req.getContent().getContent();
            json = new JSONObject(new JSONTokener(content));
        }
        catch (IOException iox)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "Could not read content from req.", iox);
        }
        catch (JSONException je)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "Could not parse JSON from req.", je);
        }

        return json;
    }

    /**
     * Helper method to get the {@link NodeRef}s for the items(s) (record(s) / record folder(s) / active content(s))
     * which will be added to the hold(s)
     *
     * @param json The request content as JSON object
     * @return List of item {@link NodeRef}s which will be added to the hold(s)
     */
    protected List<NodeRef> getItemNodeRefs(JSONObject json)
    {
        final List<NodeRef> nodeRefs = new ArrayList<>();
        try
        {
            final JSONArray nodeRefsArray = json.getJSONArray("nodeRefs");
            for (int i = 0; i < nodeRefsArray.length(); i++)
            {
                NodeRef nodeReference = new NodeRef(nodeRefsArray.getString(i));
                checkItemNodeRef(nodeReference);
                nodeRefs.add(nodeReference);
            }
        }
        catch (JSONException je)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "Could not get information from the json array.", je);
        }

        return nodeRefs;
    }

    /**
     * Helper method for checking the node reference for an item
     *
     * @param nodeRef The {@link NodeRef} of an item (record / record folder / active content)
     */
    private void checkItemNodeRef(NodeRef nodeRef)
    {
        // ensure that the node exists
        if (!nodeService.exists(nodeRef))
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Item being added to hold does not exist.");
        }

        // ensure that the node we are adding to the hold is a record or record folder or active content
        if (!recordFolderService.isRecordFolder(nodeRef) &&
                !nodeTypeUtility.instanceOf(nodeService.getType(nodeRef), ContentModel.TYPE_CONTENT))
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Items added to a hold must be either a record, a record folder or active content.");
        }
    }

    /**
     * Helper method to get the list of {@link NodeRef}(s) for the hold(s) which will contain the item (record / record folder / active content)
     *
     * @param json The request content as JSON object
     * @return List of {@link NodeRef}(s) of the hold(s)
     */
    protected List<NodeRef> getHolds(JSONObject json)
    {
        final List<NodeRef> holds = new ArrayList<>();
        try
        {
            final JSONArray holdsArray = json.getJSONArray("holds");
            for (int i = 0; i < holdsArray.length(); i++)
            {
                final NodeRef nodeRef = new NodeRef(holdsArray.getString(i));
                checkHoldNodeRef(nodeRef);
                holds.add(nodeRef);
            }
        }
        catch (JSONException je)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "Could not get information from the json array.", je);
        }

        return holds;
    }

    /**
     * Helper method for checking the node reference for a hold
     *
     * @param nodeRef The {@link NodeRef} of a hold
     */
    private void checkHoldNodeRef(NodeRef nodeRef)
    {
        // check the hold exists
        if (!nodeService.exists(nodeRef))
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "The hold does not exist.");
        }

        // check the noderef is actually a hold
        if (!holdService.isHold(nodeRef))
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Items are being added to a node that isn't a hold.");
        }
    }
}
