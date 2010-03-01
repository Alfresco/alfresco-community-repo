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
package org.alfresco.repo.dictionary;


/**
 * Child Association definition.
 * 
 * @author David Caruana
 *
 */
public class M2ChildAssociation extends M2ClassAssociation
{
    private String requiredChildName = null;
    private Boolean allowDuplicateChildName = null;
    private Boolean propagateTimestamps = null;
    
    
    /*package*/ M2ChildAssociation()
    {
    }
    
    
    /*package*/ M2ChildAssociation(String name)
    {
        super(name);
    }
    

    public String getRequiredChildName()
    {
        return requiredChildName;
    }
    
    
    public void setRequiredChildName(String requiredChildName)
    {
        this.requiredChildName = requiredChildName;
    }
    
    
    public boolean allowDuplicateChildName()
    {
        return allowDuplicateChildName == null ? true : allowDuplicateChildName;
    }
    
    
    public void setAllowDuplicateChildName(boolean allowDuplicateChildName)
    {
        this.allowDuplicateChildName = allowDuplicateChildName;
    }

    public boolean isPropagateTimestamps()
    {
        return propagateTimestamps == null ? false : propagateTimestamps;
    }
    
    public void setPropagateTimestamps(boolean propagateTimestamps)
    {
        this.propagateTimestamps = propagateTimestamps;
    }
}
