
package org.alfresco.filesys.config.acl;

import org.alfresco.jlan.server.auth.acl.AccessControl;
import org.alfresco.jlan.server.auth.acl.ProtocolAccessControl;

/**
 * Simple description of a JLAN Protocol Access control that can be configured via JMX or a Spring bean definition.
 */
public class ProtocolAccessControlBean extends AccessControlBean
{
    /** The list of protocol types. */
    private String checkList;

    /**
     * Sets the list of protocol types.
     * 
     * @param protList
     *            the list of protocol types
     */
    public void setCheckList(String protList)
    {
        this.checkList = protList;
    }

    /**
     * Gets the list of protocol types
     * 
     * @return the list of protocol types
     */
    public String getCheckList()
    {
        return this.checkList;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.filesys.config.acl.AccessControlBean#toAccessControl()
     */
    @Override
    public AccessControl toAccessControl()
    {
        return new ProtocolAccessControl(getCheckList(), "protocol", getAccessLevel());
    }
}
