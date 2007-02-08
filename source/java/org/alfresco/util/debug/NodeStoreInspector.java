/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.util.debug;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;

/**
 * Debug class that has methods to inspect the contents of a node store.
 * 
 * @author Roy Wetherall
 */
public class NodeStoreInspector
{
    /**
     * Dumps the contents of a store to a string.
     * 
     * @param nodeService   the node service
     * @param storeRef      the store reference
     * @return              string containing textual representation of the contents of the store
     */
    public static String dumpNodeStore(NodeService nodeService, StoreRef storeRef)
    {
        StringBuilder builder = new StringBuilder();
        
        if (nodeService.exists(storeRef) == true)
        {
            NodeRef rootNode = nodeService.getRootNode(storeRef);            
            builder.append(outputNode(0, nodeService, rootNode));            
        }
        else
        {
            builder.
                append("The store ").
                append(storeRef.toString()).
                append(" does not exist.");
        }
        
        return builder.toString();
    }

    /**
     * Dumps the contents of a node 
     * 
     * @param nodeService
     * @param nodeRef
     * @return
     */
    public static String dumpNode(NodeService nodeService, NodeRef nodeRef)
    {
        StringBuilder builder = new StringBuilder();
        
        if (nodeService.exists(nodeRef) == true)
        {
            builder.append(outputNode(0, nodeService, nodeRef));            
        }
        else
        {
            builder.
                append("The node ").
                append(nodeRef.toString()).
                append(" does not exist.");
        }
        
        return builder.toString();
    }
    
    /**
     * Output the node 
     * 
     * @param iIndent
     * @param nodeService
     * @param nodeRef
     * @return
     */
    private static String outputNode(int iIndent, NodeService nodeService, NodeRef nodeRef)
    {
        StringBuilder builder = new StringBuilder();
        
		try
		{
	        QName nodeType = nodeService.getType(nodeRef);
	        builder.
	            append(getIndent(iIndent)).
	            append("node: ").
	            append(nodeRef.getId()).
	            append(" (").
	            append(nodeType.getLocalName());
			
			Collection<QName> aspects = nodeService.getAspects(nodeRef);
			for (QName aspect : aspects) 
			{
				builder.
					append(", ").
					append(aspect.getLocalName());
			}
			
	        builder.append(")\n");        
		
	        Map<QName, Serializable> props = nodeService.getProperties(nodeRef);
	        for (QName name : props.keySet())
	        {
				String valueAsString = "null";
				Serializable value = props.get(name);
				if (value != null)
				{
					valueAsString = value.toString();
				}
				
	            builder.
	                append(getIndent(iIndent+1)).
	                append("@").
	                append(name.getLocalName()).
	                append(" = ").
	                append(valueAsString).
	                append("\n");
	            
	        }

            Collection<ChildAssociationRef> childAssocRefs = nodeService.getChildAssocs(nodeRef);
            for (ChildAssociationRef childAssocRef : childAssocRefs)
            {
                builder.
                    append(getIndent(iIndent+1)).
                    append("-> ").
                    append(childAssocRef.getQName().toString()).
                    append(" (").
                    append(childAssocRef.getQName().toString()).
                    append(")\n");
                
                builder.append(outputNode(iIndent+2, nodeService, childAssocRef.getChildRef()));
            }

            Collection<AssociationRef> assocRefs = nodeService.getTargetAssocs(nodeRef, RegexQNamePattern.MATCH_ALL);
            for (AssociationRef assocRef : assocRefs)
            {
                builder.
                    append(getIndent(iIndent+1)).
                    append("-> associated to ").
                    append(assocRef.getTargetRef().getId()).
                    append("\n");
            }
		}
		catch (InvalidNodeRefException invalidNode)
		{
			invalidNode.printStackTrace();
		}
        
        return builder.toString();
    }
    
    /**
     * Get the indent
     * 
     * @param iIndent  the indent value
     * @return         the indent string
     */
    private static String getIndent(int iIndent)
    {
        StringBuilder builder = new StringBuilder(iIndent*3);
        for (int i = 0; i < iIndent; i++)
        {
            builder.append("   ");            
        }
        return builder.toString();
    }

}
