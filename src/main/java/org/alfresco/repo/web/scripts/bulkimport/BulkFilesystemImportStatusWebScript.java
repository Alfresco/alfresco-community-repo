/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.repo.web.scripts.bulkimport;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.bulkimport.BulkFilesystemImporter;
import org.alfresco.service.cmr.admin.RepoUsage.LicenseMode;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.service.license.LicenseDescriptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Web Script class that provides status information on the bulk filesystem import process.
 *
 * @since 4.0
 */
public class BulkFilesystemImportStatusWebScript extends DeclarativeWebScript
{
    private final static Log logger = LogFactory.getLog(BulkFilesystemImportStatusWebScript.class);
    
    // Output parameters (for Freemarker)
    private final static String RESULT_IMPORT_STATUS = "importStatus";
    private final static String IS_ENTERPRISE = "isEnterprise";
    
    // Attributes
    private BulkFilesystemImporter bulkImporter;
    private DescriptorService descriptorService;

	public void setBulkImporter(BulkFilesystemImporter bulkImporter)
	{
		this.bulkImporter = bulkImporter;
	}
	
    public void setDescriptorService(DescriptorService descriptorService)
    {
		this.descriptorService = descriptorService;
	}

	/**
     * @see org.springframework.extensions.webscripts.DeclarativeWebScript#executeImpl(WebScriptRequest, Status, Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest request, Status status, Cache cache)
    {
        Map<String, Object> result = new HashMap<String, Object>();
        
        cache.setNeverCache(true);
        
        LicenseDescriptor licenseDescriptor = descriptorService.getLicenseDescriptor();
        boolean isEnterprise = (licenseDescriptor == null ? false : (licenseDescriptor.getLicenseMode() == LicenseMode.ENTERPRISE));

        result.put(IS_ENTERPRISE, Boolean.valueOf(isEnterprise));
        result.put(RESULT_IMPORT_STATUS, bulkImporter.getStatus());
        
        return(result);
    }
}
