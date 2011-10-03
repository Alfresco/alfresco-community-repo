/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

package org.alfresco.util;

import org.alfresco.service.cmr.admin.RepoAdminService;
import org.alfresco.service.cmr.admin.RepoUsage.LicenseMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model related utility functions.
 * 
 * @since 3.5
 */
public class ModelUtil
{
    private static final String SHARE = "Share";
    private static final String TEAM = "Team";

    public static final String PAGING_MAX_ITEMS = "maxItems";
    public static final String PAGING_SKIP_COUNT = "skipCount";
    public static final String PAGING_TOTAL_ITEMS = "totalItems";
    public static final String PAGING_TOTAL_ITEMS_RANGE_END = "totalItemsRangeEnd";
    public static final String PAGING_CONFIDENCE = "confidence";

    /**
     * Returns the name of the product currently running, determined
     * by the current license.
     * 
     * @param repoAdminService The RepoAdminService
     * @return "Share" or "Team"
     */
    public static String getProductName(RepoAdminService repoAdminService)
    {
        // the product name is never localised so it's safe to
        // return a hard-coded string but if we ever need to it's
        // centralised here.
        
        String productName = SHARE;
        
        if (repoAdminService != null && 
            repoAdminService.getRestrictions().getLicenseMode().equals(LicenseMode.TEAM))
        {
            productName = TEAM;
        }
        
        return productName;
    }

    /**
     * Returns representation of paging object
     * 
     * @param totalItems all count of object
     * @param maxItems max count of object that should be returned
     * @param skipCount count of skipped objects
     * @param confidence the confidence in the total, default is exact
     * @param totalItemsRangeEnd if the total is a range, what is the upper end of it
     * @return A model map of the details
     */
    public static Map<String, Object> buildPaging(int totalItems, int maxItems, int skipCount, 
                    ScriptPagingDetails.ItemsSizeConfidence confidence, int totalItemsRangeEnd)
    {
        HashMap<String, Object> model = new HashMap<String, Object>();
        if(confidence == null)
        {
           confidence = ScriptPagingDetails.ItemsSizeConfidence.EXACT;
        }

        model.put(PAGING_MAX_ITEMS, maxItems);
        model.put(PAGING_SKIP_COUNT, skipCount);
        model.put(PAGING_TOTAL_ITEMS, totalItems);
        model.put(PAGING_TOTAL_ITEMS_RANGE_END, totalItemsRangeEnd);
        model.put(PAGING_CONFIDENCE, confidence);
        
        return model;
    }
    
    /**
     * Returns representation of paging object
     * 
     * @param totalItems all count of object
     * @param maxItems max count of object that should be returned
     * @param skipCount count of skipped objects
     * @return A model map of the details
     */
    public static Map<String, Object> buildPaging(int totalItems, int maxItems, int skipCount)
    {
        return buildPaging(totalItems, maxItems, skipCount, null, -1);
    }
    
    /**
     * Returns representation of paging object
     * 
     * @param paging The paging object with total, skip, max etc
     */
    public static Map<String, Object> buildPaging(ScriptPagingDetails paging)
    {
        return buildPaging(
                paging.getTotalItems(),
                paging.getMaxItems(),
                paging.getSkipCount(),
                paging.getConfidence(),
                paging.getTotalItemsRangeMax()
        );
    }
    
    public static <T> List<T> page(Collection<T> objects, int maxItems, int skipCount)
    {
        return page(objects, new ScriptPagingDetails(maxItems, skipCount));
    }
    
    public static <T> List<T> page(Collection<T> objects, ScriptPagingDetails paging)
    {
        int maxItems = paging.getMaxItems();
        int skipCount = paging.getSkipCount();
        paging.setTotalItems(objects.size());
        
        List<T> result = new ArrayList<T>();
        
        // Do the paging
        int totalItems = objects.size();
        if (maxItems<1 || maxItems>totalItems)
        {
            maxItems = totalItems;
        }
        if (skipCount<0)
        {
            skipCount = 0;
        }
        int endPoint = skipCount + maxItems;
        if (endPoint > totalItems)
        {
            endPoint = totalItems;
        }
        
        int pos = 0;
        for (T entry : objects)
        {
            if(pos >= skipCount)
            {
                if(pos < endPoint)
                {
                    result.add(entry);
                } 
                else
                {
                    break;
                }
            }
            pos++;
        }
        return result;
    }
    
    public static <T> T[] page(T[] objects, int maxItems, int skipCount)
    {
        // Do the paging
        int totalItems = objects.length;
        if (maxItems<1 || maxItems>totalItems)
        {
            maxItems = totalItems;
        }
        if (skipCount<0)
        {
            skipCount = 0;
        }
        int endPoint = skipCount + maxItems;
        if (endPoint > totalItems)
        {
            endPoint = totalItems;
        }
        int size = skipCount > endPoint ? 0 : endPoint - skipCount;

        if(size == totalItems)
        {
            return objects;
        }
        
        T[] result = Arrays.copyOfRange(objects, skipCount, endPoint);
        return result;
    }
}
