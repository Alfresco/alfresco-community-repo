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
 * I/O Control Not Implemented Exception Class
 * 
 * <p>This exception may be thrown by an IOCtlInterface implementation.
 * 
 * @author gkspencer
 */
public class IOControlNotImplementedException extends Exception
{
    private static final long serialVersionUID = -7107739317519497749L;

    /**
     * Default constructor.
     */
    public IOControlNotImplementedException()
    {
        super();
    }

    /**
     * Class constructor.
     * 
     * @param s java.lang.String
     */
    public IOControlNotImplementedException(String s)
    {
        super(s);
    }
}
