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
package org.alfresco.repo.domain.hibernate;

import java.util.Date;

import org.alfresco.repo.domain.AppliedPatch;

/**
 * Hibernate-specific implementation of the persistent object.
 * 
 * @author Derek Hulley
 */
public class AppliedPatchImpl implements AppliedPatch
{
    private String id;
    private String description;
    private int fixesFromSchema;
    private int fixesToSchema;
    private int targetSchema;

    private int appliedToSchema;
    private String appliedToServer;
    private Date appliedOnDate;
    private boolean succeeded;
    private String report;
    
    public AppliedPatchImpl()
    {
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(256);
        sb.append("AppliedPatch")
          .append("[ id=").append(id)
          .append(", description=").append(description)
          .append(", fixesFromSchema=").append(fixesFromSchema)
          .append(", fixesToSchema=").append(fixesToSchema)
          .append(", targetSchema=").append(targetSchema)
          .append(", appliedToSchema=").append(appliedToSchema)
          .append(", appliedToServer=").append(appliedToServer)
          .append(", appliedOnDate=").append(appliedOnDate)
          .append(", succeeded=").append(succeeded)
          .append(", report=").append(report)
          .append("]");
        return sb.toString();
    }

    public String getId()
    {
        return id;
    }
    public void setId(String id)
    {
        this.id = id;
    }
    
    public String getDescription()
    {
        return description;
    }
    public void setDescription(String description)
    {
        if (description.length() > 1024)
        {
            // truncate as necessary
            description = (description.substring(0, 1020) + "...");
        }
        this.description = description;
    }

    public int getFixesFromSchema()
    {
        return fixesFromSchema;
    }
    public void setFixesFromSchema(int version)
    {
        this.fixesFromSchema = version;
    }

    public int getFixesToSchema()
    {
        return fixesToSchema;
    }
    public void setFixesToSchema(int version)
    {
        this.fixesToSchema = version;
    }

    public int getTargetSchema()
    {
        return targetSchema;
    }
    public void setTargetSchema(int currentSchema)
    {
        this.targetSchema = currentSchema;
    }

    public int getAppliedToSchema()
    {
        return appliedToSchema;
    }
    public void setAppliedToSchema(int version)
    {
        this.appliedToSchema = version;
    }

    public String getAppliedToServer()
    {
        return appliedToServer;
    }

    public void setAppliedToServer(String appliedToServer)
    {
        this.appliedToServer = appliedToServer;
    }

    public Date getAppliedOnDate()
    {
        return appliedOnDate;
    }
    public void setAppliedOnDate(Date appliedOnDate)
    {
        this.appliedOnDate = appliedOnDate;
    }
    
    public boolean getSucceeded()
    {
        return succeeded;
    }
    public void setSucceeded(boolean succeeded)
    {
        this.succeeded = succeeded;
    }
    
    public String getReport()
    {
        return report;
    }
    public void setReport(String report)
    {
        if (report.length() > 1024)
        {
            // truncate as necessary
            report = (report.substring(0, 1020) + "...");
        }
        this.report = report;
    }
}