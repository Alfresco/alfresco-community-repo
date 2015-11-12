package org.alfresco.repo.preference.traitextender;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.traitextender.Trait;

public interface PreferenceServiceTrait extends Trait
{
    public void setPreferences(final String userName, final Map<String, Serializable> preferences) throws Throwable;

}
