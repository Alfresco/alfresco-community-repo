package org.alfresco.repo.web.scripts.admin;

import java.util.Collections;
import java.util.Map;

import org.alfresco.service.cmr.admin.RepoAdminService;
import org.alfresco.service.cmr.admin.RepoUsage;
import org.alfresco.service.cmr.admin.RepoUsageStatus.RepoUsageLevel;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.service.license.LicenseDescriptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.DeclarativeWebScript;

/**
 * Abstract implementation for scripts that access the {@link RepoAdminService}.
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public abstract class AbstractAdminWebScript extends DeclarativeWebScript
{
    public static final String JSON_KEY_LAST_UPDATE = "lastUpdate";
    public static final String JSON_KEY_USERS = "users";
    public static final String JSON_KEY_DOCUMENTS = "documents";
    public static final String JSON_KEY_LICENSE_MODE = "licenseMode";
    public static final String JSON_KEY_READ_ONLY = "readOnly";
    public static final String JSON_KEY_UPDATED = "updated";
    public static final String JSON_KEY_LICENSE_VALID_UNTIL = "licenseValidUntil";
    public static final String JSON_KEY_LICENSE_HOLDER = "licenseHolder";
    public static final String JSON_KEY_LEVEL = "level";
    public static final String JSON_KEY_WARNINGS = "warnings";
    public static final String JSON_KEY_ERRORS = "errors";

    /**
     * Logger that can be used by subclasses.
     */
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    protected RepoAdminService repoAdminService;
    protected DescriptorService descriptorService;

    /**
     * @param repoAdminService  the service that provides the functionality
     */
    public void setRepoAdminService(RepoAdminService repoAdminService)
    {
        this.repoAdminService = repoAdminService;
    }

    /**
     * @param descriptorService  the service that provides the functionality
     */
    public void setDescriptorService(DescriptorService descriptorService)
    {
        this.descriptorService = descriptorService;
    }

    /**
     * Return an I18N'd message for the given key or the key itself if not present
     * 
     * @param args              arguments to replace the variables in the message
     */
    protected String getI18NMessage(String key, Object ... args)
    {
        return I18NUtil.getMessage(key, args);
    }
    
    /**
     * Helper to assign JSON return variables based on the repository usage data.
     */
    protected void putUsageInModel(
            Map<String, Object> model,
            RepoUsage repoUsage,
            boolean updated)
    {
        model.put(JSON_KEY_LAST_UPDATE, repoUsage.getLastUpdate());
        model.put(JSON_KEY_USERS, repoUsage.getUsers());
        model.put(JSON_KEY_DOCUMENTS, repoUsage.getDocuments());
        model.put(JSON_KEY_LICENSE_MODE, repoUsage.getLicenseMode());
        model.put(JSON_KEY_READ_ONLY, repoUsage.isReadOnly());
        model.put(JSON_KEY_LICENSE_VALID_UNTIL, repoUsage.getLicenseExpiryDate());
        model.put(JSON_KEY_UPDATED, updated);

        // Add license holder
        LicenseDescriptor license = descriptorService.getLicenseDescriptor();
        if (license != null)
        {
            model.put(JSON_KEY_LICENSE_HOLDER, license.getHolderOrganisation());
        }

        model.put(JSON_KEY_LEVEL, RepoUsageLevel.OK.ordinal());
        model.put(JSON_KEY_WARNINGS, Collections.emptyList());
        model.put(JSON_KEY_ERRORS, Collections.emptyList());
    }
}
