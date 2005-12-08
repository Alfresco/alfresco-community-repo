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
 * SMB data type class.
 * <p>
 * This class contains the data types that are used within an SMB protocol packet.
 */

public class DataType
{

    // SMB data types

    public static final char DataBlock = (char) 0x01;
    public static final char Dialect = (char) 0x02;
    public static final char Pathname = (char) 0x03;
    public static final char ASCII = (char) 0x04;
    public static final char VariableBlock = (char) 0x05;
}