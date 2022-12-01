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

import java.util.List;

import org.alfresco.utility.model.TestModel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Meenal Bhave
 * "path": {
 *      "name": "/Folder-oawzdncUXFLgnFe",
 *      "isComplete": false,
 *      "elements": [
 *      {
 *              "aspectNames": ["cm:titled", "cm:auditable", "app:uifacets"],
 *              "id": "7f0c47ae-d334-4b66-a86b-1a60d2518ad1",
 *              "name": "Folder-oawzdncUXFLgnFe",
 *              "nodeType": "cm:folder"
 *      }
 *      ]
 * }
 */
public class RestPathModel extends TestModel
{
    private String name;
    @JsonProperty(value = "isComplete")
    private boolean isComplete;
    private List<RestElementModel> elements;

    public List<RestElementModel> getElements()
    {
        return elements;
    }

    public void setElements(List<RestElementModel> elements)
    {
        this.elements = elements;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public boolean isComplete()
    {
        return isComplete;
    }

    public void setComplete(boolean isComplete)
    {
        this.isComplete = isComplete;
    }
}
