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

/**
 * 
 * Handles Site Entry in Site Membership Information response
 * 
 *      "entry": /{
          "site": {
            "id": "string",
            "guid": "string",
            "title": "string",
            "description": "string",
            "visibility": "PRIVATE",
            "role": "SiteConsumer"
          },
          "id": "string",
          "guid": "string",
          "role": "SiteConsumer"
        }
 *
 */
public class RestSiteEntry extends TestModel implements IRestModel<RestSiteEntry>
{      
    private String role;
    private String guid;
    private String id;
    
    @JsonProperty(value = "site", required = true)
    RestSiteModel site;
    
    @JsonProperty(value= "entry")
    RestSiteEntry model;

    public RestSiteModel onSite()
    {
        return site;
    }
    
    public String getRole()
    {
        return role;
    }

    public void setRole(String role)
    {
        this.role = role;
    }

    public String getGuid() {
      return guid;
    }

    public void setGuid(String guid) {
      this.guid = guid;
    }

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    @Override
    public RestSiteEntry onModel() 
    {
      return model;
    }
    
}    
