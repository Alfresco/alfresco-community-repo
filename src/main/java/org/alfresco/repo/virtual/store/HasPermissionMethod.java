/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.repo.virtual.store;

import java.util.Set;

import org.alfresco.repo.virtual.ref.AbstractProtocolMethod;
import org.alfresco.repo.virtual.ref.NodeProtocol;
import org.alfresco.repo.virtual.ref.ProtocolMethodException;
import org.alfresco.repo.virtual.ref.Reference;
import org.alfresco.repo.virtual.ref.VirtualProtocol;
import org.alfresco.repo.virtual.template.FilingParameters;
import org.alfresco.repo.virtual.template.FilingRule;
import org.alfresco.repo.virtual.template.VirtualFolderDefinition;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;

public class HasPermissionMethod extends AbstractProtocolMethod<AccessStatus>
{
    private VirtualUserPermissions userPermissions;

    private String permissionToCheck;

    private VirtualFolderDefinitionResolver resolver;

    public HasPermissionMethod(VirtualFolderDefinitionResolver resolver, VirtualUserPermissions userPermissions,
                String permissionToCheck)
    {
        super();
        this.userPermissions = userPermissions;
        this.permissionToCheck = permissionToCheck;
        this.resolver = resolver;
    }

    @Override
    public AccessStatus execute(VirtualProtocol virtualProtocol, Reference reference) throws ProtocolMethodException
    {
        VirtualFolderDefinition definition = resolver.resolveVirtualFolderDefinition(reference);
        FilingRule filingRule = definition.getFilingRule();

        boolean readonly = filingRule.isNullFilingRule()
                    || filingRule.filingNodeRefFor(new FilingParameters(reference)) == null;
        if (readonly)
        {
            Set<String> deniedPermissions = userPermissions.getDenyReadonlySmartNodes();
            if (deniedPermissions.contains(permissionToCheck))
            {
                return AccessStatus.DENIED;
            }
            
            if (PermissionService.READ.equals(permissionToCheck))
            {
                return AccessStatus.ALLOWED;
            }
        }

        return userPermissions.hasVirtualNodePermission(permissionToCheck,
                                                        readonly);
    }

    @Override
    public AccessStatus execute(NodeProtocol protocol, Reference reference) throws ProtocolMethodException
    {
        return userPermissions.hasQueryNodePermission(permissionToCheck);
    }
}
