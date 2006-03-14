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

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract Property Constraint.
 * 
 * @author Derek Hulley
 */
public class M2Constraint
{
    private String name;
    private String ref;
    private String type;
    private String description;
    private List<M2NamedValue> parameters = new ArrayList<M2NamedValue>(2);

    /*package*/ M2Constraint()
    {
    }
    
    @Override
    public String toString()
    {
        return this.name;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getRef()
    {
        return ref;
    }

    public String getType()
    {
        return type;
    }

    public String getDescription()
    {
        return description;
    }
    
    public List<M2NamedValue> getParameters()
    {
        return parameters;
    }
}
