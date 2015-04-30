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
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.classification.SecurityClearance;
import org.alfresco.module.org_alfresco_module_rm.classification.SecurityClearanceService;
import org.alfresco.module.org_alfresco_module_rm.script.AbstractRmWebScript;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.util.Pair;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Implementation for Java backed webscript to get users security clearance.
 *
 * @author Tuna Aksoy
 * @since 3.0
 */
public class UserSecurityClearanceGet extends AbstractRmWebScript
{
    /** Constants */
    private static final String TOTAL = "total";
    private static final String SORT_BY = "sortBy";
    private static final String START_INDEX = "startIndex";
    private static final String PAGE_SIZE = "pageSize";
    private static final String ITEM_COUNT = "itemCount";
    private static final String ITEMS = "items";
    private static final String DATA = "data";
    private static final String FILTER = "filter";
    private static final int TOTAL_INT = -1;
    private static final int START_INDEX_INT = 0;
    private static final int PAGE_SIZE_INT = 10;
    private static final boolean SORT_BY_ASC_BOOL = true;

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
        String filter = getFilter(req);
        boolean sortAscending = isSortAscending(req);
        PagingRequest pagingRequest = getPagingRequest(req);

        PagingResults<SecurityClearance> usersSecurityClearance = getSecurityClearanceService().getUsersSecurityClearance(filter, sortAscending, pagingRequest);
        List<SecurityClearance> securityClearanceItems = getSecurityClearanceItems(usersSecurityClearance);

        Map<String, Object> securityClearanceData = new HashMap<>();
        securityClearanceData.put(TOTAL, getTotal(usersSecurityClearance));
        securityClearanceData.put(PAGE_SIZE, pagingRequest.getMaxItems());
        securityClearanceData.put(START_INDEX, pagingRequest.getSkipCount());
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
    private int getTotal(PagingResults<SecurityClearance> usersSecurityClearance)
    {
        Pair<Integer, Integer> totalResultCount = usersSecurityClearance.getTotalResultCount();
        return totalResultCount != null ? totalResultCount.getFirst() : TOTAL_INT;
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
     * Gets the sort direction from the webscript request
     *
     * @param req {@link WebScriptRequest} The webscript request
     * @return <code>true</code> if the sort direction is ascending, <code>false</code> otherwise
     */
    private boolean isSortAscending(WebScriptRequest req)
    {
        String sortBy = req.getParameter(SORT_BY);
        return isNotBlank(sortBy) ? parseBoolean(sortBy) : SORT_BY_ASC_BOOL;
    }

    /**
     * Gets the filter from the webscript request
     *
     * @param req {@link WebScriptRequest} The webscript request
     * @return {@link String} The filter
     */
    private String getFilter(WebScriptRequest req)
    {
        return req.getParameter(FILTER);
    }

    /**
     * Helper method to create the paging request from the webscript request
     *
     * @param req {@link WebScriptRequest} The webscript request
     * @return {@link PagingRequest} The paging request
     */
    private PagingRequest getPagingRequest(WebScriptRequest req)
    {
        String startIndexAsString = req.getParameter(START_INDEX);
        String pageSizeAsString = req.getParameter(PAGE_SIZE);

        int startIndex = isNotBlank(startIndexAsString) ? parseInt(startIndexAsString) : START_INDEX_INT;
        int pageSize = isNotBlank(pageSizeAsString) ? parseInt(pageSizeAsString) : PAGE_SIZE_INT;

        return new PagingRequest(startIndex, pageSize, null);
    }
}
