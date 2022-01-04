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

package org.alfresco.module.org_alfresco_module_rm.model.rma.aspect;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.BaseBehaviourBean;
import org.alfresco.repo.copy.AbstractCopyBehaviourCallback;
import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyMap;

/**
 * rma:filePlanComponent behaviour bean
 *
 * @author Roy Wetherall
 * @since 2.2
 */
@BehaviourBean
(
   defaultType = "rma:filePlanComponent"
)
public class FilePlanComponentAspect extends    BaseBehaviourBean
                                     implements NodeServicePolicies.OnUpdatePropertiesPolicy,
                                                NodeServicePolicies.OnAddAspectPolicy,
                                                NodeServicePolicies.OnMoveNodePolicy


{
    /** Well-known location of the scripts folder. */
    private NodeRef scriptsFolderNodeRef = new NodeRef("workspace", "SpacesStore", "rm_behavior_scripts");

    /** script service */
    private ScriptService scriptService;

    /** namespace service */
    private NamespaceService namespaceService;

    /** file plan service */
    private FilePlanService filePlanService;

    /**
     * @param scriptService set script service
     */
    public void setScriptService(ScriptService scriptService)
    {
        this.scriptService = scriptService;
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
     * @see org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy#onUpdateProperties(org.alfresco.service.cmr.repository.NodeRef, java.util.Map, java.util.Map)
     */
    @Override
    @Behaviour
    (
       kind = BehaviourKind.CLASS,
       notificationFrequency = NotificationFrequency.TRANSACTION_COMMIT
    )
    public void onUpdateProperties(final NodeRef nodeRef, final Map<QName, Serializable> before, final Map<QName, Serializable> after)
    {
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            @Override
            public Void doWork()
            {
                if (nodeService.exists(nodeRef))
                {
                    lookupAndExecuteScripts(nodeRef, before, after);
                }

                return null;
            }
        });
    }

    /**
     * This method examines the old and new property sets and for those properties which
     * have changed, looks for script resources corresponding to those properties.
     * Those scripts are then called via the ScriptService.
     *
     * @param nodeWithChangedProperties the node whose properties have changed.
     * @param oldProps the old properties and their values.
     * @param newProps the new properties and their values.
     *
     * @see #lookupScripts(Map, Map)
     */
    private void lookupAndExecuteScripts(NodeRef nodeWithChangedProperties,
                                         Map<QName, Serializable> oldProps,
                                         Map<QName, Serializable> newProps)
    {
        List<NodeRef> scriptRefs = lookupScripts(oldProps, newProps);

        Map<String, Object> objectModel = new HashMap<>(1);
        objectModel.put("node", nodeWithChangedProperties);
        objectModel.put("oldProperties", oldProps);
        objectModel.put("newProperties", newProps);
        for (NodeRef scriptRef : scriptRefs)
        {
            scriptService.executeScript(scriptRef, null, objectModel);
        }
    }

    /**
     * This method determines which properties have changed and for each such property
     * looks for a script resource in a well-known location.
     *
     * @param oldProps the old properties and their values.
     * @param newProps the new properties and their values.
     * @return A list of nodeRefs corresponding to the Script resources.
     *
     * @see  org.alfresco.util.PropertyMap#getChangedProperties(Map, Map)
     */
    private List<NodeRef> lookupScripts(Map<QName, Serializable> oldProps, Map<QName, Serializable> newProps)
    {
        List<NodeRef> result = new ArrayList<>();

        Map<QName, Serializable> changedProps = PropertyMap.getChangedProperties(oldProps, newProps);
        for (QName propQName : changedProps.keySet())
        {
            QName prefixedQName = propQName.getPrefixedQName(namespaceService);

            String [] splitQName = QName.splitPrefixedQName(prefixedQName.toPrefixString());
            final String shortPrefix = splitQName[0];
            final String localName = splitQName[1];

            // This is the filename pattern which is assumed.
            // e.g. a script file cm_name.js would be called for changed to cm:name
            String expectedScriptName = shortPrefix + "_" + localName + ".js";

            NodeRef nextElement = nodeService.getChildByName(scriptsFolderNodeRef, ContentModel.ASSOC_CONTAINS, expectedScriptName);
            if (nextElement != null)
            {
                result.add(nextElement);
            }
        }

        return result;
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnAddAspectPolicy#onAddAspect(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    @Override
    @Behaviour
    (
       kind = BehaviourKind.CLASS,
       notificationFrequency = NotificationFrequency.TRANSACTION_COMMIT
    )
    public void onAddAspect(final NodeRef nodeRef, final QName aspectTypeQName)
    {
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @Override
            public Void doWork()
            {
                // Check if the node exists and the aspect hasn't been removed in the same transaction (see RM-3266)
                if (nodeService.exists(nodeRef) && nodeService.hasAspect(nodeRef, aspectTypeQName))
                {
                    // Look up the root and set on the aspect if found
                    NodeRef root = filePlanService.getFilePlan(nodeRef);
                    if (root != null)
                    {
                        nodeService.setProperty(nodeRef, PROP_ROOT_NODEREF, root);
                    }

                    // If the node has any renditions, they inherit the file plan from their source node.
                    List<ChildAssociationRef> renditions = renditionService.getRenditions(nodeRef);
                    NodeRef rendition;
                    for (ChildAssociationRef chAssRef : renditions)
                    {
                        rendition = chAssRef.getChildRef();
                        if (nodeService.exists(rendition)) 
                        {
                            // Apply file plan component aspect to node's renditions
                            nodeService.addAspect(rendition, ASPECT_FILE_PLAN_COMPONENT, null);
                        }
                    }
                }

                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnMoveNodePolicy#onMoveNode(org.alfresco.service.cmr.repository.ChildAssociationRef, org.alfresco.service.cmr.repository.ChildAssociationRef)
     */
    @Override
    @Behaviour
    (
       kind = BehaviourKind.CLASS,
       notificationFrequency = NotificationFrequency.TRANSACTION_COMMIT
    )
    public void onMoveNode(final ChildAssociationRef oldChildAssocRef, final ChildAssociationRef newChildAssocRef)
    {
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @Override
            public Void doWork()
            {
                if (nodeService.exists(newChildAssocRef.getParentRef()) &&
                    nodeService.exists(newChildAssocRef.getChildRef()))
                {
                    // Look up the root and re-set the value currently stored on the aspect
                    NodeRef root = filePlanService.getFilePlan(newChildAssocRef.getParentRef());
                    // NOTE: set the null value if no root found
                    nodeService.setProperty(newChildAssocRef.getChildRef(), PROP_ROOT_NODEREF, root);
                }

                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    /**
     * Copy behaviour call back
     *
     * @param   classRef    class reference
     * @param   copyDetails  details of the information being copied
     * @return  CopyBehaviourCallback
     */
    @Behaviour
    (
       kind = BehaviourKind.CLASS,
       policy = "alf:getCopyCallback"
    )
    public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails)
    {
        return new AbstractCopyBehaviourCallback()
        {
            /**
             * @see org.alfresco.repo.copy.CopyBehaviourCallback#getChildAssociationCopyAction(org.alfresco.service.namespace.QName, org.alfresco.repo.copy.CopyDetails, org.alfresco.repo.copy.CopyBehaviourCallback.CopyChildAssociationDetails)
             */
            public ChildAssocCopyAction getChildAssociationCopyAction(
                    QName classQName,
                    CopyDetails copyDetails,
                    CopyChildAssociationDetails childAssocCopyDetails)
            {
                // Do not copy the associations
                return null;
            }

            /**
             * @see org.alfresco.repo.copy.CopyBehaviourCallback#getCopyProperties(org.alfresco.service.namespace.QName, org.alfresco.repo.copy.CopyDetails, java.util.Map)
             */
            public Map<QName, Serializable> getCopyProperties(
                    QName classQName,
                    CopyDetails copyDetails,
                    Map<QName, Serializable> properties)
            {
                // Only copy the root node reference if the new value can be looked up via the parent
                NodeRef root = filePlanService.getFilePlan(copyDetails.getTargetParentNodeRef());
                if (root != null)
                {
                    properties.put(PROP_ROOT_NODEREF, root);
                }
                return properties;
            }

            /**
             * @see org.alfresco.repo.copy.CopyBehaviourCallback#getMustCopy(org.alfresco.service.namespace.QName, org.alfresco.repo.copy.CopyDetails)
             */
            public boolean getMustCopy(QName classQName, CopyDetails copyDetails)
            {
                // Ensure the aspect is copied
                return true;
            }
        };
    }

}
