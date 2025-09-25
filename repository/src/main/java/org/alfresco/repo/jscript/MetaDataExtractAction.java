/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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
/*
 * Copyright (C) 2005 Jesper Steen Møller
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.jscript;

import org.apache.commons.lang3.Strings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.evaluator.CompareContentConditionEvaluator;
import org.alfresco.repo.forms.FormData;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * JavaScript wrapper for the "extract-metadata" action.
 * <p>
 * This class provides a scriptable interface to trigger metadata extraction actions within the Alfresco repository.</br>
 * It is similar to {@link Actions} class but is dedicated to metadata extraction functionality.
 *
 * </br>
 *
 * @author Sayan Bhattacharya
 */
public final class MetaDataExtractAction extends BaseScopableProcessorExtension
{
    private static final Log LOG = LogFactory.getLog(MetaDataExtractAction.class);

    private final static String ACTION_NAME = "extract-metadata";

    private ContentService contentService;

    private ServiceRegistry services;

    /**
     * Set the service registry
     *
     * @param serviceRegistry
     *            the service registry
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.services = serviceRegistry;
    }

    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    /**
     * Create a new metadata extraction action instance
     *
     * @param setActionContext
     *            if true, sets the action context to "scriptaction".
     * @return the newly created action
     */

    public ScriptAction create(boolean isContentChanged)
    {
        ScriptAction scriptAction = null;
        ActionService actionService = services.getActionService();
        ActionDefinition actionDef = actionService.getActionDefinition(ACTION_NAME);
        if (actionDef != null)
        {
            Action action = actionService.createAction(ACTION_NAME);

            ActionCondition actionCondition = actionService.createActionCondition(CompareContentConditionEvaluator.NAME);
            actionCondition.setParameterValue(CompareContentConditionEvaluator.PARAM_IS_CONTENT_CHANGED, isContentChanged);
            action.addActionCondition(actionCondition);

            scriptAction = new ScriptAction(this.services, action, actionDef);
            scriptAction.setScope(getScope());
        }
        return scriptAction;
    }

    /**
     * Check if the content has been updated in the form data compared to the existing content of the node.
     *
     * @param itemId
     * @param formData
     * @return true if title or description has changed, false otherwise
     */
    public boolean isContentChanged(String itemId, FormData formData)
    {

        try
        {
            NodeRef nodeRef = NodeRef.isNodeRef(itemId) ? new NodeRef(itemId) : parseNodeRef(itemId);
            if (nodeRef == null)
            {
                return false;
            }

            ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
            String contentString = reader.getContentString();
            FormData.FieldData fieldData = formData.getFieldData("prop_cm_content");

            if (fieldData == null || fieldData.getValue() == null)
            {
                return false;
            }

            String propCmContent = String.valueOf(fieldData.getValue());
            return !Strings.CS.equals(contentString, propCmContent);
        }
        catch (Exception e)
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Unable to determine if content has changed for node: " + itemId, e);
            }
            return false;
        }
    }

    private NodeRef parseNodeRef(String itemId)
    {
        String[] parts = itemId.split("/");
        return (parts.length == 3) ? new NodeRef(parts[0], parts[1], parts[2]) : null;
    }

}
