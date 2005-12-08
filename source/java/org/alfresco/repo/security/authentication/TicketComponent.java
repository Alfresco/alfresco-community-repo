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


/**
 * Manage authentication tickets
 * 
 * @author andyh
 * 
 */
public interface TicketComponent
{
    /**
     * Register a ticket
     * 
     * @param authentication
     * @return
     * @throws AuthenticationException
     */
    public String getTicket(String userName) throws AuthenticationException;

    /**
     * Check that a certificate is valid and can be used in place of a login.
     * 
     * Tickets may be rejected because:
     * <ol>
     * <li> The certificate does not exists
     * <li> The status of the user has changed 
     * <ol>
     * <li> The user is locked
     * <li> The account has expired
     * <li> The credentials have expired
     * <li> The account is disabled
     * </ol>
     * <li> The ticket may have expired
     * <ol>
     * <li> The ticked my be invalid by timed expiry
     * <li> An attemp to reuse a once only ticket
     * </ol>
     * </ol>
     * 
     * @param authentication
     * @return
     * @throws AuthenticationException
     */
    public String validateTicket(String ticket) throws AuthenticationException;
    
    public void invalidateTicketById(String ticket);
    
    public void invalidateTicketByUser(String userName);
}
