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

import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.util.AuthenticationUtil;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * protected aspects behaviour bean 
 * allow only System user to remove this aspects
 *
 * @author Ramona Popa
 * @since 2.6
 */

public class ProtectedAspects implements NodeServicePolicies.OnRemoveAspectPolicy
{
    private PolicyComponent policyComponent;
    private AuthenticationUtil authenticationUtil;

    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    public void setAuthenticationUtil(AuthenticationUtil authenticationUtil)
    {
        this.authenticationUtil = authenticationUtil;
    }

    /**
     * Initialise method
     */
    public void init()
    {
        // Watch removal of the aspect rma:record
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnRemoveAspectPolicy.QNAME,
                RecordsManagementModel.ASPECT_RECORD,
                new JavaBehaviour(this, "onRemoveAspect"));
        // Watch removal of the aspect rma:filePlanComponent
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnRemoveAspectPolicy.QNAME,
                RecordsManagementModel.ASPECT_FILE_PLAN_COMPONENT,
                new JavaBehaviour(this, "onRemoveAspect"));
        // Watch removal of the aspect rma:recordComponentIdentifier
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnRemoveAspectPolicy.QNAME,
                RecordsManagementModel.ASPECT_RECORD_COMPONENT_ID,
                new JavaBehaviour(this, "onRemoveAspect"));
        // Watch removal of the aspect  rma:commonRecordDetails
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnRemoveAspectPolicy.QNAME,
                RecordsManagementModel.ASPECT_COMMON_RECORD_DETAILS,
                new JavaBehaviour(this, "onRemoveAspect"));

    }

    @Override
    public void onRemoveAspect(NodeRef nodeRef, QName aspectTypeQName)
    {
        if (!authenticationUtil.getRunAsUser().equals(authenticationUtil.getSystemUserName()))
        {
            throw new IntegrityException("Operation failed. Aspect " + aspectTypeQName.toString() + " is mandatory and cannot be removed.", null);
        }

    }
}
