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
package org.alfresco.rest.api.model;

import java.util.Date;
import java.util.List;

/**
 * Represents a cloud network (account).
 * 
 * @author steveglover
 *
 */
public interface Network
{
    public String getId();
    
    /**
     * Gets the date the account was created
     *
     * @return  The account creation date
     */
    public Date getCreatedAt();
    public List<Quota> getQuotas();

	/**
     * Gets whether an account is enabled or not. 
     *
     * @return true = account is enabled, false = account is disabled
     */
    public Boolean getIsEnabled();
    
    /**
     * Gets the subscription level.
     * @return one of the values of {@link AccountType.SubscriptionLevel}.
     */
    public String getSubscriptionLevel();
    
	public Boolean getPaidNetwork();
}