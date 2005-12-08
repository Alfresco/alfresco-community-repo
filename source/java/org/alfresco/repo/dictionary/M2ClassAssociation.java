/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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

    
    public boolean isTargetMandatory()
    {
        return isTargetMandatory == null ? false : isTargetMandatory;
    }
    
    
    public void setTargetMandatory(boolean isTargetMandatory)
    {
        this.isTargetMandatory = isTargetMandatory;
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
