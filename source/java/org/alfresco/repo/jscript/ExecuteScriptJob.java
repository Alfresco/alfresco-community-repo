/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.jscript;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.repository.ScriptLocation;
import org.alfresco.service.cmr.repository.ScriptService;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Quartz job that executes a scheduled JS script.
 * 
 * @author Roy Wetherall
 */
public class ExecuteScriptJob implements Job
{
    private static final String PARAM_SCRIPT_LOCATION = "scriptLocation";
    private static final String PARAM_SCRIPT_SERVICE = "scriptService"; 
    private static final String PARAM_AUTHENTICATION_COMPONENT = "authenticationComponent";
    
    /**
     * Executes the scheduled script
     * 
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        JobDataMap jobData = context.getJobDetail().getJobDataMap();
        
        // Get the script service from the job map
        Object scriptServiceObj = jobData.get(PARAM_SCRIPT_SERVICE);
        if (scriptServiceObj == null || !(scriptServiceObj instanceof ScriptService))
        {
            throw new AlfrescoRuntimeException(
                    "ExecuteScriptJob data must contain valid script service");
        }
        
        // Get the script location from the job map
        Object scriptLocationObj = jobData.get(PARAM_SCRIPT_LOCATION);
        if (scriptLocationObj == null || !(scriptLocationObj instanceof ScriptLocation))
        {
            throw new AlfrescoRuntimeException(
                    "ExecuteScriptJob data must contain valid script location");
        }
        
        // Get the authentication component from the job map
        Object authenticationComponentObj = jobData.get(PARAM_AUTHENTICATION_COMPONENT);
        if (authenticationComponentObj == null || !(authenticationComponentObj instanceof AuthenticationComponent))
        {
            throw new AlfrescoRuntimeException(
                    "ExecuteScriptJob data must contain valid authentication component");
        }
        
        
        // Execute the script as the system user
        ((AuthenticationComponent)authenticationComponentObj).setSystemUserAsCurrentUser();
        try
        {
            // Execute the script
            ((ScriptService)scriptServiceObj).executeScript((ScriptLocation)scriptLocationObj, null);
        }
        finally
        {
            ((AuthenticationComponent)authenticationComponentObj).clearCurrentSecurityContext();
        }
    }
}
