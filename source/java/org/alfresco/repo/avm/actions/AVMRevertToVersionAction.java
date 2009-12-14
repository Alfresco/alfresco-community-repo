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
package org.alfresco.repo.avm.actions;

import java.util.List;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.surf.util.Pair;
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
        AVMNodeDescriptor toRevert = 
            (AVMNodeDescriptor)action.getParameterValue(TOREVERT);
        fAVMService.revert(versionPath.getSecond(), toRevert);
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
