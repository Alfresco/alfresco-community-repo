/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.avm.actions;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This action handles reverting a selected set of nodes to a particular version.
 * The actionedUponNodeRef is a dummy and can be null.
 * @author britt
 * 
 * @deprecated
 */
public class AVMRevertListAction extends ActionExecuterAbstractBase 
{
    @SuppressWarnings("unused")
    private static Log    fgLogger = LogFactory.getLog(AVMRevertListAction.class);
    
    public static final String NAME = "avm-revert-list";
    // The version to revert to.
    public static final String PARAM_VERSION = "version";
    // The encoded list of nodes.
    public static final String PARAM_NODE_LIST = "node-list";
    // Flag for whether we should flatten after revert.
    
    /**
     * The sync service.
     */
    private AVMSyncService fSyncService;
    
    /**
     * Set the sync service.
     */
    public void setAvmSyncService(AVMSyncService service)
    {
        fSyncService = service;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) 
    {
        int revertVersion = (Integer)action.getParameterValue(PARAM_VERSION);
        List<String> paths = 
            (List<String>)action.getParameterValue(PARAM_NODE_LIST);
        List<AVMDifference> diffs = new ArrayList<AVMDifference>();
        for (String path : paths)
        {
            List<AVMDifference> diffSet = 
                fSyncService.compare(revertVersion, path, 
                                     -1, path, null);
            diffs.addAll(diffSet);
        }
        String message = "Reverted to version " + revertVersion;
        fSyncService.update(diffs, null, false, false, true, true, message, message);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
    {
        paramList.add(
            new ParameterDefinitionImpl(PARAM_VERSION,
                                        DataTypeDefinition.INT,
                                        true,
                                        getParamDisplayLabel(PARAM_VERSION)));
        paramList.add(
            new ParameterDefinitionImpl(PARAM_NODE_LIST,
                                        DataTypeDefinition.ANY,
                                        true,
                                        getParamDisplayLabel(PARAM_NODE_LIST)));
    }
}
