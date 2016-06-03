package org.alfresco.rest.api;

import org.alfresco.rest.api.model.Person;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.NoSuchPersonException;

public interface People
{
	String DEFAULT_USER = "-me-";

	String validatePerson(String personId);
	String validatePerson(String personId, boolean validateIsCurrentUser);
    NodeRef getAvatar(String personId);

    /**
     * 
     * @throws NoSuchPersonException if personId does not exist
     */
    Person getPerson(final String personId);
    //Person updatePerson(String personId, Person person);
}
