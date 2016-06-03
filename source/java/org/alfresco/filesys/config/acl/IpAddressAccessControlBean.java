
package org.alfresco.filesys.config.acl;

import org.alfresco.jlan.server.auth.acl.AccessControl;
import org.alfresco.jlan.server.auth.acl.IpAddressAccessControl;

/**
 * Simple description of a JLAN IP Address Access control that can be configured via JMX or a Spring bean definition.
 */
public class IpAddressAccessControlBean extends AccessControlBean
{
    /** The subnet. */
    private String subnet;

    /** The network mask. */
    private String netMask;

    /**
     * Sets the subnet.
     * 
     * @param subnet
     *            the subnet
     */
    public void setSubnet(String subnet)
    {
        this.subnet = subnet;
    }

    /**
     * Sets the network mask.
     * 
     * @param mask
     *            the network mask
     */
    public void setNetMask(String mask)
    {
        this.netMask = mask;
    }

    /**
     * Gets the subnet.
     * 
     * @return the subnet
     */
    public String getSubnet()
    {
        return this.subnet;
    }

    /**
     * Gets the network mask.
     * 
     * @return the network mask
     */
    public String getNetMask()
    {
        return this.netMask;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.filesys.config.acl.AccessControlBean#toAccessControl()
     */
    @Override
    public AccessControl toAccessControl()
    {
        return new IpAddressAccessControl(getSubnet(), getNetMask(), "address", getAccessLevel());
    }

}
