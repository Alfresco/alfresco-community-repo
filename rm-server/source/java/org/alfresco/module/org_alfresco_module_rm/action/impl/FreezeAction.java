/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.action.impl;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Freeze Action
 * 
 * @author Roy Wetherall
 */
public class FreezeAction extends RMActionExecuterAbstractBase
{
    private static final String MSG_FREEZE_NO_REASON = "rm.action.freeze-no-reason";
    private static final String MSG_FREEZE_ONLY_RECORDS_FOLDERS = "rm.action.freeze-only-records-folders";
    
    /** Logger */
    private static Log logger = LogFactory.getLog(FreezeAction.class);

    /** Parameter names */
    public static final String PARAM_REASON = "reason";
    
    /** Hold node reference key */
    private static final String KEY_HOLD_NODEREF = "holdNodeRef";
    
    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        final boolean isRecord = recordsManagementService.isRecord(actionedUponNodeRef);
        final boolean isFolder = this.recordsManagementService.isRecordFolder(actionedUponNodeRef);
        
        if (isRecord || isFolder)
        {
            // Get the property values
            String reason = (String)action.getParameterValue(PARAM_REASON);
            if (reason == null || reason.length() == 0)
            {
                throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_FREEZE_NO_REASON));
            }
            
            if (logger.isDebugEnabled())
            {
                StringBuilder msg = new StringBuilder();
                msg.append("Freezing node ").append(actionedUponNodeRef);
                if (isFolder)
                {
                    msg.append(" (folder)");
                }
                msg.append(" with reason '").append(reason).append("'");
                logger.debug(msg.toString());
            }

            // Get the root rm node
            NodeRef root = this.recordsManagementService.getFilePlan(actionedUponNodeRef);
            
            // Get the hold object
            NodeRef holdNodeRef = (NodeRef)AlfrescoTransactionSupport.getResource(KEY_HOLD_NODEREF);
            
            if (holdNodeRef == null)
            {
                // Calculate a transfer name
                QName nodeDbid = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "node-dbid");
                Long dbId = (Long)this.nodeService.getProperty(actionedUponNodeRef, nodeDbid);
                String transferName = padString(dbId.toString(), 10);

                // Create the hold object
                Map<QName, Serializable> holdProps = new HashMap<QName, Serializable>(2);
                holdProps.put(ContentModel.PROP_NAME, transferName);
                holdProps.put(PROP_HOLD_REASON, reason);
                final QName transferQName = QName.createQName(RM_URI, transferName);
                holdNodeRef = this.nodeService.createNode(root, 
                                                          ASSOC_HOLDS,
                                                          transferQName, 
                                                          TYPE_HOLD,
                                                          holdProps).getChildRef();
                
                if (logger.isDebugEnabled())
                {
                    StringBuilder msg = new StringBuilder();
                    msg.append("Created hold object ").append(holdNodeRef)
                        .append(" with transfer name ").append(transferQName);
                    logger.debug(msg.toString());
                }
                
                // Bind the hold node reference to the transaction
                AlfrescoTransactionSupport.bindResource(KEY_HOLD_NODEREF, holdNodeRef);
            }
                
            // Link the record to the hold
            this.nodeService.addChild(  holdNodeRef, 
                                        actionedUponNodeRef, 
                                        ASSOC_FROZEN_RECORDS, 
                                        ASSOC_FROZEN_RECORDS);

            // Apply the freeze aspect
            Map<QName, Serializable> props = new HashMap<QName, Serializable>(2);
            props.put(PROP_FROZEN_AT, new Date());
            props.put(PROP_FROZEN_BY, AuthenticationUtil.getFullyAuthenticatedUser());
            this.nodeService.addAspect(actionedUponNodeRef, ASPECT_FROZEN, props);
            
            if (logger.isDebugEnabled())
            {
                StringBuilder msg = new StringBuilder();
                msg.append("Frozen aspect applied to ").append(actionedUponNodeRef);
                logger.debug(msg.toString());
            }

                        
            // Mark all the folders contents as frozen
            if (isFolder)
            {
                List<NodeRef> records = this.recordsManagementService.getRecords(actionedUponNodeRef);
                for (NodeRef record : records)
                {
                    this.nodeService.addAspect(record, ASPECT_FROZEN, props);

                    if (logger.isDebugEnabled())
                    {
                        StringBuilder msg = new StringBuilder();
                        msg.append("Frozen aspect applied to ").append(record);
                        logger.debug(msg.toString());
                    }
                }
            }
        }
        else
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_FREEZE_ONLY_RECORDS_FOLDERS));
        }        
    }    
    
    @Override
    public Set<QName> getProtectedAspects()
    {
        HashSet<QName> qnames = new HashSet<QName>();
        qnames.add(ASPECT_FROZEN);
        return qnames;
    }

    @Override
    public Set<QName> getProtectedProperties()
    {
        HashSet<QName> qnames = new HashSet<QName>();
        qnames.add(PROP_HOLD_REASON);
        //TODO Add prop frozen at/by?
        return qnames;
    }

    @Override
    protected boolean isExecutableImpl(NodeRef filePlanComponent, Map<String, Serializable> parameters, boolean throwException)
    {
        if (this.recordsManagementService.isRecord(filePlanComponent) == true ||
                this.recordsManagementService.isRecordFolder(filePlanComponent) == true)
        {
            // Get the property values
            if(parameters != null)
            {
                String reason = (String)parameters.get(PARAM_REASON);
                if (reason == null || reason.length() == 0)
                {
                    if(throwException)
                    {
                        throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_FREEZE_NO_REASON));
                    }
                    else
                    {
                        return false;
                    }
                }
            }
            return true;
        }
        else
        {
            if(throwException)
            {
                throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_FREEZE_ONLY_RECORDS_FOLDERS));
            }
            else
            {
                return false;
            }
        }        
    }

    
}