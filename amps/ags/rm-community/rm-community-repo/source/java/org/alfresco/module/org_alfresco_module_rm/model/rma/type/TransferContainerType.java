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
package org.alfresco.module.org_alfresco_module_rm.model.rma.type;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.model.BaseBehaviourBean;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * rma:transferContainer behaviour bean
 *
 * @author Mihai Cozma
 * @since 2.4
 */
@BehaviourBean(defaultType = "rma:transferContainer")
public class TransferContainerType extends BaseBehaviourBean
            implements NodeServicePolicies.OnCreateChildAssociationPolicy,
                       NodeServicePolicies.OnCreateNodePolicy,
                       NodeServicePolicies.OnDeleteNodePolicy

{
    private final static String MSG_ERROR_ADD_CONTENT_CONTAINER = "rm.service.error-add-content-container";
    private final static String MSG_ERROR_ADD_CHILD_TO_TRANSFER_CONTAINER = "rm.action.create.transfer.container.child-error-message";
    private static final String BEHAVIOUR_NAME = "onCreateChildAssocsForTransferContainer";
    private static final String DELETE_BEHAVIOUR_NAME = "onDeleteTransferContainer";

    /**
     * Disable the behaviours for this transaction
     *
     */
    public void disable()
    {
        getBehaviour(BEHAVIOUR_NAME).disable();
        getBehaviour(DELETE_BEHAVIOUR_NAME).disable();
    }

    /**
     * Enable behaviours for this transaction
     *
     */
    public void enable()
    {
        getBehaviour(BEHAVIOUR_NAME).enable();
        getBehaviour(DELETE_BEHAVIOUR_NAME).enable();
    }

    /**
     * Prevent creating a node inside transfer container, this will be possible only through internal services in a controlled manner.
     *
     * @see org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy#onCreateChildAssociation(org.alfresco.service.cmr.repository.ChildAssociationRef,
     *      boolean)
     */
    @Override
    @Behaviour
    (
                kind = BehaviourKind.ASSOCIATION,
                name = BEHAVIOUR_NAME
    )
    public void onCreateChildAssociation(ChildAssociationRef childAssocRef, boolean bNew)
    {
        throw new IntegrityException(I18NUtil.getMessage(MSG_ERROR_ADD_CHILD_TO_TRANSFER_CONTAINER), null);
    }

    @Override
    public void onCreateNode(ChildAssociationRef childAssocRef)
    {
        NodeRef nodeRef = childAssocRef.getChildRef();
        if (instanceOf(nodeRef, ContentModel.TYPE_CONTENT) == true) { throw new AlfrescoRuntimeException(
                    I18NUtil.getMessage(MSG_ERROR_ADD_CONTENT_CONTAINER)); }
    }

    @Override
    @Behaviour
    (
                kind = BehaviourKind.CLASS,
                name = DELETE_BEHAVIOUR_NAME
    )
    public void onDeleteNode(ChildAssociationRef childAssocRef, boolean isNodeArchived)
    {
        throw new IntegrityException("Operation failed. Deletion of Transfer Container is not allowed.", null);
    }
}
