/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
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
package org.alfresco.filesys.smb.server;

/**
 * Security Mode Class
 * 
 * <p>CIFS security mode constants.
 * 
 * @author gkspencer
 */
public class SecurityMode
{
    // Security mode flags returned in the SMB negotiate response
    
    public static final int UserMode            = 0x0001;
    public static final int EncryptedPasswords  = 0x0002;
    public static final int SignaturesEnabled   = 0x0004;
    public static final int SignaturesRequired  = 0x0008;
}
