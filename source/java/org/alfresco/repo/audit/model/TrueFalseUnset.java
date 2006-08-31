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
 * An enum for the values 
 * <ol>
 *   <li> TRUE
 *   <li> FALSE
 *   <li> UNSET
 * </ol>
 * 
 * @author Andy Hind
 */
public enum TrueFalseUnset
{
    TRUE, FALSE, UNSET;
    
    public static TrueFalseUnset getTrueFalseUnset(String value)
    {
        if(value.equalsIgnoreCase("true"))
        {
            return TrueFalseUnset.TRUE;
        }
        else if(value.equalsIgnoreCase("false"))
        {
            return TrueFalseUnset.FALSE;
        }
        else if(value.equalsIgnoreCase("unset"))
        {
            return TrueFalseUnset.UNSET;
        }
        else
        {
            throw new AuditModelException("Invalid value for TrueFalseUnset: "+value);
        }
    }
}   
