/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.version.common;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.policy.ClassPolicyDelegate;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.PolicyScope;
import org.alfresco.repo.version.VersionRevertCallback;
import org.alfresco.repo.version.VersionRevertCallback.RevertAspectAction;
import org.alfresco.repo.version.VersionRevertCallback.RevertAssocAction;
import org.alfresco.repo.version.VersionRevertDetails;
import org.alfresco.repo.version.VersionServicePolicies;
import org.alfresco.repo.version.VersionServicePolicies.AfterCreateVersionPolicy;
import org.alfresco.repo.version.VersionServicePolicies.AfterVersionRevertPolicy;
import org.alfresco.repo.version.VersionServicePolicies.BeforeCreateVersionPolicy;
import org.alfresco.repo.version.VersionServicePolicies.CalculateVersionLabelPolicy;
import org.alfresco.repo.version.VersionServicePolicies.OnCreateVersionPolicy;
import org.alfresco.repo.version.VersionServicePolicies.OnRevertVersionPolicy;
import org.alfresco.repo.version.common.versionlabel.SerialVersionLabelPolicy;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionServiceException;
import org.alfresco.service.namespace.QName;

/**
 * Abstract version service implementation.
 * 
 * @author Roy Wetherall
 */
public abstract class AbstractVersionServiceImpl
{
    /**
     * The common node service
     */
    protected NodeService nodeService;

    /**
     * Policy component
     */
    protected PolicyComponent policyComponent;

    /**
     * The dictionary service
     */
    protected DictionaryService dictionaryService;

    /**
     * Policy delegates
     */
    private ClassPolicyDelegate<BeforeCreateVersionPolicy> beforeCreateVersionDelegate;
    private ClassPolicyDelegate<AfterCreateVersionPolicy> afterCreateVersionDelegate;
    private ClassPolicyDelegate<OnCreateVersionPolicy> onCreateVersionDelegate;
    private ClassPolicyDelegate<CalculateVersionLabelPolicy> calculateVersionLabelDelegate;
    private ClassPolicyDelegate<OnRevertVersionPolicy> onRevertVersionDelegate;
    private ClassPolicyDelegate<AfterVersionRevertPolicy> afterVersionRevertDelegate;

    /**
     * Sets the general node service
     * 
     * @param nodeService
     *            the node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Sets the policy component
     * 
     * @param policyComponent
     *            the policy component
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    /**
     * Sets the dictionary service
     * 
     * @param dictionaryService
     *            the dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Initialise method
     */
    public void initialise()
    {
        // Register the policies
        this.beforeCreateVersionDelegate = this.policyComponent.registerClassPolicy(VersionServicePolicies.BeforeCreateVersionPolicy.class);
        this.afterCreateVersionDelegate = this.policyComponent.registerClassPolicy(VersionServicePolicies.AfterCreateVersionPolicy.class);
        this.onCreateVersionDelegate = this.policyComponent.registerClassPolicy(VersionServicePolicies.OnCreateVersionPolicy.class);
        this.calculateVersionLabelDelegate = this.policyComponent.registerClassPolicy(VersionServicePolicies.CalculateVersionLabelPolicy.class);
        this.onRevertVersionDelegate = this.policyComponent.registerClassPolicy(VersionServicePolicies.OnRevertVersionPolicy.class);
        this.afterVersionRevertDelegate = this.policyComponent.registerClassPolicy(VersionServicePolicies.AfterVersionRevertPolicy.class);
    }

    /**
     * Invokes after version has been reverted
     * 
     * @param nodeRef
     *            the node that has been reverted
     * @param version
     *            the reverted version
     */
    protected void invokeAfterVersionRevert(NodeRef nodeRef, Version version)
    {
        // invoke for node type
        QName nodeTypeQName = nodeService.getType(nodeRef);
        this.afterVersionRevertDelegate.get(nodeRef, nodeTypeQName).afterVersionRevert(nodeRef, version);
        // invoke for node aspects
        Set<QName> nodeAspectQNames = nodeService.getAspects(nodeRef);
        this.afterVersionRevertDelegate.get(nodeRef, nodeAspectQNames).afterVersionRevert(nodeRef, version);
    }

    /**
     * Invokes the before create version policy behaviour
     * 
     * @param nodeRef
     *            the node being versioned
     */
    protected void invokeBeforeCreateVersion(NodeRef nodeRef)
    {
        // invoke for node type
        QName nodeTypeQName = nodeService.getType(nodeRef);
        this.beforeCreateVersionDelegate.get(nodeRef, nodeTypeQName).beforeCreateVersion(nodeRef);
        // invoke for node aspects
        Set<QName> nodeAspectQNames = nodeService.getAspects(nodeRef);
        this.beforeCreateVersionDelegate.get(nodeRef, nodeAspectQNames).beforeCreateVersion(nodeRef);
    }

    /**
     * Invoke the after create version policy behaviour
     * 
     * @param nodeRef
     *            the nodeRef versioned
     * @param version
     *            the created version
     */
    protected void invokeAfterCreateVersion(NodeRef nodeRef, Version version)
    {
        // invoke for node type
        QName nodeTypeQName = nodeService.getType(nodeRef);
        this.afterCreateVersionDelegate.get(nodeRef, nodeTypeQName).afterCreateVersion(nodeRef, version);
        // invoke for node aspects
        Set<QName> nodeAspectQNames = nodeService.getAspects(nodeRef);
        this.afterCreateVersionDelegate.get(nodeRef, nodeAspectQNames).afterCreateVersion(nodeRef, version);
    }

    /**
     * Invoke the on create version policy behaviour
     *
     */
    protected void invokeOnCreateVersion(
            NodeRef nodeRef,
            Map<String, Serializable> versionProperties,
            PolicyScope nodeDetails)
    {
        // Sort out the policies for the node type
        QName classRef = this.nodeService.getType(nodeRef);
        invokeOnCreateVersion(classRef, nodeRef, versionProperties, nodeDetails);

        // Sort out the policies for the aspects
        Collection<QName> aspects = this.nodeService.getAspects(nodeRef);
        for (QName aspect : aspects)
        {
            invokeOnCreateVersion(aspect, nodeRef, versionProperties, nodeDetails);
        }

    }

    /**
     * How should revert deal with this aspect?
     * 
     * @param aspectName
     *            QName
     * @param revertDetails
     *            VersionRevertDetails
     * @return the action to be taken
     */
    protected RevertAspectAction getRevertAspectAction(QName aspectName, VersionRevertDetails revertDetails)
    {
        // first check for a policy for the node type
        Collection<OnRevertVersionPolicy> policies = onRevertVersionDelegate.getList(revertDetails.getNodeRef(), aspectName);
        for (OnRevertVersionPolicy policy : policies)
        {
            VersionRevertCallback cb = policy.getRevertVersionCallback(aspectName, revertDetails);
            if (cb != null)
            {
                RevertAspectAction action = cb.getRevertAspectAction(aspectName, revertDetails);
                if (action != null && action == RevertAspectAction.IGNORE)
                {
                    // ignore always wins
                    return action;
                }
            }
        }
        return RevertAspectAction.REVERT;
    }

    /**
     * How should revert deal with this association
     * 
     * @param className
     *            QName
     * @param assocName
     *            QName
     * @param revertDetails
     *            VersionRevertDetails
     * @return the action to be taken
     */
    protected RevertAssocAction getRevertAssocAction(QName className, QName assocName, VersionRevertDetails revertDetails)
    {
        Collection<OnRevertVersionPolicy> policies = onRevertVersionDelegate.getList(revertDetails.getNodeRef(), className);
        for (OnRevertVersionPolicy policy : policies)
        {
            VersionRevertCallback cb = policy.getRevertVersionCallback(className, revertDetails);
            if (cb != null)
            {
                RevertAssocAction action = cb.getRevertAssocAction(assocName, revertDetails);
                if (action != null && action == RevertAssocAction.IGNORE)
                {
                    // ignore always wins
                    return action;
                }
            }
        }
        return RevertAssocAction.REVERT;
    }

    /**
     * Invokes the on create version policy behaviour for a given type
     * 
     * @param classRef
     *            QName
     * @param nodeDetails
     *            NodeRef
     * @param nodeRef
     *            Map<String, Serializable>
     * @param versionProperties
     *            PolicyScope
     */
    private void invokeOnCreateVersion(
            QName classRef,
            NodeRef nodeRef,
            Map<String, Serializable> versionProperties,
            PolicyScope nodeDetails)
    {
        // Call the default implementation
        defaultOnCreateVersion(
                classRef,
                nodeRef,
                versionProperties,
                nodeDetails);

        // Call the policy definitions
        Collection<OnCreateVersionPolicy> policies = this.onCreateVersionDelegate.getList(nodeRef, classRef);
        for (VersionServicePolicies.OnCreateVersionPolicy policy : policies)
        {
            policy.onCreateVersion(
                    classRef,
                    nodeRef,
                    versionProperties,
                    nodeDetails);
        }
    }

    /**
     * Default implementation of the on create version policy. Override if you wish to supply your own policy.
     * 
     * @param classRef
     *            QName
     * @param nodeRef
     *            NodeRef
     * @param nodeDetails
     *            PolicyScope
     */
    abstract protected void defaultOnCreateVersion(
            QName classRef,
            NodeRef nodeRef,
            Map<String, Serializable> versionProperties,
            PolicyScope nodeDetails);

    /**
     * Invoke the calculate version label policy behaviour
     * 
     * @param classRef
     *            QName
     * @param preceedingVersion
     *            Version
     * @param versionNumber
     *            int
     * @return String
     */
    protected String invokeCalculateVersionLabel(
            QName classRef,
            Version preceedingVersion,
            int versionNumber,
            Map<String, Serializable> versionProperties)
    {
        String versionLabel = null;

        Collection<CalculateVersionLabelPolicy> behaviours = this.calculateVersionLabelDelegate.getList(classRef);
        if (behaviours.size() == 0)
        {
            // Default the version label to the SerialVersionLabelPolicy
            SerialVersionLabelPolicy defaultVersionLabelPolicy = new SerialVersionLabelPolicy();
            versionLabel = defaultVersionLabelPolicy.calculateVersionLabel(classRef, preceedingVersion, versionNumber, versionProperties);
        }
        else if (behaviours.size() == 1)
        {
            // Call the policy behaviour
            CalculateVersionLabelPolicy[] arr = behaviours.toArray(new CalculateVersionLabelPolicy[]{});
            versionLabel = arr[0].calculateVersionLabel(classRef, preceedingVersion, versionNumber, versionProperties);
        }
        else
        {
            // Error since we can only deal with a single caculate version label policy
            throw new VersionServiceException("More than one CalculateVersionLabelPolicy behaviour has been registered for the type " + classRef.toString());
        }

        return versionLabel;
    }

    abstract public StoreRef getVersionStoreReference();
}
