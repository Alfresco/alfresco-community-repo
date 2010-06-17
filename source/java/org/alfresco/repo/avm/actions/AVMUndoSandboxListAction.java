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
import org.alfresco.service.cmr.avm.locking.AVMLockingService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;
import org.alfresco.wcm.util.WCMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Undos a list of changed nodes in a user sandbox. The set of nodes to undo is
 * passed in as a packed string (Obtained by VersionPathStuffer).
 * The actionedUponNodeRef is a dummy and can be null.
 * @author britt
 * 
 * @deprecated see org.alfresco.wcm.actions.WCMSandboxRevertListAction or org.alfresco.wcm.SandboxService.revert
 */
public class AVMUndoSandboxListAction extends ActionExecuterAbstractBase 
{
    private static Log    fgLogger = LogFactory.getLog(AVMUndoSandboxListAction.class);
    
    public static final String NAME = "avm-undo-list";
    // The encoded list of nodes.
    public static final String PARAM_NODE_LIST = "node-list";
    
    /**
     * The AVM Service reference.
     */
    private AVMService fAVMService;
   
    /**
     * The AVM Locking Service reference.
     */
    private AVMLockingService fAVMLockingService;
    
    public void setAvmService(AVMService service)
    {
        fAVMService = service;
    }

    public void setAvmLockingService(AVMLockingService service)
    {
        fAVMLockingService = service;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) 
    {
        List<Pair<Integer, String>> versionPaths = 
            (List<Pair<Integer, String>>)action.getParameterValue(PARAM_NODE_LIST);
        for (Pair<Integer, String> item : versionPaths)
        {
            String avmPath = item.getSecond();
            AVMNodeDescriptor desc = fAVMService.lookup(-1, avmPath, true);
            if (desc == null)
            {
                continue;
            }
            String [] parentChild = AVMNodeConverter.SplitBase(avmPath);
            if (parentChild.length != 2)
            {
                continue;
            }
            AVMNodeDescriptor parent = fAVMService.lookup(-1, parentChild[0], true);
            if (parent.isLayeredDirectory())
            {
                if (fgLogger.isDebugEnabled())
                   fgLogger.debug("reverting " + parentChild[1] + " in " + parentChild[0]);
                fAVMService.makeTransparent(parentChild[0], parentChild[1]);
            }
            
            if (desc.isFile() || desc.isDeletedFile())
            {
                String parts[] = AVMUtil.splitPath(avmPath);
                String avmStore = parts[0];
                String path = parts[1]; // store relative path
                
                String webProject = WCMUtil.getWebProject(fAVMService, avmStore);
                if (webProject != null)
                {
                    if (fgLogger.isDebugEnabled())
                    {
                        fgLogger.debug("unlocking file " + path + " in web project " + webProject);
                    }
                    
                    if (fAVMLockingService.getLockOwner(webProject, path) != null)
                    {
                        fAVMLockingService.removeLock(webProject, path);
                    }
                    else
                    {
                        fgLogger.warn("expected file " + path + " in " + webProject + " to be locked");
                    }
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
    {
        paramList.add(
                new ParameterDefinitionImpl(PARAM_NODE_LIST,
                                            DataTypeDefinition.ANY,
                                            true,
                                            getParamDisplayLabel(PARAM_NODE_LIST)));
    }
}
