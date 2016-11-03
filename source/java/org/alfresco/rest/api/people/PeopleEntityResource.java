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

        // Until REPO-110 is solved, we need to explicitly test for the presence of fields
        // that are present on Person but not PersonUpdate
        // see also, SiteEntityResource.update(String, Site, Parameters)
        if (p.getStatusUpdatedAt() != null)
        {
            throw new InvalidArgumentException("Unsupported field: statusUpdatedAt");
        }
        if (p.getAvatarId() != null)
        {
            throw new InvalidArgumentException("Unsupported field: avatarId");
        }
        if (p.getQuota() != null)
        {
            throw new InvalidArgumentException("Unsupported field: quota");
        }
        if (p.getQuotaUsed() != null)
        {
            throw new InvalidArgumentException("Unsupported field: quotaUsed");
        }

        List<Person> result = new ArrayList<>(1);
        Person person = new Person();
        person.setUserName(p.getUserName());
        person.setFirstName(p.getFirstName());
        person.setLastName(p.getLastName());
        person.setDescription(p.getDescription());
        person.setEmail(p.getEmail());
        person.setSkypeId(p.getSkypeId());
        person.setGoogleId(p.getGoogleId());
        person.setInstantMessageId(p.getInstantMessageId());
        person.setJobTitle(p.getJobTitle());
        person.setLocation(p.getLocation());
        person.setCompany(p.getCompany());
        person.setMobile(p.getMobile());
        person.setTelephone(p.getTelephone());
        person.setUserStatus(p.getUserStatus());
        person.setEnabled(p.isEnabled());
        person.setEmailNotificationsEnabled(p.isEmailNotificationsEnabled());
        person.setPassword(p.getPassword());

        result.add(people.create(person));
        return result;
    }

    @Override
    @WebApiDescription(title="Update person", description="Update the given person's details")
    public Person update(String personId, Person person, Parameters parameters)
    {
        validateNonUpdatableFieldsExistence(person);

        return people.update(personId, person);
    }

    /**
     * Explicitly test for the presence of fields that are present on Person but
     * shouldn't be updatable (until REPO-110 is solved).
     * 
     * @param person
     */
    private void validateNonUpdatableFieldsExistence(Person person)
    {

        if (person.getUserName() != null)
        {
            throw new InvalidArgumentException("Unsupported field: userName");
        }

        if (person.getStatusUpdatedAt() != null)
        {
            throw new InvalidArgumentException("Unsupported field: statusUpdatedAt");
        }

        if (person.getAvatarId() != null)
        {
            throw new InvalidArgumentException("Unsupported field: avatarId");
        }

        if (person.getQuota() != null)
        {
            throw new InvalidArgumentException("Unsupported field: quota");
        }

        if (person.getQuotaUsed() != null)
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