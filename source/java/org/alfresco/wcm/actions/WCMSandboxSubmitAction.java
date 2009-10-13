/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.wcm.actions;

import java.util.List;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.wcm.sandbox.SandboxService;

/**
 * WCM Submit example action (supercedes SimpleAVMSubmitAction)
 * 
 * Submit changed assets from a user sandbox to corresponding staging sandbox - either all or a list
 * 
 * The actionedUponNodeRef is a dummy and can be null.
 * 
 * @author janv
 */
public class WCMSandboxSubmitAction extends ActionExecuterAbstractBase
{
    public static String NAME = "wcm-submit";
    
    public static final String PARAM_PATH_LIST = "path-list";   // list of paths (relative to sandbox store)
    public static final String PARAM_SANDBOX_ID = "sandbox-id"; // sandbox store id
    
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
    @SuppressWarnings("unchecked")
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        String sbStoreId = (String)action.getParameterValue(PARAM_SANDBOX_ID);
        List<String> relativePaths = (List<String>)action.getParameterValue(PARAM_PATH_LIST);
        
        if ((relativePaths == null) || (relativePaths.size() == 0))
        {
            sbService.submitAll(sbStoreId, "Submit Action", "Submit all changed items in sandbox: " + sbStoreId);
        }
        else
        {
            sbService.submitList(sbStoreId, relativePaths, "Submit Action", "Submit list of changed items in sandbox: " + sbStoreId);
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        paramList.add(
                new ParameterDefinitionImpl(PARAM_PATH_LIST,
                                            DataTypeDefinition.ANY,
                                            false,
                                            getParamDisplayLabel(PARAM_PATH_LIST)));
        
        paramList.add(
                new ParameterDefinitionImpl(PARAM_SANDBOX_ID,
                                            DataTypeDefinition.TEXT,
                                            true,
                                            getParamDisplayLabel(PARAM_SANDBOX_ID)));
    }
}
