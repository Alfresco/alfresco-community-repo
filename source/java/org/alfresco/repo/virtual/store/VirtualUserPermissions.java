
package org.alfresco.repo.virtual.store;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.namespace.QName;

/**
 * Configuration bean for virtual references overridden permissions.
 *
 * @author Dinuta Silviu
 */
public class VirtualUserPermissions
{
    private Set<String> allowSmartNodes = Collections.emptySet();

    private Set<String> allowSmartNodesFull = Collections.emptySet();

    private Set<String> denySmartNodes = Collections.emptySet();

    private Set<String> denySmartNodesFull = Collections.emptySet();

    private Set<String> denyReadonlySmartNodes = Collections.emptySet();

    private Set<String> denyReadonlySmartNodesFull = Collections.emptySet();

    private Set<String> allowQueryNodes = Collections.emptySet();

    private Set<String> allowQueryNodesFull = Collections.emptySet();

    private Set<String> denyQueryNodes = Collections.emptySet();

    private Set<String> denyQueryNodesFull = Collections.emptySet();

    private QName permissionTypeQName = ContentModel.TYPE_BASE;

    public VirtualUserPermissions()
    {

    }

    public VirtualUserPermissions(VirtualUserPermissions userPermissions)
    {
        this.allowSmartNodes = new HashSet<>(userPermissions.allowSmartNodes);
        this.denySmartNodes = new HashSet<>(userPermissions.denySmartNodes);
        this.allowQueryNodes = new HashSet<>(userPermissions.allowQueryNodes);
        this.denyQueryNodes = new HashSet<>(userPermissions.denyQueryNodes);
        this.denyReadonlySmartNodes = new HashSet<>(userPermissions.denyReadonlySmartNodes);
        init();
    }

    private Set<String> asFullNamePermissions(Set<String> permissions)
    {
        Set<String> fullPermissions = new HashSet<String>();
        for (String s : permissions)
        {
            fullPermissions.add(permissionTypeQName + "." + s);
        }

        return fullPermissions;
    }

    public void init()
    {
        this.allowQueryNodesFull = asFullNamePermissions(allowQueryNodes);
        this.allowSmartNodesFull = asFullNamePermissions(allowSmartNodes);
        this.denyQueryNodesFull = asFullNamePermissions(denyQueryNodes);
        this.denySmartNodesFull = asFullNamePermissions(denySmartNodes);
        this.denyReadonlySmartNodesFull = asFullNamePermissions(denyReadonlySmartNodes);
    }

    public QName getPermissionTypeQName()
    {
        return this.permissionTypeQName;
    }

    public AccessStatus hasVirtualNodePermission(String permission, boolean readonly)
    {
        if (readonly)
        {
            if (denyReadonlySmartNodesFull.contains(permission) || denyReadonlySmartNodes.contains(permission))
            {
                return AccessStatus.DENIED;
            }
        }

        if (denySmartNodesFull.contains(permission) || denySmartNodes.contains(permission))
        {
            return AccessStatus.DENIED;
        }
        else if (allowSmartNodesFull.contains(permission) || allowSmartNodes.contains(permission))
        {
            return AccessStatus.ALLOWED;
        }
        else
        {
            return AccessStatus.UNDETERMINED;
        }
    }

    public AccessStatus hasQueryNodePermission(String permission)
    {
        if (denyQueryNodesFull.contains(permission) || denyQueryNodes.contains(permission))
        {
            return AccessStatus.DENIED;
        }
        else if (allowQueryNodesFull.contains(permission) || allowQueryNodes.contains(permission))
        {
            return AccessStatus.ALLOWED;
        }
        else
        {
            return AccessStatus.UNDETERMINED;
        }
    }

    public Set<String> getAllowSmartNodes()
    {
        return this.allowSmartNodes;
    }

    public void setAllowSmartNodes(Set<String> allowFolders)
    {
        this.allowSmartNodes = allowFolders;
    }

    public Set<String> getDenySmartNodes()
    {
        return this.denySmartNodes;
    }

    public void setDenySmartNodes(Set<String> denyFolders)
    {
        this.denySmartNodes = denyFolders;
    }

    public void setDenyReadonlySmartNodes(Set<String> denyReadonlySmartNodes)
    {
        this.denyReadonlySmartNodes = denyReadonlySmartNodes;
    }

    public Set<String> getDenyReadonlySmartNodes()
    {
        return this.denyReadonlySmartNodes;
    }

    public Set<String> getAllowQueryNodes()
    {
        return this.allowQueryNodes;
    }

    public void setAllowQueryNodes(Set<String> allowDocuments)
    {
        this.allowQueryNodes = allowDocuments;
    }

    public Set<String> getDenyQueryNodes()
    {
        return this.denyQueryNodes;
    }

    public void setDenyQueryNodes(Set<String> denyDocuments)
    {
        this.denyQueryNodes = denyDocuments;
    }
}