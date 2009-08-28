/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.dictionary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Property Definition
 * 
 * @author David Caruana
 *
 */
public class M2Property
{
    private boolean isOverride = false;
    private String name = null;
    private String title = null;
    private String description = null;
    private String propertyType = null;
    private boolean isProtected = false;
    private boolean isMandatory = false;
    private boolean isMandatoryEnforced = false;
    private boolean isMultiValued = false;
    private String defaultValue = null;
    private boolean isIndexed = true;
    private boolean isIndexedAtomically = true;
    private boolean isStoredInIndex = false;
    private IndexTokenisationMode indexTokenisationMode = IndexTokenisationMode.TRUE;
    private List<M2Constraint> constraints = new ArrayList<M2Constraint>();

    /*package*/ M2Property()
    {
    }

    
    /*package*/ M2Property(String name)
    {
        this.name = name;
    }
    
    public boolean isOverride()
    {
        return isOverride;
    }
    
    public void setOverride(boolean isOverride)
    {
        this.isOverride = isOverride;
    }
    
    public String getName()
    {
        return name;
    }

    
    public void setName(String name)
    {
        this.name = name;
    }

    
    public String getTitle()
    {
        return title;
    }
    
    
    public void setTitle(String title)
    {
        this.title = title;
    }

    
    public String getDescription()
    {
        return description;
    }
    
    
    public void setDescription(String description)
    {
        this.description = description;
    }
    
    
    public String getType()
    {
        return propertyType;
    }
    
    
    public void setType(String type)
    {
        this.propertyType = type;
    }
    
    
    public boolean isProtected()
    {
        return isProtected;
    }
    
    
    public void setProtected(boolean isProtected)
    {
        this.isProtected = isProtected;
    }
    
    
    public boolean isMandatory()
    {
        return isMandatory;
    }
    
    public void setMandatory(boolean isMandatory)
    {
        this.isMandatory = isMandatory;
    }
    
    public boolean isMandatoryEnforced()
    {
        return isMandatoryEnforced;
    }
    
    public void setMandatoryEnforced(boolean isMandatoryEnforced)
    {
        this.isMandatoryEnforced = isMandatoryEnforced;
    }
    
    public boolean isMultiValued()
    {
        return isMultiValued;
    }
    
    
    public void setMultiValued(boolean isMultiValued)
    {
        this.isMultiValued = isMultiValued;
    }
    
    
    public String getDefaultValue()
    {
        return defaultValue;
    }
    
    
    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }
    
    
    public boolean isIndexed()
    {
        return isIndexed;
    }
    
    
    public void setIndexed(boolean isIndexed)
    {
        this.isIndexed = isIndexed;
    }
    
    
    public boolean isStoredInIndex()
    {
        return isStoredInIndex;
    }
    
    
    public void setStoredInIndex(boolean isStoredInIndex)
    {
        this.isStoredInIndex = isStoredInIndex;
    }

    
    public boolean isIndexedAtomically()
    {
        return isIndexedAtomically;
    }
    
    
    public void setIndexedAtomically(boolean isIndexedAtomically)
    {
        this.isIndexedAtomically = isIndexedAtomically;
    }
    

    public IndexTokenisationMode getIndexTokenisationMode()
    {
        return indexTokenisationMode;
    }
    
    
    public void setIndexTokenisationMode(IndexTokenisationMode indexTokenisationMode)
    {
        this.indexTokenisationMode = indexTokenisationMode;
    }


    public List<M2Constraint> getConstraints()
    {
        if (constraints == null)
        {
            return Collections.emptyList();
        }
        else
        {
            return constraints;
        }
    }
    
    public boolean hasConstraints()
    {
        return ((this.constraints != null) && (constraints.size() > 0));
    }
    
    public M2Constraint addConstraintRef(String refName)
    {
        M2Constraint constraint = new M2Constraint();
        constraint.setRef(refName);
        constraints.add(constraint);
        return constraint;
    }
    
    
    public void removeConstraintRef(String refName)
    {
        List<M2Constraint> cons = new ArrayList<M2Constraint>(getConstraints());
        for (M2Constraint con : cons)
        {
            if (con.getRef().equals(refName))
            {
                constraints.remove(con);
            }
        }
    }
}
