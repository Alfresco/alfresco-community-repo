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
 * Abstract Association Definition.
 * 
 * @author David Caruana
 *
 */
public abstract class M2ClassAssociation
{
    private String name = null;
    private Boolean isProtected = null;
    private String title = null;
    private String description = null;
    private String sourceRoleName = null;
    private Boolean isSourceMandatory = null;
    private Boolean isSourceMany = null;
    private String targetClassName = null;
    private String targetRoleName = null;
    private Boolean isTargetMandatory = null;
    private Boolean isTargetMandatoryEnforced = null;
    private Boolean isTargetMany = null;
    
    
    /*package*/ M2ClassAssociation()
    {
    }
    
    
    /*package*/ M2ClassAssociation(String name)
    {
        this.name = name;
    }
    
    
    public boolean isChild()
    {
        return this instanceof M2ChildAssociation;
    }
    

    public String getName()
    {
        return name;
    }
    
    
    public void setName(String name)
    {
        this.name = name;
    }

    
    public boolean isProtected()
    {
        return isProtected == null ? false : isProtected;
    }
    
    
    public void setProtected(boolean isProtected)
    {
        this.isProtected = isProtected;
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
    
    
    public String getSourceRoleName()
    {
        return sourceRoleName;
    }
    
    
    public void setSourceRoleName(String name)
    {
        this.sourceRoleName = name;
    }


    public boolean isSourceMandatory()
    {
        return isSourceMandatory == null ? true : isSourceMandatory;
    }
    
    
    public void setSourceMandatory(boolean isSourceMandatory)
    {
        this.isSourceMandatory = isSourceMandatory;
    }
    
    
    public boolean isSourceMany()
    {
        return isSourceMany == null ? false : isSourceMany;
    }
    
    
    public void setSourceMany(boolean isSourceMany)
    {
        this.isSourceMany = isSourceMany;
    }
    
    
    public String getTargetClassName()
    {
        return targetClassName;
    }
    
    
    public void setTargetClassName(String targetClassName)
    {
        this.targetClassName = targetClassName;
    }

    
    public String getTargetRoleName()
    {
        return targetRoleName; 
    }
    
    
    public void setTargetRoleName(String name)
    {
        this.targetRoleName = name;
    }

    public Boolean getTargetMandatory()
    {
        return isTargetMandatory();
    }
    
    public boolean isTargetMandatory()
    {
        return isTargetMandatory == null ? false : isTargetMandatory;
    }
    
    
    public void setTargetMandatory(boolean isTargetMandatory)
    {
        this.isTargetMandatory = isTargetMandatory;
    }

    public Boolean getTargetMandatoryEnforced()
    {
        return isTargetMandatoryEnforced();
    }
    
    public boolean isTargetMandatoryEnforced()
    {
        return isTargetMandatoryEnforced == null ? false : isTargetMandatoryEnforced;
    }
    
    
    public void setTargetMandatoryEnforced(boolean isTargetMandatoryEnforced)
    {
        this.isTargetMandatoryEnforced = isTargetMandatoryEnforced;
    }
    
    
    public boolean isTargetMany()
    {
        return isTargetMany == null ? true : isTargetMany;
    }
    
    
    public void setTargetMany(boolean isTargetMany)
    {
        this.isTargetMany = isTargetMany;
    }
    
}
