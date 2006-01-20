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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This component is responsible for ensuring that patches are applied
 * at the appropriate time.
 * 
 * @author Derek Hulley
 */
public class PatchExecuter
{
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
    public void applyOutStandingPatches()
    {
        /*
         * TODO: This is simplistic at the moment.  It must do better reporting of failures.
         */
        
        boolean success = patchService.applyOutstandingPatches();
        if (!success)
        {
            logger.error("Not all patches could be applied");
        }
        else
        {
            logger.info("Patches applied successfully");
        }
    }
}
