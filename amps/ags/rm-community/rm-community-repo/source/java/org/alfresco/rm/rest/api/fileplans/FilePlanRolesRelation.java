/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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
package org.alfresco.rm.rest.api.fileplans;

import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rm.rest.api.RMRoles;
import org.alfresco.rm.rest.api.impl.FilePlanComponentsApiUtils;
import org.alfresco.rm.rest.api.model.RoleModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.InitializingBean;


import static org.alfresco.util.ParameterCheck.mandatory;

@RelationshipResource(name = "roles", entityResource = FilePlanEntityResource.class, title = "Roles in a file plan")
public class FilePlanRolesRelation implements RelationshipResourceAction.Read<RoleModel>, InitializingBean
{
    private RMRoles rmRoles;
    private FilePlanComponentsApiUtils apiUtils;


    @Override
    public void afterPropertiesSet() throws Exception
    {
        mandatory("rmRoles", this.rmRoles);
        mandatory("apiUtils", this.apiUtils);
    }

    @Override
    public CollectionWithPagingInfo<RoleModel> readAll(String filePlanId, Parameters params)
    {
        NodeRef filePlanNodeRef = apiUtils.lookupAndValidateNodeType(filePlanId, RecordsManagementModel.TYPE_FILE_PLAN);
        return rmRoles.getRoles(filePlanNodeRef, params);
    }

    public void setRmRoles(RMRoles rmRoles)
    {
        this.rmRoles = rmRoles;
    }

    public void setApiUtils(FilePlanComponentsApiUtils apiUtils)
    {
        this.apiUtils = apiUtils;
    }
}
