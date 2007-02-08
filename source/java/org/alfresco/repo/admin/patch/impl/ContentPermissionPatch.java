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
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Roles defined in permissionsDefinition.xml moved from <b>cm:content</b> to <b>sys:base</b>.
 * This effects the data stored in the <b>permission</b> table.
 * 
 * @author Derek Hulley
 */
public class ContentPermissionPatch extends AbstractPermissionChangePatch
{
    private static final String MSG_SUCCESS = "patch.contentPermission.result";
    
    private static final QName TYPE_QNAME_OLD = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "content");
    private static final QName TYPE_QNAME_NEW = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "base");
    private static final String[] NAMES = new String[] {"Execute", "ReadContent", "WriteContent", "ExecuteContent"};

    @Override
    protected String applyInternal() throws Exception
    {
        int updateCount = 0;
        for (String permissionName : NAMES)
        {
            updateCount += super.renamePermission(
                    ContentPermissionPatch.TYPE_QNAME_OLD,
                    permissionName,
                    ContentPermissionPatch.TYPE_QNAME_NEW,
                    permissionName);
        }

        // build the result message
        String msg = I18NUtil.getMessage(MSG_SUCCESS, updateCount);
        // done
        return msg;
    }
}
