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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.service.cmr.security.AuthenticationService;

/**
 * This class implements a simple chaining authentication service.
 * 
 * It chains together other authentication services so that authentication can happen against more than one authentication service.
 * 
 * The authentication services it uses are stored as a list.
 * 
 * Each authentication service must belong to the same domain. This is checked at configuration time.
 * 
 * Authentication will try each authentication service in order. If any allow authentication given the user name and password then the user will be accepted.
 * 
 * Additions, deletions and password changes are made to one special authentication service. This service will be tried first for authentication. Users can not be created if they
 * exist in another authentication service.
 * 
 * To avoid transactional issues in chaining, the services registered with this service must not have transactional wrappers. If not, errors will mark the transaction for roll back
 * and we can not chain down the list of authentication services.
 * 
 * 
 * @author Andy Hind
 */
public class ChainingAuthenticationServiceImpl implements AuthenticationService
{

    private List<AuthenticationService> authenticationServices;

    private AuthenticationService mutableAuthenticationService;

    public ChainingAuthenticationServiceImpl()
    {
        super();
    }

    public List<AuthenticationService> getAuthenticationServices()
    {
        return authenticationServices;
    }

    public void setAuthenticationServices(List<AuthenticationService> authenticationServices)
    {
        this.authenticationServices = authenticationServices;
    }

    public AuthenticationService getMutableAuthenticationService()
    {
        return mutableAuthenticationService;
    }

    public void setMutableAuthenticationService(AuthenticationService mutableAuthenticationService)
    {
        this.mutableAuthenticationService = mutableAuthenticationService;
    }

    public void createAuthentication(String userName, char[] password) throws AuthenticationException
    {
        if (mutableAuthenticationService == null)
        {
            throw new AuthenticationException(
                    "Unable to create authentication as there is no suitable authentication service.");
        }
        mutableAuthenticationService.createAuthentication(userName, password);
    }

    public void updateAuthentication(String userName, char[] oldPassword, char[] newPassword)
            throws AuthenticationException
    {
        if (mutableAuthenticationService == null)
        {
            throw new AuthenticationException(
                    "Unable to update authentication as there is no suitable authentication service.");
        }
        mutableAuthenticationService.updateAuthentication(userName, oldPassword, newPassword);

    }

    public void setAuthentication(String userName, char[] newPassword) throws AuthenticationException
    {
        if (mutableAuthenticationService == null)
        {
            throw new AuthenticationException(
                    "Unable to set authentication as there is no suitable authentication service.");
        }
        mutableAuthenticationService.setAuthentication(userName, newPassword);
    }

    public void deleteAuthentication(String userName) throws AuthenticationException
    {
        if (mutableAuthenticationService == null)
        {
            throw new AuthenticationException(
                    "Unable to delete authentication as there is no suitable authentication service.");
        }
        mutableAuthenticationService.deleteAuthentication(userName);

    }

    public void setAuthenticationEnabled(String userName, boolean enabled) throws AuthenticationException
    {
        if (mutableAuthenticationService == null)
        {
            throw new AuthenticationException(
                    "Unable to set authentication enabled as there is no suitable authentication service.");
        }
        mutableAuthenticationService.setAuthenticationEnabled(userName, enabled);
    }

    public boolean getAuthenticationEnabled(String userName) throws AuthenticationException
    {
        for (AuthenticationService authService : getUsableAuthenticationServices())
        {
            try
            {
                if (authService.getAuthenticationEnabled(userName))
                {
                    return true;
                }
            }
            catch (AuthenticationException e)
            {
                // Ignore and chain
            }
        }
        return false;
    }

    public void authenticate(String userName, char[] password) throws AuthenticationException
    {
        for (AuthenticationService authService : getUsableAuthenticationServices())
        {
            try
            {
                authService.authenticate(userName, password);
                return;
            }
            catch (AuthenticationException e)
            {
                // Ignore and chain
            }
        }
        throw new AuthenticationException("Failed to authenticate");

    }

    public void authenticateAsGuest() throws AuthenticationException
    {
        for (AuthenticationService authService : getUsableAuthenticationServices())
        {
            try
            {
                authService.authenticateAsGuest();
                return;
            }
            catch (AuthenticationException e)
            {
                // Ignore and chain
            }
        }
        throw new AuthenticationException("Guest authentication not supported");
    }
    
    public boolean guestUserAuthenticationAllowed()
    {
        for (AuthenticationService authService : getUsableAuthenticationServices())
        {
            if (authService.guestUserAuthenticationAllowed())
            {
                return true;
            }
        }
        // it isn't allowed in any of the authentication components
        return false;
    }

    public boolean authenticationExists(String userName)
    {
        for (AuthenticationService authService : getUsableAuthenticationServices())
        {
            if (authService.authenticationExists(userName))
            {
                return true;
            }
        }
        // it doesn't exist in any of the authentication components
        return false;
    }

    public String getCurrentUserName() throws AuthenticationException
    {
        for (AuthenticationService authService : getUsableAuthenticationServices())
        {
            try
            {
                return authService.getCurrentUserName();
            }
            catch (AuthenticationException e)
            {
                // Ignore and chain
            }
        }
        return null;
    }

    public void invalidateUserSession(String userName) throws AuthenticationException
    {
        for (AuthenticationService authService : getUsableAuthenticationServices())
        {
            try
            {
                authService.invalidateUserSession(userName);
                return;
            }
            catch (AuthenticationException e)
            {
                // Ignore and chain
            }
        }
        throw new AuthenticationException("Unable to invalidate user session");

    }

    public void invalidateTicket(String ticket) throws AuthenticationException
    {
        for (AuthenticationService authService : getUsableAuthenticationServices())
        {
            try
            {
                authService.invalidateTicket(ticket);
                return;
            }
            catch (AuthenticationException e)
            {
                // Ignore and chain
            }
        }
        throw new AuthenticationException("Unable to invalidate ticket");

    }

    public void validate(String ticket) throws AuthenticationException
    {
        for (AuthenticationService authService : getUsableAuthenticationServices())
        {
            try
            {
                authService.validate(ticket);
                return;
            }
            catch (AuthenticationException e)
            {
                // Ignore and chain
            }
        }
        throw new AuthenticationException("Unable to validate ticket");

    }

    public String getCurrentTicket()
    {
        for (AuthenticationService authService : getUsableAuthenticationServices())
        {
            try
            {
                return authService.getCurrentTicket();
            }
            catch (AuthenticationException e)
            {
                // Ignore and chain
            }
        }
        return null;
    }

    public void clearCurrentSecurityContext()
    {
        for (AuthenticationService authService : getUsableAuthenticationServices())
        {
            try
            {
                authService.clearCurrentSecurityContext();
                return;
            }
            catch (AuthenticationException e)
            {
                // Ignore and chain
            }
        }
        throw new AuthenticationException("Failed to clear security context");

    }

    public boolean isCurrentUserTheSystemUser()
    {
        for (AuthenticationService authService : getUsableAuthenticationServices())
        {
            try
            {
                return authService.isCurrentUserTheSystemUser();
            }
            catch (AuthenticationException e)
            {
                // Ignore and chain
            }
        }
        return false;
    }

    private List<AuthenticationService> getUsableAuthenticationServices()
    {
        if (mutableAuthenticationService == null)
        {
            return authenticationServices;
        }
        else
        {
            ArrayList<AuthenticationService> services = new ArrayList<AuthenticationService>(
                    authenticationServices == null ? 1 : (authenticationServices.size() + 1));
            services.add(mutableAuthenticationService);
            if (authenticationServices != null)
            {
                services.addAll(authenticationServices);
            }
            return services;
        }
    }

    public Set<String> getDomains()
    {
        HashSet<String> domains = new HashSet<String>();
        for (AuthenticationService authService : getUsableAuthenticationServices())
        {
            domains.addAll(authService.getDomains());
        }
        return domains;
    }

    public Set<String> getDomainsThatAllowUserCreation()
    {
        HashSet<String> domains = new HashSet<String>();
        if (mutableAuthenticationService != null)
        {
            domains.addAll(mutableAuthenticationService.getDomainsThatAllowUserCreation());
        }
        return domains;
    }

    public Set<String> getDomainsThatAllowUserDeletion()
    {
        HashSet<String> domains = new HashSet<String>();
        if (mutableAuthenticationService != null)
        {
            domains.addAll(mutableAuthenticationService.getDomainsThatAllowUserDeletion());
        }
        return domains;
    }

    public Set<String> getDomiansThatAllowUserPasswordChanges()
    {
        HashSet<String> domains = new HashSet<String>();
        if (mutableAuthenticationService != null)
        {
            domains.addAll(mutableAuthenticationService.getDomiansThatAllowUserPasswordChanges());
        }
        return domains;
    }

}
