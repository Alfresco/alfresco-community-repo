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

import java.util.List;


/**
 * Property override definition
 * 
 * @author David Caruana
 *
 */
public class M2PropertyOverride
{
    private String name;
    private Boolean isMandatory;
    private boolean isMandatoryEnforced = false;
    private String defaultValue;
    private List<M2Constraint> constraints;
    
    /*package*/ M2PropertyOverride()
    {
    }

    
    public String getName()
    {
        return name;
    }
    
    
    public void setName(String name)
    {
        this.name = name;
    }

    
    public Boolean isMandatory()
    {
        return isMandatory;
    }

    
    public void setMandatory(Boolean isMandatory)
    {
        this.isMandatory = isMandatory;
    }
    
    public boolean isMandatoryEnforced()
    {
        return isMandatoryEnforced;
    }
    
    public String getDefaultValue()
    {
        return defaultValue;
    }
    
    
    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    public List<M2Constraint> getConstraints()
    {
        return constraints;
    }    
}
