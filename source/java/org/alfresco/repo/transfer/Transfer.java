/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
package org.alfresco.repo.transfer;

import org.alfresco.service.cmr.transfer.TransferTarget;

/**
 * Information about a transfer which is in progress.
 *
 * @author Mark Rogers
 */
public class Transfer
{
    private String transferId;
    private TransferTarget transferTarget;

    public void setTransferId(String transferId)
    {
        this.transferId = transferId;
    }

    public String getTransferId()
    {
        return transferId;
    }
    
    // may also have capabilities of the remote system here (for when we are 
    // transfering accross versions)
    
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        else if (this == obj)
        {
            return true;
        }
        else if (obj instanceof Transfer == false)
        {
            return false;
        }
        Transfer that = (Transfer) obj;
        return (this.transferId.equals(that.getTransferId()));
    }
    
    public int hashCode()
    {
        return transferId.hashCode();
    }

    /**
     * @param target
     */
    public void setTransferTarget(TransferTarget target)
    {
        this.transferTarget = target;
    }

    /**
     * @return the transferTarget
     */
    public TransferTarget getTransferTarget()
    {
        return transferTarget;
    }
    
    public String toString()
    {
        return "TransferId" + transferId + ", target:" + transferTarget ;
    }
}
