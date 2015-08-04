/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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

package org.alfresco.rest.api.cmm;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.rest.api.CustomModels;
import org.alfresco.rest.api.model.CustomModel;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.util.PropertyCheck;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author Jamal Kaabi-Mofrad
 */
@EntityResource(name = "cmm", title = "Custom Model Management")
public class CustomModelEntityResource implements EntityResourceAction.Read<CustomModel>,
            EntityResourceAction.ReadById<CustomModel>,
            EntityResourceAction.Create<CustomModel>,
            EntityResourceAction.Update<CustomModel>,
            EntityResourceAction.Delete,
            InitializingBean
{

    private CustomModels customModels;

    public void setCustomModels(CustomModels customModels)
    {
        this.customModels = customModels;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "customModels", customModels);
    }

    @Override
    @WebApiDescription(title="Returns custom model information for the given model name.")
    public CustomModel readById(String modelName, Parameters parameters) throws EntityNotFoundException
    {
        return customModels.getCustomModel(modelName, parameters);
    }

    @Override
    @WebApiDescription(title="Returns a paged list of all custom models.")
    public CollectionWithPagingInfo<CustomModel> readAll(Parameters parameters)
    {
        return customModels.getCustomModels(parameters);
    }

    @Override
    @WebApiDescription(title="Creates custom model(s).")
    public List<CustomModel> create(List<CustomModel> entity, Parameters parameters)
    {
        List<CustomModel> result = new ArrayList<>(entity.size());
        for (CustomModel cm : entity)
        {
            result.add(customModels.createCustomModel(cm));
        }
        return result;
    }

    @Override
    @WebApiDescription(title = "Updates or activates/deactivates the custom model.")
    public CustomModel update(String modelName, CustomModel entity, Parameters parameters)
    {
        return customModels.updateCustomModel(modelName, entity, parameters);
    }

    @Override
    @WebApiDescription(title = "Deletes the custom model.")
    public void delete(String modelName, Parameters parameters)
    {
        customModels.deleteCustomModel(modelName);
    }
}