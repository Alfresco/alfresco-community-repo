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
package org.alfresco.web.bean;

import java.util.Collection;
import java.util.List;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;

import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.ui.common.component.IBreadcrumbHandler;
import org.alfresco.web.ui.common.component.data.UIRichList;

public class CategoriesProperties
{

    protected CategoryService categoryService;

    /** Component references */
    protected UIRichList categoriesRichList;

    /** Currently visible category Node */
    private Node category = null;

    /** Current category ref */
    private NodeRef categoryRef = null;

    /** Action category node */
    private Node actionCategory = null;

    /** Members of the linked items of a category */
    private Collection<ChildAssociationRef> members = null;

    /** Dialog properties */
    private String name = null;
    private String description = null;

    /** RichList view mode */
    private String viewMode = "icons";

    /** Category path breadcrumb location */
    private List<IBreadcrumbHandler> location = null;

    public CategoryService getCategoryService()
    {
        return categoryService;
    }

    public void setCategoryService(CategoryService categoryService)
    {
        this.categoryService = categoryService;
    }

    public UIRichList getCategoriesRichList()
    {
        return categoriesRichList;
    }

    public void setCategoriesRichList(UIRichList categoriesRichList)
    {
        this.categoriesRichList = categoriesRichList;
    }

    public Node getCategory()
    {
        return category;
    }

    public void setCategory(Node category)
    {
        this.category = category;
    }

    public NodeRef getCategoryRef()
    {
        return categoryRef;
    }

    public void setCategoryRef(NodeRef categoryRef)
    {
        this.categoryRef = categoryRef;
    }

    public Node getActionCategory()
    {
        return actionCategory;
    }

    public void setActionCategory(Node actionCategory)
    {
        this.actionCategory = actionCategory;
    }

    public Collection<ChildAssociationRef> getMembers()
    {
        return members;
    }

    public void setMembers(Collection<ChildAssociationRef> members)
    {
        this.members = members;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getViewMode()
    {
        return viewMode;
    }

    public void setViewMode(String viewMode)
    {
        this.viewMode = viewMode;
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
