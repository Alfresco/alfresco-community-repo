/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.dictionary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Abstract Class Definition.
 * 
 * @author David Caruana
 *
 */
public abstract class M2Class
{
    public static final Pattern PROPERTY_PLACEHOLDER = Pattern.compile("\\$\\{.*\\}\\|.*");
    
    private String name = null;
    private String title = null;
    private String description = null;
    private String parentName = null;
    private Boolean archive = null;
    private Boolean includedInSuperTypeQuery = null;
    private String analyserResourceBundleName = null;
    
    private List<M2Property> properties = new ArrayList<M2Property>();
    private List<M2PropertyOverride> propertyOverrides = new ArrayList<M2PropertyOverride>();
    private List<M2ClassAssociation> associations = new ArrayList<M2ClassAssociation>();
    private List<String> mandatoryAspects = new ArrayList<String>();

    /*package*/ M2Class()
    {
    }
    
    public boolean isAspect()
    {
        return this instanceof M2Aspect;
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
    
    public Boolean getIncludedInSuperTypeQuery()
    {
        return includedInSuperTypeQuery;
    }

    public void setIncludedInSuperTypeQuery(boolean includedInSuperTypeQuery)
    {
        this.includedInSuperTypeQuery = Boolean.valueOf(includedInSuperTypeQuery);
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

    /**
     * @return String
     */
    public String getAnalyserResourceBundleName()
    {
        return analyserResourceBundleName;
    }

    public void setAnalyserResourceBundleName(String analyserResourceBundleName)
    {
        this.analyserResourceBundleName = analyserResourceBundleName;
    } 
    
    public void setConfigProperties(Properties configProperties)
    {
        if (properties != null)
        {
            for (M2Property property : properties)
            {
                property.setConfigProperties(configProperties);
            }
        }
        if (propertyOverrides != null)
        {
            for (M2PropertyOverride propertyOverride : propertyOverrides)
            {
                propertyOverride.setConfigProperties(configProperties);
            }
        }
    }
}
