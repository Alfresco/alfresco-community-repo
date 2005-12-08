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
package org.alfresco.repo.security.authentication;

import net.sf.acegisecurity.Authentication;

public interface AuthenticationComponent
{
    
    /**
     * Authenticate
     * 
     * @param userName
     * @param password
     * @throws AuthenticationException
     */     
    public void authenticate(String userName, char[] password) throws AuthenticationException;

    /**
     * Authenticate using a token
     * 
     * @param token Authentication
     * @return Authentication
     * @throws AuthenticationException
     */
    public Authentication authenticate(Authentication token) throws AuthenticationException;
    
    /**
     * Explicitly set the current user to be authenticated.
     */
    
    public Authentication setCurrentUser(String userName);
    
    /**
     * Remove the current security information
     *
     */
    public void clearCurrentSecurityContext();
    
    /**
     * Explicitly set the current suthentication.
     */
    
    public Authentication setCurrentAuthentication(Authentication authentication);
    
    /**
     * 
     * @return
     * @throws AuthenticationException
     */
    public Authentication getCurrentAuthentication() throws AuthenticationException;
    
    /**
     * Set the system user as the current user.
     * 
     * @return
     */
    public Authentication setSystemUserAsCurrentUser();
    
    
    /**
     * Get the name of the system user
     * 
     * @return
     */
    public String getSystemUserName();
    
    /**
     * Get the current user name.
     * 
     * @return
     * @throws AuthenticationException
     */
    public String getCurrentUserName() throws AuthenticationException;
    
    /**
     * Get the enum that describes NTLM integration
     * 
     * @return
     */
    public NTLMMode getNTLMMode();
    
    /**
     * Get the MD4 password hash, as required by NTLM based authentication methods.
     * 
     * @param userName
     * @return
     */
    public String getMD4HashedPassword(String userName);
}
