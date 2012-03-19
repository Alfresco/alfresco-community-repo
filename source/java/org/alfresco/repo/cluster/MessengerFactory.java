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

import java.io.Serializable;

/**
 * Factory class responsible for creating instances of {@link Messenger} class.
 * 
 * @author Matt Ward
 */
public interface MessengerFactory
{
    /** A catch-all for unknown application regions. */
    public static final String APP_REGION_DEFAULT = "DEFAULT";
    
    /** The application region used by the EHCache heartbeat implementation over JGroups. */
    public static final String APP_REGION_EHCACHE_HEARTBEAT = "EHCACHE_HEARTBEAT";
    
    <T extends Serializable> Messenger<T> createMessenger(String appRegion);
    
    <T extends Serializable> Messenger<T> createMessenger(String appRegion, boolean acceptLocalMessages);
    
    boolean isClusterActive();
}
