
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
