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
package org.alfresco.filesys.smb.dcerpc.info;

import java.util.Vector;

import org.alfresco.filesys.smb.dcerpc.DCEBuffer;
import org.alfresco.filesys.smb.dcerpc.DCEBufferException;
import org.alfresco.filesys.smb.dcerpc.DCEList;
import org.alfresco.filesys.smb.dcerpc.DCEReadable;

/**
 * Server Share Information List Class
 * <p>
 * Holds the details for a DCE/RPC share enumeration request or response.
 */
public class ShareInfoList extends DCEList
{

    /**
     * Default constructor
     */
    public ShareInfoList()
    {
        super();
    }

    /**
     * Class constructor
     * 
     * @param buf DCEBuffer
     * @exception DCEBufferException
     */
    public ShareInfoList(DCEBuffer buf) throws DCEBufferException
    {
        super(buf);
    }

    /**
     * Class constructor
     * 
     * @param infoLevel int
     */
    public ShareInfoList(int infoLevel)
    {
        super(infoLevel);
    }

    /**
     * Return share information object from the list
     * 
     * @param idx int
     * @return ShareInfo
     */
    public final ShareInfo getShare(int idx)
    {
        return (ShareInfo) getElement(idx);
    }

    /**
     * Create a new share information object
     * 
     * @return DCEReadable
     */
    protected DCEReadable getNewObject()
    {
        return new ShareInfo(getInformationLevel());
    }

    /**
     * Add a share to the list
     * 
     * @param share ShareInfo
     */
    public final void addShare(ShareInfo share)
    {

        // Check if the share list is valid

        if (getList() == null)
            setList(new Vector());

        // Add the share

        getList().add(share);
    }

    /**
     * Set the share information list
     * 
     * @param list Vector
     */
    public final void setShareList(Vector list)
    {
        setList(list);
    }
}
