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
package org.alfresco.rest.api;

import org.alfresco.rest.api.model.Person;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.NoSuchPersonException;

public interface People
{
	String DEFAULT_USER = "-me-";
    String PARAM_INCLUDE_ASPECTNAMES = "aspectNames";
    String PARAM_INCLUDE_PROPERTIES = "properties";
    String PARAM_FIRST_NAME = "firstName";
    String PARAM_LAST_NAME = "lastName";
    String PARAM_ID = "id";

	String validatePerson(String personId);
	String validatePerson(String personId, boolean validateIsCurrentUser);
    NodeRef getAvatar(String personId);

    /**
     * 
     * @throws NoSuchPersonException if personId does not exist
     */
    Person getPerson(final String personId);

    /**
     * Create a person.
     *
     * @param person
     * @return
     */
    Person create(Person person);

    /**
     * Update the given person's details.
     * 
     * @param personId The identifier of a person.
     * @param person The person details.
     * @return The updated person details.
     */
    Person update(String personId, Person person);

    /**
     * Get people list
     * 
     * @return CollectionWithPagingInfo<Person>
     */
    CollectionWithPagingInfo<Person> getPeople(Parameters parameters);
}
