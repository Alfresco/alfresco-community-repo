 
package org.alfresco.repo.security.permissions.impl.acegi;

import java.lang.reflect.Method;

import org.alfresco.service.cmr.security.OwnableService;

/**
 * This is a workaround to make RM 2.1 backwards compatible with the Community version 4.2.d.
 * This class will be removed after Community 4.2.e has been released.
 *
 * @author Tuna Aksoy
 * @since 2.1
 */
public class RMACLEntryVoter extends ACLEntryVoter
{
    public void setOwnableService(OwnableService ownableService)
    {
        boolean exists = false;
        Method[] declaredMethods = ACLEntryVoter.class.getDeclaredMethods();
        for (Method method : declaredMethods)
        {
            if (method.getName().equals("setOwnableService"))
            {
                exists = true;
                break;
            }
        }
        if (exists)
        {
            super.setOwnableService(ownableService);
        }
    }
}
