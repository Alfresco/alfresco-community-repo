/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.event;

/**
 * Records management event 
 * 
 * @author Roy Wetherall
 */
public class RecordsManagementEvent
{ 
    /** Records management event type */
    private String type;
    
    /** Records management event name */
    private String name;
    
    /** Records management display label */
    private String displayLabel;
    
    /**
     * Constructor
     * 
     * @param type          event type
     * @param name          event name
     * @param displayLabel  event display label
     */
    public RecordsManagementEvent(String type, String name, String displayLabel)
    {
        this.type =  type;
        this.name = name;
        this.displayLabel = displayLabel;
    }
    
    /**
     * Get records management type
     * 
     * @return  String records management type
     */
    public String getType()
    {
        return this.type;
    }
    
    /**
     * Event name
     * 
     * @return String   event name
     */
    public String getName()
    {
        return this.name;
    }
    
    /**
     * 
     * @return
     */
    public String getDisplayLabel()
    {
        return displayLabel;
    }    
}
