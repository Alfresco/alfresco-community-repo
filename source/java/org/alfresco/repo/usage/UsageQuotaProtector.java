/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.repo.usage;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.usage.ContentUsageService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Implements policies/behaviour for protecting system/admin-maintained person properties
 *
 */
public class UsageQuotaProtector implements NodeServicePolicies.OnUpdatePropertiesPolicy
{
    private AuthorityService authorityService;
    private AuthenticationService authenticationService;
    private PolicyComponent policyComponent;
    private ContentUsageService contentUsageService;
    
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }
    
    public void setAuthenticationService(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }
    
    public void setContentUsageService(ContentUsageService contentUsageService)
    {
        this.contentUsageService = contentUsageService;
    }
        
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }
    
    /**
     * The initialise method     
     */
    public void init()
    {    
        if (contentUsageService.getEnabled())
        {
            // Register interest in the onUpdateProperties policy
            policyComponent.bindClassBehaviour(
                    QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateProperties"), 
                    ContentModel.TYPE_PERSON, 
                    new JavaBehaviour(this, "onUpdateProperties"));
        }
    }    
    
    /**
     * Called after a node's properties have been changed.
     * 
     * @param nodeRef reference to the updated node
     * @param before the node's properties before the change
     * @param after the node's properties after the change 
     */
    public void onUpdateProperties(
            NodeRef nodeRef,
            Map<QName, Serializable> before,
            Map<QName, Serializable> after)
    {    
        Long sizeCurrentBefore = (Long)before.get(ContentModel.PROP_SIZE_CURRENT);
        Long sizeCurrentAfter = (Long)after.get(ContentModel.PROP_SIZE_CURRENT); 
            
        Long sizeQuotaBefore = (Long)before.get(ContentModel.PROP_SIZE_QUOTA);
        Long sizeQuotaAfter = (Long)after.get(ContentModel.PROP_SIZE_QUOTA); 
        
        // Check for change in sizeCurrent
        if ((sizeCurrentBefore != sizeCurrentAfter) && (! (authorityService.hasAdminAuthority() || authenticationService.isCurrentUserTheSystemUser())))
        {
            throw new AlfrescoRuntimeException("Update failed: protected property 'sizeCurrent'");
        }
        
        // Check for change in sizeQuota
        if ((sizeQuotaBefore != sizeQuotaAfter) && (! authorityService.hasAdminAuthority()))
        {
            throw new AlfrescoRuntimeException("Update failed: protected property 'sizeQuota'");
        }
    }
}
