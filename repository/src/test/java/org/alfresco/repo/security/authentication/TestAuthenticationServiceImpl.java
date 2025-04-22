/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
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

import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.GUID;

public class TestAuthenticationServiceImpl implements MutableAuthenticationService
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

    public TestAuthenticationServiceImpl(String domain, boolean allowCreate, boolean allowDelete, boolean allowUpdate, boolean allowGuest,
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
            setCurrentUser(AuthenticationUtil.getGuestUserName());
        }
        else
        {
            throw new AuthenticationException("Guest access denied");
        }
    }

    public boolean guestUserAuthenticationAllowed()
    {
        return allowGuest;
    }

    public boolean authenticationExists(String userName)
    {
        return userNamesAndPasswords.containsKey(userName);
    }

    public boolean isAuthenticationMutable(String userName)
    {
        return authenticationExists(userName);
    }

    public boolean isAuthenticationCreationAllowed()
    {
        return allowCreate;
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

    public String getNewTicket()
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
            else if (userName.equalsIgnoreCase(AuthenticationUtil.getGuestUserName()))
            {
                GrantedAuthority[] gas = new GrantedAuthority[0];
                ud = new User(AuthenticationUtil.getGuestUserName().toLowerCase(), "", true, true, true, true, gas);
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

    public Set<String> getDefaultAdministratorUserNames()
    {
        return Collections.singleton(AuthenticationUtil.getAdminUserName());
    }

    public Set<String> getDefaultGuestUserNames()
    {
        return Collections.singleton(AuthenticationUtil.getGuestUserName());
    }

    private static final String SYSTEM_USER_NAME = "System";

}
