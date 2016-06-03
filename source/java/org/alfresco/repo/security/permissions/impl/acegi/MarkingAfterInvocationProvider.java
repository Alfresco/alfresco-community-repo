package org.alfresco.repo.security.permissions.impl.acegi;

import java.util.Collection;

import net.sf.acegisecurity.AccessDeniedException;
import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.ConfigAttribute;
import net.sf.acegisecurity.ConfigAttributeDefinition;
import net.sf.acegisecurity.afterinvocation.AfterInvocationProvider;

import org.alfresco.repo.security.permissions.PermissionCheckedValue;
import org.alfresco.repo.security.permissions.PermissionCheckedValue.PermissionCheckedValueMixin;

/**
 * Invocation provider that can be used to mark entries that have been permission checked.
 * Use an instance of this class at the end of the 'after' invocations.
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public class MarkingAfterInvocationProvider implements AfterInvocationProvider
{

    @Override
    public Object decide(
            Authentication authentication,
            Object object,
            ConfigAttributeDefinition config,
            Object returnedObject) throws AccessDeniedException
    {
        // If this object has already been marked, then leave it
        if (returnedObject == null)
        {
            return null;
        }
        else if (returnedObject instanceof PermissionCheckedValue)
        {
            return returnedObject;
        }
        else if (object instanceof Collection<?>)
        {
            // Mark it
            return PermissionCheckedValueMixin.create(returnedObject);
        }
        else
        {
            return returnedObject;
        }
    }

    @Override
    public boolean supports(ConfigAttribute attribute)
    {
        return true;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean supports(Class clazz)
    {
        return true;
    }
}
