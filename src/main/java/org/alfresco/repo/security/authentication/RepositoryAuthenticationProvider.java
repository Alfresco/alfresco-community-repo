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

import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.UserDetails;
import net.sf.acegisecurity.providers.dao.DaoAuthenticationProvider;
import net.sf.acegisecurity.providers.dao.SaltSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A DaoAuthenticationProvider that makes use of a CompositePasswordEncoder to check the
 * password is correct.
 *
 * @author Gethin James
 */
public class RepositoryAuthenticationProvider extends DaoAuthenticationProvider
{
    private static Log logger = LogFactory.getLog(RepositoryAuthenticationProvider.class);
    CompositePasswordEncoder compositePasswordEncoder;

    public void setCompositePasswordEncoder(CompositePasswordEncoder compositePasswordEncoder)
    {
        this.compositePasswordEncoder = compositePasswordEncoder;
    }

    @Override
    protected boolean isPasswordCorrect(Authentication authentication, UserDetails user)
    {
        if (user instanceof RepositoryAuthenticatedUser)
        {
            RepositoryAuthenticatedUser repoUser = (RepositoryAuthenticatedUser) user;
            return compositePasswordEncoder.matchesPassword(authentication.getCredentials().toString(),user.getPassword(), repoUser.getSalt(), repoUser.getHashIndicator() );
        }

        logger.error("Password check error for "+user.getUsername()+" unknown user type: "+user.getClass().getName());
        return false;
    }
}
