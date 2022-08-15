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

import static org.alfresco.utility.report.log.Step.STEP;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.TestModel;
import org.testng.Assert;

public class RestSiteMemberModel extends TestModel implements IRestModel<RestSiteMemberModel>
{
    @JsonProperty(value = "entry")
    RestSiteMemberModel model;

    @Override
    public RestSiteMemberModel onModel()
    {
        return model;
    }

    private UserRole role;
    private String id = "no-id";
    private boolean isMemberOfGroup;

    private RestPersonModel person;

    public UserRole getRole()
    {
        return role;
    }

    public void setRole(UserRole role)
    {
        this.role = role;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public RestPersonModel getPerson()
    {
        return person;
    }

    public boolean getIsMemberOfGroup()
    {
        return isMemberOfGroup;
    }

    public void setIsMemberOfGroup(boolean memberOfGroup)
    {
        isMemberOfGroup = memberOfGroup;
    }

    public void setPerson(RestPersonModel person)
    {
        this.person = person;
    }

    public RestSiteMemberModel assertSiteMemberHasRole(UserRole role) {
        STEP(String.format("REST API: Assert that site member role is '%s'", role));
        Assert.assertEquals(getRole(), role, "Site member role is not as expected.");
        
        return this;
    }
}
