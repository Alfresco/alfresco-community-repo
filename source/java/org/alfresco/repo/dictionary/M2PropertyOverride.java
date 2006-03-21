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
    
}
