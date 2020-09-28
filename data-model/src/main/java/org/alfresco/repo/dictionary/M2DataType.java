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


/**
 * Property Type Definition
 * 
 * @author David Caruana
 *
 */
public class M2DataType
{
    private String name = null;
    private String title = null;
    private String description = null;
    private String defaultAnalyserClassName = null;
    private String javaClassName = null;
    private String analyserResourceBundleName = null;
    
    
    /*package*/ M2DataType()
    {
        super();
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
    
    
    public String getDefaultAnalyserClassName()
    {
        return defaultAnalyserClassName;
    }
    
    
    public void setDefaultAnalyserClassName(String defaultAnalyserClassName)
    {
        this.defaultAnalyserClassName = defaultAnalyserClassName;;
    }

    
    public String getJavaClassName()
    {
        return javaClassName;
    }
    
    
    public void setJavaClassName(String javaClassName)
    {
        this.javaClassName = javaClassName;;
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
    
}
