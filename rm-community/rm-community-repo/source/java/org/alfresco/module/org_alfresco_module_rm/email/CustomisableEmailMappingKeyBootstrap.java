 
package org.alfresco.module.org_alfresco_module_rm.email;

import java.util.List;

/**
 * Bootstrap bean that indicates that the specified mappings are customisable
 *
 * @author Tuna Aksoy
 * @since 2.1
 */
public class CustomisableEmailMappingKeyBootstrap
{
    /** List of mappings to register as customisable */
    private List<String> customisable;

    /** Custom email mapping service */
    private CustomEmailMappingService customEmailMappingService;

    /**
     * @param customizable  list of mappings to register as customisable
     */
    public void setCustomisable(List<String> customisable)
    {
        this.customisable = customisable;
    }

    /**
     * Custom email mapping service
     *
     * @param customEmailMappingService the custom email mapping service
     */
    public void setCustomEmailMappingService(CustomEmailMappingService customEmailMappingService)
    {
        this.customEmailMappingService = customEmailMappingService;
    }

    /**
     * Bean initialisation method
     */
    public void init()
    {
        for (String customEmailMappingKey : customisable)
        {
            customEmailMappingService.registerEMailMappingKey(customEmailMappingKey);
        }
    }
}
