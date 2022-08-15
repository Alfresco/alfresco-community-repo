/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
package org.alfresco.rest.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.utility.model.CustomAspectModel;
import org.alfresco.utility.model.CustomAspectPropertiesModel;
import org.testng.Assert;

/**
 * @author Bogdan Bocancea
 */
public class RestCustomAspectModel extends CustomAspectModel implements IRestModel<RestCustomAspectModel>
{
    @JsonProperty(value = "entry")
    RestCustomAspectModel model;

    @Override
    public RestCustomAspectModel onModel()
    {
        return model;
    }

    public RestCustomAspectModel assertHasProperties(CustomAspectPropertiesModel... propertiesName)
    {
        List<String> propertiesModelName = new ArrayList<String>();
        List<CustomAspectPropertiesModel> properiesModel = getProperties();
        for (CustomAspectPropertiesModel model : properiesModel)
        {
            propertiesModelName.add(model.getName());
        }
        for (CustomAspectPropertiesModel property : propertiesName)
        {
            Assert.assertTrue(propertiesModelName.contains(property.getName()), String.format("Property %s was found.", property.getName()));
        }
        return this;
    }
}
