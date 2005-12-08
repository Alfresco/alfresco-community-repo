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
package org.alfresco.filesys.smb.server;

/**
 * OpenAndX Flags Class
 */
class OpenAndX
{

    // File types, for OpenAndX

    protected static final int FileTypeDisk = 0;
    protected static final int FileTypeBytePipe = 1;
    protected static final int FileTypeMsgPipe = 2;
    protected static final int FileTypePrinter = 3;
    protected static final int FileTypeUnknown = 0xFFFF;
}
