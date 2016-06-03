/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.virtual.bundle;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.repo.virtual.VirtualContentModel;
import org.alfresco.repo.virtual.ref.Reference;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.traitextender.SpringBeanExtension;
import org.alfresco.traitextender.Trait;

public abstract class VirtualSpringBeanExtension<E, T extends Trait> extends SpringBeanExtension<E, T>
{
    public VirtualSpringBeanExtension(Class<T> traitClass)
    {
        super(traitClass);
    }

    public boolean isVirtualContextFolder(NodeRef nodeRef, ActualEnvironment environment)
    {
        boolean isReference=Reference.isReference(nodeRef);
        boolean isFolder=environment.isSubClass(environment.getType(nodeRef),
                                                ContentModel.TYPE_FOLDER);
        boolean virtualContext=environment.hasAspect(nodeRef,VirtualContentModel.ASPECT_VIRTUAL_DOCUMENT);
        return  isReference && isFolder && virtualContext;
    }
}
