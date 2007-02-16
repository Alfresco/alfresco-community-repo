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
            permissionService.deletePermission(personRef, guestId, PermissionService.CONSUMER);
            permissionService.setPermission(personRef, guestId, PermissionService.READ, true);
        }

        return I18NUtil.getMessage(MSG_SUCCESS);
    }

}
