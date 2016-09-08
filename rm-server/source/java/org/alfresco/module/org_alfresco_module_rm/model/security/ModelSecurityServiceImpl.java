/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.model.security;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;


/**
 * Model security service implementation.
 * <p>
 * This service records the protected properties and aspects, ensuring that only those with the appropriate capabilities can edit them.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public class ModelSecurityServiceImpl implements ModelSecurityService,
                                                 RecordsManagementModel,
                                                 NodeServicePolicies.BeforeAddAspectPolicy,
                                                 NodeServicePolicies.BeforeRemoveAspectPolicy,
                                                 NodeServicePolicies.OnUpdatePropertiesPolicy
{
    /** Indicates whether model security is enabled or not */
    private boolean enabled = true;

    /** Policy component */
    private PolicyComponent policyComponent;

    /** Node service */
    private NodeService nodeService;

    /** Namespace service */
    private NamespaceService namespaceService;
    
    /** File plan service */
    private FilePlanService filePlanService;

    /** Map of protected properties keyed by name */
    private Map<QName, ProtectedProperty> protectedProperties = new HashMap<QName, ProtectedProperty>(21);

    /** Map of protected aspects keyed by name */
    private Map<QName, ProtectedAspect> protectedAspects= new HashMap<QName, ProtectedAspect>(21);

    /** Behaviour instances */
    private JavaBehaviour beforeAddAspectBehaviour = new JavaBehaviour(this,
                                                                       "beforeAddAspect",
                                                                       NotificationFrequency.EVERY_EVENT);
    private JavaBehaviour beforeRemoveAspectBehaviour = new JavaBehaviour(this,
                                                                          "beforeRemoveAspect",
                                                                          NotificationFrequency.EVERY_EVENT);
    private JavaBehaviour onUpdatePropertiesBehaviour = new JavaBehaviour(this,
                                                                          "onUpdateProperties",
                                                                          NotificationFrequency.EVERY_EVENT);

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.model.security.ModelSecurityService#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.model.security.ModelSecurityService#isEnabled()
     */
    public boolean isEnabled()
    {
        return enabled;
    }

    /**
     * @param policyComponent   policy component
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    /**
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param namespaceService  namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    /**
     * @param filePlanService   file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }

    /**
     * Init method
     */
    public void init()
    {
        // bind model security behaviours to all records management artifacts components
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.BeforeAddAspectPolicy.QNAME,
                this,
                beforeAddAspectBehaviour);
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.BeforeRemoveAspectPolicy.QNAME,
                this,
                beforeRemoveAspectBehaviour);
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
                this,
                onUpdatePropertiesBehaviour);
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.model.security.ModelSecurityService#disable()
     */
    @Override
    public void disable() 
    {
    	beforeAddAspectBehaviour.disable();
    	beforeRemoveAspectBehaviour.disable();
    	onUpdatePropertiesBehaviour.disable();
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.model.security.ModelSecurityService#enable()
     */
    @Override
    public void enable() 
    {
    	beforeAddAspectBehaviour.enable();
    	beforeRemoveAspectBehaviour.enable();
    	onUpdatePropertiesBehaviour.enable();	
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.model.security.ModelSecurityService#register(org.alfresco.module.org_alfresco_module_rm.model.security.ProtectedModelArtifact)
     */
    @Override
    public void register(ProtectedModelArtifact artifact)
    {
        // TODO validate that the artifact has a valid property and has a capability set ...

        if (artifact instanceof ProtectedProperty)
        {
            protectedProperties.put(artifact.getQName(), (ProtectedProperty)artifact);
        }
        else if (artifact instanceof ProtectedAspect)
        {
            protectedAspects.put(artifact.getQName(), (ProtectedAspect)artifact);
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.model.security.ModelSecurityService#isProtectedProperty(org.alfresco.service.namespace.QName)
     */
    @Override
    public boolean isProtectedProperty(QName property)
    {
        return protectedProperties.containsKey(property);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.model.security.ModelSecurityService#getProtectedProperties()
     */
    @Override
    public Set<QName> getProtectedProperties()
    {
        return Collections.unmodifiableSet(protectedProperties.keySet());
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.model.security.ModelSecurityService#getProtectedProperty(org.alfresco.service.namespace.QName)
     */
    @Override
    public ProtectedProperty getProtectedProperty(QName name)
    {
        return protectedProperties.get(name);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.model.security.ModelSecurityService#canEditProtectedProperty(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    @Override
    public boolean canEditProtectedProperty(NodeRef nodeRef, QName property)
    {
        boolean result = false;

        ProtectedModelArtifact artifact = getProtectedProperty(property);
        if (artifact == null)
        {
            result = true;
        }
        else
        {
            result = canEdit(nodeRef, artifact);
        }

        return result;
    }

    /**
     * Indicates whether the current user can edit protected model artifact in the context
     * of a given node or not.
     *
     * @param nodeRef   node reference
     * @param artifact  protected model artifact
     * @return boolean  true if the current user can edit the protected model artifact, false otherwise
     */
    private boolean canEdit(NodeRef nodeRef, ProtectedModelArtifact artifact)
    {
        boolean result = false;

        NodeRef filePlan = filePlanService.getFilePlan(nodeRef);
        if (filePlan != null)
        {            
            for (Capability capability : artifact.getCapabilities())
            {
                if (capability.hasPermission(nodeRef).equals(AccessStatus.ALLOWED) == true)
                {
                    result = true;
                    break;
                }
            }                
        }

        return result;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.model.security.ModelSecurityService#isProtectedAspect(org.alfresco.service.namespace.QName)
     */
    @Override
    public boolean isProtectedAspect(QName aspect)
    {
        return protectedAspects.containsKey(aspect);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.model.security.ModelSecurityService#getProtectedAspects()
     */
    @Override
    public Set<QName> getProtectedAspects()
    {
        return Collections.unmodifiableSet(protectedAspects.keySet());
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.model.security.ModelSecurityService#getProtectedAspect(org.alfresco.service.namespace.QName)
     */
    @Override
    public ProtectedAspect getProtectedAspect(QName name)
    {
        return protectedAspects.get(name);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.model.security.ModelSecurityService#canEditProtectedAspect(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    @Override
    public boolean canEditProtectedAspect(NodeRef nodeRef, QName aspect)
    {
        boolean result = false;

        ProtectedModelArtifact artifact = getProtectedAspect(aspect);
        if (artifact == null)
        {
            result = true;
        }
        else
        {
            result = canEdit(nodeRef, artifact);
        }

        return result;
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.BeforeAddAspectPolicy#beforeAddAspect(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    @Override
    public void beforeAddAspect(NodeRef nodeRef, QName aspect)
    {
        if (enabled == true)
        {
            if (AuthenticationUtil.getFullyAuthenticatedUser() != null &&
                AuthenticationUtil.isRunAsUserTheSystemUser() == false &&
                isProtectedAspect(aspect) == true &&
                nodeService.exists(nodeRef) == true &&
                canEditProtectedAspect(nodeRef, aspect) == false)
            {
                // the user can't edit the protected aspect
                throw new ModelAccessDeniedException(
                        "The user " + AuthenticationUtil.getFullyAuthenticatedUser() +
                        " does not have the permission to add the protected aspect " + aspect.toPrefixString(namespaceService) +
                        " from the node " + nodeRef.toString());
            }
        }
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.BeforeRemoveAspectPolicy#beforeRemoveAspect(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    @Override
    public void beforeRemoveAspect(NodeRef nodeRef, QName aspect)
    {
        if (enabled == true)
        {
            if (AuthenticationUtil.getFullyAuthenticatedUser() != null &&
                AuthenticationUtil.isRunAsUserTheSystemUser() == false &&
                isProtectedAspect(aspect) == true &&
                nodeService.exists(nodeRef) == true &&
                canEditProtectedAspect(nodeRef, aspect) == false)
            {
                // the user can't edit the protected aspect
                throw new ModelAccessDeniedException(
                        "The user " + AuthenticationUtil.getFullyAuthenticatedUser() +
                        " does not have the permission to remove the protected aspect " + aspect.toPrefixString(namespaceService) +
                        " from the node " + nodeRef.toString());
            }
        }
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy#onUpdateProperties(org.alfresco.service.cmr.repository.NodeRef, java.util.Map, java.util.Map)
     */
    @Override
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        if (enabled == true)
        {
            if (AuthenticationUtil.getFullyAuthenticatedUser() != null &&
                AuthenticationUtil.isRunAsUserTheSystemUser() == false &&
                nodeService.exists(nodeRef) == true)
            {
                for (QName property : after.keySet())
                {
                    if (isProtectedProperty(property) == true)
                    {
                        // always allow if this is the first time we are setting the protected property
                        if (before == null || before.isEmpty() || before.get(property) == null)
                        {
                            return;
                        }

                        if (EqualsHelper.nullSafeEquals(before.get(property), after.get(property)) == false &&
                           canEditProtectedProperty(nodeRef, property) == false)
                        {
                            // the user can't edit the protected property
                            throw new ModelAccessDeniedException(
                                "The user " + AuthenticationUtil.getFullyAuthenticatedUser() +
                                " does not have the permission to edit the protected property " + property.toPrefixString(namespaceService) +
                                " on the node " + nodeRef.toString());
                        }
                    }
                }
            }
        }
    }
}
