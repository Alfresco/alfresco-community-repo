/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.repo.security.authority;

import java.util.Set;
import java.util.regex.Pattern;

import org.alfresco.service.cmr.security.AuthorityType;

/**
 * This class extends {@link AuthorityDAOImpl}</br>
 * and overrides two methods from the original class</br>
 * </br>
 * addAuthorityNameIfMatches(Set<String> authorities, String authorityName, AuthorityType type)</br>
 * </br>
 * and</br>
 * </br>
 * addAuthorityNameIfMatches(Set<String> authorities, String authorityName, AuthorityType type, Pattern pattern)</br>
 */
public class RMAuthorityDAOImpl extends AuthorityDAOImpl
{
    protected void addAuthorityNameIfMatches(Set<String> authorities, String authorityName, AuthorityType type)
    {
        if (isAuthorityNameMatching(authorityName, type))
        {
            authorities.add(authorityName);
        }
    }

    protected void addAuthorityNameIfMatches(Set<String> authorities, String authorityName, AuthorityType type, Pattern pattern)
    {
        if (isAuthorityNameMatching(authorityName, type))
        {
            if (pattern == null)
            {
                authorities.add(authorityName);
            }
            else
            {
                if (pattern.matcher(getShortName(authorityName)).matches())
                {
                    authorities.add(authorityName);
                }
                else
                {
                    String displayName = getAuthorityDisplayName(authorityName);
                    if (displayName != null && pattern.matcher(displayName).matches())
                    {
                        authorities.add(authorityName);
                    }
                }
            }
        }
    }

    private boolean isAuthorityNameMatching(String authorityName, AuthorityType type)
    {
        boolean isMatching = false;
        if (type == null || AuthorityType.getAuthorityType(authorityName).equals(type) && !getAuthorityZones(authorityName).contains("APP.RM"))
        {
            isMatching = true;
        }
        return isMatching;
    }
}
