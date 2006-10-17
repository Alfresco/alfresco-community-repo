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
 * The roles defined in permissionsDefinition.xml moved from <b>cm:folder</b> to <b>cm:cmobject</b>.
 * This effects the data stored in the <b>permission</b> table.
 * <p>
 * JIRA: {@link http://www.alfresco.org/jira/browse/AR-344 AR-344}
 * 
 * @author Derek Hulley
 */
public class PermissionDataPatch extends AbstractPermissionChangePatch
{
    private static final String MSG_SUCCESS = "patch.updatePermissionData.result";
    
    private static final QName TYPE_QNAME_OLD = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "folder");
    private static final QName TYPE_QNAME_NEW = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "cmobject");
    private static final String[] NAMES = new String[] {"Coordinator", "Contributor", "Editor", "Guest"};

    @Override
    protected String applyInternal() throws Exception
    {
        int updateCount = 0;
        for (String permissionName : NAMES)
        {
            updateCount += super.renamePermission(
                    PermissionDataPatch.TYPE_QNAME_OLD,
                    permissionName,
                    PermissionDataPatch.TYPE_QNAME_NEW,
                    permissionName);
        }

        // build the result message
        String msg = I18NUtil.getMessage(MSG_SUCCESS, updateCount);
        // done
        return msg;
    }
}
