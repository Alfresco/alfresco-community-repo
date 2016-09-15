/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.service.cmr.repository;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.api.AlfrescoPublicApi;
import org.apache.commons.logging.Log;

/**
 * Reference to a node
 * 
 * @author Derek Hulley
 */
@AlfrescoPublicApi
public final class NodeRef implements EntityRef, Serializable
{
    // Let's force introspection of this class straight away so that we don't get contention when multiple threads try
    // to cache BeanInfo
    static
    {
        try
        {
            Introspector.getBeanInfo(NodeRef.class);
        }
        catch (IntrospectionException e)
        {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static final long serialVersionUID = 3760844584074227768L;
    private static final String URI_FILLER = "/";
    private static final Pattern nodeRefPattern = Pattern.compile(".+://.+/.+");
    
    private final StoreRef storeRef;
    private final String id;

    /**
     * @see #NodeRef(StoreRef, String)
     * @see StoreRef#StoreRef(String, String)
     */
    public NodeRef(String protocol, String identifier, String id)
    {
        this(new StoreRef(protocol, identifier), id);
    }
    
    /**
     * Construct a Node Reference from a Store Reference and Node Id
     * 
     * @param storeRef store reference
     * @param id the manually assigned identifier of the node
     */
    public NodeRef(StoreRef storeRef, String id)
    {
        if (storeRef == null)
        {
            throw new IllegalArgumentException("Store reference may not be null");
        }
        if (id == null)
        {
            throw new IllegalArgumentException("Node id may not be null");
        }

        this.storeRef = storeRef;
        this.id = id;
    }

    /**
     * Construct a Node Reference from a string representation of a Node Reference.
     * <p>
     * The string representation of a Node Reference is as follows:
     * </p>
     * <pre><storeref>/<nodeId></pre>
     * 
     * @param nodeRef the string representation of a node ref
     */
    public NodeRef(String nodeRef)
    {
        int lastForwardSlash = nodeRef.lastIndexOf('/');
        if(lastForwardSlash == -1)
        {
            throw new MalformedNodeRefException("Invalid node ref - does not contain forward slash: " + nodeRef);
        }
        this.storeRef = new StoreRef(nodeRef.substring(0, lastForwardSlash));
        this.id = nodeRef.substring(lastForwardSlash+1);
    }

    @Override
    public String toString()
    {
        return storeRef.toString() + URI_FILLER + id;
    }

    /**
     * Override equals for this ref type
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj instanceof NodeRef)
        {
            NodeRef that = (NodeRef) obj;
            return (this.id.equals(that.id)
                    && this.storeRef.equals(that.storeRef));
        }
        else
        {
            return false;
        }
    }
    
    /**
     * Hashes on ID alone.  As the number of copies of a particular node will be minimal, this is acceptable
     */
    @Override
    public int hashCode()
    {
        return id.hashCode();
    }

    /**
     * @return The StoreRef part of this reference
     */
    public final StoreRef getStoreRef()
    {
        return storeRef;
    }

    /**
     * @return The Node Id part of this reference
     */
    public final String getId()
    {
        return id;
    }

    /**
     * Determine if passed string conforms to the pattern of a node reference
     * 
     * @param nodeRef  the node reference as a string
     * @return  true => it matches the pattern of a node reference
     */
    public static boolean isNodeRef(String nodeRef)
    {
    	Matcher matcher = nodeRefPattern.matcher(nodeRef);
    	return matcher.matches();
    }
    
    /**
     * Converts a {@link String} containing a comma-separated list of {@link NodeRef} Ids into NodeRefs.
     * @param values the {@link String} of {@link NodeRef} ids.
     * @return A {@link List} of {@link NodeRef NodeRefs}.
     */
    public static List<NodeRef> getNodeRefs(String values)
    {
        return getNodeRefs(values, null);
    }
    
    /**
     * Converts a {@link String} containing a comma-separated list of {@link NodeRef} Ids into NodeRefs.
     * If a <code>logger</code> is supplied then invalid ids are logged as warnings.
     * @param values the {@link String} of {@link NodeRef} ids.
     * @param logger Log
     * @return A {@link List} of {@link NodeRef NodeRefs}.
     */
    public static List<NodeRef> getNodeRefs(String values, Log logger)
    {
        if(values==null || values.length()==0)
            return Collections.emptyList();
        String[] nodeRefIds = values.split(",");
        List<NodeRef> nodeRefs = new ArrayList<NodeRef>(nodeRefIds.length);
        for (String nodeRefString : nodeRefIds)
        {
            String nodeRefId = nodeRefString.trim();
            if (NodeRef.isNodeRef(nodeRefId))
            {
                NodeRef nodeRef = new NodeRef(nodeRefId);
                nodeRefs.add(nodeRef);
            }
            else if (logger!=null)
            {
                logNodeRefError(nodeRefId, logger);
            }
        }
        return nodeRefs;
    }

    private static void logNodeRefError(String nodeRefId, Log logger)
    {
        if (logger.isWarnEnabled())
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Target Node: ").append(nodeRefId);
            msg.append(" is not a valid NodeRef and has been ignored.");
            logger.warn(msg.toString());
        }
    }
    
    /**
     * Helper class to convey the status of a <b>node</b>.
     * 
     * @author Derek Hulley
     */
    public static class Status
    {
        private final Long dbId;
        private final NodeRef nodeRef;
        private final String changeTxnId;
        private final Long dbTxnId;
        private final boolean deleted;
        
        public Status(Long dbId, NodeRef nodeRef, String changeTxnId, Long dbTxnId, boolean deleted)
        {
            this.dbId = dbId;
            this.nodeRef = nodeRef;
            this.changeTxnId = changeTxnId;
            this.dbTxnId = dbTxnId;
            this.deleted = deleted;
        }
        /**
         * Return the database ID for the node
         */
        public Long getDbId()
        {
            return dbId;
        }
        /**
         * @return Returns the NodeRef that to which this status applies
         */
        public NodeRef getNodeRef()
        {
            return nodeRef;
        }
        /**
         * @return Returns the ID of the last transaction to change the node
         * 
         * @deprecated  This will be removed when we have switched to SOLR tracking only
         */
        public String getChangeTxnId()
        {
            return changeTxnId;
        }
        /**
         * @return Returns the db ID of the last transaction to change the node
         */
        public Long getDbTxnId()
        {
            return dbTxnId;
        }
        /**
         * @return Returns true if the node has been deleted, otherwise false
         */
        public boolean isDeleted()
        {
            return deleted;
        }
        
        // debug display string
        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder(50);
            
            sb.append("Status[")
              .append("id=").append(dbId)
              .append("changeTxnId=").append(changeTxnId)
              .append(", dbTxnId=").append(dbTxnId)
              .append(", deleted=").append(deleted)
              .append("]");
            
            return sb.toString();
        }
    }
}
