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
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;

/**
 * Change Guest Person permission from Guest to Read
 * 
 * Guest (now Consumer) permission is not valid for cm:person type.
 */
public class GuestPersonPermissionPatch extends AbstractPatch
{
    private static final String MSG_SUCCESS = "patch.guestPersonPermission.result";

    private PersonService personService;

    private PermissionService permissionService;

    private String guestId = "guest";

    public GuestPersonPermissionPatch()
    {
        super();
    }

    public void setGuestId(String guestId)
    {
        this.guestId = guestId;
    }

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }

    @Override
    protected String applyInternal() throws Exception
    {
        if (personService.personExists(guestId))
        {
            NodeRef personRef = personService.getPerson(guestId);
            permissionService.setInheritParentPermissions(personRef, false);
            permissionService.deletePermission(personRef, guestId, PermissionService.CONSUMER, true);
            permissionService.setPermission(personRef, guestId, PermissionService.READ, true);
        }

        return I18NUtil.getMessage(MSG_SUCCESS);
    }

}
