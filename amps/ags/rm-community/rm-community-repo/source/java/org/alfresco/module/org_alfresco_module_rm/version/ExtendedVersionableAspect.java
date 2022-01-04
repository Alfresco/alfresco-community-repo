/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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

package org.alfresco.module.org_alfresco_module_rm.version;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.util.AlfrescoTransactionSupport;
import org.alfresco.module.org_alfresco_module_rm.util.AuthenticationUtil;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Extend versionable aspect auto-version behaviour to allow versions to be
 * created when the content type is changed.
 * 
 * Note: this behaviour should be merged into core asap
 * 
 * @author Roy Wetherall
 * @since 2.3.1
 */
@BehaviourBean
public class ExtendedVersionableAspect implements NodeServicePolicies.OnSetNodeTypePolicy
{
    /** The i18n'ized messages */
    private static final String MSG_AUTO_VERSION = "create_version.auto_version";
    
    /** Transaction resource key */
    private static final String KEY_VERSIONED_NODEREFS = "versioned_noderefs";
    
    /** node service */
    private NodeService nodeService;
    
    /** version service */
    private VersionService versionService;
   
    /** lock service */
    private LockService lockService;
    
    /** alfresco transaction support */
    private AlfrescoTransactionSupport alfrescoTransactionSupport;
    
    /** authentication util */
    private AuthenticationUtil authenticationUtil;
    
    /** indicates whether auto version should be triggered on type change */
    private boolean isAutoVersionOnTypeChange = false;
    
    /**
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * @param versionService    version service
     */
    public void setVersionService(VersionService versionService)
    {
        this.versionService = versionService;
    }
    
    /**
     * @param lockService   lock service
     */
    public void setLockService(LockService lockService)
    {
        this.lockService = lockService;
    }
    
    /**
     * @param alfrescoTransactionSupport    alfresco transaction support
     */
    public void setAlfrescoTransactionSupport(AlfrescoTransactionSupport alfrescoTransactionSupport)
    {
        this.alfrescoTransactionSupport = alfrescoTransactionSupport;
    }
    
    /**
     * @param authenticationUtil    authentication util
     */
    public void setAuthenticationUtil(AuthenticationUtil authenticationUtil)
    {
        this.authenticationUtil = authenticationUtil;
    }
    
    /**
     * @param isAutoVersionOnTypeChange  true if auto version on type change, false otherwise
     */
    public void setAutoVersionOnTypeChange(boolean isAutoVersionOnTypeChange)
    {
        this.isAutoVersionOnTypeChange = isAutoVersionOnTypeChange;
    }
    
    /**
     * On set node type behaviour
     * 
     * @param nodeRef   node reference
     * @param oldType   old type
     * @param newType   new type
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    @Behaviour
    (
            type="cm:versionable",
            kind=BehaviourKind.CLASS,
            notificationFrequency=NotificationFrequency.TRANSACTION_COMMIT
    )
    public void onSetNodeType(NodeRef nodeRef, QName oldType, QName newType)
    {
        if (isAutoVersionOnTypeChange &&
            nodeService.exists(nodeRef) &&
            !lockService.isLockedAndReadOnly(nodeRef) &&
            nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE) &&
            !nodeService.hasAspect(nodeRef, ContentModel.ASPECT_TEMPORARY))
        {
            Map<NodeRef, NodeRef> versionedNodeRefs = (Map)alfrescoTransactionSupport.getResource(KEY_VERSIONED_NODEREFS);
            if (versionedNodeRefs == null || !versionedNodeRefs.containsKey(nodeRef))
            {
                // Determine whether the node is auto versionable (for content updates) or not
                boolean autoVersion = false;
                Boolean value = (Boolean)nodeService.getProperty(nodeRef, ContentModel.PROP_AUTO_VERSION);
                if (value != null)
                {
                    // If the value is not null then 
                    autoVersion = value.booleanValue();
                }
                
                // NOTE: auto version on type change is a global setting, if thins extension was moved into the 
                //       core then cm:versionable could be extended with a property consistent with the current
                //       implementation
                
                if (autoVersion)
                {
                    // Create the auto-version
                    Map<String, Serializable> versionProperties = new HashMap<>(1);
                    versionProperties.put(Version.PROP_DESCRIPTION, I18NUtil.getMessage(MSG_AUTO_VERSION));
                    
                    createVersionImpl(nodeRef, versionProperties);
                }
            }
        }       
    }
    
    /**
     * On create version implementation method.
     * 
     * @param nodeRef               node reference
     * @param versionProperties     version properties
     */
    private void createVersionImpl(final NodeRef nodeRef, final Map<String, Serializable> versionProperties)
    {
        authenticationUtil.runAsSystem(new RunAsWork<Void>() 
        {            
            @Override
            public Void doWork() throws Exception 
            {
                recordCreateVersion(nodeRef, null);
                versionService.createVersion(nodeRef, versionProperties);
                return null;
            }
        });        
    }
    
    /**
     * Record that the new version has been created
     * 
     * @param versionableNode   versionable node reference
     * @param version           version
     */
    @SuppressWarnings("unchecked")
    private void recordCreateVersion(NodeRef versionableNode, Version version) 
    {
        Map<NodeRef, NodeRef> versionedNodeRefs = (Map<NodeRef, NodeRef>)alfrescoTransactionSupport.getResource(KEY_VERSIONED_NODEREFS);
        if (versionedNodeRefs == null)
        {
            versionedNodeRefs = new HashMap<>();
            alfrescoTransactionSupport.bindResource(KEY_VERSIONED_NODEREFS, versionedNodeRefs);
        }
        versionedNodeRefs.put(versionableNode, versionableNode);
    }
}
