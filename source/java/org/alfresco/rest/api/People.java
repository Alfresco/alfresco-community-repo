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
package org.alfresco.rest.api;

import org.alfresco.rest.api.model.Person;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.NoSuchPersonException;

public interface People
{
	public static String DEFAULT_USER = "-me-";

	String validatePerson(String personId);
	String validatePerson(String personId, boolean validateIsCurrentUser);
    NodeRef getAvatar(String personId);

    /**
     * 
     * @throws NoSuchPersonException if personId does not exist
     */
    Person getPerson(final String personId);
    Person updatePerson(String personId, Person person);
}
