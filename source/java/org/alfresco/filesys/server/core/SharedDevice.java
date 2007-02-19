/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.filesys.server.core;

import org.alfresco.filesys.server.auth.acl.AccessControl;
import org.alfresco.filesys.server.auth.acl.AccessControlList;

/**
 * <p>
 * The shared device class is the base class for all shared device implementations.
 */
public class SharedDevice implements Comparable
{
    // Share attribute types

    public static final int Admin = 0x0001;
    public static final int Hidden = 0x0002;
    public static final int ReadOnly = 0x0004;
    public static final int Temporary = 0x0008;

    // Shared device name

    private String m_name;

    // Shared device type

    private int m_type;

    // Comment

    private String m_comment;

    // Device interface and context object

    private DeviceInterface m_interface;
    private DeviceContext m_drvCtx;

    // Share attributes

    private int m_attrib;

    // Current and maximum connections to this shared device

    private int m_maxUses = -1; // unlimited
    private int m_curUses;

    // Access control list

    private AccessControlList m_acls;

    /**
     * SharedDevice constructor.
     * 
     * @param name Shared device name.
     * @param typ Share device type, as specified by class ShareType.
     * @param ctx Context object that will be passed to the interface.
     */
    protected SharedDevice(String name, int typ, DeviceContext ctx)
    {

        // Set the shared name and device type

        setName(name);
        setType(typ);
        setContext(ctx);
    }

    /**
     * Return the shared device attribtues.
     * 
     * @return int
     */
    public final int getAttributes()
    {
        return m_attrib;
    }

    /**
     * Determine if the shared device has any access controls configured
     * 
     * @return boolean
     */
    public final boolean hasAccessControls()
    {
        if (m_acls == null)
            return false;
        return true;
    }

    /**
     * Return the access control list
     * 
     * @return AccessControlList
     */
    public final AccessControlList getAccessControls()
    {
        return m_acls;
    }

    /**
     * Check if the shared device has a comment
     * 
     * @return boolean
     */
    public final boolean hasComment()
    {
        return m_comment != null ? true : false;
    }

    /**
     * Return the shared device comment.
     * 
     * @return java.lang.String
     */
    public final String getComment()
    {
        return m_comment;
    }

    /**
     * Return the device interface specific context object.
     * 
     * @return Device context.
     */
    public final DeviceContext getContext()
    {
        return m_drvCtx;
    }

    /**
     * Return the device interface for this shared device.
     * 
     * @return DeviceInterface
     */
    public DeviceInterface getInterface() throws InvalidDeviceInterfaceException
    {
        return m_interface;
    }

    /**
     * Return the shared device name.
     * 
     * @return java.lang.String
     */
    public final String getName()
    {
        return m_name;
    }

    /**
     * Return the shared device type, as specified by the ShareType class.
     * 
     * @return int
     */
    public int getType()
    {
        return m_type;
    }

    /**
     * Return the current connection count for the share
     * 
     * @return int
     */
    public final int getCurrentConnectionCount()
    {
        return m_curUses;
    }

    /**
     * Return the maximum connection count for the share
     * 
     * @return int
     */
    public final int getMaximumConnectionCount()
    {
        return m_maxUses;
    }

    /**
     * Generates a hash code for the receiver. This method is supported primarily for hash tables,
     * such as those provided in java.util.
     * 
     * @return an integer hash code for the receiver
     * @see java.util.Hashtable
     */
    public int hashCode()
    {

        // Use the share name to generate the hash code.

        return getName().hashCode();
    }

    /**
     * Determine if this is an admin share.
     * 
     * @return boolean
     */
    public final boolean isAdmin()
    {
        return (m_attrib & Admin) == 0 ? false : true;
    }

    /**
     * Determine if this is a hidden share.
     * 
     * @return boolean
     */
    public final boolean isHidden()
    {
        return (m_attrib & Hidden) == 0 ? false : true;
    }

    /**
     * Determine if the share is read-only.
     * 
     * @return boolean
     */
    public final boolean isReadOnly()
    {
        return (m_attrib & ReadOnly) == 0 ? false : true;
    }

    /**
     * Determine if the share is a temporary share
     * 
     * @return boolean
     */
    public final boolean isTemporary()
    {
        return (m_attrib & Temporary) == 0 ? false : true;
    }

    /**
     * Set the shared device comment string.
     * 
     * @param comm java.lang.String
     */
    public final void setComment(String comm)
    {
        m_comment = comm;
    }

    /**
     * Set the shared device attributes.
     * 
     * @param attr int
     */
    public final void setAttributes(int attr)
    {
        m_attrib = attr;
    }

    /**
     * Set the context that is passed to the device interface.
     * 
     * @param ctx DeviceContext
     */
    protected void setContext(DeviceContext ctx)
    {
        m_drvCtx = ctx;
    }

    /**
     * Set the device interface for this shared device.
     * 
     * @param iface DeviceInterface
     */
    protected final void setInterface(DeviceInterface iface)
    {
        m_interface = iface;
    }

    /**
     * Set the shared device name.
     * 
     * @param name java.lang.String Shared device name.
     */
    protected final void setName(String name)
    {
        m_name = name;
    }

    /**
     * Set the shared device type.
     * 
     * @param typ int Shared device type, as specified by class ShareType.
     */
    protected final void setType(int typ)
    {
        m_type = typ;
    }

    /**
     * Set the maximum connection coutn for this shared device
     * 
     * @param maxConn int
     */
    public final void setMaximumConnectionCount(int maxConn)
    {
        m_maxUses = maxConn;
    }

    /**
     * Set the access control list using the specified list
     * 
     * @param acls AccessControlList
     */
    public final void setAccessControlList(AccessControlList acls)
    {
        m_acls = acls;
    }

    /**
     * Add an access control to the shared device
     * 
     * @param acl AccessControl
     */
    public final void addAccessControl(AccessControl acl)
    {

        // Check if the access control list has been allocated

        if (m_acls == null)
            m_acls = new AccessControlList();

        // Add the access control

        m_acls.addControl(acl);
    }

    /**
     * Remove an access control
     * 
     * @param idx int
     * @return AccessControl
     */
    public final AccessControl removeAccessControl(int idx)
    {

        // validate the index

        if (m_acls == null || idx < 0 || idx >= m_acls.numberOfControls())
            return null;

        // Remove the access control

        return m_acls.removeControl(idx);
    }

    /**
     * Remove all access controls from this shared device
     */
    public final void removeAllAccessControls()
    {
        if (m_acls != null)
        {
            m_acls.removeAllControls();
            m_acls = null;
        }
    }

    /**
     * Parse and validate the parameters string and create a device context for the shared device.
     * 
     * @param args String[]
     * @return DeviceContext
     */
    public DeviceContext createContext(String[] args)
    {
        return new DeviceContext("", args[0]);
    }

    /**
     * Increment the connection count for the share
     */
    public synchronized void incrementConnectionCount()
    {
        m_curUses++;
    }

    /**
     * Decrement the connection count for the share
     */
    public synchronized void decrementConnectionCount()
    {
        m_curUses--;
    }

    /**
     * Compare this shared device to another shared device using the device name
     * 
     * @param obj Object
     */
    public int compareTo(Object obj)
    {
        if (obj instanceof SharedDevice)
        {
            SharedDevice sd = (SharedDevice) obj;
            return getName().compareTo(sd.getName());
        }
        return -1;
    }

    /**
     * Compares two objects for equality. Returns a boolean that indicates whether this object is
     * equivalent to the specified object. This method is used when an object is stored in a
     * hashtable.
     * 
     * @param obj the Object to compare with
     * @return true if these Objects are equal; false otherwise.
     * @see java.util.Hashtable
     */
    public boolean equals(Object obj)
    {

        // Check if the object is a SharedDevice

        if (obj instanceof SharedDevice)
        {

            // Check if the share names are equal

            SharedDevice shr = (SharedDevice) obj;
            if (getName().compareTo(shr.getName()) == 0)
                return true;
        }

        // Object type, or share name is not equal

        return false;
    }

    /**
     * Returns a String that represents the value of this object.
     * 
     * @return a string representation of the receiver
     */
    public String toString()
    {

        // Build a string that represents this shared device

        StringBuffer str = new StringBuffer();
        str.append("[");
        str.append(getName());
        str.append(",");
        str.append(ShareType.TypeAsString(getType()));
        str.append(",");

        if (hasAccessControls())
        {
            str.append("ACLs=");
            str.append(m_acls.numberOfControls());
        }

        if (isAdmin())
            str.append(",Admin");

        if (isHidden())
            str.append(",Hidden");

        if (isReadOnly())
            str.append(",ReadOnly");

        if (isTemporary())
            str.append(",Temp");

        if (getContext() != null && getContext().isAvailable() == false)
            str.append(",Offline");

        if (m_drvCtx != null)
        {
            str.append(",");
            str.append(m_drvCtx.toString());
        }
        str.append("]");

        return str.toString();
    }
}