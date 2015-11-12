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

import org.alfresco.model.ContentModel;
import org.alfresco.repo.preference.traitextender.PreferenceServiceExtension;
import org.alfresco.repo.preference.traitextender.PreferenceServiceTrait;
import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.repo.virtual.ref.GetActualNodeRefMethod;
import org.alfresco.repo.virtual.ref.GetParentReferenceMethod;
import org.alfresco.repo.virtual.ref.Reference;
import org.alfresco.repo.virtual.store.VirtualStore;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.traitextender.SpringBeanExtension;

public class VirtualPreferenceServiceExtension extends
            SpringBeanExtension<PreferenceServiceExtension, PreferenceServiceTrait>implements PreferenceServiceExtension
{

    private static final String DOCUMENTS_FAVOURITES_KEY = "org.alfresco.share.documents.favourites";

    private static final String CREATED_AT = ".createdAt";

    private static final String EXT_DOCUMENTS_FAVOURITES = "org.alfresco.ext.documents.favourites.";

    private VirtualStore virtualStore;

    private ActualEnvironment environment;

    private PreferenceService preferenceService;

    public VirtualPreferenceServiceExtension()
    {
        super(PreferenceServiceTrait.class);
    }

    public ActualEnvironment getEnvironment()
    {
        return environment;
    }

    public void setEnvironment(ActualEnvironment environment)
    {
        this.environment = environment;
    }

    public PreferenceService getPreferenceService()
    {
        return preferenceService;
    }

    public void setPreferenceService(PreferenceService preferenceService)
    {
        this.preferenceService = preferenceService;
    }

    public void setVirtualStore(VirtualStore virtualStore)
    {
        this.virtualStore = virtualStore;
    }

    private String getExtDocumentPreferenceKey(Map<String, Serializable> preferences)
    {
        String extKey = null;
        if (!preferences.containsKey(DOCUMENTS_FAVOURITES_KEY))
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
            if (key.startsWith(EXT_DOCUMENTS_FAVOURITES))
            {
                extKey = key;
                break;
            }
        }

        return extKey;
    }

    @Override
    public void setPreferences(String userName, Map<String, Serializable> preferences) throws Throwable
    {
        final String comma = ",";

        String extKey = getExtDocumentPreferenceKey(preferences);
        if (extKey != null)
        {
            String pattern = "^" + EXT_DOCUMENTS_FAVOURITES + "(\\S+)" + CREATED_AT + "$";
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(extKey);
            if (m.find())
            {
                String documentNodeRefStr = m.group(1);
                String favorites = (String) preferences.get(DOCUMENTS_FAVOURITES_KEY);
                if (documentNodeRefStr != null && !documentNodeRefStr.isEmpty())
                {
                    NodeRef documentNodeRef = new NodeRef(documentNodeRefStr);

                    if (Reference.isReference(documentNodeRef))
                    {
                        Reference reference = Reference.fromNodeRef(documentNodeRef);
                        NodeRef actualNodeRef = reference.execute(new GetActualNodeRefMethod(null));
                        String actualNodeRefStr = actualNodeRef.toString();
                        Reference parentVF = reference.execute(new GetParentReferenceMethod());
                        NodeRef actualFolder = parentVF.execute(new GetActualNodeRefMethod(environment));
                        Reference virtualizedRoot = virtualStore.virtualize(actualFolder);
                        String documentName = (String) environment.getProperty(documentNodeRef,
                                                                               ContentModel.PROP_NAME);
                        List<Reference> results = virtualStore.search(virtualizedRoot,
                                                                      documentName,
                                                                      true,
                                                                      false,
                                                                      true);
                        if (favorites.contains(documentNodeRefStr))
                        {
                            for (Reference ref : results)
                            {
                                NodeRef nodeRef = ref.toNodeRef();
                                String nodeRefStr = nodeRef.toString();
                                if (!favorites.contains(nodeRefStr))
                                {
                                    if (favorites.isEmpty())
                                    {
                                        favorites = nodeRefStr;
                                    }
                                    else
                                    {
                                        favorites = favorites + comma + nodeRefStr;
                                    }

                                }
                            }
                            if (!favorites.contains(actualNodeRefStr))
                            {
                                favorites = favorites + comma + actualNodeRefStr;
                            }
                            preferences.put(DOCUMENTS_FAVOURITES_KEY,
                                            favorites);
                        }
                        else
                        {
                            List<String> elements = new ArrayList<String>(Arrays.asList(favorites.split(comma)));
                            for (Reference ref : results)
                            {
                                NodeRef nodeRef = ref.toNodeRef();
                                String nodeRefStr = nodeRef.toString();
                                if (elements.contains(nodeRefStr))
                                {
                                    elements.remove(nodeRefStr);
                                    String preferenceToClear = EXT_DOCUMENTS_FAVOURITES + nodeRefStr + CREATED_AT;
                                    preferenceService.clearPreferences(userName,
                                                                       preferenceToClear);
                                }
                            }
                            if (elements.contains(actualNodeRefStr))
                            {
                                elements.remove(actualNodeRefStr);
                                String preferenceToClear = EXT_DOCUMENTS_FAVOURITES + actualNodeRefStr + CREATED_AT;
                                preferenceService.clearPreferences(userName,
                                                                   preferenceToClear);
                            }
                            favorites = "";
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
                            preferences.put(DOCUMENTS_FAVOURITES_KEY,
                                            favorites);
                        }
                    }
                    else
                    {
                        ChildAssociationRef parentAssociation = environment.getPrimaryParent(documentNodeRef);
                        NodeRef parentNodeRef = parentAssociation.getParentRef();
                        if (virtualStore.canVirtualize(parentNodeRef))
                        {
                            Reference virtualizedRoot = virtualStore.virtualize(parentNodeRef);
                            String documentName = (String) environment.getProperty(documentNodeRef,
                                                                                   ContentModel.PROP_NAME);
                            List<Reference> results = virtualStore.search(virtualizedRoot,
                                                                          documentName,
                                                                          true,
                                                                          false,
                                                                          true);
                            if (preferences.get(extKey) == null)
                            {
                                List<String> elements = new ArrayList<String>(Arrays.asList(favorites.split(comma)));
                                for (Reference ref : results)
                                {
                                    NodeRef nodeRef = ref.toNodeRef();
                                    String nodeRefStr = nodeRef.toString();
                                    if (elements.contains(nodeRefStr))
                                    {
                                        elements.remove(nodeRefStr);
                                        String preferenceToClear = EXT_DOCUMENTS_FAVOURITES + nodeRefStr + CREATED_AT;
                                        preferenceService.clearPreferences(userName,
                                                                           preferenceToClear);

                                    }
                                }
                                favorites = "";
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
                                preferences.put(DOCUMENTS_FAVOURITES_KEY,
                                                favorites);
                            }
                            else
                            {
                                for (Reference ref : results)
                                {
                                    NodeRef nodeRef = ref.toNodeRef();
                                    String nodeRefStr = nodeRef.toString();
                                    if (!favorites.contains(nodeRefStr))
                                    {
                                        if (favorites.isEmpty())
                                        {
                                            favorites = nodeRefStr;
                                        }
                                        else
                                        {
                                            favorites = favorites + comma + nodeRefStr;
                                        }
                                    }
                                }
                                preferences.put(DOCUMENTS_FAVOURITES_KEY,
                                                favorites);
                            }
                        }
                    }
                }
            }
        }
        getTrait().setPreferences(userName,
                                  preferences);

    }
}
