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
package org.alfresco.module.org_alfresco_module_rm.capability;

import net.sf.acegisecurity.vote.AccessDecisionVoter;

import org.alfresco.module.org_alfresco_module_rm.RecordsManagementService;
import org.alfresco.module.org_alfresco_module_rm.caveat.RMCaveatConfigComponent;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Roy Wetherall
 * @since 2.0
 */
public class RMSecurityCommon
{    
    protected int NOSET_VALUE = -100;
    
    private static Log logger = LogFactory.getLog(RMSecurityCommon.class);
    
    protected NodeService nodeService;
    protected PermissionService permissionService;
    protected RecordsManagementService rmService;
    protected RMCaveatConfigComponent caveatConfigComponent;
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }
    
    public void setRecordsManagementService(RecordsManagementService rmService)
    {
        this.rmService = rmService;
    }
    
    public void setCaveatConfigComponent(RMCaveatConfigComponent caveatConfigComponent)
    {
        this.caveatConfigComponent = caveatConfigComponent;
    }
    
    /**
     * 
     * @param prefix
     * @param nodeRef
     * @param value
     * @return
     */
    protected int setTransactionCache(String prefix, NodeRef nodeRef, int value)
    {
        String user = AuthenticationUtil.getRunAsUser();
        AlfrescoTransactionSupport.bindResource(prefix + nodeRef.toString() + user, Integer.valueOf(value));
        return value;        
    }
    
    /**
     * 
     * @param prefix
     * @param nodeRef
     * @return
     */
    protected int getTransactionCache(String prefix, NodeRef nodeRef)
    {
        int result = NOSET_VALUE;
        String user = AuthenticationUtil.getRunAsUser();
        Integer value = (Integer)AlfrescoTransactionSupport.getResource(prefix + nodeRef.toString() + user);
        if (value != null)
        {
            result = value.intValue();
        }
        return result;
    }
    
    /**
     * 
     * @param nodeRef
     * @return
     */
    public int checkRead(NodeRef nodeRef)
    {
        int result = AccessDecisionVoter.ACCESS_ABSTAIN;
        if (nodeRef != null)
        {
            // now we know the node - we can abstain for certain types and aspects (eg, rm)
            result = checkRead(nodeRef, false);
        }

        return result;
    }
    
    /**
     * 
     * @param nodeRef
     * @param allowDMRead
     * @return
     */
    public int checkRead(NodeRef nodeRef, boolean allowDMRead)
    {
        int result = AccessDecisionVoter.ACCESS_ABSTAIN;
        
        if (rmService.isFilePlanComponent(nodeRef) == true)
        {
            result = checkRmRead(nodeRef);
        }
        else if (allowDMRead == true)
        {
            // Check DM read for copy etc
            // DM does not grant - it can only deny
            if (permissionService.hasPermission(nodeRef, PermissionService.READ) == AccessStatus.DENIED)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("\t\tPermission is denied");
                    Thread.dumpStack();
                }
                result = AccessDecisionVoter.ACCESS_DENIED;
            }
            else
            {
                result =  AccessDecisionVoter.ACCESS_GRANTED;
            }
        }
        
        return result;            
    }  
    
    /**
     * 
     * @param nodeRef
     * @return
     */
    public int checkRmRead(NodeRef nodeRef)
    {           
        int result = getTransactionCache("checkRmRead", nodeRef);
        if (result != NOSET_VALUE)
        {
            return result;
        }
        
        // Get the file plan for the node
        NodeRef filePlan = rmService.getFilePlan(nodeRef);
        
        // Admin role
        if (permissionService.hasPermission(filePlan, RMPermissionModel.ROLE_ADMINISTRATOR) == AccessStatus.ALLOWED)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("\t\tAdmin access");
                Thread.dumpStack();
            }
            return setTransactionCache("checkRmRead", nodeRef, AccessDecisionVoter.ACCESS_GRANTED);            
        }

        if (permissionService.hasPermission(nodeRef, RMPermissionModel.READ_RECORDS) == AccessStatus.DENIED)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("\t\tPermission is denied");
                Thread.dumpStack();
            }
            return setTransactionCache("checkRmRead", nodeRef, AccessDecisionVoter.ACCESS_DENIED); 
        }

        if (permissionService.hasPermission(filePlan, RMPermissionModel.VIEW_RECORDS) == AccessStatus.DENIED)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("\t\tPermission is denied");
                Thread.dumpStack();
            }
            return setTransactionCache("checkRmRead", nodeRef, AccessDecisionVoter.ACCESS_DENIED); 
        }

        if (caveatConfigComponent.hasAccess(nodeRef))
        {
            return setTransactionCache("checkRmRead", nodeRef, AccessDecisionVoter.ACCESS_GRANTED); 
        }
        else
        {
            return setTransactionCache("checkRmRead", nodeRef, AccessDecisionVoter.ACCESS_DENIED); 
        }

    }

}
