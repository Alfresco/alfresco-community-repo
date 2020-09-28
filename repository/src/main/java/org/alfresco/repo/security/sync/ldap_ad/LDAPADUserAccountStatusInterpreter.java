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
package org.alfresco.repo.security.sync.ldap_ad;

import java.io.Serializable;

import org.alfresco.repo.security.sync.ldap.AbstractDirectoryServiceUserAccountStatusInterpreter;

public class LDAPADUserAccountStatusInterpreter extends AbstractDirectoryServiceUserAccountStatusInterpreter
{
    @Override
    public boolean isUserAccountDisabled(Serializable userAccountStatusValue)
    {
        checkForNullArgument(userAccountStatusValue);

        /*
         * References:
         * https://blogs.technet.microsoft.com/heyscriptingguy/2005/05/12/how-can-i-get-a-list-of-all-the-disabled-user-accounts-in-active-directory
         * http://stackoverflow.com/questions/19250969/include-enabled-disabled-account-status-of-ldap-user-in-results/19252033#19252033
         */
        return ((Integer.parseInt(userAccountStatusValue.toString())) & 2) != 0;
    }

    @Override
    public boolean acceptsNullArgument()
    {
        return false;
    }
}
