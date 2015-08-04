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
import org.alfresco.rest.api.model.CustomType;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.util.PropertyCheck;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author Jamal Kaabi-Mofrad
 */
@RelationshipResource(name = "types", entityResource = CustomModelEntityResource.class, title = "Custom Model Types")
public class CustomModelTypesRelation implements RelationshipResourceAction.Read<CustomType>,
            RelationshipResourceAction.ReadById<CustomType>,
            RelationshipResourceAction.Create<CustomType>,
            RelationshipResourceAction.Update<CustomType>,
            RelationshipResourceAction.Delete,
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
    @WebApiDescription(title = "Returns a paged list of all the custom model's types.")
    public CollectionWithPagingInfo<CustomType> readAll(String modelName, Parameters parameters)
    {
        return customModels.getCustomTypes(modelName, parameters);
    }

    @Override
    @WebApiDescription(title = "Returns custom type information for the given 'typeName' in 'modelName'.")
    public CustomType readById(String modelName, String typeName, Parameters parameters)
    {
        return customModels.getCustomType(modelName, typeName, parameters);
    }

    @Override
    @WebApiDescription(title = "Removes the custom type for the given 'typeName' in 'modelName'.")
    public void delete(String modelName, String typeName, Parameters parameters)
    {
        customModels.deleteCustomType(modelName, typeName);
    }

    @Override
    @WebApiDescription(title = "Updates the custom type in the given 'modelName'.")
    public CustomType update(String modelName, CustomType type, Parameters parameters)
    {
        return customModels.updateCustomType(modelName, type, parameters);
    }

    @Override
    @WebApiDescription(title = "Creates custom types for the model 'modelName'.")
    public List<CustomType> create(String modelName, List<CustomType> types, Parameters parameters)
    {
        List<CustomType> result = new ArrayList<>(types.size());
        for (CustomType type : types)
        {
            result.add(customModels.createCustomType(modelName, type));
        }
        return result;
    }
}
