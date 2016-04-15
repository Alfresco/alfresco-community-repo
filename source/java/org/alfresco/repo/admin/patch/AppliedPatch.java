package org.alfresco.repo.admin.patch;

import java.util.Date;

/**
 * Applied patch bean
 * 
 * @author Derek Hulley
 * @since 3.4
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
