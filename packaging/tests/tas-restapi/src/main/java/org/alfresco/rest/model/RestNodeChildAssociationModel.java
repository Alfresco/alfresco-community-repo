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

import com.fasterxml.jackson.annotation.JsonProperty;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.utility.model.TestModel;

public class RestNodeChildAssociationModel extends TestModel implements IRestModel<RestNodeChildAssociationModel>
{
    @Override
    public RestNodeChildAssociationModel onModel()
    {
        return model;
    }

    @JsonProperty(value = "entry")
    RestNodeChildAssociationModel model;

    @JsonProperty(required = true)
    private String childId;

    @JsonProperty(required = true)
    private String assocType;

    public RestNodeChildAssociationModel(String childId, String assocType)
    {
        this.childId = childId;
        this.assocType = assocType;
    }

    public RestNodeChildAssociationModel()
    {

    }

    public String getChildId()
    {
        return childId;
    }

    public void setChildId(String childId)
    {
        this.childId = childId;
    }

    public String getAssocType()
    {
        return assocType;
    }

    public void setAssocType(String assocType)
    {
        this.assocType = assocType;
    }

}
