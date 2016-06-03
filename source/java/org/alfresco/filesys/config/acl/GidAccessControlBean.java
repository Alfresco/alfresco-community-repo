
package org.alfresco.filesys.config.acl;

import org.alfresco.jlan.server.auth.acl.AccessControl;
import org.alfresco.jlan.server.auth.acl.GidAccessControl;

/**
 * Simple description of a JLAN Group ID Access control that can be configured via JMX or a Spring bean definition.
 */
public class GidAccessControlBean extends AccessControlBean
{
    /** The group id to check for. */
    private int gid;

    /**
     * Sets the group id to check for
     * 
     * @param gid
     *            the group id to check for
     */
    public final void setGid(int gid)
    {
        this.gid = gid;
    }

    /**
     * Gets the group id to check for.
     * 
     * @return the group id to check for
     */
    public final int getGid()
    {
        return this.gid;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.filesys.config.acl.AccessControlBean#toAccessControl()
     */
    @Override
    public AccessControl toAccessControl()
    {
        return new GidAccessControl(getName(), getGid(), "gid", getAccessLevel());
    }
}
