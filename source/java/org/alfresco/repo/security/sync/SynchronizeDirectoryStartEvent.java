/*
 * Copyright (C) 2013-2013 Alfresco Software Limited.
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
package org.alfresco.repo.security.sync;

/**
 * Start of synchronize directory
 * 
 * @author mrogers
 * @since 4.2
 */
public class SynchronizeDirectoryStartEvent extends SynchronizeDirectoryEvent
{
    private String batchProcessNames[];
    public SynchronizeDirectoryStartEvent(Object o, String zone, String batchProcessNames[])
    {
        super(o, zone);
        this.batchProcessNames = batchProcessNames;
    }
    
    public String[] getBatchProcessNames()
    {
        return batchProcessNames;
    }

    /**
     * 
     */
    private static final long serialVersionUID = 5374340649898136746L;

}
