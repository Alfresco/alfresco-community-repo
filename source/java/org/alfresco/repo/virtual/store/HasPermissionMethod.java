/* 
 * Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see http://www.gnu.org/licenses/.
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
            Set<String> deniedPermissions = userPermissions.getDenyReadonlyVirtualNodes();
            if (deniedPermissions.contains(permissionToCheck))
            {
                return AccessStatus.DENIED;
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
