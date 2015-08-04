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
import org.alfresco.rest.api.model.CustomModelConstraint;
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
@RelationshipResource(name = "constraints", entityResource = CustomModelEntityResource.class, title = "Custom Model Constraints")
public class CustomModelConstraintRelation implements RelationshipResourceAction.Read<CustomModelConstraint>,
            RelationshipResourceAction.ReadById<CustomModelConstraint>,
            RelationshipResourceAction.Create<CustomModelConstraint>,
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
    @WebApiDescription(title = "Returns a paged list of all the custom model's constraints.")
    public CollectionWithPagingInfo<CustomModelConstraint> readAll(String modelName, Parameters parameters)
    {
        return customModels.getCustomModelConstraints(modelName, parameters);
    }

    @Override
    @WebApiDescription(title = "Returns custom constraint information for the given 'constraintName' in 'modelName'.")
    public CustomModelConstraint readById(String modelName, String constraintName, Parameters parameters)
    {
        return customModels.getCustomModelConstraint(modelName, constraintName, parameters);
    }

    @Override
    @WebApiDescription(title = "Creates custom constraints for the model 'modelName'.")
    public List<CustomModelConstraint> create(String modelName, List<CustomModelConstraint> constraints, Parameters parameters)
    {
        List<CustomModelConstraint> result = new ArrayList<>(constraints.size());
        for (CustomModelConstraint constraint : constraints)
        {
            result.add(customModels.createCustomModelConstraint(modelName, constraint));
        }
        return result;
    }
}
