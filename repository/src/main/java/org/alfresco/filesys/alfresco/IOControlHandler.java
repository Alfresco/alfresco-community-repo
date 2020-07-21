/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.filesys.alfresco;

import org.alfresco.filesys.repo.ContentContext;
import org.alfresco.filesys.repo.ContentDiskDriver;
import org.alfresco.jlan.server.SrvSession;
import org.alfresco.jlan.server.filesys.IOControlNotImplementedException;
import org.alfresco.jlan.server.filesys.TreeConnection;
import org.alfresco.jlan.smb.SMBException;
import org.alfresco.jlan.util.DataBuffer;

/**
 * I/O Control Handler Interface
 * 
 * @author gkspencer
 */
public interface IOControlHandler
{
    /**
     * ProcessIOControl
     * 
     * @param sess SrvSession
     * @param tree TreeConnection
     * @param ctrlCode int
     * @param fid int
     * @param dataBuf DataBuffer
     * @param isFSCtrl boolean
     * @param filter int
     * @param contentDriver Object
     * @param contentContext ContentContext
     * @return DataBuffer
     */
    public org.alfresco.jlan.util.DataBuffer processIOControl(SrvSession sess, TreeConnection tree, int ctrlCode, int fid, DataBuffer dataBuf,
            boolean isFSCtrl, int filter, Object contentDriver, ContentContext contentContext)
        throws IOControlNotImplementedException, SMBException;
    
}
