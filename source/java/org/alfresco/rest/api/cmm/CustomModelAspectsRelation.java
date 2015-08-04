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
import org.alfresco.rest.api.model.CustomAspect;
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
@RelationshipResource(name = "aspects", entityResource = CustomModelEntityResource.class, title = "Custom Model Aspects")
public class CustomModelAspectsRelation implements RelationshipResourceAction.Read<CustomAspect>,
            RelationshipResourceAction.ReadById<CustomAspect>,
            RelationshipResourceAction.Create<CustomAspect>,
            RelationshipResourceAction.Update<CustomAspect>,
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
    @WebApiDescription(title = "Returns a paged list of all the custom model's aspects.")
    public CollectionWithPagingInfo<CustomAspect> readAll(String modelName, Parameters parameters)
    {
        return customModels.getCustomAspects(modelName, parameters);
    }

    @Override
    @WebApiDescription(title = "Returns custom aspect information for the given 'aspectName' in 'modelName'.")
    public CustomAspect readById(String modelName, String aspectName, Parameters parameters)
    {
        return customModels.getCustomAspect(modelName, aspectName, parameters);
    }

    @Override
    @WebApiDescription(title = "Removes the custom aspect for the given 'aspectName' in 'modelName'.")
    public void delete(String modelName, String aspectName, Parameters parameters)
    {
        customModels.deleteCustomAspect(modelName, aspectName);
    }

    @Override
    @WebApiDescription(title = "Updates the custom aspect in the given 'modelName'.")
    public CustomAspect update(String modelName, CustomAspect aspect, Parameters parameters)
    {
        return customModels.updateCustomAspect(modelName, aspect, parameters);
    }

    @Override
    @WebApiDescription(title = "Creates custom aspects for the model 'modelName'.")
    public List<CustomAspect> create(String modelName, List<CustomAspect> aspects, Parameters parameters)
    {
        List<CustomAspect> result = new ArrayList<>(aspects.size());
        for (CustomAspect aspect : aspects)
        {
            result.add(customModels.createCustomAspect(modelName, aspect));
        }
        return result;
    }
}
