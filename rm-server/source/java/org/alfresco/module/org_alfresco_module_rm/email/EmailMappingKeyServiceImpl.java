/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.email;

import java.util.List;

import org.alfresco.util.ParameterCheck;

/**
 * EMail Mapping Key Service
 *
 * @author Tuna Aksoy
 * @since 2.1
 */
public class EmailMappingKeyServiceImpl implements EmailMappingKeyService
{
    List<String> emailMappingKeys;

    public void setEmailMappingKeys(List<String> emailMappingKeys)
    {
        this.emailMappingKeys = emailMappingKeys;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.email.CustomEmailMappingService#getEmailMappingKeys()
     */
    @Override
    public List<String> getEmailMappingKeys()
    {
        return emailMappingKeys;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.email.EmailMappingKeyService#makeCustomisable(java.lang.String)
     */
    @Override
    public void makeCustomisable(String emailMappingKey)
    {
        ParameterCheck.mandatoryString("emailMappingKey", emailMappingKey);

        emailMappingKeys.add(emailMappingKey);
    }
}
