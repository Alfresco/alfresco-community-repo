/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

import java.security.InvalidParameterException;

import org.alfresco.module.org_alfresco_module_rm.model.BaseBehaviourBean;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.service.cmr.repository.ChildAssociationRef;

/**
 * rma:transfer behaviour bean
 *
 * @author Silviu Dinuta
 * @since 2.6
 */
@BehaviourBean(defaultType = "rma:transfer")
public class TransferType extends BaseBehaviourBean implements NodeServicePolicies.OnCreateChildAssociationPolicy
{
    private static final String BEHAVIOUR_NAME = "onCreateChildAssocsForTransferType";

    /**
     * Disable the behaviours for this transaction
     *
     */
    public void disable()
    {
        getBehaviour(BEHAVIOUR_NAME).disable();
    }

    /**
     * Enable behaviours for this transaction
     *
     */
    public void enable()
    {
        getBehaviour(BEHAVIOUR_NAME).enable();
    }

    @Override
    @Behaviour
    (
                kind = BehaviourKind.ASSOCIATION,
                name = BEHAVIOUR_NAME
    )
    public void onCreateChildAssociation(ChildAssociationRef childAssocRef, boolean isNewNode)
    {
        throw new InvalidParameterException("Operation failed. Creation is not allowed in Transfer Folders");
    }
}
