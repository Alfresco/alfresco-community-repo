package org.alfresco.repo.admin.patch;

/**
 * 
 * @author Roy Wetherall
 */
public class AppliedModulePatch extends AppliedPatch
{
    
    private String moduleId;
    
    public AppliedModulePatch()
    {
        super();
    }

    public AppliedModulePatch(AppliedModulePatch appliedModulePatch)
    {
        super(appliedModulePatch);
        
        this.moduleId = appliedModulePatch.getModuleId();
    }
    
    public String getModuleId()
    {
        return this.moduleId;
    }
    
    public void setModuleId(String moduleId)
    {
        this.moduleId = moduleId;
    }
}
