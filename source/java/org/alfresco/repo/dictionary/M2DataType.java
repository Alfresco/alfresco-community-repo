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
    private String analyserClassName = null;
    private String javaClassName = null;
    
    
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
    
    
    public String getAnalyserClassName()
    {
        return analyserClassName;
    }
    
    
    public void setAnalyserClassName(String analyserClassName)
    {
        this.analyserClassName = analyserClassName;;
    }

    
    public String getJavaClassName()
    {
        return javaClassName;
    }
    
    
    public void setJavaClassName(String javaClassName)
    {
        this.javaClassName = javaClassName;;
    }
    
}
