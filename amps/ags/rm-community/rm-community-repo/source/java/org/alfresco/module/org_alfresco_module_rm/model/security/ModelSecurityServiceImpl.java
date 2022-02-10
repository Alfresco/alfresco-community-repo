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

package org.alfresco.module.org_alfresco_module_rm.model.security;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.BaseBehaviourBean;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
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
@BehaviourBean
public class ModelSecurityServiceImpl extends    BaseBehaviourBean
                                      implements ModelSecurityService,
                                                 NodeServicePolicies.BeforeAddAspectPolicy,
                                                 NodeServicePolicies.BeforeRemoveAspectPolicy,
                                                 NodeServicePolicies.OnUpdatePropertiesPolicy
{
    /** Indicates whether model security is enabled or not */
    private boolean enabled = true;

    /** Namespace service */
    private NamespaceService namespaceService;

    /** File plan service */
    private FilePlanService filePlanService;

    /** Map of protected properties keyed by name */
    private Map<QName, ProtectedProperty> protectedProperties = new HashMap<>(21);

    /** Map of protected aspects keyed by name */
    private Map<QName, ProtectedAspect> protectedAspects= new HashMap<>(21);

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
     * @see org.alfresco.module.org_alfresco_module_rm.model.security.ModelSecurityService#disable()
     */
    @Override
    public void disable()
    {
    	getBehaviour("beforeAddAspect").disable();
    	getBehaviour("beforeRemoveAspect").disable();
    	getBehaviour("onUpdateProperties").disable();
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.model.security.ModelSecurityService#enable()
     */
    @Override
    public void enable()
    {
        getBehaviour("beforeAddAspect").enable();
        getBehaviour("beforeRemoveAspect").enable();
        getBehaviour("onUpdateProperties").enable();
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
                if (capability.hasPermission(nodeRef).equals(AccessStatus.ALLOWED))
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
    @Behaviour
    (
            kind = BehaviourKind.CLASS,
            isService = true,
            name = "beforeAddAspect"
    )
    public void beforeAddAspect(NodeRef nodeRef, QName aspect)
    {
        if (enabled &&
                AuthenticationUtil.getFullyAuthenticatedUser() != null &&
                !AuthenticationUtil.isRunAsUserTheSystemUser() &&
                isProtectedAspect(aspect) &&
                nodeService.exists(nodeRef) &&
                !canEditProtectedAspect(nodeRef, aspect))
        {
            // the user can't edit the protected aspect
            throw new ModelAccessDeniedException(
                    "The user " + AuthenticationUtil.getFullyAuthenticatedUser() +
                    " does not have the permission to add the protected aspect " + aspect.toPrefixString(namespaceService) +
                    " to the node " + nodeRef.toString());
        }
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.BeforeRemoveAspectPolicy#beforeRemoveAspect(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    @Override
    @Behaviour
    (
            kind = BehaviourKind.CLASS,
            isService = true,
            name = "beforeRemoveAspect"
    )
    public void beforeRemoveAspect(NodeRef nodeRef, QName aspect)
    {
        if (enabled &&
                AuthenticationUtil.getFullyAuthenticatedUser() != null &&
                !AuthenticationUtil.isRunAsUserTheSystemUser() &&
                isProtectedAspect(aspect) &&
                nodeService.exists(nodeRef) &&
                !canEditProtectedAspect(nodeRef, aspect))
        {
            // the user can't edit the protected aspect
            throw new ModelAccessDeniedException(
                    "The user " + AuthenticationUtil.getFullyAuthenticatedUser() +
                    " does not have the permission to remove the protected aspect " + aspect.toPrefixString(namespaceService) +
                    " from the node " + nodeRef.toString());
        }
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy#onUpdateProperties(org.alfresco.service.cmr.repository.NodeRef, java.util.Map, java.util.Map)
     */
    @Override
    @Behaviour
    (
            kind = BehaviourKind.CLASS,
            isService = true,
            name = "onUpdateProperties"
    )
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        if (enabled &&
                AuthenticationUtil.getFullyAuthenticatedUser() != null &&
                !AuthenticationUtil.isRunAsUserTheSystemUser() &&
                nodeService.exists(nodeRef))
        {
            for (Map.Entry<QName, Serializable> entry : after.entrySet())
            {
                QName property = entry.getKey();
                if (isProtectedProperty(property))
                {
                    // always allow if this is the first time we are setting the protected property
                    if (before == null || before.isEmpty() || before.get(property) == null)
                    {
                        return;
                    }

                    if (!EqualsHelper.nullSafeEquals(before.get(property), entry.getValue()) &&
                            !canEditProtectedProperty(nodeRef, property))
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
