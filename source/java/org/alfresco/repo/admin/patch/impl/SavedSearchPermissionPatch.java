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
