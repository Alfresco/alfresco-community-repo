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

package org.alfresco.filesys.smb.server.repo;

import org.alfresco.filesys.server.filesys.IOCtlInterface;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;

/**
 * I/O Control Handler Interface
 * 
 * @author gkspencer
 */
public interface IOControlHandler extends IOCtlInterface
{
    /**
     * Initialize the I/O control handler
     * 
     *
     * @param contentDriver ContentDiskDriver
     * @param contentContext ContentContext
     */
    public void initialize( ContentDiskDriver contentDriver, ContentContext contentContext);
}
