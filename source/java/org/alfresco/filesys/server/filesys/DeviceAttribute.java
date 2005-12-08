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
 * Device Attribute Constants Class
 * <p>
 * Specifies the constants that can be used to set the DiskDeviceContext device attributes.
 */
public final class DeviceAttribute
{
    // Device attributes

    public static final int Removable = 0x0001;
    public static final int ReadOnly = 0x0002;
    public static final int FloppyDisk = 0x0004;
    public static final int WriteOnce = 0x0008;
    public static final int Remote = 0x0010;
    public static final int Mounted = 0x0020;
    public static final int Virtual = 0x0040;
}
