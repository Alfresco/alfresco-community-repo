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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.module.org_alfresco_module_rm.script.classification;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.springframework.extensions.webscripts.Status.STATUS_INTERNAL_SERVER_ERROR;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.classification.SecurityClearance;
import org.alfresco.module.org_alfresco_module_rm.classification.SecurityClearanceService;
import org.alfresco.module.org_alfresco_module_rm.classification.UserQueryParams;
import org.alfresco.module.org_alfresco_module_rm.script.AbstractRmWebScript;
import org.alfresco.query.PagingResults;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.collections.iterators.ArrayIterator;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Implementation for Java backed webscript to get users security clearance.
 *
 * @author Tuna Aksoy
 * @since 3.0.a
 */
public class UserSecurityClearanceGet extends AbstractRmWebScript
{
    /** Constants */
    private static final String TOTAL = "total";
    private static final String SKIP_COUNT = "startIndex";
    private static final String MAX_ITEMS = "pageSize";
    private static final String SORT_FIELDS = "sortField";
    private static final String SORT_ASCENDING_FLAGS = "sortAscending";
    private static final String SEPARATOR = ",";
    private static final String ITEM_COUNT = "itemCount";
    private static final String ITEMS = "items";
    private static final String DATA = "data";
    private static final String NAME_FILTER = "nameFilter";
    private static final String PAGE_NUMBER = "page";

    /** Security clearance service */
    private SecurityClearanceService securityClearanceService;

    /**
     * @return the securityClearanceService
     */
    protected SecurityClearanceService getSecurityClearanceService()
    {
        return this.securityClearanceService;
    }

    /**
     * @param securityClearanceService the securityClearanceService to set
     */
    public void setSecurityClearanceService(SecurityClearanceService securityClearanceService)
    {
        this.securityClearanceService = securityClearanceService;
    }

    /**
     * @see org.springframework.extensions.webscripts.DeclarativeWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest,
     *      org.springframework.extensions.webscripts.Status,
     *      org.springframework.extensions.webscripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        String nameFilter = getNameFilter(req);
        UserQueryParams userQueryParams = new UserQueryParams(nameFilter);
        setMaxItems(userQueryParams, req);
        setSkipCount(userQueryParams, req);
        setSortProps(userQueryParams, req);

        PagingResults<SecurityClearance> usersSecurityClearance = getSecurityClearanceService().getUsersSecurityClearance(userQueryParams);

        int total = getTotal(usersSecurityClearance).intValue();
        int maxItems = userQueryParams.getMaxItems();
        if (total != 0 && getPageNumber(req) > Math.ceil((double) total / (double) maxItems))
        {
            throw new WebScriptException("The requested page is not valid");
        }

        List<SecurityClearance> securityClearanceItems = getSecurityClearanceItems(usersSecurityClearance);

        Map<String, Object> securityClearanceData = new HashMap<>();
        securityClearanceData.put(TOTAL, total);
        securityClearanceData.put(MAX_ITEMS, maxItems);
        securityClearanceData.put(SKIP_COUNT, userQueryParams.getSkipCount());
        securityClearanceData.put(ITEM_COUNT, securityClearanceItems.size());
        securityClearanceData.put(ITEMS, securityClearanceItems);

        Map<String, Object> model = new HashMap<>();
        model.put(DATA, securityClearanceData);

        return model;
    }

    /**
     * Helper method to get the total number of security clearance items
     *
     * @param usersSecurityClearance {@link PagingResults} The security clearance results
     * @return The total number of security clearance items
     */
    private Integer getTotal(PagingResults<SecurityClearance> usersSecurityClearance)
    {
        Pair<Integer, Integer> totalResultCount = usersSecurityClearance.getTotalResultCount();
        if (totalResultCount == null)
        {
            throw new WebScriptException(STATUS_INTERNAL_SERVER_ERROR, "Total result count cannot be determined.");
        }
        return totalResultCount.getFirst();
    }

    /**
     * Helper method to get the security clearance items from the {@link PagingResults}
     *
     * @param usersSecurityClearance {@link PagingResults} The security clearance results
     * @return {@link List}<{@link SecurityClearance}> The list of security clearance items
     */
    private List<SecurityClearance> getSecurityClearanceItems(PagingResults<SecurityClearance> usersSecurityClearance)
    {
        return usersSecurityClearance.getPage();
    }

    /**
     * Gets the name filter from the webscript request
     *
     * @param req {@link WebScriptRequest} The webscript request
     * @return {@link String} The name filter from the request (or the empty string if the request doesn't contain one).
     */
    private String getNameFilter(WebScriptRequest req)
    {
        String nameFilter = req.getParameter(NAME_FILTER);
        return (nameFilter != null ? nameFilter : "");
    }

    /**
     * Gets the page number from the webscript request
     *
     * @param req {@link WebScriptRequest} The webscript request
     * @return <code>int</code> The page number
     */
    private int getPageNumber(WebScriptRequest req)
    {
        String pageNumber = req.getParameter(PAGE_NUMBER);
        return isNotBlank(pageNumber) ? parseInt(pageNumber) : 1;
    }

    /**
     * Helper method to set the max index for the request query object
     *
     * @param userQueryParams {@link UserQueryParams} The request query object
     * @param req {@link WebScriptRequest} The webscript request
     */
    private void setMaxItems(UserQueryParams userQueryParams, WebScriptRequest req)
    {
        String maxItems = req.getParameter(MAX_ITEMS);
        if (isNotBlank(maxItems))
        {
            userQueryParams.withMaxItems(parseInt(maxItems));
        }
    }

    /**
     * Helper method to set the skip count for the query object
     *
     * @param userQueryParams {@link UserQueryParams} The request query object
     * @param req {@link WebScriptRequest} The webscript request
     */
    private void setSkipCount(UserQueryParams userQueryParams, WebScriptRequest req)
    {
        String skipCount = req.getParameter(SKIP_COUNT);
        if (isNotBlank(skipCount))
        {
            userQueryParams.withSkipCount(parseInt(skipCount));
        }
    }

    /**
     * Helper method to set sort properties for the query object
     *
     * @param userQueryParams {@link UserQueryParams} The request query object
     * @param req {@link WebScriptRequest} The webscript request
     */
    @SuppressWarnings("unchecked")
    protected void setSortProps(UserQueryParams userQueryParams, WebScriptRequest req)
    {
        String sortFields = req.getParameter(SORT_FIELDS);
        if (isBlank(sortFields))
        {
            return;
        }
        String sortAscendingFlags = req.getParameter(SORT_ASCENDING_FLAGS);
        sortAscendingFlags = (isBlank(sortAscendingFlags) ? "True" : sortAscendingFlags);

        List<Pair<QName, Boolean>> sortPairs = new ArrayList<>();
        Iterator<String> ascendingFlagIterator = new ArrayIterator((String[]) sortAscendingFlags.split(SEPARATOR));
        for (String sortField : sortFields.split(SEPARATOR))
        {
            boolean ascendingFlag = (ascendingFlagIterator.hasNext() ? parseBoolean(ascendingFlagIterator.next()) : true);

            if (isNotBlank(sortField))
            {
                Pair<QName, Boolean> sortPair = new Pair<>(QName.createQName(sortField, getNamespaceService()), ascendingFlag);
                sortPairs.add(sortPair);
            }
        }
        if (!sortPairs.isEmpty())
        {
            userQueryParams.withSortProps(sortPairs);
        }
    }
}
