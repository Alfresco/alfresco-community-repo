
package org.alfresco.repo.virtual.store;

import java.util.HashSet;
import java.util.Set;

import org.alfresco.repo.security.permissions.impl.AccessPermissionImpl;
import org.alfresco.repo.virtual.ref.AbstractProtocolMethod;
import org.alfresco.repo.virtual.ref.NodeProtocol;
import org.alfresco.repo.virtual.ref.ProtocolMethodException;
import org.alfresco.repo.virtual.ref.Reference;
import org.alfresco.repo.virtual.ref.VirtualProtocol;
import org.alfresco.repo.virtual.template.FilingParameters;
import org.alfresco.repo.virtual.template.FilingRule;
import org.alfresco.repo.virtual.template.VirtualFolderDefinition;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;

public class GetAllSetPermissionsMethod extends AbstractProtocolMethod<Set<AccessPermission>>
{
    private VirtualUserPermissions userPermissions;

    private String authority;

    private VirtualFolderDefinitionResolver resolver;

    public GetAllSetPermissionsMethod(VirtualFolderDefinitionResolver resolver, VirtualUserPermissions userPermissions,
                String authority)
    {
        super();
        this.userPermissions = userPermissions;
        this.authority = authority;
        this.resolver = resolver;
    }

    @Override
    public Set<AccessPermission> execute(VirtualProtocol virtualProtocol, Reference reference)
                throws ProtocolMethodException
    {
        Set<String> toAllow = userPermissions.getAllowSmartNodes();
        Set<String> toDeny = userPermissions.getDenySmartNodes();

        VirtualFolderDefinition definition = resolver.resolveVirtualFolderDefinition(reference);
        FilingRule filingRule = definition.getFilingRule();
        boolean readonly = filingRule.isNullFilingRule()
                    || filingRule.filingNodeRefFor(new FilingParameters(reference)) == null;
        if (readonly)
        {
            Set<String> deniedPermissions = userPermissions.getDenyReadonlySmartNodes();
            toDeny = new HashSet<>(toDeny);
            toDeny.addAll(deniedPermissions);
            toAllow.add(PermissionService.READ);
        }
        

        return execute(reference,
                       toAllow,
                       toDeny);
    }

    private Set<AccessPermission> execute(Reference reference, Set<String> toAllow, Set<String> toDeny)
    {
        Set<AccessPermission> permissions = new HashSet<>();

        for (String permission : toAllow)
        {
            permissions.add(new AccessPermissionImpl(permission,
                                                     AccessStatus.ALLOWED,
                                                     authority,
                                                     0));
        }

        for (String permission : toDeny)
        {

            permissions.add(new AccessPermissionImpl(permission,
                                                     AccessStatus.DENIED,
                                                     authority,
                                                     0));
        }

        return permissions;
    }

    @Override
    public Set<AccessPermission> execute(NodeProtocol protocol, Reference reference) throws ProtocolMethodException
    {
        Set<String> toAllow = userPermissions.getAllowQueryNodes();
        Set<String> toDeny = userPermissions.getDenyQueryNodes();

        return execute(reference,
                       toAllow,
                       toDeny);
    }
}
