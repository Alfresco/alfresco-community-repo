/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.bootstrap;

import org.alfresco.service.cmr.admin.RepoUsage.LicenseMode;
import org.alfresco.service.cmr.module.ModuleService;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.service.license.LicenseDescriptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * Module compatibility component.
 * <p>
 * Checks that the currently installed RM AMP licence mode matches that of the
 * underlying repository.
 * 
 * @author Roy Wetherall
 * @since 2.4
 */
public class ModuleCompatibilityComponent implements ApplicationListener<ContextRefreshedEvent>
{
    /** Logger */
    private static Log logger = LogFactory.getLog(ModuleCompatibilityComponent.class);

    // TODO get this from somewhere
    private static final String RM_ENT_MODULE_ID = "alfresco-rm-enterprise-repo";

    /** descriptor service */
    private DescriptorService descriptorService;

    /** module service */
    private ModuleService moduleService;

    /**
     * @param descriptorService descriptor service
     */
    public void setDescriptorService(DescriptorService descriptorService)
    {
        this.descriptorService = descriptorService;
    }

    /**
     * @param moduleService module service
     */
    public void setModuleService(ModuleService moduleService)
    {
        this.moduleService = moduleService;
    }

    /**
     * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent)
    {
        // license mode
        LicenseMode licenseMode = LicenseMode.UNKNOWN;

        // grab the application context
        ApplicationContext applicationContext = contextRefreshedEvent.getApplicationContext();

        // get the license mode
        LicenseDescriptor license = descriptorService.getLicenseDescriptor();
        if (license != null)
        {
            licenseMode = license.getLicenseMode();
        }

        // determine whether RM Enterprise is installed or not
        boolean isRMEnterprise = isRMEnterprise();

        // debug log
        if (logger.isDebugEnabled())
        {
            logger.debug("Module compatibility information:");
            logger.debug("   Repository licence mode = " + licenseMode.toString());
            logger.debug("   RM Enterprise installed = " + isRMEnterprise);
        }

        if (LicenseMode.ENTERPRISE.equals(licenseMode) && !isRMEnterprise)
        {
            // running enterprise rm on community core so close application
            // context
            closeApplicationContext(applicationContext,
                        "Running Community Records Management Module on Enterprise Alfresco One is not a supported configuration.");

        }
        else if (!LicenseMode.ENTERPRISE.equals(licenseMode) && isRMEnterprise)
        {
            // running community rm on enterprise core so close application
            // context
            closeApplicationContext(applicationContext,
                        "Running Enterprise Records Management module on Community Alfresco One is not a supported configuration.");
        }
    }

    /**
     * Indicates whether RM Enterprise module is installed or not.
     * 
     * @return boolean true if RM Enterprise is installed, false otherwise
     */
    private boolean isRMEnterprise()
    {
        return (moduleService.getModule(RM_ENT_MODULE_ID) != null);
    }

    /**
     * Close application context, logging message.
     * 
     * @param applicationContext application context
     * @param message closure message
     */
    private void closeApplicationContext(ApplicationContext applicationContext, String message)
    {
        // log closure message
        if (logger.isErrorEnabled())
        {
            logger.error(message);
        }

        // close the application context!
        ((ConfigurableApplicationContext) applicationContext).close();
    }
}
