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
 * <p>
 * This error indicates that too many tree connections are currently open on a session. The new tree
 * connection request will be rejected by the server.
 */
public class TooManyConnectionsException extends Exception
{
    private static final long serialVersionUID = 3257845497929414961L;

    /**
     * TooManyConnectionsException constructor.
     */
    public TooManyConnectionsException()
    {
        super();
    }

    /**
     * TooManyConnectionsException constructor.
     * 
     * @param s java.lang.String
     */
    public TooManyConnectionsException(String s)
    {
        super(s);
    }
}