/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.node.integrity;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;

/**
 * Base class for integrity events.  It provides basic support for checking
 * model integrity.
 * 
 * @author Derek Hulley
 */
public abstract class AbstractIntegrityEvent implements IntegrityEvent
{
    protected final NodeService nodeService;
    protected final DictionaryService dictionaryService;

    /** the potential problem traces */
    private List<StackTraceElement[]> traces;
    /** support for derived classes */
    private final NodeRef nodeRef;
    /** support for derived classes */
    private final QName typeQName;
    /** support for derived classes */
    private final QName qname;
    
    /** cached hashcode as the members are all final */
    private int hashCode = 0;

    /**
     * Constructor with helper values for storage
     */
    protected AbstractIntegrityEvent(
            NodeService nodeService,
            DictionaryService dictionaryService,
            NodeRef nodeRef,
            QName typeQName,
            QName qname)
    {
        this.nodeService = nodeService;
        this.dictionaryService = dictionaryService;
        this.traces = new ArrayList<StackTraceElement[]>(0);
        
        this.nodeRef = nodeRef;
        this.typeQName = typeQName;
        this.qname = qname;
    }

    @Override
    public int hashCode()
    {
        if (hashCode == 0)
        {
            hashCode = 
                0
                + 1 * (nodeRef == null ? 0 : nodeRef.hashCode())
                - 17* (typeQName == null ? 0 : typeQName.hashCode())
                + 17* (qname == null ? 0 : qname.hashCode());
        }
        return hashCode;
    }
    
    /**
     * Compares based on the class of this instance and the incoming instance, before
     * comparing based on all the internal data.  If derived classes store additional
     * data for their functionality, then they should override this.
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
            return false;
        else if (this == obj)
            return true;
        else if (this.getClass() != obj.getClass())
            return false;
        // we can safely cast
        AbstractIntegrityEvent that = (AbstractIntegrityEvent) obj;
        return
                EqualsHelper.nullSafeEquals(this.nodeRef, that.nodeRef) &&
                EqualsHelper.nullSafeEquals(this.typeQName, that.typeQName) &&
                EqualsHelper.nullSafeEquals(this.qname, that.qname);
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(56);
        sb.append("IntegrityEvent")
          .append("[ name=").append(getClass().getName());
        if (nodeRef != null)
            sb.append(", nodeRef=").append(nodeRef);
        if (typeQName != null)
            sb.append(", typeQName=").append(typeQName);
        if (qname != null)
            sb.append(", qname=").append(qname);
        sb.append("]");
        // done
        return sb.toString();
    }
    
    /**
     * Gets the node type if the node exists
     * 
     * @param nodeRef NodeRef
     * @return Returns the node's type or null if the node no longer exists
     */
    protected QName getNodeType(NodeRef nodeRef)
    {
        try
        {
            return nodeService.getType(nodeRef);
        }
        catch (InvalidNodeRefException e)
        {
            // node has disappeared
            return null;
        }
    }
    
    /**
     * @return Returns the traces (if present) that caused the creation of this event
     */
    public List<StackTraceElement[]> getTraces()
    {
        return traces;
    }

    public void addTrace(StackTraceElement[] trace)
    {
        traces.add(trace);
    }

    protected NodeRef getNodeRef()
    {
        return nodeRef;
    }

    protected QName getTypeQName()
    {
        return typeQName;
    }

    protected QName getQName()
    {
        return qname;
    }
    
    /**
     * Gets the association definition from the dictionary.  If the source node type is
     * provided then the association particular to the subtype is attempted.
     * 
     * @param eventResults results to add a violation message to
     * @param assocTypeQName the type of the association
     * @return Returns the association definition, or null if not found
     */
    protected AssociationDefinition getAssocDef(List<IntegrityRecord> eventResults, QName assocTypeQName)
    {
        return dictionaryService.getAssociation(assocTypeQName);
    }
    
    protected String getMultiplicityString(boolean mandatory, boolean allowMany)
    {
        StringBuilder sb = new StringBuilder(4);
        sb.append(mandatory ? "1" : "0");
        sb.append("..");
        sb.append(allowMany ? "*" : "1");
        return sb.toString();
    }
}
