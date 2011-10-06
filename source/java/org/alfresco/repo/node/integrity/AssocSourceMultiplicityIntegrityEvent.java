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
package org.alfresco.repo.node.integrity;

import java.util.List;

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Event raised to check the source multiplicity for an association type
 * from the given node.
 * <p>
 * Checks are ignored is the target node doesn't exist.
 * 
 * @author Derek Hulley
 */
public class AssocSourceMultiplicityIntegrityEvent extends AbstractIntegrityEvent
{
    private static Log logger = LogFactory.getLog(AssocSourceMultiplicityIntegrityEvent.class);
    
    /** true if the assoc type may not be valid, e.g. during association deletions */
    private boolean isDelete;
    
    public AssocSourceMultiplicityIntegrityEvent(
            NodeService nodeService,
            DictionaryService dictionaryService,
            NodeRef targetNodeRef,
            QName assocTypeQName,
            boolean isDelete)
    {
        super(nodeService, dictionaryService, targetNodeRef, assocTypeQName, null);
        this.isDelete = isDelete;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (!super.equals(obj))
        {
            return false;
        }
        // so far, so good
        AssocSourceMultiplicityIntegrityEvent that = (AssocSourceMultiplicityIntegrityEvent) obj;
        return this.isDelete == that.isDelete;
    }
    
    public void checkIntegrity(List<IntegrityRecord> eventResults)
    {
        QName assocTypeQName = getTypeQName();
        NodeRef targetNodeRef = getNodeRef();
        // event is irrelevant if the node is gone
        QName targetNodeTypeQName = getNodeType(targetNodeRef);
        if (targetNodeTypeQName == null)
        {
            // target or source is missing
            if (logger.isDebugEnabled())
            {
                logger.debug("Ignoring integrity check - node gone: \n" +
                        "   event: " + this);
            }
            return;
        }
        
        // get the association def
        AssociationDefinition assocDef = getAssocDef(eventResults, assocTypeQName);
        // the association definition must exist
        if (assocDef == null)
        {
            if (!isDelete)                         // strict about the type
            {
                IntegrityRecord result = new IntegrityRecord(
                        "Association type does not exist: \n" +
                        "   Target Node: " + targetNodeRef + "\n" +
                        "   Target Node Type: " + targetNodeTypeQName + "\n" +
                        "   Association Type: " + assocTypeQName);
                eventResults.add(result);
                return;
            }
            else                                    // not strict about the type
            {
                return;
            }
        }
        
        // perform required checks
        checkSourceMultiplicity(eventResults, assocDef, assocTypeQName, targetNodeRef);
    }
    
    /**
     * Checks that the source multiplicity has not been violated for the
     * target of the association.
     */
    protected void checkSourceMultiplicity(
            List<IntegrityRecord> eventResults,
            AssociationDefinition assocDef,
            QName assocTypeQName,
            NodeRef targetNodeRef)
    {
        // get the source multiplicity
        boolean mandatory = assocDef.isSourceMandatory();
        boolean allowMany = assocDef.isSourceMany();
        // do we need to check
        if (!mandatory && allowMany)
        {
            // it is not mandatory and it allows many on both sides of the assoc
            return;
        }
        
        // check the target of the association if this is a delete
        if (isDelete)
        {
            // get the class the association is defined for and see if it is an aspect
            ClassDefinition classDef = assocDef.getTargetClass(); 
            if (classDef.isAspect())
            {
                // see if the target node has the aspect applied, if it does not
                // there's no need to check multiplicity
                if (!this.nodeService.hasAspect(targetNodeRef, classDef.getName()))
                {
                    return;
                }
            }
        }
        
        int actualSize = 0;
        if (assocDef.isChild())
        {
            // check the parent assocs present
            List<ChildAssociationRef> parentAssocRefs = nodeService.getParentAssocs(
                    targetNodeRef,
                    assocTypeQName,
                    RegexQNamePattern.MATCH_ALL);
            actualSize = parentAssocRefs.size();
        }
        else
        {
            // check the source assocs present
            List<AssociationRef> sourceAssocRefs = nodeService.getSourceAssocs(targetNodeRef, assocTypeQName);
            actualSize = sourceAssocRefs.size();
        }
        
        if ((mandatory && actualSize == 0) || (!allowMany && actualSize > 1))
        {
            if (actualSize == 0)
            {
                // ALF-9591: Integrity check: Association source multiplicity checking is incorrect
                //           At this point, there is no point worrying.  There are no more associations
                //           pointing *to* this node and therefore the checking of the source
                //           multiplicity (a feature of the source type/aspect) is not relevant
                return;
            }
            
            String parentOrSourceStr = (assocDef.isChild() ? "parent" : "source");
            IntegrityRecord result = new IntegrityRecord(
                    "The association " + parentOrSourceStr + " multiplicity has been violated: \n" +
                    "   Target Node: " + targetNodeRef + "\n" +
                    "   Association: " + assocDef + "\n" +
                    "   Required " + parentOrSourceStr + " Multiplicity: " + getMultiplicityString(mandatory, allowMany) + "\n" +
                    "   Actual " + parentOrSourceStr + " Multiplicity: " + actualSize);
            eventResults.add(result);
        }
    }
}
