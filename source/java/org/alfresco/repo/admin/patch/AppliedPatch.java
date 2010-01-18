/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.admin.patch;

import java.util.Date;

/**
 * Applied patch bean
 * 
 * @author Derek Hulley
 * @since 3.3
 */
public class AppliedPatch
{
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

    /**
     * Default constructor
     */
    public AppliedPatch()
    {
    }
    
    /**
     * Construct an instance from another patch info-provider
     */
    public AppliedPatch(AppliedPatch appliedPatch)
    {
        this.id = appliedPatch.getId();
        this.description = appliedPatch.getDescription();
        this.fixesFromSchema = appliedPatch.getFixesFromSchema();
        this.fixesToSchema = appliedPatch.getFixesToSchema();
        this.targetSchema = appliedPatch.getTargetSchema();
        this.appliedToSchema = appliedPatch.getAppliedToSchema();
        this.appliedToServer = appliedPatch.getAppliedToServer();
        this.appliedOnDate = appliedPatch.getAppliedOnDate();
        this.wasExecuted = appliedPatch.getWasExecuted();
        this.succeeded = appliedPatch.getSucceeded();
        this.report = appliedPatch.getReport();
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
        this.description = description;
    }
    
    public int getFixesFromSchema()
    {
        return fixesFromSchema;
    }
    public void setFixesFromSchema(int fixesFromSchema)
    {
        this.fixesFromSchema = fixesFromSchema;
    }
    
    public int getFixesToSchema()
    {
        return fixesToSchema;
    }
    public void setFixesToSchema(int fixesToSchema)
    {
        this.fixesToSchema = fixesToSchema;
    }
    
    public int getTargetSchema()
    {
        return targetSchema;
    }
    public void setTargetSchema(int targetSchema)
    {
        this.targetSchema = targetSchema;
    }
    
    public int getAppliedToSchema()
    {
        return appliedToSchema;
    }
    public void setAppliedToSchema(int appliedToSchema)
    {
        this.appliedToSchema = appliedToSchema;
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
        this.report = report;
    }
}
