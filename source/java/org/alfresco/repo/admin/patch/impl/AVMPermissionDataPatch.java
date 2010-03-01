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
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;


/**
 * The roles defined in permissionsDefinition.xml moved from <b>wca:webfolder</b> to <b>cm:cmobject</b>.
 * This effects the data stored in the <b>permission</b> table.
 * <p>
 * 
 */
public class AVMPermissionDataPatch extends AbstractPermissionChangePatch
{
    private static final String MSG_SUCCESS = "patch.updateAvmPermissionData.result";
    
    private static final QName TYPE_QNAME_OLD = QName.createQName(NamespaceService.WCMAPP_MODEL_1_0_URI, "webfolder");
    private static final QName TYPE_QNAME_NEW = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "cmobject");
    private static final String[] NAMES = new String[] {"ContentManager", "ContentPublisher", "ContentContributor", "ContentReviewer"};

    @Override
    protected String applyInternal() throws Exception
    {
        int updateCount = 0;
        for (String permissionName : NAMES)
        {
            updateCount += super.renamePermission(
                    AVMPermissionDataPatch.TYPE_QNAME_OLD,
                    permissionName,
                    AVMPermissionDataPatch.TYPE_QNAME_NEW,
                    permissionName);
        }

        // build the result message
        String msg = I18NUtil.getMessage(MSG_SUCCESS, updateCount);
        // done
        return msg;
    }
}
