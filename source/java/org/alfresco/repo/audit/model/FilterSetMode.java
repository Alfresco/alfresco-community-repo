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
package org.alfresco.repo.audit.model;

/**
 * The enum to define if elements of a filter set are combined using AND or OR.
 * 
 * @author Andy Hind
 */
public enum FilterSetMode
{
    AND, OR;
    
    public static FilterSetMode getFilterSetMode(String value)
    {
        if(value.equalsIgnoreCase("or"))
        {
            return FilterSetMode.OR;
        }
        else if(value.equalsIgnoreCase("or"))
        {
            return FilterSetMode.AND;
        }
        else
        {
            throw new AuditModelException("Invalid FilterSetMode: "+value);
        }
    }
}
