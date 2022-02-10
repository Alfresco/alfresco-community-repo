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

package org.alfresco.repo.jscript;

import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.util.ParameterCheck;

/**
 * Extended jscript search implementation.
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public class ExtendedSearch extends Search
{
    /**
     * Extended to take into account record read permission check.
     * 
     * @see org.alfresco.repo.jscript.Search#findNode(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public ScriptNode findNode(NodeRef ref)
    {
        ParameterCheck.mandatory("ref", ref);       
        if (this.services.getNodeService().exists(ref) &&
            (this.services.getPermissionService().hasPermission(ref, PermissionService.READ) == AccessStatus.ALLOWED ||
             this.services.getPermissionService().hasPermission(ref, RMPermissionModel.READ_RECORDS) == AccessStatus.ALLOWED))
        {
            return new ScriptNode(ref, this.services, getScope());
        }
        return null;
    }

}
