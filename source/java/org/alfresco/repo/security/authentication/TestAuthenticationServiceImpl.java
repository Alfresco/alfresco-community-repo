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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.GrantedAuthority;
import net.sf.acegisecurity.GrantedAuthorityImpl;
import net.sf.acegisecurity.UserDetails;
import net.sf.acegisecurity.context.Context;
import net.sf.acegisecurity.context.ContextHolder;
import net.sf.acegisecurity.context.security.SecureContext;
import net.sf.acegisecurity.context.security.SecureContextImpl;
import net.sf.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import net.sf.acegisecurity.providers.dao.User;

import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.GUID;

public class TestAuthenticationServiceImpl implements AuthenticationService
{
    private Map<String, String> userNamesAndPasswords = new HashMap<String, String>();

    private Set<String> disabledUsers = new HashSet<String>();

    private Map<String, String> userToTicket = new HashMap<String, String>();

    String domain;

    boolean allowCreate;

    boolean allowDelete;

    boolean allowUpdate;

    boolean allowGuest;

    public TestAuthenticationServiceImpl(String domain, boolean allowCreate, boolean allowDelete, boolean allowUpdate, boolean allowGuest)
    {
        super();
        this.domain = domain;
        this.allowCreate = allowCreate;
        this.allowDelete = allowDelete;
        this.allowUpdate = allowUpdate;
        this.allowGuest = allowGuest;
    }

    public TestAuthenticationServiceImpl(String domain, boolean allowCreate, boolean allowDelete, boolean allowUpdate,  boolean allowGuest,
            Map<String, String> users, Set<String> disabled)
    {
        this(domain, allowCreate, allowDelete, allowUpdate, allowGuest);
        if (users != null)
        {
            userNamesAndPasswords.putAll(users);
        }
        if (disabled != null)
        {
            disabledUsers.addAll(disabled);
        }

    }

    public void createAuthentication(String userName, char[] password) throws AuthenticationException
    {
        if (!allowCreate)
        {
            throw new AuthenticationException("Create not allowed");
        }
        if (userNamesAndPasswords.containsKey(userName))
        {
            throw new AuthenticationException("User exists");
        }
        else
        {
            userNamesAndPasswords.put(userName, new String(password));
        }

    }

    public void updateAuthentication(String userName, char[] oldPassword, char[] newPassword)
            throws AuthenticationException
    {
        if (!allowUpdate)
        {
            throw new AuthenticationException("Update not allowed");
        }
        if (!userNamesAndPasswords.containsKey(userName))
        {
            throw new AuthenticationException("User does not exist");
        }
        else
        {
            if (userNamesAndPasswords.get(userName).equals(new String(oldPassword)))
            {
                userNamesAndPasswords.put(userName, new String(newPassword));
            }
            else
            {
                throw new AuthenticationException("Password does not match existing");
            }
        }

    }

    public void setAuthentication(String userName, char[] newPassword) throws AuthenticationException
    {
        if (!allowUpdate)
        {
            throw new AuthenticationException("Update not allowed");
        }
        if (!userNamesAndPasswords.containsKey(userName))
        {
            throw new AuthenticationException("User does not exist");
        }
        else
        {
            userNamesAndPasswords.put(userName, new String(newPassword));
        }

    }

    public void deleteAuthentication(String userName) throws AuthenticationException
    {
        if (!allowDelete)
        {
            throw new AuthenticationException("Delete not allowed");
        }
        if (!userNamesAndPasswords.containsKey(userName))
        {
            throw new AuthenticationException("User does not exist");
        }
        else
        {
            userNamesAndPasswords.remove(userName);
        }

    }

    public void setAuthenticationEnabled(String userName, boolean enabled) throws AuthenticationException
    {
        if (!allowUpdate)
        {
            throw new AuthenticationException("Update not allowed");
        }
        if (!userNamesAndPasswords.containsKey(userName))
        {
            throw new AuthenticationException("User does not exist");
        }
        else
        {
            if (enabled)
            {
                disabledUsers.remove(userName);
            }
            else
            {
                disabledUsers.add(userName);
            }
        }

    }

    public boolean getAuthenticationEnabled(String userName) throws AuthenticationException
    {
        if (!userNamesAndPasswords.containsKey(userName))
        {
            return false;
        }
        else
        {
            return !disabledUsers.contains(userName);
        }

    }

    public void authenticate(String userName, char[] password) throws AuthenticationException
    {
        if (!userNamesAndPasswords.containsKey(userName))
        {
            throw new AuthenticationException("User does not exist");
        }
        else if (disabledUsers.contains(userName))
        {
            throw new AuthenticationException("User disabled0");
        }
        else
        {
            if (userNamesAndPasswords.get(userName).equals(new String(password)))
            {
                setCurrentUser(userName);
            }
            else
            {
                throw new AuthenticationException("Unknown user/password");
            }
        }

    }

    public void authenticateAsGuest() throws AuthenticationException
    {
        if (allowGuest)
        {
            setCurrentUser(PermissionService.GUEST_AUTHORITY);
        }
        else
        {
            throw new AuthenticationException("Guest access denied");
        }
    }

    public boolean authenticationExists(String userName)
    {
        return userNamesAndPasswords.containsKey(userName);
    }

    public String getCurrentUserName() throws AuthenticationException
    {
        Context context = ContextHolder.getContext();
        if ((context == null) || !(context instanceof SecureContext))
        {
            return null;
        }
        return getUserName(((SecureContext) context).getAuthentication());
    }

    private String getUserName(Authentication authentication)
    {
        String username = authentication.getPrincipal().toString();

        if (authentication.getPrincipal() instanceof UserDetails)
        {
            username = ((UserDetails) authentication.getPrincipal()).getUsername();
        }

        return username;
    }

    public void invalidateUserSession(String userName) throws AuthenticationException
    {
        userToTicket.remove(userName);
    }

    public void invalidateTicket(String ticket) throws AuthenticationException
    {
        String userToRemove = null;
        for (String user : userToTicket.keySet())
        {
            String currentTicket = userToTicket.get(user);
            if (EqualsHelper.nullSafeEquals(currentTicket, ticket))
            {
                userToRemove = user;
            }
        }
        if (userToRemove != null)
        {
            userToTicket.remove(userToRemove);
        }

    }

    public void validate(String ticket) throws AuthenticationException
    {
        String userToSet = null;
        for (String user : userToTicket.keySet())
        {
            String currentTicket = userToTicket.get(user);
            if (EqualsHelper.nullSafeEquals(currentTicket, ticket))
            {
                userToSet = user;
            }
        }
        if (userToSet != null)
        {
            setCurrentUser(userToSet);
        }
        else
        {
            throw new AuthenticationException("Invalid ticket");
        }

    }

    public String getCurrentTicket()
    {
        String currentUser = getCurrentUserName();
        String ticket = userToTicket.get(currentUser);
        if (ticket == null)
        {
            ticket = GUID.generate();
            userToTicket.put(currentUser, ticket);
        }
        return ticket;
    }

    public void clearCurrentSecurityContext()
    {
        ContextHolder.setContext(null);
    }

    public boolean isCurrentUserTheSystemUser()
    {
        String userName = getCurrentUserName();
        if ((userName != null) && userName.equals(SYSTEM_USER_NAME))
        {
            return true;
        }
        return false;
    }

    public Set<String> getDomains()
    {
        return Collections.singleton(domain);
    }

    public Set<String> getDomainsThatAllowUserCreation()
    {
        if (allowCreate)
        {
            return Collections.singleton(domain);
        }
        else
        {
            return Collections.<String> emptySet();
        }
    }

    public Set<String> getDomainsThatAllowUserDeletion()
    {
        if (allowDelete)
        {
            return Collections.singleton(domain);
        }
        else
        {
            return Collections.<String> emptySet();
        }
    }

    public Set<String> getDomiansThatAllowUserPasswordChanges()
    {
        if (allowUpdate)
        {
            return Collections.singleton(domain);
        }
        else
        {
            return Collections.<String> emptySet();
        }
    }

    /**
     * Explicitly set the current user to be authenticated.
     * 
     * @param userName
     *            String
     * @return Authentication
     */
    public Authentication setCurrentUser(String userName) throws AuthenticationException
    {
        if (userName == null)
        {
            throw new AuthenticationException("Null user name");
        }

        try
        {
            UserDetails ud = null;
            if (userName.equals(SYSTEM_USER_NAME))
            {
                GrantedAuthority[] gas = new GrantedAuthority[1];
                gas[0] = new GrantedAuthorityImpl("ROLE_SYSTEM");
                ud = new User(SYSTEM_USER_NAME, "", true, true, true, true, gas);
            }
            else if (userName.equalsIgnoreCase(PermissionService.GUEST_AUTHORITY))
            {
                GrantedAuthority[] gas = new GrantedAuthority[0];
                ud = new User(PermissionService.GUEST_AUTHORITY.toLowerCase(), "", true, true, true, true, gas);
            }
            else
            {
                ud = getUserDetails(userName);
            }

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(ud, "", ud
                    .getAuthorities());
            auth.setDetails(ud);
            auth.setAuthenticated(true);
            return setCurrentAuthentication(auth);
        }
        catch (net.sf.acegisecurity.AuthenticationException ae)
        {
            throw new AuthenticationException(ae.getMessage(), ae);
        }
    }

    /**
     * Default implementation that makes an ACEGI object on the fly
     * 
     * @param userName
     * @return
     */
    protected UserDetails getUserDetails(String userName)
    {
        GrantedAuthority[] gas = new GrantedAuthority[1];
        gas[0] = new GrantedAuthorityImpl("ROLE_AUTHENTICATED");
        UserDetails ud = new User(userName, "", true, true, true, true, gas);
        return ud;
    }

    public Authentication setCurrentAuthentication(Authentication authentication)
    {
        Context context = ContextHolder.getContext();
        SecureContext sc = null;
        if ((context == null) || !(context instanceof SecureContext))
        {
            sc = new SecureContextImpl();
            ContextHolder.setContext(sc);
        }
        else
        {
            sc = (SecureContext) context;
        }
        authentication.setAuthenticated(true);
        sc.setAuthentication(authentication);
        return authentication;
    }

    private static final String SYSTEM_USER_NAME = "System";

}
