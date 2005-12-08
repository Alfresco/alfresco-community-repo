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
package org.alfresco.filesys.server.core;

/**
 * Device Context Exception Class
 * <p>
 * Thrown when a device context parameter string is invalid.
 */
public class DeviceContextException extends Exception
{
    private static final long serialVersionUID = 3761124938182244658L;

    /**
     * Class constructor
     */
    public DeviceContextException()
    {
        super();
    }

    /**
     * Class constructor
     * 
     * @param s java.lang.String
     */
    public DeviceContextException(String s)
    {
        super(s);
    }

}
