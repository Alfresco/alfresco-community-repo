/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.filesys.server.filesys;

import org.alfresco.filesys.server.core.DeviceContext;
import org.alfresco.filesys.server.core.DeviceContextException;
import org.alfresco.filesys.smb.server.notify.NotifyChangeHandler;
import org.alfresco.filesys.smb.server.notify.NotifyRequest;

/**
 * Disk Device Context Class
 */
public class DiskDeviceContext extends DeviceContext
{

    // Change notification handler

    private NotifyChangeHandler m_changeHandler;

    // Volume information

    private VolumeInfo m_volumeInfo;

    // Disk sizing information

    private SrvDiskInfo m_diskInfo;

    // Filesystem attributes, required to enable features such as compression and encryption

    private int m_filesysAttribs;

    // Disk device attributes, can be used to make the device appear as a removeable, read-only,
    // or write-once device for example.

    private int m_deviceAttribs;

    /**
     * Class constructor
     */
    public DiskDeviceContext()
    {
        super();
    }

    /**
     * Class constructor
     * 
     * @param devName String
     */
    public DiskDeviceContext(String devName)
    {
        super(devName);
    }

    /**
     * Determine if the volume information is valid
     * 
     * @return boolean
     */
    public final boolean hasVolumeInformation()
    {
        return m_volumeInfo != null ? true : false;
    }

    /**
     * Return the volume information
     * 
     * @return VolumeInfo
     */
    public final VolumeInfo getVolumeInformation()
    {
        return m_volumeInfo;
    }

    /**
     * Determine if the disk sizing information is valid
     * 
     * @return boolean
     */
    public final boolean hasDiskInformation()
    {
        return m_diskInfo != null ? true : false;
    }

    /**
     * Return the disk sizing information
     * 
     * @return SMBSrvDiskInfo
     */
    public final SrvDiskInfo getDiskInformation()
    {
        return m_diskInfo;
    }

    /**
     * Return the filesystem attributes
     * 
     * @return int
     */
    public final int getFilesystemAttributes()
    {
        return m_filesysAttribs;
    }

    /**
     * Return the filesystem type, either FileSystem.TypeFAT or FileSystem.TypeNTFS.
     * 
     * Defaults to FileSystem.FAT but will be overridden if the filesystem driver implements the
     * NTFSStreamsInterface.
     * 
     * @return String
     */
    public String getFilesystemType()
    {
        return FileSystem.TypeFAT;
    }
    
    /**
     * Return the device attributes
     * 
     * @return int
     */
    public final int getDeviceAttributes()
    {
        return m_deviceAttribs;
    }

    /**
     * Determine if the filesystem is case sensitive or not
     * 
     * @return boolean
     */
    public final boolean isCaseless()
    {
        return (m_filesysAttribs & FileSystem.CasePreservedNames) == 0 ? true : false;
    }

    /**
     * Enable/disable the change notification handler for this device
     * 
     * @param ena boolean
     */
    public final void enableChangeHandler(boolean ena)
    {
        if (ena == true)
            m_changeHandler = new NotifyChangeHandler(this);
        else
        {

            // Shutdown the change handler, if valid

            if (m_changeHandler != null)
                m_changeHandler.shutdownRequest();
            m_changeHandler = null;
        }
    }

    /**
     * Close the disk device context. Release the file state cache resources.
     */
    public void CloseContext()
    {

        // Call the base class

        super.CloseContext();
    }

    /**
     * Determine if the disk context has a change notification handler
     * 
     * @return boolean
     */
    public final boolean hasChangeHandler()
    {
        return m_changeHandler != null ? true : false;
    }

    /**
     * Return the change notification handler
     * 
     * @return NotifyChangeHandler
     */
    public final NotifyChangeHandler getChangeHandler()
    {
        return m_changeHandler;
    }

    /**
     * Add a request to the change notification list
     * 
     * @param req NotifyRequest
     */
    public final void addNotifyRequest(NotifyRequest req)
    {
        m_changeHandler.addNotifyRequest(req);
    }

    /**
     * Remove a request from the notify change request list
     * 
     * @param req NotifyRequest
     */
    public final void removeNotifyRequest(NotifyRequest req)
    {
        m_changeHandler.removeNotifyRequest(req);
    }

    /**
     * Set the volume information
     * 
     * @param vol VolumeInfo
     */
    public final void setVolumeInformation(VolumeInfo vol)
    {
        m_volumeInfo = vol;
    }

    /**
     * Set the disk information
     * 
     * @param disk SMBSrvDiskInfo
     */
    public final void setDiskInformation(SrvDiskInfo disk)
    {
        m_diskInfo = disk;
    }

    /**
     * Set the filesystem attributes
     * 
     * @param attrib int
     */
    public final void setFilesystemAttributes(int attrib)
    {
        m_filesysAttribs = attrib;
    }

    /**
     * Set the device attributes
     * 
     * @param attrib int
     */
    public final void setDeviceAttributes(int attrib)
    {
        m_deviceAttribs = attrib;
    }

    /**
     * Context has been initialized and attached to a shared device, do any startup processing in
     * this method.
     * 
     * @param share DiskSharedDevice
     * @exception DeviceContextException
     */
    public void startFilesystem(DiskSharedDevice share) throws DeviceContextException
    {
    }
}
