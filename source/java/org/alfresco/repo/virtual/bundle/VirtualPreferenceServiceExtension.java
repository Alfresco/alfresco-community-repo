/* 
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see http://www.gnu.org/licenses/.
 */

package org.alfresco.repo.virtual.bundle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.repo.preference.traitextender.PreferenceServiceExtension;
import org.alfresco.repo.preference.traitextender.PreferenceServiceTrait;
import org.alfresco.repo.virtual.ref.GetActualNodeRefMethod;
import org.alfresco.repo.virtual.ref.Reference;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.traitextender.SpringBeanExtension;

/**
 * PreferenceServiceImpl extension used for manipulate favorites preferences
 * that are set for virtual references.
 * 
 * @author sdinuta
 */
public class VirtualPreferenceServiceExtension extends
            SpringBeanExtension<PreferenceServiceExtension, PreferenceServiceTrait> implements
            PreferenceServiceExtension
{
    private static final String EMPTY_STRING = "";

    private static final String DOCUMENTS_FAVOURITES_KEY = "org.alfresco.share.documents.favourites";

    private static final String FOLDERS_FAVOURITES_KEY = "org.alfresco.share.folders.favourites";

    private static final String CREATED_AT = ".createdAt";

    private static final String EXT_DOCUMENTS_FAVOURITES = "org.alfresco.ext.documents.favourites.";

    private static final String EXT_FOLDERS_FAVOURITES = "org.alfresco.ext.folders.favourites.";

    private PreferenceService preferenceService;

    public VirtualPreferenceServiceExtension()
    {
        super(PreferenceServiceTrait.class);
    }

    public void setPreferenceService(PreferenceService preferenceService)
    {
        this.preferenceService = preferenceService;
    }

    /**
     * Obtains the org.alfresco.ext.documents.favourites.* or
     * org.alfresco.ext.folders.favourites.* key used for setting favorites for
     * documents and folders, or null if not favorites are targeted.
     * 
     * @param preferences
     * @return the org.alfresco.ext.documents.favourites.* or
     *         org.alfresco.ext.folders.favourites.* key used for setting
     *         favorites for documents and folders, or null if not favorites are
     *         targeted.
     */
    private String getExtPreferenceKey(Map<String, Serializable> preferences)
    {
        String extKey = null;
        if (!preferences.containsKey(DOCUMENTS_FAVOURITES_KEY) && !preferences.containsKey(FOLDERS_FAVOURITES_KEY))
        {
            return null;
        }
        Set<Entry<String, Serializable>> entrySet = preferences.entrySet();
        if (entrySet == null)
        {
            return null;
        }
        Iterator<Entry<String, Serializable>> iterator = entrySet.iterator();
        if (!iterator.hasNext())
        {
            return null;
        }
        while (iterator.hasNext())
        {
            Entry<String, Serializable> entry = iterator.next();
            String key = entry.getKey();
            if (key.startsWith(EXT_DOCUMENTS_FAVOURITES) || key.startsWith(EXT_FOLDERS_FAVOURITES))
            {
                extKey = key;
                break;
            }
        }
        return extKey;
    }

    /**
     * If the favorites preferences are changed then for virtual references the
     * actual nodeRef is added/removed from favorites preferences instead of
     * virtual nodeRef. For non virtual entries or for preferences that are not
     * related to favorites the original implementation from
     * PreferenceServiceImpl is used.
     */
    @Override
    public void setPreferences(String userName, Map<String, Serializable> preferences) throws Throwable
    {
        final String comma = ",";

        String extKey = getExtPreferenceKey(preferences);
        if (extKey != null)
        {
            String extFavKey;
            String favKey;
            if (extKey.startsWith(EXT_DOCUMENTS_FAVOURITES))
            // favorites for documents
            {
                extFavKey = EXT_DOCUMENTS_FAVOURITES;
                favKey = DOCUMENTS_FAVOURITES_KEY;
            }
            else
            // favorites for folders
            {
                extFavKey = EXT_FOLDERS_FAVOURITES;
                favKey = FOLDERS_FAVOURITES_KEY;
            }

            String pattern = "^" + extFavKey + "(\\S+)" + CREATED_AT + "$";
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(extKey);
            if (m.find())
            {
                String documentNodeRefStr = m.group(1);
                String favorites = (String) preferences.get(favKey);
                if (documentNodeRefStr != null && !documentNodeRefStr.isEmpty())
                {
                    NodeRef documentNodeRef = new NodeRef(documentNodeRefStr);

                    if (Reference.isReference(documentNodeRef))
                    {
                        Reference reference = Reference.fromNodeRef(documentNodeRef);
                        NodeRef actualNodeRef = reference.execute(new GetActualNodeRefMethod(null));
                        String actualNodeRefStr = actualNodeRef.toString();
                        String actualExtPreference = extFavKey + actualNodeRefStr + CREATED_AT;
                        List<String> elements = new ArrayList<String>(Arrays.asList(favorites.split(comma)));
                        boolean elementsChanged = false;

                        if (favorites.contains(documentNodeRefStr))
                        // add favorite
                        {
                            if (!preferences.containsKey(actualExtPreference))
                            {
                                Serializable value = preferences.get(extKey);
                                preferences.put(actualExtPreference,
                                                value);
                            }
                            preferences.remove(extKey);

                            if (!favorites.contains(actualNodeRefStr))
                            {
                                favorites = favorites.replace(documentNodeRefStr,
                                                              actualNodeRefStr);
                            }
                            else
                            {
                                if (elements.contains(documentNodeRefStr))
                                {
                                    elements.remove(documentNodeRefStr);
                                    elementsChanged = true;
                                }
                            }
                        }
                        else
                        // remove favorite
                        {
                            if (elements.contains(actualNodeRefStr))
                            {
                                elements.remove(actualNodeRefStr);
                                preferenceService.clearPreferences(userName,
                                                                   actualExtPreference);
                                elementsChanged = true;
                            }
                        }

                        if (elementsChanged)
                        {
                            favorites = EMPTY_STRING;
                            for (String element : elements)
                            {
                                if (favorites.isEmpty())
                                {
                                    favorites = element;
                                }
                                else
                                {
                                    favorites = favorites + comma + element;
                                }
                            }
                        }
                        preferences.put(favKey,
                                        favorites);
                    }
                }
            }
        }
        getTrait().setPreferences(userName,
                                  preferences);
    }
}
