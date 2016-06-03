/*
 * Copyright (C) 2005-2016 Alfresco Software Limited.
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

package org.alfresco.rest.api.authentications;

import org.alfresco.rest.api.Authentications;
import org.alfresco.rest.api.model.LoginTicket;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiNoAuth;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.webscripts.WithResponse;
import org.alfresco.util.PropertyCheck;
import org.springframework.beans.factory.InitializingBean;

import java.util.Collections;
import java.util.List;

/**
 * @author Jamal Kaabi-Mofrad
 */
@EntityResource(name = "tickets", title = "Authentication tickets")
public class AuthenticationTicketsEntityResource implements EntityResourceAction.Create<LoginTicket>,
            EntityResourceAction.ReadByIdWithResponse<LoginTicket>,
            EntityResourceAction.DeleteWithResponse,
            InitializingBean
{
    private Authentications authentications;

    public void setAuthentications(Authentications authentications)
    {
        this.authentications = authentications;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "authentications", authentications);
    }

    @WebApiDescription(title = "Login", description = "Login.")
    @WebApiNoAuth
    @Override
    public List<LoginTicket> create(List<LoginTicket> entity, Parameters parameters)
    {
        if (entity == null || entity.size() != 1)
        {
            throw new InvalidArgumentException("Please specify one login request only.");
        }
        LoginTicket result = authentications.createTicket(entity.get(0), parameters);
        return Collections.singletonList(result);
    }

    @WebApiDescription(title = "Validate login ticket", description = "Validates the specified ticket is still valid.")
    @Override
    public LoginTicket readById(String ticket, Parameters parameters, WithResponse withResponse)
    {
        return authentications.validateTicket(ticket, parameters, withResponse);
    }

    @WebApiDescription(title = "Logout", description = "Logout.")
    @Override
    public void delete(String ticket, Parameters parameters, WithResponse withResponse)
    {
        authentications.deleteTicket(ticket, parameters, withResponse);
    }
}
