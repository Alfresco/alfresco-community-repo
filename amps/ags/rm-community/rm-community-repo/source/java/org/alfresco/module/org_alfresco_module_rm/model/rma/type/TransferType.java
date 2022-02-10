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

import java.io.Serializable;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.model.BaseBehaviourBean;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * rma:transfer behaviour bean
 *
 * @author Silviu Dinuta
 * @since 2.6
 */
@BehaviourBean(defaultType = "rma:transfer")
public class TransferType extends BaseBehaviourBean 
                          implements NodeServicePolicies.OnCreateChildAssociationPolicy,
                                     NodeServicePolicies.OnUpdatePropertiesPolicy
{
    private final static String MSG_ERROR_ADD_CHILD_TO_TRANSFER = "rm.action.create.transfer.child-error-message";
    private final static String MSG_ERROR_EDIT_TRANSFER_PROPERTIES = "rm.action.transfer-non-editable";

    private static final String CREATE_CHILD_BEHAVIOUR_NAME = "onCreateChildAssocsForTransferType";
    private static final String EDIT_PROPERTIES_BEHAVIOUR_NAME = "onEditTransferProperties";

    /**
     * Disable the behaviours for this transaction
     *
     */
    public void disable()
    {
        getBehaviour(CREATE_CHILD_BEHAVIOUR_NAME).disable();
        getBehaviour(EDIT_PROPERTIES_BEHAVIOUR_NAME).disable();
    }

    /**
     * Enable behaviours for this transaction
     *
     */
    public void enable()
    {
        getBehaviour(CREATE_CHILD_BEHAVIOUR_NAME).enable();
        getBehaviour(EDIT_PROPERTIES_BEHAVIOUR_NAME).enable();
    }

    /**
     * Prevent creating a node inside transfer folder, this will be possible only through internal services in a controlled manner.
     */
    @Override
    @Behaviour
    (
                kind = BehaviourKind.ASSOCIATION,
                name = CREATE_CHILD_BEHAVIOUR_NAME
    )
    public void onCreateChildAssociation(ChildAssociationRef childAssocRef, boolean isNewNode)
    {
        throw new IntegrityException(I18NUtil.getMessage(MSG_ERROR_ADD_CHILD_TO_TRANSFER), null);
    }

    @Override
    @Behaviour
    (
            kind = BehaviourKind.CLASS, 
            name = EDIT_PROPERTIES_BEHAVIOUR_NAME
    )
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        if(!authenticationUtil.isRunAsUserTheSystemUser())
        {
            throw new IntegrityException(I18NUtil.getMessage(MSG_ERROR_EDIT_TRANSFER_PROPERTIES), null);
        }
    }
}
