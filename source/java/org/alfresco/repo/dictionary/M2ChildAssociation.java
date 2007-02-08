/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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

}
