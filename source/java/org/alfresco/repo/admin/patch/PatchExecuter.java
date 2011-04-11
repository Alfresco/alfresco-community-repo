/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.admin.patch;

import java.util.Date;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * This component is responsible for ensuring that patches are applied
 * at the appropriate time.
 * 
 * @author Derek Hulley
 */
public class PatchExecuter extends AbstractLifecycleBean
{
    private static final String MSG_CHECKING = "patch.executer.checking";
    private static final String MSG_NO_PATCHES_REQUIRED = "patch.executer.no_patches_required";
    private static final String MSG_SYSTEM_READ_ONLY = "patch.executer.system_readonly";
    private static final String MSG_NOT_EXECUTED = "patch.executer.not_executed";
    private static final String MSG_EXECUTED = "patch.executer.executed";
    private static final String MSG_FAILED = "patch.executer.failed";
    
    private static Log logger = LogFactory.getLog(PatchExecuter.class);
    
    private PatchService patchService;

    /**
     * @param patchService the server that actually executes the patches
     */
    public void setPatchService(PatchService patchService)
    {
        this.patchService = patchService;
    }

    /**
     * Ensures that all outstanding patches are applied.
     */
    public void applyOutstandingPatches()
    {
        // Apply patches even if we are in read only mode.   The system may not work safely otherwise.
        
        if (!patchService.validatePatches())
        {
            logger.warn(I18NUtil.getMessage(MSG_SYSTEM_READ_ONLY));
            return;
        }
        
        logger.info(I18NUtil.getMessage(MSG_CHECKING));
        
        Date before = new Date(System.currentTimeMillis() - 60000L);  // 60 seconds ago
        boolean applySucceeded = patchService.applyOutstandingPatches();
        Date after = new Date(System .currentTimeMillis() + 20000L);  // 20 seconds ahead
        
        // get all the patches executed in the time
        List<AppliedPatch> appliedPatches = patchService.getPatches(before, after);
        
        // don't report anything if nothing was done
        if (applySucceeded && appliedPatches.size() == 0)
        {
            logger.info(I18NUtil.getMessage(MSG_NO_PATCHES_REQUIRED));
        }
        else
        {
            boolean allPassed = true;
            // list all patches applied, including failures
            for (AppliedPatch patchInfo : appliedPatches)
            {
                if (!patchInfo.getWasExecuted())
                {
                    // the patch was not executed
                    logger.debug(I18NUtil.getMessage(MSG_NOT_EXECUTED, patchInfo.getId(), patchInfo.getReport()));
                }
                else if (patchInfo.getSucceeded())
                {
                    logger.info(I18NUtil.getMessage(MSG_EXECUTED, patchInfo.getId(), patchInfo.getReport()));
                }
                else
                {
                    allPassed = false;
                    logger.error(I18NUtil.getMessage(MSG_FAILED, patchInfo.getId(), patchInfo.getReport()));
               }
            }
            // generate an error if there was a failure
            if (!allPassed || !applySucceeded)
            {
                throw new AlfrescoRuntimeException("Not all patches could be applied");
            }
        }
    }

    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        RunAsWork<Void> runPatches = new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                applyOutstandingPatches();
                return null;
            }
        };
        AuthenticationUtil.runAs(runPatches, AuthenticationUtil.getSystemUserName());
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        // NOOP
    }
}
