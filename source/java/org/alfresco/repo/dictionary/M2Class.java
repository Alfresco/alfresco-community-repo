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
 * Abstract Class Definition.
 * 
 * @author David Caruana
 *
 */
public abstract class M2Class
{
    private String name = null;
    private String title = null;
    private String description = null;
    private String parentName = null;
    private Boolean archive = null;
    
    private List<M2Property> properties = new ArrayList<M2Property>();
    private List<M2PropertyOverride> propertyOverrides = new ArrayList<M2PropertyOverride>();
    private List<M2ClassAssociation> associations = new ArrayList<M2ClassAssociation>();
    private List<String> mandatoryAspects = new ArrayList<String>();

    /*package*/ M2Class()
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

    
    public String getParentName()
    {
        return parentName;
    }
    
    
    public void setParentName(String parentName)
    {
        this.parentName = parentName;
    }
    
    
    public Boolean getArchive()
    {
        return archive;
    }

    public void setArchive(boolean archive)
    {
        this.archive = Boolean.valueOf(archive);
    }

    public M2Property createProperty(String name)
    {
        M2Property property = new M2Property();
        property.setName(name);
        properties.add(property);
        return property;
    }
    
    
    public void removeProperty(String name)
    {
        M2Property property = getProperty(name);
        if (property != null)
        {
            properties.remove(property);
        }
    }


    public List<M2Property> getProperties()
    {
        return Collections.unmodifiableList(properties);
    }

    
    public M2Property getProperty(String name)
    {
        for (M2Property candidate : properties)
        {
            if (candidate.getName().equals(name))
            {
                return candidate;
            }
        }
        return null;
    }

    
    public M2Association createAssociation(String name)
    {
        M2Association association = new M2Association();
        association.setName(name);
        associations.add(association);
        return association;
    }

    
    public M2ChildAssociation createChildAssociation(String name)
    {
        M2ChildAssociation association = new M2ChildAssociation();
        association.setName(name);
        associations.add(association);
        return association;
    }
    
    
    public void removeAssociation(String name)
    {
        M2ClassAssociation association = getAssociation(name);
        if (association != null)
        {
            associations.remove(association);
        }
    }

    
    public List<M2ClassAssociation> getAssociations()
    {
        return Collections.unmodifiableList(associations);
    }

    
    public M2ClassAssociation getAssociation(String name)
    {
        for (M2ClassAssociation candidate : associations)
        {
            if (candidate.getName().equals(name))
            {
                return candidate;
            }
        }
        return null;
    }
    
    
    public M2PropertyOverride createPropertyOverride(String name)
    {
        M2PropertyOverride property = new M2PropertyOverride();
        property.setName(name);
        propertyOverrides.add(property);
        return property;
    }
    
    
    public void removePropertyOverride(String name)
    {
        M2PropertyOverride property = getPropertyOverride(name);
        if (property != null)
        {
            propertyOverrides.remove(property);
        }
    }

    
    public List<M2PropertyOverride> getPropertyOverrides()
    {
        return Collections.unmodifiableList(propertyOverrides);
    }


    public M2PropertyOverride getPropertyOverride(String name)
    {
        for (M2PropertyOverride candidate : propertyOverrides)
        {
            if (candidate.getName().equals(name))
            {
                return candidate;
            }
        }
        return null;
    }
    
    public void addMandatoryAspect(String name)
    {
        mandatoryAspects.add(name);
    }
    
    
    public void removeMandatoryAspect(String name)
    {
        mandatoryAspects.remove(name);
    }
    

    public List<String> getMandatoryAspects()
    {
        return Collections.unmodifiableList(mandatoryAspects);
    } 
    
}
