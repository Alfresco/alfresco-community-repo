/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;

import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.ASPECT_SAVED_SEARCH;

/**
 * Records management search service implementation
 *
 * @author Roy Wetherall
 */
public class RecordsManagementSearchServiceImpl implements RecordsManagementSearchService
{
    private static final String SITES_SPACE_QNAME_PATH = "/app:company_home/st:sites/";

    /** Name of the main site container used to store the saved searches within */
    private static final String SEARCH_CONTAINER = "Saved Searches";

    /** File folder service */
    private FileFolderService fileFolderService;

    /** Search service */
    private SearchService searchService;

	/** Site service */
	private SiteService siteService;

	/** Namespace service */
	private NamespaceService namespaceService;

    /**
     * Node service
     */
    private NodeService nodeService;

    /** List of report details */
	private List<ReportDetails> reports = new ArrayList<>(13);

    /**
     * Records Search Parameters
     */
    private RecordsManagementSearchParameters recordsManagementSearchParameters;

	/**
	 * @param fileFolderService    file folder service
	 */
	public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }

	/**
	 * @param searchService    search service
	 */
	public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

	/**
	 * @param siteService  site service
	 */
	public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }

	/**
	 * @param namespaceService namespace service
	 */
	public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    /**
     * @param nodeService Node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
	 * @param reportsJSON
	 */
	public void setReportsJSON(String reportsJSON)
    {
	    try
	    {
    	   JSONArray jsonArray = new JSONArray(reportsJSON);
           for (int i=0; i < jsonArray.length(); i++)
           {
               JSONObject report = jsonArray.getJSONObject(i);

               // Get the name
               if (!report.has(SavedSearchDetails.NAME))
               {
                   throw new AlfrescoRuntimeException("Unable to load report details because name has not been specified. \n" + reportsJSON);
               }
               String name = report.getString(SavedSearchDetails.NAME);
               String translatedName = I18NUtil.getMessage(name);
               if (translatedName != null)
               {
                   name = translatedName;
               }

               // Get the query
               if (!report.has(SavedSearchDetails.SEARCH))
               {
                   throw new AlfrescoRuntimeException("Unable to load report details because search has not been specified for report " + name + ". \n" + reportsJSON);
               }
               String query = report.getString(SavedSearchDetails.SEARCH);

               // Get the description
               String description = "";
               if (report.has(SavedSearchDetails.DESCRIPTION))
               {
                   description = report.getString(SavedSearchDetails.DESCRIPTION);
                   String translatedDescription = I18NUtil.getMessage(description);
                   if (translatedDescription != null)
                   {
                       description = translatedDescription;
                   }
               }

               if (report.has("searchparams"))
               {
                   recordsManagementSearchParameters = RecordsManagementSearchParameters.createFromJSON(report.getJSONObject("searchparams"), namespaceService);
               }

               // Create the report details and add to list
               ReportDetails reportDetails = new ReportDetails(name, description, query, recordsManagementSearchParameters);
               reports.add(reportDetails);
           }
	    }
	    catch (JSONException exception)
	    {
	        throw new AlfrescoRuntimeException("Unable to load report details.\n" + reportsJSON, exception);
	    }
    }

    /**
     * Set RecordsManagementSearchParameters service
     *
     * @param recordsManagementSearchParameters
     */
    public void setRecordsManagementSearchParameters(RecordsManagementSearchParameters recordsManagementSearchParameters)
    {
        this.recordsManagementSearchParameters = recordsManagementSearchParameters;
    }

	/**
	 * @see org.alfresco.module.org_alfresco_module_rm.search.RecordsManagementSearchService#search(java.lang.String, java.lang.String, org.alfresco.module.org_alfresco_module_rm.search.RecordsManagementSearchParameters)
	 */
    @Override
    public List<Pair<NodeRef, NodeRef>> search(String siteId, String query, RecordsManagementSearchParameters rmSearchParameters)
    {
        // build the full RM query
        StringBuilder fullQuery = new StringBuilder(1024);
        fullQuery.append("PATH:\"")
                 .append(SITES_SPACE_QNAME_PATH)
                 .append("cm:").append(ISO9075.encode(siteId)).append("/cm:documentLibrary//*\"")
                 .append(" AND (")
                 .append(buildQueryString(query, rmSearchParameters))
                 .append(")");

        // create the search parameters
        SearchParameters searchParameters = new SearchParameters();
        searchParameters.setQuery(fullQuery.toString());
        searchParameters.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        searchParameters.setMaxItems(recordsManagementSearchParameters.getMaxItems());
        searchParameters.setNamespace(RecordsManagementModel.RM_URI);

        // set sort
        for(SortItem entry : rmSearchParameters.getSortOrder())
        {
            searchParameters.addSort(entry.property.toPrefixString(namespaceService), entry.assc);
        }

        // set templates
        for (Entry<String, String> entry : rmSearchParameters.getTemplates().entrySet())
        {
            searchParameters.addQueryTemplate(entry.getKey(), entry.getValue());
        }

        // execute query
        ResultSet resultSet = searchService.query(searchParameters);
        
        // process results
        List<Pair<NodeRef, NodeRef>> result = new ArrayList<>(resultSet.length());
        for (ChildAssociationRef childAssoc : resultSet.getChildAssocRefs())
        {
        	result.add(new Pair<>(childAssoc.getParentRef(), childAssoc.getChildRef()));
        }

        // return results
        return result;
    }

    /**
     *
     * @param queryTerm
     * @param aspects
     * @param types
     * @return
     */
    /*package*/ String buildQueryString(String queryTerm, RecordsManagementSearchParameters searchParameters)
    {
       StringBuilder aspectQuery = new StringBuilder();
       if (searchParameters.isIncludeRecords())
       {
           appendAspect(aspectQuery, "rma:record");
           if (!searchParameters.isIncludeUndeclaredRecords())
           {
               appendAspect(aspectQuery, "rma:declaredRecord");
           }
           if (searchParameters.isIncludeVitalRecords())
           {
               appendAspect(aspectQuery, "rma:vitalRecord");
           }
       }

       StringBuilder typeQuery = new StringBuilder();
       if (searchParameters.isIncludeRecordFolders())
       {
           appendType(typeQuery, "rma:recordFolder");
       }
       List<QName> includedContainerTypes = searchParameters.getIncludedContainerTypes();
       if (includedContainerTypes != null && includedContainerTypes.size() != 0)
       {
           for (QName includedContainerType : includedContainerTypes)
           {
               appendType(typeQuery, includedContainerType.toPrefixString(namespaceService));
           }
       }

       StringBuilder query = new StringBuilder();
       if (queryTerm == null || queryTerm.length() == 0)
       {
           // Default to search for everything
           query.append("ISNODE:T");
       }
       else
       {
           if (isComplexQueryTerm(queryTerm))
           {
               query.append(queryTerm);
           }
           else
           {
               query.append("keywords:\"" + queryTerm + "\"");
           }
       }

       StringBuilder fullQuery = new StringBuilder(1024);
       if (aspectQuery.length() != 0 || typeQuery.length() != 0)
       {
           if (aspectQuery.length() != 0 && typeQuery.length() != 0)
           {
               fullQuery.append("(");
           }

           if (aspectQuery.length() != 0)
           {
               fullQuery.append("(").append(aspectQuery).append(") ");
           }

           if (typeQuery.length() != 0)
           {
               fullQuery.append("(").append(typeQuery).append(")");
           }

           if (aspectQuery.length() != 0 && typeQuery.length() != 0)
           {
               fullQuery.append(")");
           }
       }

       if (searchParameters.isIncludeFrozen())
       {
           appendAspect(fullQuery, "rma:frozen");
       }
       else
       {
           appendNotAspect(fullQuery, "rma:frozen");
       }
       if (searchParameters.isIncludeCutoff())
       {
           appendAspect(fullQuery, "rma:cutOff");
       }

       if (fullQuery.length() != 0)
       {
           fullQuery.append(" AND ");
       }
       fullQuery.append(query).append(" AND NOT ASPECT:\"rma:versionedRecord\"");

       return fullQuery.toString();
    }

    private boolean isComplexQueryTerm(String query)
    {
        return query.matches(".*[\":].*");
    }

    /**
     *
     * @param sb
     * @param aspect
     */
    private void appendAspect(StringBuilder sb, String aspect)
    {
        appendWithJoin(sb, " AND ", "ASPECT:\"", aspect, "\"");
    }

    private void appendNotAspect(StringBuilder sb, String aspect)
    {
        appendWithJoin(sb, " AND ", "NOT ASPECT:\"", aspect, "\"");
    }

    /**
     *
     * @param sb
     * @param type
     */
    private void appendType(StringBuilder sb, String type)
    {
        appendWithJoin(sb, " ", "TYPE:\"", type, "\"");
    }

    /**
     *
     * @param sb
     * @param withJoin
     * @param prefix
     * @param value
     * @param postfix
     */
    private void appendWithJoin(StringBuilder sb, String withJoin, String prefix, String value, String postfix)
    {
        if (sb.length() != 0)
        {
            sb.append(withJoin);
        }
        sb.append(prefix).append(value).append(postfix);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.search.RecordsManagementSearchService#getSavedSearches(java.lang.String)
     */
    @Override
    public List<SavedSearchDetails> getSavedSearches(String siteId)
    {
        List<SavedSearchDetails> result = new ArrayList<>(17);

        NodeRef container = siteService.getContainer(siteId, SEARCH_CONTAINER);
        if (container != null)
        {
            // add the details of all the public saved searches
            List<FileInfo> searches = fileFolderService.listFiles(container);
            for (FileInfo search : searches)
            {
                addSearchDetailsToList(result, search.getNodeRef());
            }

            // add the details of any "private" searches for the current user
            String userName = AuthenticationUtil.getFullyAuthenticatedUser();
            NodeRef userContainer = fileFolderService.searchSimple(container, userName);
            if (userContainer != null)
            {
                List<FileInfo> userSearches = fileFolderService.listFiles(userContainer);
                for (FileInfo userSearch : userSearches)
                {
                    addSearchDetailsToList(result, userSearch.getNodeRef());
                }
            }
        }

        return result;
    }

    /**
     * Add the search details to the list.
     * @param searches      list of search details
     * @param searchNode    search node
     */
    private void addSearchDetailsToList(List<SavedSearchDetails> searches, NodeRef searchNode)
    {
        ContentReader reader = fileFolderService.getReader(searchNode);
        String jsonString = reader.getContentString();
        SavedSearchDetails savedSearchDetails = SavedSearchDetails.createFromJSON(jsonString, namespaceService, this, searchNode);
        searches.add(savedSearchDetails);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.search.RecordsManagementSearchService#getSavedSearch(java.lang.String, java.lang.String)
     */
    @Override
    public SavedSearchDetails getSavedSearch(String siteId, String name)
    {
        // check for mandatory parameters
        ParameterCheck.mandatory("siteId", siteId);
        ParameterCheck.mandatory("name", name);

        SavedSearchDetails result = null;

        // get the saved search node
        NodeRef searchNode = getSearchNodeRef(siteId, name);

        if (searchNode != null)
        {
            // get the json content
            ContentReader reader = fileFolderService.getReader(searchNode);
            String jsonString = reader.getContentString();

            // create the saved search details
            result = SavedSearchDetails.createFromJSON(jsonString, namespaceService, this, searchNode);
        }

        return result;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.search.RecordsManagementSearchService#saveSearch(String, String, String, String, RecordsManagementSearchParameters, boolean)
     */
	@Override
	public SavedSearchDetails saveSearch(String siteId, String name, String description, String query, RecordsManagementSearchParameters searchParameters, boolean isPublic)
	{
	    // Check for mandatory parameters
	    ParameterCheck.mandatory("siteId", siteId);
	    ParameterCheck.mandatory("name", name);
	    ParameterCheck.mandatory("query", query);
	    ParameterCheck.mandatory("searchParameters", searchParameters);

        // Create saved search details
        SavedSearchDetails savedSearchDetails = new SavedSearchDetails(siteId, name, description, query, searchParameters, isPublic, false, namespaceService, this);

        // Save search details
        return saveSearch(savedSearchDetails);
	}

	/**
	 * @see org.alfresco.module.org_alfresco_module_rm.search.RecordsManagementSearchService#saveSearch(org.alfresco.module.org_alfresco_module_rm.search.SavedSearchDetails)
	 */
	@Override
	public SavedSearchDetails saveSearch(final SavedSearchDetails savedSearchDetails)
    {
	    // Check for mandatory parameters
	    ParameterCheck.mandatory("savedSearchDetails", savedSearchDetails);

	    // Get the root saved search container
	    final String siteId = savedSearchDetails.getSiteId();
	    NodeRef container = siteService.getContainer(siteId, SEARCH_CONTAINER);
        if (container == null)
        {
            container = AuthenticationUtil.runAs(new RunAsWork<NodeRef>()
            {
                @Override
                public NodeRef doWork()
                {
                    return siteService.createContainer(siteId, SEARCH_CONTAINER, null, null);
                }
            }, AuthenticationUtil.getSystemUserName());
        }

        // Get the private container for the current user
        if (!savedSearchDetails.isPublic())
        {
            final String userName = AuthenticationUtil.getFullyAuthenticatedUser();
            NodeRef userContainer = fileFolderService.searchSimple(container, userName);
            if (userContainer == null)
            {
                final NodeRef parentContainer = container;
                userContainer = AuthenticationUtil.runAs(new RunAsWork<NodeRef>()
                {
                    @Override
                    public NodeRef doWork()
                    {
                        return fileFolderService.create(parentContainer, userName, ContentModel.TYPE_FOLDER).getNodeRef();
                    }
                }, AuthenticationUtil.getSystemUserName());
            }
            container = userContainer;
        }

        // Get the saved search node
        NodeRef searchNode = fileFolderService.searchSimple(container, savedSearchDetails.getName());
        if (searchNode == null)
        {
            final NodeRef searchContainer = container;
            searchNode = AuthenticationUtil.runAs(new RunAsWork<NodeRef>()
            {
                @Override
                public NodeRef doWork()
                {
                    return fileFolderService.create(searchContainer, savedSearchDetails.getName(), ContentModel.TYPE_CONTENT).getNodeRef();
                }
            }, AuthenticationUtil.getSystemUserName());
        }
        nodeService.addAspect(searchNode, ASPECT_SAVED_SEARCH, null);
        // Write the JSON content to search node
        final NodeRef writableSearchNode = searchNode;
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @Override
            public Void doWork()
            {
                ContentWriter writer = fileFolderService.getWriter(writableSearchNode);
                writer.setEncoding("UTF-8");
                writer.setMimetype(MimetypeMap.MIMETYPE_JSON);
                writer.putContent(savedSearchDetails.toJSONString());

                return null;
            }
        }, AuthenticationUtil.getSystemUserName());

        return savedSearchDetails;
    }

	/**
	 * @see org.alfresco.module.org_alfresco_module_rm.search.RecordsManagementSearchService#deleteSavedSearch(java.lang.String, java.lang.String)
	 */
	@Override
	public void deleteSavedSearch(String siteId, String name)
	{
	    // Check parameters
	    ParameterCheck.mandatory("siteId", siteId);
	    ParameterCheck.mandatory("name", name);

	    // Get the search node for the saved query
        NodeRef searchNode = getSearchNodeRef(siteId, name);
        if (searchNode != null && fileFolderService.exists(searchNode))
        {
            fileFolderService.delete(searchNode);
        }
	}

	/**
	 * @see org.alfresco.module.org_alfresco_module_rm.search.RecordsManagementSearchService#deleteSavedSearch(org.alfresco.module.org_alfresco_module_rm.search.SavedSearchDetails)
	 */
    @Override
    public void deleteSavedSearch(SavedSearchDetails savedSearchDetails)
    {
        // Check parameters
        ParameterCheck.mandatory("savedSearchDetails", savedSearchDetails);

        // Delete the saved search
        deleteSavedSearch(savedSearchDetails.getSiteId(), savedSearchDetails.getName());
    }

    /**
     * Get the saved search node reference.
     * @param siteId    site id
     * @param name      search name
     * @return {@link NodeRef}  search node reference
     */
    private NodeRef getSearchNodeRef(String siteId, String name)
    {
        NodeRef searchNode = null;

        // Get the root saved search container
        NodeRef container = siteService.getContainer(siteId, SEARCH_CONTAINER);
        if (container != null)
        {
            // try and find the search node
            searchNode = fileFolderService.searchSimple(container, name);

            // can't find it so check the users container
            if (searchNode == null)
            {
                String userName = AuthenticationUtil.getFullyAuthenticatedUser();
                NodeRef userContainer = fileFolderService.searchSimple(container, userName);
                if (userContainer != null)
                {
                    searchNode = fileFolderService.searchSimple(userContainer, name);
                }
            }
        }

        return searchNode;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.search.RecordsManagementSearchService#addReports(java.lang.String)
     */
    @Override
    public void addReports(String siteId)
    {
        for (ReportDetails report : reports)
        {
            // Create saved search details
            SavedSearchDetails savedSearchDetails = new SavedSearchDetails(
                                                            siteId,
                                                            report.getName(),
                                                            report.getDescription(),
                                                            report.getSearch(),
                                                            report.getSearchParameters(),
                                                            true,
                                                            true,
                                                            namespaceService,
                                                            this);

            // Save search details
            saveSearch(savedSearchDetails);
        }
    }
}
