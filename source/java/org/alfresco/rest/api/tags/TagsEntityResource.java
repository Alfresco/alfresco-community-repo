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
package org.alfresco.rest.api.tags;

import org.alfresco.rest.api.Tags;
import org.alfresco.rest.api.model.Tag;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.util.ParameterCheck;
import org.springframework.beans.factory.InitializingBean;

@EntityResource(name="tags", title = "Tags")
public class TagsEntityResource implements EntityResourceAction.Read<Tag>, EntityResourceAction.ReadById<Tag>, EntityResourceAction.Update<Tag>, InitializingBean
{
    private Tags tags;

    public void setTags(Tags tags)
    {
        this.tags = tags;
    }

	@Override
    public void afterPropertiesSet()
    {
        ParameterCheck.mandatory("tags", this.tags);
    }

	/**
	 * 
	 * Returns a paged list of all currently used tags in the store workspace://SpacesStore for the current tenant.
	 * 
	 */
	@Override
    @WebApiDescription(title="A paged list of all tags in the network.")
	public CollectionWithPagingInfo<Tag> readAll(Parameters parameters)
	{
		return tags.getTags(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, parameters.getPaging());
	}

	@Override
    @WebApiDescription(title="Updates a tag by unique Id")
	public Tag update(String id, Tag entity, Parameters parameters)
	{
		return tags.changeTag(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id, entity);
	}

	@Override
	public Tag readById(String id, Parameters parameters) throws EntityNotFoundException
	{
		return tags.getTag(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id);
	}
}
