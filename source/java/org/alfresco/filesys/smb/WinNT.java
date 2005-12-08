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
package org.alfresco.filesys.smb;

/**
 * Windows NT Constants Class
 */
public class WinNT
{

    // Compression format types

    public final static int CompressionFormatNone = 0;
    public final static int CompressionFormatDefault = 1;
    public final static int CompressionFormatLZNT1 = 2;

    // Get/set security descriptor flags

    public final static int SecurityOwner = 0x0001;
    public final static int SecurityGroup = 0x0002;
    public final static int SecurityDACL = 0x0004;
    public final static int SecuritySACL = 0x0008;

    // Security impersonation levels

    public static final int SecurityAnonymous = 0;
    public static final int SecurityIdentification = 1;
    public static final int SecurityImpersonation = 2;
    public static final int SecurityDelegation = 3;

    // Security flags

    public static final int SecurityContextTracking = 0x00040000;
    public static final int SecurityEffectiveOnly = 0x00080000;

    // NTCreateAndX flags (oplocks/target)

    public static final int RequestOplock = 0x0002;
    public static final int RequestBatchOplock = 0x0004;
    public static final int TargetDirectory = 0x0008;
    public static final int ExtendedResponse = 0x0010;

    // NTCreateAndX create options flags

    public static final int CreateFile = 0x00000000;
    public static final int CreateDirectory = 0x00000001;
    public static final int CreateWriteThrough = 0x00000002;
    public static final int CreateSequential = 0x00000004;

    public static final int CreateNonDirectory = 0x00000040;
    public static final int CreateRandomAccess = 0x00000800;
    public static final int CreateDeleteOnClose = 0x00001000;
}
