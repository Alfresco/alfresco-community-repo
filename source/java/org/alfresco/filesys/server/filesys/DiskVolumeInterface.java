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
 * Disk Volume Interface
 * <p>
 * Optional interface that a DiskInterface driver can implement to provide disk volume information.
 * The disk volume information may also be specified via the configuration.
 */
public interface DiskVolumeInterface
{

    /**
     * Return the disk device volume information.
     * 
     * @param ctx DiskDeviceContext
     * @return VolumeInfo
     */
    public VolumeInfo getVolumeInformation(DiskDeviceContext ctx);
}
