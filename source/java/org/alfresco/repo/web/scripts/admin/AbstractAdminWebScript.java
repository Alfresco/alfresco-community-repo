/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.web.scripts.admin;

import java.util.Collections;
import java.util.Map;

import org.alfresco.service.cmr.admin.RepoAdminService;
import org.alfresco.service.cmr.admin.RepoUsage;
import org.alfresco.service.cmr.admin.RepoUsageStatus.RepoUsageLevel;
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
    public static final String JSON_KEY_LEVEL = "level";
    public static final String JSON_KEY_WARNINGS = "warnings";
    public static final String JSON_KEY_ERRORS = "errors";

    /**
     * Logger that can be used by subclasses.
     */
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    protected RepoAdminService repoAdminService;

    /**
     * @param repoAdminService  the service that provides the functionality
     */
    public void setRepoAdminService(RepoAdminService repoAdminService)
    {
        this.repoAdminService = repoAdminService;
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
        
        model.put(JSON_KEY_LEVEL, RepoUsageLevel.OK.ordinal());
        model.put(JSON_KEY_WARNINGS, Collections.emptyList());
        model.put(JSON_KEY_ERRORS, Collections.emptyList());
    }
}
