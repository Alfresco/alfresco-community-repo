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
package org.alfresco.repo.cluster;

/**
 * Static container for MessengerFactory. This allows code to obtain the correct
 * {@link MessengerFactory} implementation where dependency injection is not available.
 * 
 * @author Matt Ward
 */
public class MessengerFactoryProvider
{
    private static MessengerFactory instance;
    
    public void setInstance(MessengerFactory messengerFactory)
    {
        instance = messengerFactory;
    }
    
    public static MessengerFactory getInstance()
    {
        if (instance == null)
        {
            throw new IllegalStateException("MessengerFactory instance not configured yet.");
        }
        return instance;
    }
}
