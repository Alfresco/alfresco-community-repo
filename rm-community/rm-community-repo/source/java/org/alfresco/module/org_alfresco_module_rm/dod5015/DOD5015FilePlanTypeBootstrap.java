package org.alfresco.module.org_alfresco_module_rm.dod5015;

import org.alfresco.module.org_alfresco_module_rm.model.rma.type.RmSiteType;

/**
 * Bootstrap bean that registers the dod:filePlan for creation when 
 * a dod:site is created.
 * 
 * @author Roy Wetherall
 * @since 2.2
 */
public class DOD5015FilePlanTypeBootstrap implements DOD5015Model
{
    /** RM site type bean */
    private RmSiteType rmSiteType;
    
    /**
     * @param rmSiteType    RM site type bean
     */
    public void setRmSiteType(RmSiteType rmSiteType)
    {
        this.rmSiteType = rmSiteType;
    }
    
    /**
     * Init method
     */
    public void init()
    {
        // register dod file plan type for the dod site type
        rmSiteType.registerFilePlanType(TYPE_DOD_5015_SITE, TYPE_DOD_5015_FILE_PLAN);
    }
}
