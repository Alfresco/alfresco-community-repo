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
    private Boolean isIndexed = Boolean.TRUE;
    private Boolean isIndexedAtomically = null;
    private Boolean isStoredInIndex = null;
    private Boolean isFacetable = null;
    private IndexTokenisationMode indexTokenisationMode = null;
    private String  analyserResourceBundleName;
    private List<M2Constraint> constraints = new ArrayList<M2Constraint>();
    private Properties configProperties = new Properties();

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
        if (defaultValue != null && M2Class.PROPERTY_PLACEHOLDER.matcher(defaultValue).matches())
        {
            String key = defaultValue.substring(defaultValue.indexOf("${") + 2, defaultValue.indexOf("}"));
            String value = defaultValue.substring(defaultValue.indexOf("|") + 1);
            
            return configProperties.getProperty(key, value);
        }
        else
        {
            return defaultValue;
        }
    }
    
    
    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }
    
    
    public Boolean isIndexed()
    {
        return isIndexed;
    }
    
    
    public void setIndexed(boolean isIndexed)
    {
        this.isIndexed = Boolean.valueOf(isIndexed);
    }
    
    
    public Boolean isStoredInIndex()
    {
        return isStoredInIndex;
    }
 
    
    public void setStoredInIndex(boolean isStoredInIndex)
    {
        this.isStoredInIndex = Boolean.valueOf(isStoredInIndex);
    }
    
    public Boolean isFacetable()
    {
        return isFacetable;
    }
 
    
    public void setFacetable(boolean isFacetable)
    {
        this.isFacetable = Boolean.valueOf(isFacetable);
    }

    
    public Boolean isIndexedAtomically()
    {
        return isIndexedAtomically;
    }
    
    
    public void setIndexedAtomically(boolean isIndexedAtomically)
    {
        this.isIndexedAtomically = Boolean.valueOf(isIndexedAtomically);
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

    public M2Constraint addConstraint(String name, String type)
    {
        M2Constraint constraint = new M2Constraint();
        constraint.setName(name);
        constraint.setType(type);
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

    public void removeConstraint(String name)
    {
        if(name == null)
        {
            return;
        }
        List<M2Constraint> cons = new ArrayList<M2Constraint>(getConstraints());
        for (M2Constraint con : cons)
        {
            if (name.equals(con.getName()))
            {
                constraints.remove(con);
            }
        }
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
        this.configProperties = configProperties;
    }
}
