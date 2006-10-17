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
