/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.admin.patch.impl;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.ContentModel;

/**
 * The permission 'Guest' has been renamed to 'Consumer'.
 *
 * @author David Caruana
 * @author Derek Hulley
 */
public class UpdateGuestPermissionPatch extends AbstractPermissionChangePatch
{
    private static final String MSG_SUCCESS = "patch.updateGuestPermission.result";
    
    @Override
    protected String applyInternal() throws Exception
    {
        int updateCount = super.renamePermission(ContentModel.TYPE_CMOBJECT, "Guest", ContentModel.TYPE_CMOBJECT, "Consumer");
        
        // build the result message
        String msg = I18NUtil.getMessage(MSG_SUCCESS, updateCount);
        // done
        return msg;
    }
}
