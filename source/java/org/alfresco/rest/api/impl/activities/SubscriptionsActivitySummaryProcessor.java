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
package org.alfresco.rest.api.impl.activities;

public class SubscriptionsActivitySummaryProcessor extends BaseActivitySummaryProcessor
{
	//{"lastName":"Glover","userFirstName":"David","followerLastName":"Glover","userUserName":"david.bowie@alfresco.com","followerFirstName":"Steve",
	// "userLastName":"Bowie","followerUserName":"steven.glover@alfresco.com","firstName":"Steve","tenantDomain":"alfresco.com"}
	
	@Override
	protected Change processEntry(String key, Object value)
	{
		Change change = super.processEntry(key, value);

		if(key.equals("userUserName"))
		{
			change = new ChangeKey(key, "userPersonId");
		}
		
		if(key.equals("followerUserName"))
		{
			change = new ChangeKey(key, "followerPersonId");
		}

		return change;
	}

}
