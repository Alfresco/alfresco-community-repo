package org.alfresco.rest.api;

import org.alfresco.rest.api.model.Preference;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;

public interface Preferences
{
	public Preference getPreference(String personId, String preferenceName);
	public CollectionWithPagingInfo<Preference> getPreferences(String personId, Paging paging);
}
