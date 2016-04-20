
package org.alfresco.filesys.config.acl;

import org.alfresco.jlan.server.auth.acl.AccessControl;
import org.alfresco.jlan.server.auth.acl.DomainAccessControl;

/**
 * Simple description of a JLAN Domain Access control that can be configured via JMX or a Spring bean definition.
 */
public class DomainAccessControlBean extends AccessControlBean
{
    /*
     * (non-Javadoc)
     * @see org.alfresco.filesys.config.acl.AccessControlBean#toAccessControl()
     */
    @Override
    public AccessControl toAccessControl()
    {
        return new DomainAccessControl(getName(), "domain", getAccessLevel());
    }
}
