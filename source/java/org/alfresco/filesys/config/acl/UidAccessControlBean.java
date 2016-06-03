
package org.alfresco.filesys.config.acl;

import org.alfresco.jlan.server.auth.acl.AccessControl;
import org.alfresco.jlan.server.auth.acl.UidAccessControl;

/**
 * Simple description of a JLAN User ID Access control that can be configured via JMX or a Spring bean definition.
 */
public class UidAccessControlBean extends AccessControlBean
{

    /** The user id. */
    private int uid;

    /**
     * Sets the user id.
     * 
     * @param uid
     *            the user id
     */
    public void setUid(int uid)
    {
        this.uid = uid;
    }

    /**
     * Gets the user id.
     * 
     * @return the user id
     */
    public int getUid()
    {
        return this.uid;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.filesys.config.acl.AccessControlBean#toAccessControl()
     */
    @Override
    public AccessControl toAccessControl()
    {
        return new UidAccessControl(getName(), getUid(), "uid", getAccessLevel());
    }
}
