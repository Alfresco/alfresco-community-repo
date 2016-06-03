package org.alfresco.rest.api.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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

	public Preference getPreference(String personId, String preferenceName)
	{
		personId = people.validatePerson(personId);
		Serializable preferenceValue = preferenceService.getPreference(personId, preferenceName);
		if(preferenceValue != null)
		{
			return new Preference(preferenceName, preferenceValue);
		}
		else
		{
			throw new RelationshipResourceNotFoundException(personId, preferenceName);
		}
	}
	
	public CollectionWithPagingInfo<Preference> getPreferences(String personId, Paging paging)
	{
		personId = people.validatePerson(personId);

		PagingResults<Pair<String, Serializable>> preferences = preferenceService.getPagedPreferences(personId, null, Util.getPagingRequest(paging));
		List<Preference> ret = new ArrayList<Preference>(preferences.getPage().size());
		for(Pair<String, Serializable> prefEntity : preferences.getPage())
		{
			Preference pref = new Preference(prefEntity.getFirst(), prefEntity.getSecond());
			ret.add(pref);
		}

        return CollectionWithPagingInfo.asPaged(paging, ret, preferences.hasMoreItems(), preferences.getTotalResultCount().getFirst());
	}
}
