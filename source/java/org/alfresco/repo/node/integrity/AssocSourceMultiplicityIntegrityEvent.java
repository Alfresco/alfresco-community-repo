/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
            String parentOrSourceStr = (assocDef.isChild() ? "parent" : "source");
            IntegrityRecord result = new IntegrityRecord(
                    "The association " + parentOrSourceStr + " multiplicity has been violated: \n" +
                    "   Association: " + assocDef + "\n" +
                    "   Required " + parentOrSourceStr + " Multiplicity: " + getMultiplicityString(mandatory, allowMany) + "\n" +
                    "   Actual " + parentOrSourceStr + " Multiplicity: " + actualSize);
            eventResults.add(result);
        }
    }
}
