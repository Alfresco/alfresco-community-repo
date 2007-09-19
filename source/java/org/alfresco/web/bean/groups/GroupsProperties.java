/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.web.bean.groups;

import java.util.List;

import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.web.ui.common.component.IBreadcrumbHandler;
import org.alfresco.web.ui.common.component.data.UIRichList;

public class GroupsProperties
{
    private static final String FILTER_CHILDREN = "children";

    /** The AuthorityService to be used by the bean */
    private AuthorityService authService;

    /** personService bean reference */
    private PersonService personService;

    /** Component references */
    private UIRichList groupsRichList;
    private UIRichList usersRichList;

    /** Currently visible Group Authority */
    private String group = null;
    private String groupName = null;

    /** Action group authority */
    private String actionGroup = null;
    private String actionGroupName = null;
    private int actionGroupItems = 0;

    /** Dialog properties */
    private String name = null;

    /** RichList view mode */
    private String viewMode = "icons";

    /** List filter mode */
    private String filterMode = FILTER_CHILDREN;

    /** Groups path breadcrumb location */
    private List<IBreadcrumbHandler> location = null;

    public AuthorityService getAuthService()
    {
        return authService;
    }

    public void setAuthService(AuthorityService authService)
    {
        this.authService = authService;
    }

    public PersonService getPersonService()
    {
        return personService;
    }

    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }

    public UIRichList getGroupsRichList()
    {
        return groupsRichList;
    }

    public void setGroupsRichList(UIRichList groupsRichList)
    {
        this.groupsRichList = groupsRichList;
    }

    public UIRichList getUsersRichList()
    {
        return usersRichList;
    }

    public void setUsersRichList(UIRichList usersRichList)
    {
        this.usersRichList = usersRichList;
    }

    public String getGroup()
    {
        return group;
    }

    public void setGroup(String group)
    {
        this.group = group;
    }

    public String getGroupName()
    {
        return groupName;
    }

    public void setGroupName(String groupName)
    {
        this.groupName = groupName;
    }

    public String getActionGroup()
    {
        return actionGroup;
    }

    public void setActionGroup(String actionGroup)
    {
        this.actionGroup = actionGroup;
    }

    public String getActionGroupName()
    {
        return actionGroupName;
    }

    public void setActionGroupName(String actionGroupName)
    {
        this.actionGroupName = actionGroupName;
    }

    public int getActionGroupItems()
    {
        return actionGroupItems;
    }

    public void setActionGroupItems(int actionGroupItems)
    {
        this.actionGroupItems = actionGroupItems;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getViewMode()
    {
        return viewMode;
    }

    public void setViewMode(String viewMode)
    {
        this.viewMode = viewMode;
    }

    public String getFilterMode()
    {
        return filterMode;
    }

    public void setFilterMode(String filterMode)
    {
        this.filterMode = filterMode;
    }

    public List<IBreadcrumbHandler> getLocation()
    {
        return location;
    }

    public void setLocation(List<IBreadcrumbHandler> location)
    {
        this.location = location;
    }

}
