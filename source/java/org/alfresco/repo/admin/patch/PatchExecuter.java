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
