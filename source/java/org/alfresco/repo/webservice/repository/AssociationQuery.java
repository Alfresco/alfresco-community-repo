/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.webservice.repository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.webservice.AbstractQuery;
import org.alfresco.repo.webservice.Utils;
import org.alfresco.repo.webservice.types.NamedValue;
import org.alfresco.repo.webservice.types.Reference;
import org.alfresco.repo.webservice.types.ResultSet;
import org.alfresco.repo.webservice.types.ResultSetRow;
import org.alfresco.repo.webservice.types.ResultSetRowNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.alfresco.service.namespace.RegexQNamePattern;

/**
 * A query to retrieve normal node associations.
 * 
 * @author Derek Hulley
 * @since 2.1
 */
public class AssociationQuery extends AbstractQuery<ResultSet>
{
    private static final long serialVersionUID = -672399618512462040L;

    private Reference node;
    private Association association;
    
    private final static String SOURCE = "source";

    /**
     * @param node
     *            The node to query against
     * @param association
     *            The association type to query or <tt>null</tt> to query all
     */
    public AssociationQuery(Reference node, Association association)
    {
        this.node = node;
        this.association = association;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append("AssociationQuery")
          .append("[ node=").append(node.getUuid())
          .append(", association=").append(association)
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

        // create the node ref and get the children from the repository
        NodeRef nodeRef = Utils.convertToNodeRef(node, nodeService, searchService, namespaceService);
        List<AssociationRef> assocRefs = null;
        if (this.association == null)
        {
        	assocRefs = nodeService.getTargetAssocs(nodeRef, RegexQNamePattern.MATCH_ALL);
        }
        else
        {
            QNamePattern name = RegexQNamePattern.MATCH_ALL;
            String assocType = this.association.getAssociationType();
            if (assocType != null)
            {
                name = QName.createQName(assocType);
            }
            if (SOURCE.equals(this.association.getDirection()) == true)
            {
                assocRefs = nodeService.getSourceAssocs(nodeRef, name);
            }
            else
            {
                assocRefs = nodeService.getTargetAssocs(nodeRef, name);
            }
        }

        ResultSet results = new ResultSet();
        List<ResultSetRow> rows = new ArrayList<ResultSetRow>(assocRefs.size());

        int index = 0;
        NodeRef childNodeRef = null;
        for (AssociationRef assocRef : assocRefs)
        {
        	if (SOURCE.equals(this.association.getDirection()) == true)
        	{
        		childNodeRef = assocRef.getSourceRef();
        	}
        	else
        	{
        		childNodeRef = assocRef.getTargetRef();
        	}
            
            Map<QName, Serializable> props = null;
            try
            {
                props = nodeService.getProperties(childNodeRef);
            }
            catch (AccessDeniedException e)
            {
                // user has no access to associated node
            }
            
            if (props != null)
            {
                ResultSetRowNode rowNode = createResultSetRowNode(childNodeRef, nodeService);

                // create columns for all the properties of the node
                // get the data for the row and build up the columns structure                
                NamedValue[] columns = new NamedValue[props.size()+2];
                int col = 0;
                for (QName propName : props.keySet())
                {
                    columns[col] = Utils.createNamedValue(dictionaryService, propName, props.get(propName)); 
                    col++;
                }
            
                // Now add the system columns containing the association details
                columns[col] = new NamedValue(SYS_COL_ASSOC_TYPE, Boolean.FALSE, assocRef.getTypeQName().toString(), null);
            
                // Add one more column for the node's path
                col++;
                columns[col] = Utils.createNamedValue(
                        dictionaryService,
                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "path"),
                        nodeService.getPath(childNodeRef).toString());
            
                ResultSetRow row = new ResultSetRow();
                row.setRowIndex(index);
                row.setNode(rowNode);
                row.setColumns(columns);

                // add the row to the overall results
                rows.add(row);
                index++;
            }
        }

        // add the rows to the result set and set the total row count
        results.setRows(rows.toArray(new ResultSetRow[0]));
        results.setTotalRowCount(rows.size());

        return results;
    }
}