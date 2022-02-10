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

package org.alfresco.rm.rest.api.impl;

import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.rest.antlr.WhereClauseParser;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.resource.parameters.where.Query;
import org.alfresco.rest.framework.resource.parameters.where.QueryHelper;
import org.alfresco.rest.workflow.api.impl.MapBasedQueryWalker;
import org.alfresco.rm.rest.api.model.RMNode;
import org.alfresco.rm.rest.api.model.RecordCategoryChild;
import org.alfresco.rm.rest.api.model.UnfiledChild;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

/**
 * Utility class that handles common api endpoint tasks
 *
 * @author Ana Bozianu
 * @since 2.6
 */
public class SearchTypesFactory
{
    private DictionaryService dictionaryService;
    private Nodes nodes;

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public void setNodes(Nodes nodes)
    {
        this.nodes = nodes;
    }

    public Set<QName> buildSearchTypesForFilePlanEndpoint()
    {
        Set<QName> searchTypeQNames = new HashSet<>();
        searchTypeQNames.add(RecordsManagementModel.TYPE_RECORD_CATEGORY);
        return searchTypeQNames;
    }

    /**
     * Helper method to build search types for unfiled container and unfiled record folders endpoints
     * @param parameters
     * @param listFolderChildrenEqualsQueryProperties
     * @return
     */
    public Set<QName> buildSearchTypesForUnfiledEndpoint(Parameters parameters, Set<String> listFolderChildrenEqualsQueryProperties)
    {
        Set<QName> searchTypeQNames = new HashSet<>();

        Query q = parameters.getQuery();

        boolean includeUnfiledRecordFolders = false;
        boolean includeRecords = false;
        boolean includeSubTypes = false;

        if (q != null)
        {
            // filtering via "where" clause
            MapBasedQueryWalker propertyWalker = new MapBasedQueryWalker(listFolderChildrenEqualsQueryProperties, null);
            QueryHelper.walk(q, propertyWalker);

            Boolean isUnfiledRecordFolder = propertyWalker.getProperty(UnfiledChild.PARAM_IS_UNFILED_RECORD_FOLDER,
                    WhereClauseParser.EQUALS, Boolean.class);
            Boolean isRecord = propertyWalker.getProperty(UnfiledChild.PARAM_IS_RECORD, WhereClauseParser.EQUALS, Boolean.class);
            if ((isUnfiledRecordFolder != null && isUnfiledRecordFolder.booleanValue()) || (isRecord != null && !isRecord.booleanValue()))
            {
                includeUnfiledRecordFolders = true;
            }
            else if ((isUnfiledRecordFolder != null && !isUnfiledRecordFolder.booleanValue()) || (isRecord != null && isRecord.booleanValue()))
            {
                includeRecords = true;
            }

            String nodeTypeQNameStr = propertyWalker.getProperty(UnfiledChild.PARAM_NODE_TYPE, WhereClauseParser.EQUALS, String.class);
            QName filterNodeTypeQName;
            if (nodeTypeQNameStr != null)
            {
                if ((isUnfiledRecordFolder != null) || (isRecord != null))
                {
                    throw new InvalidArgumentException("Invalid filter - nodeType and isUnfiledRecordFolder/isRecord are mutually exclusive");
                }

                Pair<QName, Boolean> pair = parseNodeTypeFilter(nodeTypeQNameStr);
                filterNodeTypeQName = pair.getFirst();
                includeSubTypes = pair.getSecond();

                if (filterNodeTypeQName.equals(RecordsManagementModel.TYPE_UNFILED_RECORD_FOLDER))
                {
                    includeUnfiledRecordFolders = true;
                }
                else if (filterNodeTypeQName.equals(ContentModel.TYPE_CONTENT))
                {
                    includeRecords = true;
                }
                else if (dictionaryService.isSubClass(filterNodeTypeQName, ContentModel.TYPE_CONTENT))
                {
                    searchTypeQNames.add(filterNodeTypeQName);
                    if (includeSubTypes)
                    {
                        Collection<QName> qnames = dictionaryService.getSubTypes(filterNodeTypeQName, true);
                        searchTypeQNames.addAll(qnames);
                    }
                }
                else
                {
                    throw new InvalidParameterException("Filter nodeType: " + nodeTypeQNameStr + " is invalid for this endpoint");
                }
            }
        }
        else
        {
            includeRecords = true;
            includeUnfiledRecordFolders = true;
            includeSubTypes = true;
        }

        if (includeUnfiledRecordFolders)
        {
            searchTypeQNames.add(RecordsManagementModel.TYPE_UNFILED_RECORD_FOLDER);
        }
        if (includeRecords)
        {

            if (includeSubTypes)
            {
                Collection<QName> qnames = dictionaryService.getSubTypes(ContentModel.TYPE_CONTENT, true);
                searchTypeQNames.addAll(qnames);
            }
            else
            {
                searchTypeQNames.add(ContentModel.TYPE_CONTENT);
                searchTypeQNames.add(RecordsManagementModel.TYPE_NON_ELECTRONIC_DOCUMENT);
            }
        }
        return searchTypeQNames;
    }

    /**
     * Helper method to build search types for categories endpoint
     * @param parameters
     * @param listRecordCategoryChildrenEqualsQueryProperties
     * @return
     */
    public Set<QName> buildSearchTypesCategoriesEndpoint(Parameters parameters, Set<String> listRecordCategoryChildrenEqualsQueryProperties)
    {
        Set<QName> searchTypeQNames = new HashSet<>();

        Query q = parameters.getQuery();

        boolean includeRecordFolders = false;
        boolean includeRecordCategories = false;

        if (q != null)
        {
            // filtering via "where" clause
            MapBasedQueryWalker propertyWalker = new MapBasedQueryWalker(listRecordCategoryChildrenEqualsQueryProperties, null);
            QueryHelper.walk(q, propertyWalker);

            Boolean isRecordFolder = propertyWalker.getProperty(RecordCategoryChild.PARAM_IS_RECORD_FOLDER,
                    WhereClauseParser.EQUALS, Boolean.class);
            Boolean isRecordCategory = propertyWalker.getProperty(RecordCategoryChild.PARAM_IS_RECORD_CATEGORY, WhereClauseParser.EQUALS, Boolean.class);

            if ((isRecordFolder != null && isRecordFolder.booleanValue()) || (isRecordCategory != null && !isRecordCategory.booleanValue()))
            {
                includeRecordFolders = true;
            }
            else if ((isRecordFolder != null && !isRecordFolder.booleanValue()) || (isRecordCategory != null && isRecordCategory.booleanValue()))
            {
                includeRecordCategories = true;
            }

            String nodeTypeQNameStr = propertyWalker.getProperty(RecordCategoryChild.PARAM_NODE_TYPE, WhereClauseParser.EQUALS, String.class);
            QName filterNodeTypeQName;
            if (nodeTypeQNameStr != null)
            {
                if ((isRecordFolder != null) || (isRecordCategory != null))
                {
                    throw new InvalidArgumentException("Invalid filter - nodeType and isRecordFolder/isRecordCategory are mutually exclusive");
                }

                Pair<QName, Boolean> pair = parseNodeTypeFilter(nodeTypeQNameStr);
                filterNodeTypeQName = pair.getFirst();
                if (filterNodeTypeQName.equals(RecordsManagementModel.TYPE_RECORD_FOLDER))
                {
                    includeRecordFolders = true;

                }
                else if (filterNodeTypeQName.equals(RecordsManagementModel.TYPE_RECORD_CATEGORY))
                {
                    includeRecordCategories = true;
                }
                else
                {
                    throw new InvalidParameterException("Filter nodeType: " + nodeTypeQNameStr + " is invalid for this endpoint");
                }
            }
        }
        else
        {
            includeRecordCategories = true;
            includeRecordFolders = true;
        }

        if (includeRecordFolders)
        {
            searchTypeQNames.add(RecordsManagementModel.TYPE_RECORD_FOLDER);
        }
        if (includeRecordCategories)
        {
            searchTypeQNames.add(RecordsManagementModel.TYPE_RECORD_CATEGORY);
        }
        return searchTypeQNames;
    }

    /**
     * Helper method to build search types for transfer containers endpoint
     * @return
     */
    public Set<QName> buildSearchTypesForTransferContainersEndpoint()
    {
        Set<QName> searchTypeQNames = new HashSet<>();
        searchTypeQNames.add(RecordsManagementModel.TYPE_TRANSFER);
        return searchTypeQNames;
    }

    /**
     * Helper method to parse the nodeType filter
     * default nodeType filtering is without subTypes (unless nodeType value is suffixed with ' INCLUDESUBTYPES')
     * @param nodeTypeStr
     * @return
     */
    private Pair<QName, Boolean> parseNodeTypeFilter(String nodeTypeStr)
    {
        boolean filterIncludeSubTypes = false;

        int idx = nodeTypeStr.lastIndexOf(' ');
        if (idx > 0)
        {
            String suffix = nodeTypeStr.substring(idx);
            if (suffix.equalsIgnoreCase(" " + RMNode.PARAM_INCLUDE_SUBTYPES))
            {
                filterIncludeSubTypes = true;
                nodeTypeStr = nodeTypeStr.substring(0, idx);
            }
        }

        QName filterNodeTypeQName = nodes.createQName(nodeTypeStr);
        if (dictionaryService.getType(filterNodeTypeQName) == null)
        {
            throw new InvalidParameterException("Filter nodeType: " + nodeTypeStr + " is invalid");
        }

        return new Pair<>(filterNodeTypeQName, filterIncludeSubTypes);
    }
}
