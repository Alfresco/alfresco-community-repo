/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2026 Alfresco Software Limited
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
package org.alfresco.repo.node;

import static org.alfresco.model.ContentModel.TYPE_CD_DEFINITION;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.BeforeCreateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.BeforeMoveNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.BeforeSetNodeTypePolicy;
import org.alfresco.repo.node.NodeServicePolicies.BeforeUpdateNodePolicy;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Behaviour bean that protects the Cascading Dictionary types ({@code cd:definition} from being created, updated, deleted, moved via the everyday Alfresco APIs (Node REST API, CMIS, WebDAV, scripts, …).
 * <p>
 * The goal is to catch <i>accidental</i> misuse, not to be a hardened sandbox. Power-user attacks via lower-level APIs are intentionally out of scope — they're not what a normal user does by mistake. We also deliberately do <b>not</b> bind anything on {@link ContentModel#TYPE_BASE} for property updates: that would fire on every property write in the entire repository, paying a tax on the hottest path in the product just to reject planting a meaningless {@code cd:*} residual property on a non-CD node — a non-issue because the CD subsystem never reads such residuals.
 * <p>
 * The CD subsystem opts out of these checks by surrounding its legitimate writes with {@code behaviourFilter.disableBehaviour(ContentModel.TYPE_CD_DEFINITION)}, silences the {@code cd:definition}-bound bindings}.
 * <p>
 * <b>Design rule:</b> handlers must <b>not</b> call into {@code NodeService} or any other security-checked service. Some bootstrap paths (keystore creation, system-store writes, …) fire these policies before an authenticated context is established, and {@code NodeService} would crash with {@code AuthenticationCredentialsNotFoundException}. Every check here works only with the arguments the policy passes in.
 */
public class CDDefinitionProtection implements BeforeCreateNodePolicy,
        BeforeUpdateNodePolicy,
        BeforeDeleteNodePolicy,
        BeforeMoveNodePolicy,
        BeforeSetNodeTypePolicy
{
    private final PolicyComponent policyComponent;
    private final NamespaceService namespaceService;

    public CDDefinitionProtection(PolicyComponent policyComponent, NamespaceService namespaceService)
    {
        this.policyComponent = policyComponent;
        this.namespaceService = namespaceService;
    }

    public void init()
    {
        // --- cd:definition-bound bindings (fire when the affected node IS-A cd:definition) ---

        policyComponent.bindClassBehaviour(
                BeforeCreateNodePolicy.QNAME,
                TYPE_CD_DEFINITION,
                new JavaBehaviour(this, "beforeCreateNode", Behaviour.NotificationFrequency.EVERY_EVENT));

        policyComponent.bindClassBehaviour(
                BeforeUpdateNodePolicy.QNAME,
                TYPE_CD_DEFINITION,
                new JavaBehaviour(this, "beforeUpdateNode", Behaviour.NotificationFrequency.EVERY_EVENT));

        policyComponent.bindClassBehaviour(
                BeforeDeleteNodePolicy.QNAME,
                TYPE_CD_DEFINITION,
                new JavaBehaviour(this, "beforeDeleteNode", Behaviour.NotificationFrequency.EVERY_EVENT));

        policyComponent.bindClassBehaviour(
                BeforeMoveNodePolicy.QNAME,
                TYPE_CD_DEFINITION,
                new JavaBehaviour(this, "beforeMoveNode", Behaviour.NotificationFrequency.EVERY_EVENT));

        // --- sys:base bindings (catch things the type-bound handlers cannot see) ---

        // setType(node, cd:definition) — dispatched on the current class, so filter on newType.
        policyComponent.bindClassBehaviour(
                BeforeSetNodeTypePolicy.QNAME,
                ContentModel.TYPE_BASE,
                new JavaBehaviour(this, "beforeSetNodeType", Behaviour.NotificationFrequency.EVERY_EVENT));
    }

    @Override
    public void beforeCreateNode(NodeRef parentRef, QName assocTypeQName, QName assocQName, QName nodeTypeQName)
    {
        deny("create", nodeTypeQName);
    }

    @Override
    public void beforeUpdateNode(NodeRef nodeRef)
    {
        deny("update", TYPE_CD_DEFINITION);
    }

    @Override
    public void beforeDeleteNode(NodeRef nodeRef)
    {
        deny("delete", TYPE_CD_DEFINITION);
    }

    @Override
    public void beforeMoveNode(ChildAssociationRef oldChildAssocRef, NodeRef newParentRef)
    {
        deny("move", TYPE_CD_DEFINITION);
    }

    /** {@code sys:base} binding — rejects {@code setType(node, cd:definition)}. */
    @Override
    public void beforeSetNodeType(NodeRef nodeRef, QName oldType, QName newType)
    {
        if (TYPE_CD_DEFINITION.equals(newType))
        {
            deny("setType", TYPE_CD_DEFINITION);
        }
    }

    private void deny(String operation, QName protectedQName)
    {
        String message = "Operation: " + operation + " is not allowed on protected: " + protectedQName.toPrefixString(namespaceService);
        throw new AlfrescoRuntimeException(message);
    }
}
