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
package org.alfresco.repo.domain.hibernate;

import java.util.Date;

import org.alfresco.repo.domain.AppliedPatch;

/**
 * Hibernate-specific implementation of the persistent object.
 * 
 * @author Derek Hulley
 */
public class AppliedPatchImpl implements AppliedPatch
{
    private String id;
    private String description;
    private String applyAfterVersion;
    private boolean succeeded;
    private String appliedOnVersion;
    private Date appliedOnDate;
    private String report;

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
        if (description.length() > 1024)
        {
            // truncate as necessary
            description = (description.substring(0, 1020) + "...");
        }
        this.description = description;
    }
    
    public String getAppliedOnVersion()
    {
        return appliedOnVersion;
    }
    public void setAppliedOnVersion(String appliedOnVersion)
    {
        this.appliedOnVersion = appliedOnVersion;
    }
    
    public boolean getSucceeded()
    {
        return succeeded;
    }
    public void setSucceeded(boolean succeeded)
    {
        this.succeeded = succeeded;
    }

    public String getApplyToVersion()
    {
        return applyAfterVersion;
    }
    public void setApplyToVersion(String applyAfterVersion)
    {
        this.applyAfterVersion = applyAfterVersion;
    }
    
    public Date getAppliedOnDate()
    {
        return appliedOnDate;
    }
    public void setAppliedOnDate(Date appliedOnDate)
    {
        this.appliedOnDate = appliedOnDate;
    }
    
    public String getReport()
    {
        return report;
    }
    public void setReport(String report)
    {
        if (report.length() > 1024)
        {
            // truncate as necessary
            report = (report.substring(0, 1020) + "...");
        }
        this.report = report;
    }
}