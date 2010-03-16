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

import java.util.List;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.avm.util.AVMUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Revert a single path to a specified node. The path in head is passed
 * as actionedUponNodeRef.  The node to revert to is passed as an AVMNodeDescriptor
 * parameter.
 * 
 * TODO refactor and add to WCM services
 * 
 * @author britt
 * 
 * @deprecated
 */
public class AVMRevertToVersionAction extends ActionExecuterAbstractBase 
{
    @SuppressWarnings("unused")
    private static Log    fgLogger = LogFactory.getLog(AVMRevertToVersionAction.class);
    
    public static final String NAME = "avm-revert-to-version";
    // The node to revert to. Passed as an AVMNodeDescriptor.
    public static final String TOREVERT = "to-revert";
    
    private AVMService fAVMService;

    /**
     * Set the AVMService.
     */
    public void setAvmService(AVMService service)
    {
        fAVMService = service;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) 
    {
        Pair<Integer, String> versionPath = 
            AVMNodeConverter.ToAVMVersionPath(actionedUponNodeRef);
        AVMNodeDescriptor toRevertTo = 
            (AVMNodeDescriptor)action.getParameterValue(TOREVERT);
        fAVMService.revert(versionPath.getSecond(), toRevertTo);
        
        String[] storePath = AVMUtil.splitPath(versionPath.getSecond());
        fAVMService.createSnapshot(storePath[0], null, "Reverted "+versionPath.getSecond()+" to version "+toRevertTo.getVersionID());
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
    {
        paramList.add(
                new ParameterDefinitionImpl(TOREVERT,
                                            DataTypeDefinition.ANY,
                                            true,
                                            getParamDisplayLabel(TOREVERT)));
    }
}
