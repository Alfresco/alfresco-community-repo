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

/**
 * Disk Size Interface
 * <p>
 * Optional interface that a DiskInterface driver can implement to provide disk sizing information.
 * The disk size information may also be specified via the configuration.
 */
public interface DiskSizeInterface
{

    /**
     * Get the disk information for this shared disk device.
     * 
     * @param cts DiskDeviceContext
     * @param diskDev SrvDiskInfo
     * @exception java.io.IOException The exception description.
     */
    public void getDiskInformation(DiskDeviceContext ctx, SrvDiskInfo diskDev) throws java.io.IOException;
}
