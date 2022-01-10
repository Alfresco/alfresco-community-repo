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

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.util.ParameterCheck;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Saved search details.
 *
 * Example format of posted Saved Search JSON:
 *
 *      {
 *         "siteid" : "rm",
 *         "name": "search name",
 *         "description": "the search description",
 *         "search": "the search sting as entered by the user",
 *         "public": boolean,
 *         "searchparams" :
 *         {
 *            "maxItems" : 500,
 *            "records" : true,
 *            "undeclaredrecords" : false,
 *            "vitalrecords" : false,
 *            "recordfolders" : false,
 *            "frozen" : false,
 *            "cutoff" : false,
 *            "containertypes" :
 *            [
 *              "rma:recordSeries",
 *              "rma:recordCategory"
 *            ]
 *            "sort" :
 *            [
 *               {
 *                  "field" : "cm:name",
 *                  "ascending" : true
 *               }
 *            ]
 *         }
 *      }
 *
 *      where: name and query values are mandatory,
 *             searchparams contains the filters, sort, etc information about the query
 *             query is there for backward compatibility
 *      note:
 *            "params": "terms=keywords:xyz&amp;undeclared=true",
 *            "sort": "cm:name/asc"
 *            "query": "the complete search query string",
 *            ... are sometimes found in the place of searchparams and are migrated to the new format when re-saved
 *            params are in URL encoded name/value pair format
 *            sort is in comma separated "property/dir" packed format i.e. "cm:name/asc,cm:title/desc"
 *
 * @author Roy Wetherall
 */
// Not @AlfrescoPublicApi at the moment as it requires RecordsManagementSearchServiceImpl which is not public API.
public class SavedSearchDetails extends ReportDetails
{
    // JSON label values
    public static final String SITE_ID = "siteid";
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String SEARCH = "search";
    public static final String PUBLIC = "public";
    public static final String REPORT = "report";
    public static final String SEARCHPARAMS = "searchparams";

    // JSON values for backwards compatibility
    public static final String QUERY = "query";
    public static final String SORT = "sort";
    public static final String PARAMS = "params";

    private static final String DEFAULT_SITE_ID = "rm";

    /** Site id */
	private String siteId;

	/** Indicates whether the saved search is public or not */
	private boolean isPublic = true;

	/** Indicates whether the saved search is a report */
	private boolean isReport = false;

	/** Helper method to link to search node ref if provided */
	private NodeRef nodeRef = null;

    /** Namespace service */
    NamespaceService namespaceService;

    /** Records management search service */
    RecordsManagementSearchServiceImpl searchService;

    /** Saves search details compatibility */
    private SavedSearchDetailsCompatibility compatibility;

	/**
	 *
	 * @param jsonString
	 * @return
	 */
	/*package*/ static SavedSearchDetails createFromJSON(String jsonString, NamespaceService namespaceService, RecordsManagementSearchServiceImpl searchService, NodeRef nodeRef)
	{
	    try
	    {
    	    JSONObject search = new JSONObject(jsonString);

    	    // Get the site id
    	    String siteId = DEFAULT_SITE_ID;
    	    if (search.has(SITE_ID))
    	    {
    	        siteId = search.getString(SITE_ID);
    	    }

    	    // Get the name
    	    if (!search.has(NAME))
    	    {
    	        throw new AlfrescoRuntimeException("Can not create saved search details from json, because required name is not present. " + jsonString);
    	    }
    	    String name = search.getString(NAME);

    	    // Get the description
    	    String description = "";
    	    if (search.has(DESCRIPTION))
    	    {
    	        description = search.getString(DESCRIPTION);
                String translated = I18NUtil.getMessage(description);
                if (translated != null)
                {
                    description = translated;
                }
    	    }

    	    // Get the query
    	    String query = null;
    	    if (!search.has(SEARCH))
    	    {
    	        // We are probably dealing with a "old" style saved search
    	        if (search.has(PARAMS))
    	        {
    	            String oldParams = search.getString(PARAMS);
    	            query = SavedSearchDetailsCompatibility.getSearchFromParams(oldParams);
    	        }
    	        else
    	        {
    	            throw new AlfrescoRuntimeException("Can not create saved search details from json, because required search is not present. " + jsonString);
    	        }

    	    }
    	    else
    	    {
    	        query = search.getString(SEARCH);
    	    }

    	    // Get the search parameters
            RecordsManagementSearchParameters searchParameters = new RecordsManagementSearchParameters();
            if (search.has(SEARCHPARAMS))
            {
                searchParameters = RecordsManagementSearchParameters.createFromJSON(search.getJSONObject(SEARCHPARAMS), namespaceService);
            }
            else
            {
                // See if we are dealing with the old style of saved search
                if (search.has(PARAMS))
                {
                    String oldParams = search.getString(PARAMS);
                    String oldSort = search.getString(SORT);
                    searchParameters = SavedSearchDetailsCompatibility.createSearchParameters(oldParams, oldSort, namespaceService);
                }
            }

    	    // Determine whether the saved query is public or not
    	    boolean isPublic = true;
    	    if (search.has(PUBLIC))
    	    {
    	        isPublic = search.getBoolean(PUBLIC);
    	    }

    	    // Determine whether the saved query is a report or not
    	    boolean isReport = false;
    	    if (search.has(REPORT))
    	    {
    	        isReport = search.getBoolean(REPORT);
    	    }

    	    // Create the saved search details object
    	    SavedSearchDetails savedSearchDetails = new SavedSearchDetails(siteId, name, description, query, searchParameters, isPublic, isReport, namespaceService, searchService);
    	    savedSearchDetails.nodeRef = nodeRef;
    	    return savedSearchDetails;
	    }
	    catch (JSONException exception)
	    {
	        throw new AlfrescoRuntimeException("Can not create saved search details from json. " + jsonString, exception);
	    }
	}

	/**
	 * @param siteId
	 * @param name
	 * @param description
	 * @param isPublic
	 */
	/*package*/ SavedSearchDetails(
	        String siteId,
	        String name,
	        String description,
	        String serach,
	        RecordsManagementSearchParameters searchParameters,
	        boolean isPublic,
	        boolean isReport,
	        NamespaceService namespaceService,
	        RecordsManagementSearchServiceImpl searchService)
	{
	    super(name, description, serach, searchParameters);

        ParameterCheck.mandatory("siteId", siteId);
        ParameterCheck.mandatory("namespaceService", namespaceService);
        ParameterCheck.mandatory("searchService", searchService);

	    this.siteId = siteId;
		this.isPublic = isPublic;
		this.isReport = isReport;
		this.namespaceService = namespaceService;
		this.compatibility = new SavedSearchDetailsCompatibility(this, namespaceService, searchService);
		this.searchService = searchService;
	}

	/**
	 * @return
	 */
	public String getSiteId()
	{
		return siteId;
	}

	/**
	 * @return
	 */
	public boolean isPublic()
	{
		return isPublic;
	}

	/**
	 * @return
	 */
	public boolean isReport()
    {
        return isReport;
    }

	public SavedSearchDetailsCompatibility getCompatibility()
	{
	    return compatibility;
	}

	/**
	 * @return NodeRef search node ref, null if not set
	 */
	public NodeRef getNodeRef()
	{
	    return nodeRef;
	}

	/**
	 * @return
	 */
	public String toJSONString()
	{
	    try
	    {
    	    JSONObject jsonObject = new JSONObject();
    	    jsonObject.put(SITE_ID, siteId);
    	    jsonObject.put(NAME, name);
    	    jsonObject.put(DESCRIPTION, description);
    	    jsonObject.put(SEARCH, search);
    	    jsonObject.put(SEARCHPARAMS, searchParameters.toJSONObject(namespaceService));
    	    jsonObject.put(PUBLIC, isPublic);

    	    // Add full query for backward compatibility
    	    jsonObject.put(QUERY, searchService.buildQueryString(search, searchParameters));
    	    jsonObject.put(SORT, compatibility.getSort());

    	    return jsonObject.toString();
	    }
	    catch (JSONException exception)
	    {
	        throw new AlfrescoRuntimeException("Can not convert saved search details into JSON.", exception);
	    }
	}
}
