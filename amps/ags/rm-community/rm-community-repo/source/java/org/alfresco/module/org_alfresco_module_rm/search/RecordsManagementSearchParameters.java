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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Roy Wetherall
 */
@AlfrescoPublicApi
@SuppressWarnings("serial")
public class RecordsManagementSearchParameters
{
    /** Default sort order */
    private static final List<SortItem> DEFAULT_SORT_ORDER = new ArrayList<SortItem>()
    {
        {
            add(new SortItem(ContentModel.PROP_NAME, Boolean.TRUE));
        }
    };

    /** Default templates */
    private static final Map<String, String> DEFAULT_TEMPLATES = new HashMap<String, String>()
    {
        {
            put("keywords", "%(cm:name cm:title cm:description TEXT)");
            put("name", "%(cm:name)");
            put("title", "%(cm:title)");
            put("description", "%(cm:description)");
            put("creator", "%(cm:creator)");
            put("created", "%(cm:created)");
            put("modifier", "%(cm:modifier)");
            put("modified", "%(cm:modified)");
            put("author", "%(cm:author)");
            put("markings", "%(rmc:supplementalMarkingList)");
            put("dispositionEvents", "%(rma:recordSearchDispositionEvents)");
            put("dispositionActionName", "%(rma:recordSearchDispositionActionName)");
            put("dispositionActionAsOf", "%(rma:recordSearchDispositionActionAsOf)");
            put("dispositionEventsEligible", "%(rma:recordSearchDispositionEventsEligible)");
            put("dispositionPeriod", "%(rma:recordSearchDispositionPeriod)");
            put("hasDispositionSchedule", "%(rma:recordSearchHasDispositionSchedule)");
            put("dispositionInstructions", "%(rma:recordSearchDispositionInstructions)");
            put("dispositionAuthority", "%(rma:recordSearchDispositionAuthority)");
            put("vitalRecordReviewPeriod", "%(rma:recordSearchVitalRecordReviewPeriod)");
        }
    };

    /** Default included container types */
    private static final List<QName> DEFAULT_INCLUDED_CONTAINER_TYPES = Collections.emptyList();

    /** Max items */
    private int maxItems;

    private boolean includeRecords = true;
    private boolean includeUndeclaredRecords = false;
    private boolean includeVitalRecords = false;
    private boolean includeRecordFolders = true;
    private boolean includeFrozen = false;
    private boolean includeCutoff = false;

    private List<QName> includedContainerTypes = DEFAULT_INCLUDED_CONTAINER_TYPES;
    private List<SortItem> sortOrder = DEFAULT_SORT_ORDER;
    private Map<String, String> templates = DEFAULT_TEMPLATES;

    private static final String JSON_MAXITEMS = "maxitems";
    private static final String JSON_RECORDS = "records";
    private static final String JSON_UNDECLAREDRECORDS = "undeclaredrecords";
    private static final String JSON_VITALRECORDS = "vitalrecords";
    private static final String JSON_RECORDFOLDERES = "recordfolders";
    private static final String JSON_FROZEN = "frozen";
    private static final String JSON_CUTOFF = "cutoff";
    private static final String JSON_CONTAINERTYPES = "containertypes";
    private static final String JSON_SORT = "sort";
    private static final String JSON_FIELD = "field";
    private static final String JSON_ASCENDING = "ascending";

    /**
     * {
     *    "maxItems" : 500,
     *    "records" : true,
     *    "undeclaredrecords" : false,
     *    "vitalrecords" : false,
     *    "recordfolders" : false,
     *    "frozen" : false,
     *    "cutoff" : false,
     *    "containertypes" :
     *    [
     *       "rma:recordSeries",
     *       "rma:recordCategory"
     *    ]
     *    "sort" :
     *    [
     *       {
     *          "field" : "cm:name",
     *          "ascending" : true
     *       }
     *    ]
     * }
     */
    public static RecordsManagementSearchParameters createFromJSON(String json, NamespaceService namespaceService)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(json);
            return RecordsManagementSearchParameters.createFromJSON(jsonObject, namespaceService);
        }
        catch (JSONException e)
        {
            throw new AlfrescoRuntimeException("Unable to create records management search parameters from json string.  " + json, e);
        }
    }

    /**
     *
     * @param jsonObject
     * @return
     */
    public static RecordsManagementSearchParameters createFromJSON(JSONObject jsonObject, NamespaceService namespaceService)
    {
        try
        {
            RecordsManagementSearchParameters searchParameters = new RecordsManagementSearchParameters();

            // Get the search parameter properties
            if (jsonObject.has(JSON_MAXITEMS))
            {
                searchParameters.setMaxItems(jsonObject.getInt(JSON_MAXITEMS));
            }
            if (jsonObject.has(JSON_RECORDS))
            {
                searchParameters.setIncludeRecords(jsonObject.getBoolean(JSON_RECORDS));
            }
            if (jsonObject.has(JSON_UNDECLAREDRECORDS))
            {
                searchParameters.setIncludeUndeclaredRecords(jsonObject.getBoolean(JSON_UNDECLAREDRECORDS));
            }
            if (jsonObject.has(JSON_VITALRECORDS))
            {
                searchParameters.setIncludeVitalRecords(jsonObject.getBoolean(JSON_VITALRECORDS));
            }
            if (jsonObject.has(JSON_RECORDFOLDERES))
            {
                searchParameters.setIncludeRecordFolders(jsonObject.getBoolean(JSON_RECORDFOLDERES));
            }
            if (jsonObject.has(JSON_FROZEN))
            {
                searchParameters.setIncludeFrozen(jsonObject.getBoolean(JSON_FROZEN));
            }
            if (jsonObject.has(JSON_CUTOFF))
            {
                searchParameters.setIncludeCutoff(jsonObject.getBoolean(JSON_CUTOFF));
            }

            // Get container types
            if (jsonObject.has(JSON_CONTAINERTYPES))
            {
                JSONArray jsonArray = jsonObject.getJSONArray(JSON_CONTAINERTYPES);
                List<QName> containerTypes = new ArrayList<>(jsonArray.length());
                for (int i = 0; i < jsonArray.length(); i++)
                {
                    String type = jsonArray.getString(i);
                    containerTypes.add(QName.createQName(type, namespaceService));
                }
                searchParameters.setIncludedContainerTypes(containerTypes);
            }

            // Get sort details
            if (jsonObject.has(JSON_SORT))
            {
                JSONArray jsonArray = jsonObject.getJSONArray(JSON_SORT);
                List<SortItem> sortOrder = new ArrayList<>(jsonArray.length());
                for (int i = 0; i < jsonArray.length(); i++)
                {
                    JSONObject sortJSONObject = jsonArray.getJSONObject(i);
                    if (sortJSONObject.has(JSON_FIELD) &&
                        sortJSONObject.has(JSON_ASCENDING))
                    {
                        sortOrder.add(new SortItem(
                                QName.createQName(sortJSONObject.getString(JSON_FIELD), namespaceService),
                                sortJSONObject.getBoolean(JSON_ASCENDING)));
                    }
                }
                searchParameters.setSortOrder(sortOrder);
            }

            return searchParameters;
        }
        catch (JSONException e)
        {
            throw new AlfrescoRuntimeException("Unable to create records management search parameters from json string.  " + jsonObject.toString(), e);
        }
    }

    /**
     *
     * @return
     */
    public String toJSONString(NamespaceService namespaceService)
    {
        return toJSONObject(namespaceService).toString();
    }

    public JSONObject toJSONObject(NamespaceService namespaceService)
    {
        try
        {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(JSON_MAXITEMS, maxItems);
            jsonObject.put(JSON_RECORDS, includeRecords);
            jsonObject.put(JSON_UNDECLAREDRECORDS, includeUndeclaredRecords);
            jsonObject.put(JSON_VITALRECORDS, includeVitalRecords);
            jsonObject.put(JSON_RECORDFOLDERES, includeRecordFolders);
            jsonObject.put(JSON_FROZEN, includeFrozen);
            jsonObject.put(JSON_CUTOFF, includeCutoff);

            // Included containers
            JSONArray jsonArray = new JSONArray();
            for (QName containerType : includedContainerTypes)
            {
                jsonArray.put(containerType.toPrefixString(namespaceService));
            }
            jsonObject.put(JSON_CONTAINERTYPES, jsonArray);

            // Sort
            JSONArray jsonSortArray = new JSONArray();
            for (SortItem entry : sortOrder)
            {
                JSONObject jsonEntry = new JSONObject();
                jsonEntry.put(JSON_FIELD, entry.property.toPrefixString(namespaceService));
                jsonEntry.put(JSON_ASCENDING, entry.assc);
                jsonSortArray.put(jsonEntry);
            }
            jsonObject.put(JSON_SORT, jsonSortArray);

            return jsonObject;
        }
        catch (JSONException e)
        {
            throw new AlfrescoRuntimeException("Unable to generate json string for records management search parameters.", e);
        }
    }

    public void setMaxItems(int maxItems)
    {
        this.maxItems = maxItems;
    }

    public int getMaxItems()
    {
        return maxItems;
    }

    public void setSortOrder(List<SortItem> sortOrder)
    {
        this.sortOrder = sortOrder;
    }

    public List<SortItem> getSortOrder()
    {
        return sortOrder;
    }

    public void setTemplates(Map<String, String> templates)
    {
        this.templates = templates;
    }

    public Map<String, String> getTemplates()
    {
        return templates;
    }

    public void setIncludeRecords(boolean includeRecords)
    {
        this.includeRecords = includeRecords;
    }

    public boolean isIncludeRecords()
    {
        return includeRecords;
    }

    public void setIncludeUndeclaredRecords(boolean includeUndeclaredRecords)
    {
        this.includeUndeclaredRecords = includeUndeclaredRecords;
    }

    public boolean isIncludeUndeclaredRecords()
    {
        return includeUndeclaredRecords;
    }

    public void setIncludeVitalRecords(boolean includeVitalRecords)
    {
        this.includeVitalRecords = includeVitalRecords;
    }

    public boolean isIncludeVitalRecords()
    {
        return includeVitalRecords;
    }

    public void setIncludeRecordFolders(boolean includeRecordFolders)
    {
        this.includeRecordFolders = includeRecordFolders;
    }

    public boolean isIncludeRecordFolders()
    {
        return includeRecordFolders;
    }

    public void setIncludeFrozen(boolean includeFrozen)
    {
        this.includeFrozen = includeFrozen;
    }

    public boolean isIncludeFrozen()
    {
        return includeFrozen;
    }

    public void setIncludeCutoff(boolean includeCutoff)
    {
        this.includeCutoff = includeCutoff;
    }

    public boolean isIncludeCutoff()
    {
        return includeCutoff;
    }

    public void setIncludedContainerTypes(List<QName> includedContainerTypes)
    {
        this.includedContainerTypes = includedContainerTypes;
    }

    public List<QName> getIncludedContainerTypes()
    {
        return includedContainerTypes;
    }
    
}
