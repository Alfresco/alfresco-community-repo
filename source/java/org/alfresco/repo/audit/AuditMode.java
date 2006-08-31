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
package org.alfresco.repo.audit;

import org.alfresco.repo.audit.model.AuditModelException;

/**
 * An enum to specify the audit mode:
 * 
 * <ol>
 *   <li> ALL - all calls are audited
 *   <li> SUCCESS - only successful calls are audited (audited in the same TX)
 *   <li> FAIL - only fail calls are audited (in a new transaction)
 *   <li> NONE - noting is audited 
 *   <li> UNSET
 * </ol>
 * 
 * The mode is inherited from containers if nothing is specified
 * 
 * @author Andy Hind
 */
public enum AuditMode
{
    ALL, SUCCESS, FAIL, NONE, UNSET;
    
    public static AuditMode getAuditMode(String value)
    {
        if(value.equalsIgnoreCase("all"))
        {
            return AuditMode.ALL;
        }
        else if(value.equalsIgnoreCase("success"))
        {
            return AuditMode.SUCCESS;
        }
        else if(value.equalsIgnoreCase("fail"))
        {
            return AuditMode.FAIL;
        }
        else if(value.equalsIgnoreCase("none"))
        {
            return AuditMode.NONE;
        }
        else if(value.equalsIgnoreCase("unset"))
        {
            return AuditMode.UNSET;
        }
        else
        {
            throw new AuditModelException("Invalid audit mode: "+value);
        }
    }
}
