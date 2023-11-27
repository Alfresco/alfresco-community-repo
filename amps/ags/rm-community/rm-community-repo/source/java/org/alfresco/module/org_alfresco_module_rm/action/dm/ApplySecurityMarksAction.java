/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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
public class ApplySecurityMarksAction extends ActionExecuterAbstractBase
{
    /** Logger */
    private static Log LOGGER = getLog(ApplySecurityMarksAction.class);

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

    }

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {

    }

}
