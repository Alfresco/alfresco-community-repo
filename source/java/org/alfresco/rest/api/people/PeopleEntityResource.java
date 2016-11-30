/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.rest.api.people;

import org.alfresco.model.ContentModel;
import org.alfresco.rest.api.People;
import org.alfresco.rest.api.model.Person;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.core.ResourceParameter;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of an Entity Resource for a Person
 * 
 * @author sglover
 * @author Gethin James
 */
@EntityResource(name="people", title = "People")
public class PeopleEntityResource implements EntityResourceAction.ReadById<Person>, EntityResourceAction.Create<Person>, EntityResourceAction.Update<Person>,EntityResourceAction.Read<Person>, InitializingBean
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
     * @see org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction.ReadById#readById(String, org.alfresco.rest.framework.resource.parameters.Parameters)
     */
    @Override
    @WebApiDescription(title = "Get Person Information", description = "Get information for the person with id 'personId'")
    @WebApiParam(name = "personId", title = "The person's username")
    public Person readById(String personId, Parameters parameters)
    {
        Person person = people.getPerson(personId);
        return person;
    }

    @Override
    @WebApiDescription(title="Create person", description="Create a person")
    @WebApiParam(name="entity", title="A single person", description="A single person, multiple people are not supported.",
            kind= ResourceParameter.KIND.HTTP_BODY_OBJECT, allowMultiple=false)
    public List<Person> create(List<Person> persons, Parameters parameters)
    {
        Person p = persons.get(0);

        validateDerivedFieldsExistence(p);

        List<Person> result = new ArrayList<>(1);
        result.add(people.create(p));
        return result;
    }

    @Override
    @WebApiDescription(title="Update person", description="Update the given person's details")
    public Person update(String personId, Person person, Parameters parameters)
    {
        if (person.wasSet(ContentModel.PROP_USERNAME))
        {
            // REPO-1537
            throw new InvalidArgumentException("Unsupported field: id");
        }

        validateDerivedFieldsExistence(person);

        return people.update(personId, person);
    }

    /**
     * Explicitly test for the presence of system-maintained (derived) fields that are settable on Person (see also REPO-110).
     * 
     * @param person
     */
    private void validateDerivedFieldsExistence(Person person)
    {
        if (person.wasSet(ContentModel.PROP_USER_STATUS_TIME))
        {
            throw new InvalidArgumentException("Unsupported field: statusUpdatedAt");
        }

        if (person.wasSet(Person.PROP_PERSON_AVATAR_ID))
        {
            throw new InvalidArgumentException("Unsupported field: avatarId");
        }

        if (person.wasSet(ContentModel.PROP_SIZE_QUOTA))
        {
            throw new InvalidArgumentException("Unsupported field: quota");
        }

        if (person.wasSet(ContentModel.PROP_SIZE_CURRENT))
        {
            throw new InvalidArgumentException("Unsupported field: quotaUsed");
        }
    }

    @Override
    @WebApiDescription(title = "Get List of People", description = "Get List of People")
    public CollectionWithPagingInfo<Person> readAll(Parameters params)
    {
        return people.getPeople(params);
    }
}