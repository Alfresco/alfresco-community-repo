/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import org.alfresco.rest.api.Preferences;
import org.alfresco.rest.api.model.Preference;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.util.ParameterCheck;

@RelationshipResource(name = "preferences", entityResource = PeopleEntityResource.class, title = "Person Preferences")
public class PersonPreferencesRelation implements RelationshipResourceAction.Read<Preference>, RelationshipResourceAction.ReadById<Preference>, RelationshipResourceAction.Update<Preference>, InitializingBean
{
    private static final Log logger = LogFactory.getLog(PersonPreferencesRelation.class);

    private Preferences preferences;

    public void setPreferences(Preferences preferences)
    {
        this.preferences = preferences;
    }

    @Override
    public void afterPropertiesSet()
    {
        ParameterCheck.mandatory("preferences", this.preferences);
    }

    /**
     * Returns a paged list of preferences for the user personId.
     * 
     * If personId does not exist, NotFoundException (status 404).
     * 
     * @see org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction.Read#readAll(java.lang.String, org.alfresco.rest.framework.resource.parameters.Parameters)
     */
    @Override
    @WebApiDescription(title = "A paged list of the persons preferences.")
    public CollectionWithPagingInfo<Preference> readAll(String personId, Parameters parameters)
    {
        return preferences.getPreferences(personId, parameters.getPaging());
    }

    /**
     * Returns information regarding the preference 'preferenceName' for user personId.
     * 
     * If personId does not exist, NotFoundException (status 404).
     * 
     * @see org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction.ReadById#readById(String, String, org.alfresco.rest.framework.resource.parameters.Parameters)
     */
    @Override
    @WebApiDescription(title = "Preference value for preference 'preferenceName' for person 'personId'.")
    public Preference readById(String personId, String preferenceName, Parameters parameters)
    {
        // fix for REPO-855
        String url = parameters.getRequest().getURL();
        if (url.matches(".*workspace://SpacesStore/.*"))
        {
            preferenceName += url.substring(url.indexOf("//SpacesStore/"));
        }
        //

        return preferences.getPreference(personId, preferenceName);
    }

    @Override
    public Preference update(String personId, Preference preference, Parameters parameters)
    {
        if (preference.getName() == null || preference.getName().isBlank())
        {
            throw new InvalidArgumentException();
        }
        return preferences.updatePreference(personId, preference);
    }
}
