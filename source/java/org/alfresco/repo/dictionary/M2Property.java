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
    private String name = null;
    private String title = null;
    private String description = null;
    private String propertyType = null;
    private boolean isProtected = false;
    private boolean isMandatory = false;
    private boolean isMultiValued = false;
    private String defaultValue = null;
    private boolean isIndexed = true;
    private boolean isIndexedAtomically = true;
    private boolean isStoredInIndex = false;
    private boolean isTokenisedInIndex = true;
    private List<M2Constraint> constraints;

    /*package*/ M2Property()
    {
    }

    
    /*package*/ M2Property(String name)
    {
        this.name = name;
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
    

    public boolean isTokenisedInIndex()
    {
        return isTokenisedInIndex;
    }
    
    
    public void setTokenisedInIndex(boolean isTokenisedInIndex)
    {
        this.isTokenisedInIndex = isTokenisedInIndex;
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
}
