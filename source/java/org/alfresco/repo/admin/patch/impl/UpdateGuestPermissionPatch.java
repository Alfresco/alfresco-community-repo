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
package org.alfresco.repo.admin.patch.impl;

import org.springframework.extensions.surf.util.I18NUtil;
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
