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

import org.alfresco.module.org_alfresco_module_rm.model.BaseBehaviourBean;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * qshare:shared aspect behaviour bean 
 * do not allow this aspect to be added for records
 *
 * @author Ramona Popa
 * @since 2.5
 */
@BehaviourBean(defaultType = "qshare:shared")
public class QShareAspect extends BaseBehaviourBean implements NodeServicePolicies.BeforeAddAspectPolicy
{
    /**
     * Behaviour to prevent sharing a record
     *
     * @see org.alfresco.repo.node.NodeServicePolicies.BeforeAddAspectPolicy#beforeAddAspect(org.alfresco.service.cmr.repository.NodeRef,
     *      org.alfresco.service.namespace.QName)
     */
    @Override
    @Behaviour(kind = BehaviourKind.CLASS, notificationFrequency = NotificationFrequency.FIRST_EVENT)
    public void beforeAddAspect(final NodeRef nodeRef, final QName aspectTypeQName)
    {
        if (nodeService.hasAspect(nodeRef, RecordsManagementModel.ASPECT_RECORD))
        {
            throw new IntegrityException("Operation failed. Aspect " + aspectTypeQName.toString() + " cannot be added for records.", null);
        }
    }
}
