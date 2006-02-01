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
package org.alfresco.repo.admin.patch.impl;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.service.cmr.admin.PatchException;
import org.alfresco.service.cmr.security.PermissionService;

/**
 * Grant <b>CONTRIBUTOR</b> role to <b>EVERYONE</b> in <b>savedsearches</b> folder.
 * <p>
 * This patch expects the folder to be present.
 * <p>
 * JIRA: {@link http://www.alfresco.org/jira/browse/AWC-487 AR-487}
 * 
 * @see org.alfresco.repo.admin.patch.impl.SavedSearchFolderPatch
 * @author Derek Hulley
 */
public class SavedSearchPermissionPatch extends SavedSearchFolderPatch
{
    private static final String MSG_CREATED = "patch.savedSearchesPermission.result.applied";
    private static final String ERR_NOT_FOUND = "patch.savedSearchesPermission.err.not_found";
    
    private PermissionService permissionService;
    
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    @Override
    protected String applyInternal() throws Exception
    {
        // properties must be set
        checkCommonProperties();
        if (permissionService == null)
        {
            throw new PatchException("'permissionService' property has not been set");
        }
        
        // get useful values
        setUp();
        
        if (savedSearchesFolderNodeRef == null)
        {
            // it doesn't exist
            String msg = I18NUtil.getMessage(ERR_NOT_FOUND);
            throw new PatchException(msg);
        }
        // apply permission
        permissionService.setPermission(
                savedSearchesFolderNodeRef,
                PermissionService.ALL_AUTHORITIES,
                PermissionService.CONTRIBUTOR,
                true);
        String msg = I18NUtil.getMessage(MSG_CREATED, savedSearchesFolderNodeRef);

        // done
        return msg;
    }
}
