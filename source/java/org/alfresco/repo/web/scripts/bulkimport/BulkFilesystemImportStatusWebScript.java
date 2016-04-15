
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
