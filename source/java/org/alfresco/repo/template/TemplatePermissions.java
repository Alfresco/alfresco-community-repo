package org.alfresco.repo.template;

import java.util.List;

/**
 * Contract for Template API objects that support permissions.
 * 
 * @author Kevin Roast
 */
public interface TemplatePermissions extends TemplateNodeRef
{
    /**
     * @return List of permissions applied to this Node.
     *         Strings returned are of the format [ALLOWED|DENIED];[USERNAME|GROUPNAME];PERMISSION for example
     *         ALLOWED;kevinr;Consumer so can be easily tokenized on the ';' character.
     */
    public List<String> getPermissions();
    
    /**
     * @return true if this node inherits permissions from its parent node, false otherwise.
     */
    public boolean getInheritsPermissions();
    
    /**
     * @param permission        Permission name to test
     * 
     * @return true if the current user is granted the specified permission on the node
     */
    public boolean hasPermission(String permission);
}
