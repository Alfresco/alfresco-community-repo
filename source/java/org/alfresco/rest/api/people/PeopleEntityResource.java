/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.rest.api.people;

import org.alfresco.rest.api.People;
import org.alfresco.rest.api.model.Person;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * An implementation of an Entity Resource for a Person
 * 
 * @author sglover
 * @author Gethin James
 */
@EntityResource(name="people", title = "People")
public class PeopleEntityResource implements EntityResourceAction.ReadById<Person>, InitializingBean
{
    private static Log logger = LogFactory.getLog(PeopleEntityResource.class);
    
    private People people;
    
	public void setPeople(People people)
	{
		this.people = people;
	}

	@Override
    public void afterPropertiesSet()
    {
        ParameterCheck.mandatory("people", this.people);
    }
	
    /**
     * Get a person by userName.
     * 
     * @see org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction.ReadById#readById(java.lang.String)
     */
    @Override
    @WebApiDescription(title = "Get Person Information", description = "Get information for the person with id 'personId'")
    @WebApiParam(name = "personId", title = "The person's username")
    public Person readById(String personId, Parameters parameters)
    {
        Person person = people.getPerson(personId);
        return person;
    }

}