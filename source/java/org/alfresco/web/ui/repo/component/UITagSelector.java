/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.web.ui.repo.component;

import java.util.ArrayList;
import java.util.Collection;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.cmr.search.CategoryService.Depth;
import org.alfresco.web.bean.repository.Repository;

/**
 * Component to allow the selection of a tags
 */
public class UITagSelector extends UICategorySelector
{
    public Collection<NodeRef> getRootChildren(FacesContext context)
    {
        Collection<ChildAssociationRef> childRefs = getCategoryService(context).getCategories(Repository.getStoreRef(), ContentModel.ASPECT_TAGGABLE, Depth.IMMEDIATE);
        Collection<NodeRef> refs = new ArrayList<NodeRef>(childRefs.size());
        for (ChildAssociationRef childRef : childRefs)
        {
            refs.add(childRef.getChildRef());
        }

        return refs;
    }

    private static CategoryService getCategoryService(FacesContext context)
    {
        CategoryService service = Repository.getServiceRegistry(context).getCategoryService();
        if (service == null)
        {
            throw new IllegalStateException("Unable to obtain CategoryService bean reference.");
        }

        return service;
    }
}