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
package org.alfresco.repo.node.integrity;

import java.util.List;

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
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
 * Event raised to check the target multiplicity for an association type
 * from the given node.
 * <p>
 * Checks are ignored is the target node doesn't exist.
 * 
 * @author Derek Hulley
 */
public class AssocTargetMultiplicityIntegrityEvent extends AbstractIntegrityEvent
{
    private static Log logger = LogFactory.getLog(AssocTargetMultiplicityIntegrityEvent.class);
    
    /** true if the assoc type may not be valid, e.g. during association deletions */
    private boolean isDelete;
    
    public AssocTargetMultiplicityIntegrityEvent(
            NodeService nodeService,
            DictionaryService dictionaryService,
            NodeRef sourceNodeRef,
            QName assocTypeQName,
            boolean isDelete)
    {
        super(nodeService, dictionaryService, sourceNodeRef, assocTypeQName, null);
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
        AssocTargetMultiplicityIntegrityEvent that = (AssocTargetMultiplicityIntegrityEvent) obj;
        return this.isDelete == that.isDelete;
    }
    
    public void checkIntegrity(List<IntegrityRecord> eventResults)
    {
        QName assocTypeQName = getTypeQName();
        NodeRef sourceNodeRef = getNodeRef();
        // event is irrelevant if the node is gone
        QName sourceNodeTypeQName = getNodeType(sourceNodeRef);
        if (sourceNodeTypeQName == null)
        {
            // target or target is missing
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
                        "   Source Node Type: " + sourceNodeTypeQName + "\n" +
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
        checkTargetMultiplicity(eventResults, assocDef, assocTypeQName, sourceNodeRef);
    }

    /**
     * Checks that the target multiplicity has not been violated for the
     * source of the association.
     */
    protected void checkTargetMultiplicity(
            List<IntegrityRecord> eventResults,
            AssociationDefinition assocDef,
            QName assocTypeQName,
            NodeRef sourceNodeRef)
    {
        // get the source multiplicity
        boolean mandatory = assocDef.isTargetMandatory();
        boolean allowMany = assocDef.isTargetMany();
        // do we need to check
        if (!mandatory && allowMany)
        {
            // it is not mandatory and it allows many on both sides of the assoc
            return;
        }
        int actualSize = 0;
        if (assocDef.isChild())
        {
            // check the child assocs present
            List<ChildAssociationRef> childAssocRefs = nodeService.getChildAssocs(
                    sourceNodeRef,
                    assocTypeQName,
                    RegexQNamePattern.MATCH_ALL);
            actualSize = childAssocRefs.size();
        }
        else
        {
            // check the target assocs present
            List<AssociationRef> targetAssocRefs = nodeService.getTargetAssocs(sourceNodeRef, assocTypeQName);
            actualSize = targetAssocRefs.size();
        }
        if ((mandatory && actualSize == 0) || (!allowMany && actualSize > 1))
        {
            String childOrTargetStr = (assocDef.isChild() ? "child" : "target");
            IntegrityRecord result = new IntegrityRecord(
                    "The association " + childOrTargetStr + " multiplicity has been violated: \n" +
                    "   Association: " + assocDef + "\n" +
                    "   Required " + childOrTargetStr + " Multiplicity: " + getMultiplicityString(mandatory, allowMany) + "\n" +
                    "   Actual " + childOrTargetStr + " Multiplicity: " + actualSize);
            eventResults.add(result);
        }
    }
}
