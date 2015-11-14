package org.alfresco.repo.preference.traitextender;

import java.io.Serializable;
import java.util.Map;

public interface PreferenceServiceExtension
{
    public void setPreferences(final String userName, final Map<String, Serializable> preferences) throws Throwable;
}
