/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.filesys.server.filesys;

import org.alfresco.filesys.server.core.DeviceInterface;
import org.alfresco.filesys.server.core.ShareType;
import org.alfresco.filesys.server.core.SharedDevice;

/**
 * <p>
 * A disk shared device has a name, a driver class and a context for the driver.
 */
public class DiskSharedDevice extends SharedDevice
{

    /**
     * Construct a disk share with the specified name and device interface.
     * 
     * @param name Disk share name.
     * @param iface Disk device interface.
     * @param ctx Context that will be passed to the device interface.
     */
    public DiskSharedDevice(String name, DeviceInterface iface, DiskDeviceContext ctx)
    {
        super(name, ShareType.DISK, ctx);
        setInterface(iface);
    }

    /**
     * Construct a disk share with the specified name and device interface.
     * 
     * @param name java.lang.String
     * @param iface DeviceInterface
     * @param ctx DeviceContext
     * @param attrib int
     */
    public DiskSharedDevice(String name, DeviceInterface iface, DiskDeviceContext ctx, int attrib)
    {
        super(name, ShareType.DISK, ctx);
        setInterface(iface);
        setAttributes(attrib);
    }

    /**
     * Return the disk device context
     * 
     * @return DiskDeviceContext
     */
    public final DiskDeviceContext getDiskContext()
    {
        return (DiskDeviceContext) getContext();
    }

    /**
     * Return the disk interface
     * 
     * @return DiskInterface
     */
    public final DiskInterface getDiskInterface()
    {
        try
        {
            if (getInterface() instanceof DiskInterface)
                return (DiskInterface) getInterface();
        }
        catch (Exception ex)
        {
        }
        return null;
    }
}