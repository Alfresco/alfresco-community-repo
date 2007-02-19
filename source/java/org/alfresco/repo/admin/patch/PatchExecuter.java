/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.admin.patch;

import java.util.Date;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.i18n.I18NUtil;
import org.alfresco.util.AbstractLifecycleBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;

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
        logger.info(I18NUtil.getMessage(MSG_CHECKING));
        
        Date before = new Date(System.currentTimeMillis() - 60000L);  // 60 seconds ago
        patchService.applyOutstandingPatches();
        Date after = new Date(System .currentTimeMillis() + 20000L);  // 20 seconds ahead
        
        // get all the patches executed in the time
        List<PatchInfo> appliedPatches = patchService.getPatches(before, after);
        
        // don't report anything if nothing was done
        if (appliedPatches.size() == 0)
        {
            logger.info(I18NUtil.getMessage(MSG_NO_PATCHES_REQUIRED));
        }
        else
        {
            boolean succeeded = true;
            // list all patches applied, including failures
            for (PatchInfo patchInfo : appliedPatches)
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
                    succeeded = false;
                    logger.error(I18NUtil.getMessage(MSG_FAILED, patchInfo.getId(), patchInfo.getReport()));
               }
            }
            // generate an error if there was a failure
            if (!succeeded)
            {
                throw new AlfrescoRuntimeException("Not all patches could be applied");
            }
        }
    }

    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        applyOutstandingPatches();
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        // NOOP
    }

}
