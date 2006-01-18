/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.service.cmr.admin;

import java.io.Serializable;
import java.util.Date;

import org.alfresco.repo.domain.AppliedPatch;

/**
 * Provides information regarding an individual patch.
 * 
 * @since 1.2
 * @author Derek Hulley
 */
public class PatchInfo implements Serializable
{
    private static final long serialVersionUID = -8288217080763245510L;

    private String id;
    private String description;
    private String applyToVersion;
    private boolean succeeded;
    private String appliedOnVersion;
    private Date appliedOnDate;
    private String report;
    
    public PatchInfo(String id, String description, String applyAfterVersion)
    {
        this.id = id;
        this.description = description;
        this.applyToVersion = applyAfterVersion;
        this.succeeded = false;
    }
    
    public PatchInfo(AppliedPatch appliedPatch)
    {
        this.id = appliedPatch.getId();
        this.description = appliedPatch.getDescription();
        this.applyToVersion = appliedPatch.getApplyToVersion();
        
        this.succeeded = appliedPatch.getSucceeded();
        this.appliedOnVersion = appliedPatch.getAppliedOnVersion();
        this.appliedOnDate = appliedPatch.getAppliedOnDate();
        this.report = appliedPatch.getReport();
    }
    
    /**
     * @return Returns the unique patch identifier
     */
    public String getId()
    {
        return id;
    }

    /**
     * @return Returns a description of the patch
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * @return Returns the version of the repository after which this patch must be applied
     */
    public String getApplyToVersion()
    {
        return applyToVersion;
    }

    /**
     * @return Returns true if the patch has been successfully applied
     */
    public boolean getSucceeded()
    {
        return succeeded;
    }

    /**
     * @return Returns the repository version that the patch was applied on, or null if the patch
     *      has not been applied
     */
    public String getAppliedOnVersion()
    {
        return appliedOnVersion;
    }

    /**
     * @return Returns the date that the patch was applied, or null if the patch has not been applied
     */
    public Date getAppliedOnDate()
    {
        return appliedOnDate;
    }

    /**
     * Get a report generated during the last attempted application.  This will be an error report if
     * the last attempt failed.  If the application of the patch was successful
     * ({@link #getAppliedOnDate() applied date} is not null) the it will be a message saying that it worked.
     * 
     * @return Returns a report generated during application.  This is only null if no attempt has been
     *      made to apply the patch.
     */
    public String getReport()
    {
        return report;
    }
}
