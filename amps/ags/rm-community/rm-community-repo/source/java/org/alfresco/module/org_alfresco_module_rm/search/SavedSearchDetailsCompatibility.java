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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Compatibility class.
 *
 * Used to bridge between the old style of saved search passed and required by the UI and the new actual saved search details.
 * Eventually will be factored out as web scripts are brought up to date.
 */
// Not @AlfrescoPublicApi at the moment as it requires RecordsManagementSearchServiceImpl which is not public API.
public class SavedSearchDetailsCompatibility implements RecordsManagementModel
{
    /** Saved search details */
    private final SavedSearchDetails savedSearchDetails;

    /** Namespace service */
    private final NamespaceService namespaceService;

    /** Records management search service implementation */
    private final RecordsManagementSearchServiceImpl searchService;

    /**
     * Retrieve the search from the parameter string
     * @param params    parameter string
     * @return String   search term
     */
    public static String getSearchFromParams(String params)
    {
        String search = null;
        String[] values = params.split("&");
        for (String value : values)
        {
            if (value.startsWith("terms"))
            {
                String[] terms = value.trim().split("=");
                try
                {
                    search = URLDecoder.decode(terms[1], "UTF-8");
                }
                catch (UnsupportedEncodingException e)
                {
                    // Do nothing just return null
                    search = null;
                }
                break;
            }
        }

        return search;
    }

    public static RecordsManagementSearchParameters createSearchParameters(String params, String sort, NamespaceService namespaceService)
    {
        return createSearchParameters(params, new String[]{"&", "="}, sort, namespaceService);
    }

    /**
     *
     * @param params
     * @param sort
     * @param namespaceService
     * @return
     */
    public static RecordsManagementSearchParameters createSearchParameters(String params, String[] paramsDelim, String sort, NamespaceService namespaceService)
    {
        RecordsManagementSearchParameters result = new RecordsManagementSearchParameters();
        List<QName> includedContainerTypes = new ArrayList<>(2);

        // Map the param values into the search parameter object
        String[] values = params.split(paramsDelim[0]);
        for (String value : values)
        {
            String[] paramValues = value.split(paramsDelim[1]);
            String paramName = paramValues[0].trim();
            String paramValue = paramValues[1].trim();
            if ("records".equals(paramName))
            {
                result.setIncludeRecords(Boolean.parseBoolean(paramValue));
            }
            else if ("undeclared".equals(paramName))
            {
                result.setIncludeUndeclaredRecords(Boolean.parseBoolean(paramValue));
            }
            else if ("vital".equals(paramName))
            {
                result.setIncludeVitalRecords(Boolean.parseBoolean(paramValue));
            }
            else if ("folders".equals(paramName))
            {
                result.setIncludeRecordFolders(Boolean.parseBoolean(paramValue));
            }
            else if ("frozen".equals(paramName))
            {
                result.setIncludeFrozen(Boolean.parseBoolean(paramValue));
            }
            else if ("cutoff".equals(paramName))
            {
                result.setIncludeCutoff(Boolean.parseBoolean(paramValue));
            }
            else if ("categories".equals(paramName) && Boolean.parseBoolean(paramValue))
            {
                includedContainerTypes.add(TYPE_RECORD_CATEGORY);
            }
        }
        result.setIncludedContainerTypes(includedContainerTypes);

        if (sort != null)
        {
            // Map the sort string into the search details
            String[] sortPairs = sort.split(",");
            List<SortItem> sortOrder = new ArrayList<>(sortPairs.length);
            for (String sortPairString : sortPairs)
            {
                String[] sortPair = sortPairString.split("/");
                QName field = QName.createQName(sortPair[0], namespaceService);
                Boolean isAcsending = Boolean.FALSE;
                if ("asc".equals(sortPair[1]))
                {
                    isAcsending = Boolean.TRUE;
                }
                sortOrder.add(new SortItem(field, isAcsending));
            }
            result.setSortOrder(sortOrder);
        }

        return result;
    }

    /**
     * Constructor
     * @param savedSearchDetails
     */
    public SavedSearchDetailsCompatibility(SavedSearchDetails savedSearchDetails,
                                           NamespaceService namespaceService,
                                           RecordsManagementSearchServiceImpl searchService)
    {
        this.savedSearchDetails = savedSearchDetails;
        this.namespaceService = namespaceService;
        this.searchService = searchService;
    }

    /**
     * Get the sort string from the saved search details
     * @return
     */
    public String getSort()
    {
        StringBuilder builder = new StringBuilder(64);

        for (SortItem entry : this.savedSearchDetails.getSearchParameters().getSortOrder())
        {
            if (builder.length() !=0)
            {
                builder.append(",");
            }

            String order = "desc";
            if (entry.assc)
            {
                order = "asc";
            }
            builder.append(entry.property.toPrefixString(this.namespaceService))
                   .append("/")
                   .append(order);
        }

        return builder.toString();
    }

    /**
     * Get the parameter string from the saved search details
     * @return
     */
    public String getParams()
    {
        List<QName> includeContainerTypes = this.savedSearchDetails.getSearchParameters().getIncludedContainerTypes();
        StringBuilder builder = new StringBuilder(128);
        builder.append("terms=").append(this.savedSearchDetails.getSearch()).append("&")
               .append("records=").append(this.savedSearchDetails.getSearchParameters().isIncludeRecords()).append("&")
               .append("undeclared=").append(this.savedSearchDetails.getSearchParameters().isIncludeUndeclaredRecords()).append("&")
               .append("vital=").append(this.savedSearchDetails.getSearchParameters().isIncludeVitalRecords()).append("&")
               .append("folders=").append(this.savedSearchDetails.getSearchParameters().isIncludeRecordFolders()).append("&")
               .append("frozen=").append(this.savedSearchDetails.getSearchParameters().isIncludeFrozen()).append("&")
               .append("cutoff=").append(this.savedSearchDetails.getSearchParameters().isIncludeCutoff()).append("&")
               .append("categories=").append(includeContainerTypes.contains(TYPE_RECORD_CATEGORY)).append("&")
               .append("series=").append(false);
        return builder.toString();
    }

    /**
     * Build the full query string
     * @return
     */
    public String getQuery()
    {
        return searchService.buildQueryString(this.savedSearchDetails.getSearch(), this.savedSearchDetails.getSearchParameters());
    }
}
