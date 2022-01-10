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

import static org.alfresco.model.ContentModel.TYPE_CONTENT;
import static org.alfresco.model.ContentModel.TYPE_FOLDER;
import static org.alfresco.repo.site.SiteModel.ASPECT_SITE_CONTAINER;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService;
import org.alfresco.module.org_alfresco_module_rm.model.BaseBehaviourBean;
import org.alfresco.module.org_alfresco_module_rm.util.PropertyModificationAllowedCheck;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * rma:frozen behaviour bean
 *
 * @author Roy Wetherall
 * @since 2.2
 */
@BehaviourBean
(
   defaultType = "rma:frozen"
)
public class FrozenAspect extends    BaseBehaviourBean
                          implements NodeServicePolicies.BeforeDeleteNodePolicy,
                                     NodeServicePolicies.OnAddAspectPolicy,
                                     NodeServicePolicies.OnRemoveAspectPolicy,
                                     NodeServicePolicies.OnUpdatePropertiesPolicy,
                                     NodeServicePolicies.BeforeMoveNodePolicy
{
    /**
     * Behaviour name for on update properties for frozen aspect
     */
    private static final String ON_UPDATE_PROP_FROZEN_BEHAVIOUR_NAME = "onUpdatePropertiesFrozenAspect";

    /** freeze service */
    protected FreezeService freezeService;

    /**
     * Utility class for property modification
     */
    private PropertyModificationAllowedCheck propertyModificationAllowedCheck;

    /**
     * @param freezeService freeze service
     */
    public void setFreezeService(FreezeService freezeService)
    {
        this.freezeService = freezeService;
    }

    /**
     * Setter for property modification check utility
     * @param propertyModificationAllowedCheck Utility class for property modification
     */
    public void setPropertyModificationAllowedCheck(PropertyModificationAllowedCheck propertyModificationAllowedCheck)
    {
        this.propertyModificationAllowedCheck = propertyModificationAllowedCheck;
    }

    /**
     * Disable the on update properties for frozen aspect behaviour
     */
    public void disableOnPropUpdateFrozenAspect()
    {
        org.alfresco.repo.policy.Behaviour behaviour = getBehaviour(ON_UPDATE_PROP_FROZEN_BEHAVIOUR_NAME);
        if (behaviour != null)
        {
            behaviour.disable();
        }

    }

    /**
     * Enable the on update properties for frozen aspect
     */
    public void enableOnPropUpdateFrozenAspect()
    {
        org.alfresco.repo.policy.Behaviour behaviour = getBehaviour(ON_UPDATE_PROP_FROZEN_BEHAVIOUR_NAME);
        if (behaviour != null && !behaviour.isEnabled())
        {
            behaviour.enable();
        }
    }

    /**
     * Ensure that no frozen node is deleted.
     *
     * @see org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy#beforeDeleteNode(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    @Behaviour
    (
            kind = BehaviourKind.CLASS,
            notificationFrequency = NotificationFrequency.FIRST_EVENT
    )
    public void beforeDeleteNode(final NodeRef nodeRef)
    {
        AuthenticationUtil.runAsSystem((RunAsWork<Void>) () -> {
            if (nodeService.exists(nodeRef) && freezeService.isFrozen(nodeRef))
            {
                // never allow to delete a frozen node
                throw new PermissionDeniedException(I18NUtil.getMessage("rm.hold.delete-frozen-node"));
            }

            // check children
            checkChildren(nodeService.getChildAssocs(nodeRef));
            return null;
        });
    }

    /**
     * Checks the children for frozen nodes. Throws security error if any are
     * found.
     *
     * @param assocs
     */
    private void checkChildren(List<ChildAssociationRef> assocs)
    {
        for (ChildAssociationRef assoc : assocs)
        {
            // we only care about primary children
            if (assoc.isPrimary())
            {
                NodeRef nodeRef = assoc.getChildRef();
                if (freezeService.isFrozen(nodeRef))
                {
                    // never allow to delete a node with a frozen child
                    throw new PermissionDeniedException(I18NUtil.getMessage("rm.hold.delete-node-frozen-children"));
                }

                // check children
                checkChildren(nodeService.getChildAssocs(nodeRef));
            }
        }
    }
    
    @Override
    @Behaviour
    (
            kind = BehaviourKind.CLASS,
            notificationFrequency = NotificationFrequency.TRANSACTION_COMMIT
    )
    public void onAddAspect(final NodeRef nodeRef, final QName aspectTypeQName)
    {
        AuthenticationUtil.runAsSystem((RunAsWork<Void>) () -> {
            if (nodeService.exists(nodeRef) && instanceOf(nodeRef, TYPE_CONTENT))
            {
                // get the owning folder
                final NodeRef parentRef = nodeService.getPrimaryParent(nodeRef).getParentRef();
                // check that the aspect has been added
                if (nodeService.hasAspect(parentRef, ASPECT_HELD_CHILDREN))
                {
                    // increment current count
                    int currentCount = (Integer) nodeService.getProperty(parentRef, PROP_HELD_CHILDREN_COUNT);
                    currentCount = currentCount + 1;
                    nodeService.setProperty(parentRef, PROP_HELD_CHILDREN_COUNT, currentCount);
                }
                else
                {
                    if (instanceOf(parentRef, TYPE_FOLDER) && !nodeService.hasAspect(parentRef, ASPECT_SITE_CONTAINER))
                    {
                        // add aspect and set count to 1
                        final Map<QName, Serializable> props = new HashMap<>(1);
                        props.put(PROP_HELD_CHILDREN_COUNT, 1);
                        getInternalNodeService().addAspect(parentRef, ASPECT_HELD_CHILDREN, props);
                    }
                }
            }
            return null;
        });
    }

    @Override
    @Behaviour
    (
            kind = BehaviourKind.CLASS,
            notificationFrequency = NotificationFrequency.TRANSACTION_COMMIT
    )
    public void onRemoveAspect(final NodeRef nodeRef, QName aspectTypeQName)
    {
        AuthenticationUtil.runAsSystem((RunAsWork<Void>) () -> {

            if (nodeService.exists(nodeRef) && instanceOf(nodeRef, TYPE_CONTENT))
            {
                // get the owning folder
                final NodeRef owningFolder = nodeService.getPrimaryParent(nodeRef).getParentRef();

                // check that the aspect has been added
                if (nodeService.hasAspect(owningFolder, ASPECT_HELD_CHILDREN))
                {
                    // decrement current count
                    final int currentCount = (Integer) nodeService.getProperty(owningFolder, PROP_HELD_CHILDREN_COUNT);
                    if (currentCount > 0)
                    {
                        nodeService.setProperty(owningFolder, PROP_HELD_CHILDREN_COUNT, currentCount - 1);
                    }
                }
            }
            return null;
        });
    }

    /**
     * Behaviour associated with moving a frozen node
     * <p>
     * Prevent frozen items being moved
     */
    @Override
    @Behaviour
            (
                    kind = BehaviourKind.CLASS,
                    notificationFrequency = NotificationFrequency.FIRST_EVENT
            )
    public void beforeMoveNode(ChildAssociationRef oldChildAssocRef, NodeRef newParentRef)
    {
        AuthenticationUtil.runAsSystem((RunAsWork<Void>) () -> {
            if (nodeService.exists(oldChildAssocRef.getChildRef()) &&
                    freezeService.isFrozen(oldChildAssocRef.getChildRef()))
            {
                throw new PermissionDeniedException(I18NUtil.getMessage("rm.hold.move-frozen-node"));
            }
            return null;
        });
    }

    /**
     * Behaviour associated with updating properties
     * <p>
     * Prevents frozen items being updated
     */
    @Override
    @Behaviour
            (
                    kind = BehaviourKind.CLASS,
                    name = ON_UPDATE_PROP_FROZEN_BEHAVIOUR_NAME,
                    notificationFrequency = NotificationFrequency.FIRST_EVENT
            )
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        AuthenticationUtil.runAsSystem((RunAsWork<Void>) () -> {
            // check to not throw exception when the aspect is being added
            if (nodeService.exists(nodeRef) && freezeService.isFrozen(nodeRef) &&
                    !transactionalResourceHelper.getSet("frozen").contains(nodeRef) &&
                        !propertyModificationAllowedCheck.check(before, after))
                {
                    throw new PermissionDeniedException(I18NUtil.getMessage("rm.hold.update-frozen-node"));
                }
            return null;
        });
    }
}
