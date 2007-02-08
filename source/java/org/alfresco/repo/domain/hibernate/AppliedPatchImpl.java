/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.domain.hibernate;

import java.io.Serializable;
import java.util.Date;

import org.alfresco.repo.domain.AppliedPatch;

/**
 * Hibernate-specific implementation of the persistent object.
 * 
 * @author Derek Hulley
 */
public class AppliedPatchImpl implements AppliedPatch, Serializable
{
    private static final long serialVersionUID = 2694230422651768785L;

    private String id;
    private String description;
    private int fixesFromSchema;
    private int fixesToSchema;
    private int targetSchema;

    private int appliedToSchema;
    private String appliedToServer;
    private Date appliedOnDate;
    private boolean wasExecuted;
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
          .append(", wasExecuted=").append(wasExecuted)
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
        if (description != null && description.length() > 1024)
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
    
    public boolean getWasExecuted()
    {
        return wasExecuted;
    }
    public void setWasExecuted(boolean wasExecuted)
    {
        this.wasExecuted = wasExecuted;
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
        if (report != null && report.length() > 1024)
        {
            // truncate as necessary
            report = (report.substring(0, 1020) + "...");
        }
        this.report = report;
    }
}