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
 * Change Guest Person permission to make visible to all users as 'Consumer'.
 * 
 * This allows users other than admin to select the Guest user for Invite to a Space.
 */
public class GuestPersonPermissionPatch2 extends AbstractPatch
{
    private static final String MSG_SUCCESS = "patch.guestPersonPermission2.result";

    private PersonService personService;

    private PermissionService permissionService;

    private String guestId = "guest";

    public GuestPersonPermissionPatch2()
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
            permissionService.setInheritParentPermissions(personRef, true);
        }

        return I18NUtil.getMessage(MSG_SUCCESS);
    }

}
