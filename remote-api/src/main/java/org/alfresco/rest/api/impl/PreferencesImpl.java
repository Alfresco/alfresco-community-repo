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
package org.alfresco.rest.api.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.query.PagingResults;
import org.alfresco.rest.api.People;
import org.alfresco.rest.api.Preferences;
import org.alfresco.rest.api.model.Preference;
import org.alfresco.rest.framework.core.exceptions.RelationshipResourceNotFoundException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.util.Pair;

/**
 * Centralises access to preference services and maps between representations.
 * 
 * @author steveglover
 * @since publicapi1.0
 */
public class PreferencesImpl implements Preferences
{
    private People people;
    private PreferenceService preferenceService;

    public void setPeople(People people)
    {
        this.people = people;
    }

    public void setPreferenceService(PreferenceService preferenceService)
    {
        this.preferenceService = preferenceService;
    }

    @Override
    public Preference getPreference(String personId, String preferenceName)
    {
        personId = people.validatePerson(personId);
        Serializable preferenceValue = preferenceService.getPreference(personId, preferenceName);
        if (preferenceValue != null)
        {
            return new Preference(preferenceName, preferenceValue);
        }
        else
        {
            throw new RelationshipResourceNotFoundException(personId, preferenceName);
        }
    }

    @Override
    public CollectionWithPagingInfo<Preference> getPreferences(String personId, Paging paging)
    {
        personId = people.validatePerson(personId);

        PagingResults<Pair<String, Serializable>> preferences = preferenceService.getPagedPreferences(personId, null, Util.getPagingRequest(paging));
        List<Preference> ret = new ArrayList<>(preferences.getPage().size());
        for (Pair<String, Serializable> prefEntity : preferences.getPage())
        {
            Preference pref = new Preference(prefEntity.getFirst(), prefEntity.getSecond());
            ret.add(pref);
        }

        return CollectionWithPagingInfo.asPaged(paging, ret, preferences.hasMoreItems(), preferences.getTotalResultCount().getFirst());
    }

    @Override
    public Preference updatePreference(String personId, Preference preference)
    {
        personId = people.validatePerson(personId, true);
        final Map<String, Serializable> preferencesToSet;
        if (preference.getValue() == null || "".equals(preference.getValue()))
        {
            preferencesToSet = new HashMap<>(1);
            preferencesToSet.put(preference.getName(), null);
        }
        else
        {
            preferencesToSet = Map.of(preference.getName(), preference.getValue());
        }

        preferenceService.setPreferences(personId, preferencesToSet);
        return new Preference(preference.getName(), preferenceService.getPreference(personId, preference.getName()));
    }
}