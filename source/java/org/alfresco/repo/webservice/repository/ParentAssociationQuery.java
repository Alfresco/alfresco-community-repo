/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.repo.webservice.repository;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.webservice.AbstractQuery;
import org.alfresco.repo.webservice.Utils;
import org.alfresco.repo.webservice.types.NamedValue;
import org.alfresco.repo.webservice.types.Reference;
import org.alfresco.repo.webservice.types.ResultSet;
import org.alfresco.repo.webservice.types.ResultSetRow;
import org.alfresco.repo.webservice.types.ResultSetRowNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * A query to retrieve all parent associations on a node.
 * 
 * @author Derek Hulley
 * @since 2.1
 */
public class ParentAssociationQuery extends AbstractQuery<ResultSet>
{
    private static final long serialVersionUID = -4157476722256947274L;

    private Reference node;

    /**
     * @param node
     *            The node to query against
     */
    public ParentAssociationQuery(Reference node)
    {
        this.node = node;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append("ParentAssociationQuery")
          .append("[ node=").append(node.getUuid())
          .append("]");
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    public ResultSet execute(ServiceRegistry serviceRegistry)
    {
        SearchService searchService = serviceRegistry.getSearchService();
        NodeService nodeService = serviceRegistry.getNodeService();
        DictionaryService dictionaryService = serviceRegistry.getDictionaryService();
        NamespaceService namespaceService = serviceRegistry.getNamespaceService();

        // create the node ref and get the parent from the repository
        NodeRef nodeRef = Utils.convertToNodeRef(node, nodeService, searchService, namespaceService);
        List<ChildAssociationRef> assocRefs = nodeService.getParentAssocs(nodeRef);

        int totalRows = assocRefs.size();

        ResultSet results = new ResultSet();
        ResultSetRow[] rows = new ResultSetRow[totalRows];

        int index = 0;
        for (ChildAssociationRef assocRef : assocRefs)
        {
            NodeRef parentNodeRef = assocRef.getParentRef();
            ResultSetRowNode rowNode = createResultSetRowNode(parentNodeRef, nodeService);
            
            // create columns for all the properties of the node
            // get the data for the row and build up the columns structure
            Map<QName, Serializable> props = nodeService.getProperties(parentNodeRef);
            NamedValue[] columns = new NamedValue[props.size()+5];
            int col = 0;
            for (QName propName : props.keySet())
            {
               columns[col] = Utils.createNamedValue(dictionaryService, propName, props.get(propName));
               col++;
            }
            
            // Now add the system columns containing the association details
            columns[col] = new NamedValue(SYS_COL_ASSOC_TYPE, Boolean.FALSE, assocRef.getTypeQName().toString(), null);
            col++;
            columns[col] = new NamedValue(SYS_COL_ASSOC_NAME, Boolean.FALSE, assocRef.getQName().toString(), null);
            col++;
            columns[col] = new NamedValue(SYS_COL_IS_PRIMARY, Boolean.FALSE, Boolean.toString(assocRef.isPrimary()), null);
            col++;
            columns[col] = new NamedValue(SYS_COL_NTH_SIBLING, Boolean.FALSE, Integer.toString(assocRef.getNthSibling()), null);
            
            // Add one more column for the node's path
            col++;
            columns[col] = Utils.createNamedValue(
                    dictionaryService,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "path"),
                    nodeService.getPath(parentNodeRef).toString());
            
            ResultSetRow row = new ResultSetRow();
            row.setRowIndex(index);
            row.setNode(rowNode);
            row.setColumns(columns);

            // add the row to the overall results
            rows[index] = row;
            index++;
        }

        // add the rows to the result set and set the total row count
        results.setRows(rows);
        results.setTotalRowCount(totalRows);

        return results;
    }
}