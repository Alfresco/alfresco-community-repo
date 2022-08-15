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
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.TestModel;

/**
 * 
 * "activitySummary": {
            "firstName": "string",
            "lastName": "string",
            "parentObjectId": "string",
            "title": "string",
            "objectId": "string"
          }
 *
 * @author Cristina Axinte
 * 
 */
public class RestActivitySummaryModel extends TestModel implements IRestModel<RestActivitySummaryModel>
{
    @JsonProperty(value = "entry")
    RestActivitySummaryModel activitySummaryModel;
    
    
    @Override
    public RestActivitySummaryModel onModel() 
    {        
      return activitySummaryModel;
    }
        
    String firstName;
    String lastName;
    String parentObjectId;
    String title;
    String objectId;    
    String memberFirstName;
    UserRole role;
    String memberLastName;
    String memberPersonId;
    
    public String getMemberFirstName()    
    {
        return memberFirstName;
    }
    public void setMemberFirstName(String memberFirstName)
    {
        this.memberFirstName = memberFirstName;
    }
    public UserRole getRole()
    {
        return role;
    }
    public void setRole(UserRole role)
    {
        this.role = role;
    }
    public String getMemberLastName()
    {
        return memberLastName;
    }
    public void setMemberLastName(String memberLastName)
    {
        this.memberLastName = memberLastName;
    }
    public String getMemberPersonId()
    {
        return memberPersonId;
    }
    public void setMemberPersonId(String memberPersonId)
    {
        this.memberPersonId = memberPersonId;
    }
    
    public String getFirstName()
    {
        return firstName;
    }
    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    public String getLastName()
    {
        return lastName;
    }
    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    public String getParentObjectId()
    {
        return parentObjectId;
    }
    public void setParentObjectId(String parentObjectId)
    {
        this.parentObjectId = parentObjectId;
    }
    public String getTitle()
    {
        return title;
    }
    public void setTitle(String title)
    {
        this.title = title;
    }
    public String getObjectId()
    {
        return objectId;
    }
    public void setObjectId(String objectId)
    {
        this.objectId = objectId;
    }
}
