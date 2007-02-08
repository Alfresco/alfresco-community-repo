/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.search;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

public class QueryRegisterComponentImpl implements QueryRegisterComponent
{
    private DictionaryService dictionaryService;

    private NamespacePrefixResolver namespaceService;
    
    private List<String> initCollections = null;

    private Map<String, QueryCollection> collections = new HashMap<String, QueryCollection>();

    public QueryRegisterComponentImpl()
    {
        super();
    }

    private synchronized void loadCollectionsOnDemand()
    {
        if(initCollections != null)
        {
            for(String location: initCollections)
            {
                loadQueryCollection(location);
            }
        }
        initCollections = null;
    }
    
    public CannedQueryDef getQueryDefinition(QName qName)
    {
        loadCollectionsOnDemand();
        for(String key: collections.keySet())
        {
            QueryCollection collection = collections.get(key);
            CannedQueryDef  def = collection.getQueryDefinition(qName);
            if(def != null)
            {
                return def;
            }
        }
        return null;
    }

    public String getCollectionNameforQueryDefinition(QName qName)
    {
        loadCollectionsOnDemand();
        for(String key: collections.keySet())
        {
            QueryCollection collection = collections.get(key);
            if(collection.containsQueryDefinition(qName))
            {
                return key;
            }
        }
        return null;
    }

    public QueryParameterDefinition getParameterDefinition(QName qName)
    {
        loadCollectionsOnDemand();
        for(String key: collections.keySet())
        {
            QueryCollection collection = collections.get(key);
            QueryParameterDefinition  def = collection.getParameterDefinition(qName);
            if(def != null)
            {
                return def;
            }
        }
        return null;
    }

    public String getCollectionNameforParameterDefinition(QName qName)
    {
        loadCollectionsOnDemand();
        for(String key: collections.keySet())
        {
            QueryCollection collection = collections.get(key);
            if(collection.containsParameterDefinition(qName))
            {
                return key;
            }
        }
        return null;
    }

    public QueryCollection getQueryCollection(String location)
    {
        loadCollectionsOnDemand();
        return collections.get(location);
    }

    public void loadQueryCollection(String location)
    {
        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(location);
            SAXReader reader = new SAXReader();
            Document document = reader.read(is);
            is.close();
            QueryCollection collection = QueryCollectionImpl.createQueryCollection(document.getRootElement(), dictionaryService, namespaceService);
            collections.put(location, collection);
        }
        catch (DocumentException de)
        {
            throw new AlfrescoRuntimeException("Error reading XML", de);
        }
        catch (IOException e)
        {
            throw new AlfrescoRuntimeException("IO Error reading XML", e);
        }
    }
    
    public void setCollections(List<String> collections)
    {
        this.initCollections = collections;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public void setNamespaceService(NamespacePrefixResolver namespaceService)
    {
        this.namespaceService = namespaceService;
    }
}
