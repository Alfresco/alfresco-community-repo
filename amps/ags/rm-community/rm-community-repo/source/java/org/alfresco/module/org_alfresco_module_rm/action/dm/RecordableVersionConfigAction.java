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

package org.alfresco.module.org_alfresco_module_rm.action.dm;

import static org.alfresco.model.ContentModel.ASPECT_VERSIONABLE;
import static org.alfresco.model.ContentModel.TYPE_CONTENT;
import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.ASPECT_RECORD;
import static org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionModel.PROP_RECORDABLE_VERSION_POLICY;
import static org.alfresco.service.cmr.dictionary.DataTypeDefinition.TEXT;
import static org.apache.commons.logging.LogFactory.getLog;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionPolicy;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;

/**
 * Sets the recordable version config for a document within a collaboration site.
 *
 * Note: This is a 'normal' dm action, rather than a records management action.
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public class RecordableVersionConfigAction extends ActionExecuterAbstractBase
{
    /** Logger */
    private static Log LOGGER = getLog(RecordableVersionConfigAction.class);

    /** Action name */
    public static final String NAME = "recordable-version-config";

    /** Parameter names */
    public static final String PARAM_VERSION = "version";

    /** Node service */
    private NodeService nodeService;

    /** Dictionary service */
    private DictionaryService dictionaryService;

    /**
     * Gets the node service
     *
     * @return The node service
     */
    protected NodeService getNodeService()
    {
        return this.nodeService;
    }

    /**
     * Sets the node service
     *
     * @param nodeService The node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Gets the dictionary service
     *
     * @return The dictionary service
     */
    protected DictionaryService getDictionaryService()
    {
        return this.dictionaryService;
    }

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#setDictionaryService(org.alfresco.service.cmr.dictionary.DictionaryService)
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        if (passedChecks(actionedUponNodeRef))
        {
            String version = (String) action.getParameterValue(PARAM_VERSION);
            getNodeService().setProperty(actionedUponNodeRef, PROP_RECORDABLE_VERSION_POLICY, RecordableVersionPolicy.valueOf(version));
        }
    }

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        paramList.add(new ParameterDefinitionImpl(PARAM_VERSION, TEXT, true, getParamDisplayLabel(PARAM_VERSION), false, "ac-versions"));
    }

    /**
     * Helper method to do checks on the actioned upon node reference
     *
     * @param actionedUponNodeRef The actioned upon node reference
     * @return <code>true</code> if the actioned upon node reference passes the checks, <code>false</code> otherwise
     */
    private boolean passedChecks(NodeRef actionedUponNodeRef)
    {
        boolean passedChecks = true;

        if (!getNodeService().exists(actionedUponNodeRef))
        {
            passedChecks = false;
            if (LOGGER.isDebugEnabled())
            {
                String message = buildLogMessage(actionedUponNodeRef, "' because the node does not exist.");
                LOGGER.debug(message);
            }
        }

        QName type = getNodeService().getType(actionedUponNodeRef);
        if (!getDictionaryService().isSubClass(type, TYPE_CONTENT))
        {
            passedChecks = false;
            if (LOGGER.isDebugEnabled())
            {
                String message = buildLogMessage(actionedUponNodeRef, "' because the type of the node '" + type.getLocalName()  + "' is not supported.");
                LOGGER.debug(message);
            }
        }

        if (getNodeService().hasAspect(actionedUponNodeRef, ASPECT_RECORD))
        {
            passedChecks = false;
            if (LOGGER.isDebugEnabled())
            {
                String message = buildLogMessage(actionedUponNodeRef, "' because the rule cannot be applied to records.");
                LOGGER.debug(message);
            }
        }

        if (!getNodeService().hasAspect(actionedUponNodeRef, ASPECT_VERSIONABLE))
        {
            passedChecks = false;
            if (LOGGER.isDebugEnabled())
            {
                String buildLogMessage = buildLogMessage(actionedUponNodeRef, "' because the rule cannot be applied to records.");
                LOGGER.debug(buildLogMessage);
            }
        }

        return passedChecks;
    }

    /**
     * Helper method to construct log message
     *
     * @param actionedUponNodeRef The actioned upon node reference
     * @param messagePart The message which should be appended.
     * @return The constructed log message
     */
    private String buildLogMessage(NodeRef actionedUponNodeRef, String messagePart)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Cannot set recordable version config for '");
        sb.append(actionedUponNodeRef.toString());
        sb.append(messagePart);
        return sb.toString();
    }
}
