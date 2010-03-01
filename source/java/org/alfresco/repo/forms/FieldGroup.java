/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.forms;

/**
 * Represents a field group
 *
 * @author Gavin Cornwell
 */
public class FieldGroup
{
    protected String id;
    protected String label;
    protected FieldGroup parent;
    protected boolean repeats;
    protected boolean mandatory;
    
    /**
     * Constructs a FieldGroup
     * 
     * @param id        The id of the group
     * @param label     The display label of the group
     * @param mandatory Whether the group is mandatory
     * @param repeats   Whether the group of fields can repeat
     * @param parent    The group's parent group or null if it 
     *                  doesn't have a parent
     */
    public FieldGroup(String id, String label, boolean mandatory, 
                      boolean repeats, FieldGroup parent)
    {
        this.id = id;
        this.label = label;
        this.mandatory = mandatory;
        this.parent = parent;
        this.repeats = repeats;
    }

    /**
     * Returns the id of the group 
     * 
     * @return The id of the group
     */
    public String getId()
    {
        return this.id;
    }

    /**
     * Returns the display label of the group
     * 
     * @return The display label of the group
     */
    public String getLabel()
    {
        return this.label;
    }

    /**
     * Returns the parent group
     * 
     * @return The parent group or null if there isn't a parent
     */
    public FieldGroup getParent()
    {
        return this.parent;
    }

    /**
     * Determines whether the fields inside this group can 
     * repeat multiple times
     * 
     * @return true if the group repeats
     */
    public boolean isRepeating()
    {
        return this.repeats;
    }

    /**
     * Determines if the group is mandatory
     * 
     * @return true if the group is mandatory
     */
    public boolean isMandatory()
    {
        return this.mandatory;
    }
}
