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

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.model.BaseBehaviourBean;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * rma:unfiledRecordContainer behaviour bean
 *
 * @author Silviu Dinuta
 * @since 2.6
 */
@BehaviourBean(defaultType = "rma:unfiledRecordContainer")
public class UnfiledRecordContainerType extends BaseBehaviourBean
            implements NodeServicePolicies.OnCreateChildAssociationPolicy
{
    @Override
    @Behaviour(kind = BehaviourKind.ASSOCIATION)
    public void onCreateChildAssociation(ChildAssociationRef childAssocRef, boolean isNewNode)
    {
        // ensure we are not trying to put a record folder in the unfiled records container
        NodeRef nodeRef = childAssocRef.getChildRef();
        NodeRef parent = childAssocRef.getParentRef();
        if (isUnfiledRecordsContainer(parent) && isRecordFolder(nodeRef))
        {
            throw new AlfrescoRuntimeException("Operation failed, because you can not place a record folder in the unfiled records container.");
        }
    }
}
