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
package org.alfresco.filesys.server.auth;

/**
 * Authenticator Exception Class
 * 
 * @author gkspencer
 */
public class AuthenticatorException extends Exception
{
    private static final long serialVersionUID = 7816213724352083486L;

    /**
     * Default constructor.
     */
    public AuthenticatorException()
    {
        super();
    }

    /**
     * Class constructor.
     * 
     * @param s String
     */
    public AuthenticatorException(String s)
    {
        super(s);
    }
}
