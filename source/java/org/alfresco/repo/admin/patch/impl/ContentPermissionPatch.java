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

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.service.cmr.admin.PatchException;
import org.hibernate.SessionFactory;

/**
 * Roles defined in permissionsDefinition.xml moved from <b>cm:content</b> to <b>sys:base</b>.
 * This effects the data stored in the <b>node_perm_entry</b> table.
 * <p>
 * <b>WILL NOT EXECUTE ANYMORE</b>
 * 
 * @author Derek Hulley
 */
public class ContentPermissionPatch extends AbstractPatch
{
    private static final String MSG_UPGRADE = "patch.contentPermission.upgrade";
    
    public ContentPermissionPatch()
    {
    }
    
    public void setSessionFactory(SessionFactory sessionFactory)
    {
    }
    
    @Override
    protected String applyInternal() throws Exception
    {
        throw new PatchException(MSG_UPGRADE);
    }
}
