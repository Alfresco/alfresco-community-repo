package org.alfresco.repo.template;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;

/**
 * Base class for Template API objects that support permissions.
 * 
 * @author Kevin Roast
 */
public abstract class BasePermissionsNode extends BaseContentNode implements TemplatePermissions
{
    private List<String> permissions = null;
    private List<String> directPermissions = null;
    private List<String> fullPermissions = null;
    
    // ------------------------------------------------------------------------------
    // Security API 
    
    /**
     * @return List of permissions applied to this Node, including inherited.
     *         Strings returned are of the format [ALLOWED|DENIED];[USERNAME|GROUPNAME];PERMISSION for example
     *         ALLOWED;kevinr;Consumer so can be easily tokenized on the ';' character.
     */
    public List<String> getPermissions()
    {
        if (this.permissions == null)
        {
            this.permissions = retrieveAllSetPermissions(false, false);
        }
        return this.permissions;
    }

    /**
     * @return List of permissions applied to this Node (does not include inherited).
     *         Strings returned are of the format [ALLOWED|DENIED];[USERNAME|GROUPNAME];PERMISSION for example
     *         ALLOWED;kevinr;Consumer so can be easily tokenized on the ';' character.
     */
    public List<String> getDirectPermissions()
    {
        if (this.directPermissions == null)
        {
            this.directPermissions = retrieveAllSetPermissions(true, false);
        }
        return this.directPermissions;
    }

    /**
     * @return List of permissions applied to this Node, including inherited.
     *         Strings returned are of the format [ALLOWED|DENIED];[USERNAME|GROUPNAME];PERMISSION;[INHERITED|DIRECT] for example
     *         ALLOWED;kevinr;Consumer so can be easily tokenized on the ';' character.
     */
    public List<String> getFullPermissions()
    {
        if (this.fullPermissions == null)
        {
            this.fullPermissions = retrieveAllSetPermissions(false, true);
        }
        return this.fullPermissions;
    }

    /**
     * Helper to construct the response object for the various getPermissions() calls.
     * 
     * @param direct    True to only retrieve direct permissions, false to get inherited also
     * @param full      True to retrieve full data string with [INHERITED|DIRECT] element
     *                  This exists to maintain backward compatibility with existing permission APIs.
     * 
     * @return List<String> of permissions.
     */
    private List<String> retrieveAllSetPermissions(boolean direct, boolean full)
    {
        String userName = this.services.getAuthenticationService().getCurrentUserName();
        List<String> permissions = new ArrayList<String>(4);
        if (hasPermission(PermissionService.READ_PERMISSIONS))
        {
            Set<AccessPermission> acls = this.services.getPermissionService().getAllSetPermissions(getNodeRef());
            for (AccessPermission permission : acls)
            {
                if (!direct || permission.isSetDirectly())
                {
                    StringBuilder buf = new StringBuilder(64);
                    buf.append(permission.getAccessStatus())
                        .append(';')
                        .append(permission.getAuthority())
                        .append(';')
                        .append(permission.getPermission());
                    if (full)
                    {
                        buf.append(';').append(permission.isSetDirectly() ? "DIRECT" : "INHERITED");
                    }
                    permissions.add(buf.toString());
                }
            }
        }
        return permissions;
    }
    
    /**
     * @return true if this node inherits permissions from its parent node, false otherwise.
     */
    public boolean getInheritsPermissions()
    {
        return this.services.getPermissionService().getInheritParentPermissions(getNodeRef());
    }
    
    /**
     * @param permission        Permission name to test
     * 
     * @return true if the current user is granted the specified permission on the node
     */
    public boolean hasPermission(String permission)
    {
        return (this.services.getPermissionService().hasPermission(getNodeRef(), permission) == AccessStatus.ALLOWED);
    }
}
