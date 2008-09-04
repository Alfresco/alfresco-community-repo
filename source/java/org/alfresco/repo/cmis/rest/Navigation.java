/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.cmis.rest;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.QueryParameterDefImpl;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.InitializingBean;


/**
 * CMIS Navigation Service
 * 
 * @author davidc
 */
public class Navigation implements InitializingBean
{
    /**
     * Types Filter
     *  
     * @author davidc
     */
    public enum TypesFilter
    {
        Folders,
        FoldersAndDocuments,
        Documents
    };
        
    /** Query Parameters */
    private static final QName PARAM_PARENT = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "parent");

    /** Shallow search for all files and folders */
    private static final String LUCENE_QUERY_SHALLOW_FOLDERS =
        "+PARENT:\"${cm:parent}\" " +
        "-TYPE:\"" + ContentModel.TYPE_SYSTEM_FOLDER + "\" " +
        "+TYPE:\"" + ContentModel.TYPE_FOLDER + "\" ";
    
    /** Shallow search for all files and folders */
    private static final String LUCENE_QUERY_SHALLOW_FILES =
        "+PARENT:\"${cm:parent}\" " +
        "-TYPE:\"" + ContentModel.TYPE_SYSTEM_FOLDER + "\" " +
        "+TYPE:\"" + ContentModel.TYPE_CONTENT + "\" ";

    // dependencies
    private DictionaryService dictionaryService;
    private SearchService searchService;
    private DataTypeDefinition nodeRefDataType;

    
    /**
     * @param dictionaryService
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    /**
     * @param searchService
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }
    
    /**
     * Initialisation
     */
    public void afterPropertiesSet() throws Exception
    {
        nodeRefDataType = dictionaryService.getDataType(DataTypeDefinition.NODE_REF);
    }

    
    /**
     * Query for node children
     * 
     * @param parent  node to query children for
     * @param typesFilter  types filter
     * @return  children of node
     */
    public NodeRef[] getChildren(NodeRef parent, TypesFilter typesFilter)
    {
        if (typesFilter == TypesFilter.FoldersAndDocuments)
        {
            NodeRef[] folders = queryChildren(parent, TypesFilter.Folders);
            NodeRef[] docs = queryChildren(parent, TypesFilter.Documents);
            NodeRef[] foldersAndDocs = new NodeRef[folders.length + docs.length];
            System.arraycopy(folders, 0, foldersAndDocs, 0, folders.length);
            System.arraycopy(docs, 0, foldersAndDocs, folders.length, docs.length);
            return foldersAndDocs;
        }
        else if (typesFilter == TypesFilter.Folders)
        {
            NodeRef[] folders = queryChildren(parent, TypesFilter.Folders);
            return folders;
        }
        else if (typesFilter == TypesFilter.Documents)
        {
            NodeRef[] docs = queryChildren(parent, TypesFilter.Documents);
            return docs;
        }
        
        return new NodeRef[0];
    }

    /**
     * Query children helper
     * 
     * NOTE: Queries for folders only or documents only
     * 
     * @param parent  node to query children for
     * @param typesFilter  folders or documents
     * @return  node children
     */
    private NodeRef[] queryChildren(NodeRef parent, TypesFilter typesFilter)
    {
        SearchParameters params = new SearchParameters();
        params.setLanguage(SearchService.LANGUAGE_LUCENE);
        params.addStore(parent.getStoreRef());
        QueryParameterDefinition parentDef = new QueryParameterDefImpl(PARAM_PARENT, nodeRefDataType, true, parent.toString());
        params.addQueryParameterDefinition(parentDef);
        
        if (typesFilter == TypesFilter.Folders)
        {
            params.setQuery(LUCENE_QUERY_SHALLOW_FOLDERS);
        }
        else if (typesFilter == TypesFilter.Documents)
        {
            params.setQuery(LUCENE_QUERY_SHALLOW_FILES);
        }
        
        ResultSet resultSet = searchService.query(params);
        try
        {
            List<NodeRef> results = resultSet.getNodeRefs();
            NodeRef[] nodeRefs = new NodeRef[results.size()];
            return results.toArray(nodeRefs);
        }
        finally
        {
            resultSet.close();
        }
    }
    
}
