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
package org.alfresco.web.bean.wcm;

import java.util.ResourceBundle;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.alfresco.web.app.Application;

public class WCMPermissionsUtils
{
    /**
     * @return The list of available permissions for the users/groups
     */
    public static SelectItem[] getPermissions()
    {
        ResourceBundle bundle = Application.getBundle(FacesContext.getCurrentInstance());

        Set<String> perms = ManagePermissionsDialog.getPermissionsForType();
        SelectItem[] permissions = new SelectItem[perms.size()];
        int index = 0;
        for (String permission : perms)
        {
            String displayLabel = bundle.getString(permission);
            permissions[index++] = new SelectItem(permission, displayLabel);
        }

        return permissions;
    }

}
