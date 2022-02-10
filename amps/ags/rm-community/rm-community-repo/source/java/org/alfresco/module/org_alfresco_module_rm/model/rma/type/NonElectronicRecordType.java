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

import org.alfresco.module.org_alfresco_module_rm.model.BaseBehaviourBean;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * rma:nonElectronicDocument behaviour bean.
 *
 * @author silviudinuta
 * @since 2.4
 */
@BehaviourBean(defaultType = "rma:nonElectronicDocument")
public class NonElectronicRecordType extends BaseBehaviourBean implements NodeServicePolicies.OnUpdateNodePolicy
{

    /** record service */
    protected RecordService recordService;

    /**
     * @param recordService record service
     */
    public void setRecordService(RecordService recordService)
    {
        this.recordService = recordService;
    }

    @Behaviour(kind = BehaviourKind.CLASS, notificationFrequency = NotificationFrequency.FIRST_EVENT)
    @Override
    public void onUpdateNode(final NodeRef nodeRef)
    {
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            @Override
            public Void doWork()
            {
                final NodeRef child = nodeRef;
                if (nodeService.exists(child))
                {
                    NodeRef parentRef = nodeService.getPrimaryParent(child).getParentRef();
                    QName parentType = nodeService.getType(parentRef);
                    boolean isUnfiledRecordContainer = parentType
                                .equals(RecordsManagementModel.TYPE_UNFILED_RECORD_CONTAINER);
                    boolean isUnfiledRecordFolder = parentType
                                .equals(RecordsManagementModel.TYPE_UNFILED_RECORD_FOLDER);
                    if (isUnfiledRecordContainer || isUnfiledRecordFolder)
                    {
                        if (!nodeService.hasAspect(child, ASPECT_FILE_PLAN_COMPONENT))
                        {
                            nodeService.addAspect(child, ASPECT_FILE_PLAN_COMPONENT, null);
                        }
                        if (!nodeService.hasAspect(child, ASPECT_RECORD))
                        {
                            recordService.makeRecord(child);
                        }
                    }
                }
                return null;
            }
        });
    }

}
