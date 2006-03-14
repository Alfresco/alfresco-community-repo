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
 * Definition of a named value that can be used for property injection.
 * 
 * @author Derek Hulley
 */
public class M2NamedValue
{
    private String name;
    private String value;
   
    
    /*package*/ M2NamedValue()
    {
    }


    @Override
    public String toString()
    {
        return (name + "=" + value);
    }

    public String getName()
    {
        return name;
    }
    
    /**
     * @return Returns the raw, unconverted value
     */
    public String getValue()
    {
        return value;
    }
}
