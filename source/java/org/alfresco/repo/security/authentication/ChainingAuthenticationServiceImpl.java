/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.security.authentication;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;

/**
 * This class implements a simple chaining authentication service. It chains together other authentication services so
 * that authentication can happen against more than one authentication service. The authentication services it uses are
 * stored as a list. Each authentication service must belong to the same domain. This is checked at configuration time.
 * Authentication will try each authentication service in order. If any allow authentication given the user name and
 * password then the user will be accepted. Additions, deletions and password changes are made to one special
 * authentication service. This service will be tried first for authentication. Users can not be created if they exist
 * in another authentication service. To avoid transactional issues in chaining, the services registered with this
 * service must not have transactional wrappers. If not, errors will mark the transaction for roll back and we can not
 * chain down the list of authentication services.
 * 
 * @author Andy Hind
 */
public class ChainingAuthenticationServiceImpl extends AbstractChainingAuthenticationService
{

    List<AuthenticationService> authenticationServices;

    MutableAuthenticationService mutableAuthenticationService;

    public ChainingAuthenticationServiceImpl()
    {
        super();
    }

    public void setAuthenticationServices(List<AuthenticationService> authenticationServices)
    {
        this.authenticationServices = authenticationServices;
    }

    @Override
    public MutableAuthenticationService getMutableAuthenticationService()
    {
        return this.mutableAuthenticationService;
    }

    public void setMutableAuthenticationService(MutableAuthenticationService mutableAuthenticationService)
    {
        this.mutableAuthenticationService = mutableAuthenticationService;
    }

    @Override
    protected List<AuthenticationService> getUsableAuthenticationServices()
    {
        if (this.mutableAuthenticationService == null)
        {
            return this.authenticationServices;
        }
        else
        {
            ArrayList<AuthenticationService> services = new ArrayList<AuthenticationService>(
                    this.authenticationServices == null ? 1 : this.authenticationServices.size() + 1);
            services.add(this.mutableAuthenticationService);
            if (this.authenticationServices != null)
            {
                services.addAll(this.authenticationServices);
            }
            return services;
        }
    }
}
