/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.audit;

import org.alfresco.repo.audit.model._3.Application;
import org.alfresco.util.ParameterCheck;

/**
 * Entity bean for <b>alf_audit_session</b> table.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class AuditSession
{
    private final Application application;
    private final String rootPath;
    
    public AuditSession(Application application, String rootPath)
    {
        ParameterCheck.mandatory("application", application);
        ParameterCheck.mandatoryString("rootPath", rootPath);
        
        this.application = application;
        this.rootPath = rootPath;
    }
    
    @Override
    public int hashCode()
    {
        return (application.getName().hashCode() + rootPath.hashCode());
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (obj instanceof AuditSession)
        {
            AuditSession that = (AuditSession) obj;
            return this.application.getName().equals(that.application.getName()) &&
                   this.rootPath.equals(that.rootPath);
        }
        else
        {
            return false;
        }
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("AuditSession")
          .append("[ application=").append(application.getName())
          .append(", rootPath=").append(rootPath)
          .append("]");
        return sb.toString();
    }

    public Application getApplication()
    {
        return application;
    }

    public String getRootPath()
    {
        return rootPath;
    }
}
