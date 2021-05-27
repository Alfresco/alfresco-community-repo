/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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

package org.alfresco.rest.api.storageclasses;

import org.alfresco.rest.api.ContentStorageClasses;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;

/**
 * An implementation of an Entity Resource for handling storage classes.
 */
@EntityResource(name = "storage-classes", title = "Storage Classes")
public class StorageClassesEntityResource implements EntityResourceAction.Read<String>
{
    private ContentStorageClasses contentStorageClasses;

    public void setContentStorageClasses(ContentStorageClasses contentStorageClasses)
    {
        this.contentStorageClasses = contentStorageClasses;
    }
    
    @Override
    @WebApiDescription(title = "Get List of Storage Classes", description = "Get List of Storage Classes")
    public CollectionWithPagingInfo<String> readAll(Parameters params)
    {
        return contentStorageClasses.getStorageClasses(params.getPaging());
    }

}
