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
package org.alfresco.wcm.actions;

import java.util.List;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;
import org.alfresco.wcm.sandbox.SandboxService;
import org.alfresco.wcm.util.WCMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * WCM Revert Snapshot example action (supercedes AVMRevertStoreAction)
 * 
 * Reverts (staging) sandbox to a specified snapshot version.
 * 
 * @author janv
 */
public class WCMSandboxRevertSnapshotAction extends ActionExecuterAbstractBase 
{
    private static Log logger = LogFactory.getLog(WCMSandboxRevertSnapshotAction.class);
    
    public static final String NAME = "wcm-revert-snapshot";
    public static final String PARAM_VERSION = "version";
    
    /**
     * The WCM SandboxService
     */
    private SandboxService sbService;
    
    public void setSandboxService(SandboxService sbService)
    {
        this.sbService = sbService;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) 
    {
        // All this does is an override submit from the older version
        // to head of the store implied in the path.
        Pair<Integer, String> pathVersion = 
            AVMNodeConverter.ToAVMVersionPath(actionedUponNodeRef);
        
        if (pathVersion.getFirst() != -1)
        {
            logger.warn("Ignored version "+pathVersion.getFirst()+" for "+actionedUponNodeRef+" (will revert latest)");
        }
        
        int revertVersion = (Integer)action.getParameterValue(PARAM_VERSION);
        String sbStoreId = WCMUtil.getSandboxStoreId(pathVersion.getSecond());
        
        sbService.revertSnapshot(sbStoreId, revertVersion);
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
    }
}
