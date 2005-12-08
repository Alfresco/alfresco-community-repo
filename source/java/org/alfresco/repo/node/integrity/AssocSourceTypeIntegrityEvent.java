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
import java.util.Set;

import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Event to check the source type of an association
 * <p>
 * Checks are ignored if the source node has been deleted.
 * 
 * @author Derek Hulley
 */
public class AssocSourceTypeIntegrityEvent extends AbstractIntegrityEvent
{
    private static Log logger = LogFactory.getLog(AssocSourceTypeIntegrityEvent.class);
    
    public AssocSourceTypeIntegrityEvent(
            NodeService nodeService,
            DictionaryService dictionaryService,
            NodeRef sourceNodeRef,
            QName assocTypeQName)
    {
        super(nodeService, dictionaryService, sourceNodeRef, assocTypeQName, null);
    }
    
    public void checkIntegrity(List<IntegrityRecord> eventResults)
    {
        QName assocTypeQName = getTypeQName();
        NodeRef sourceNodeRef = getNodeRef();
        // if the node is gone then the check is irrelevant
        QName sourceNodeTypeQName = getNodeType(sourceNodeRef);
        if (sourceNodeTypeQName == null)
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
            IntegrityRecord result = new IntegrityRecord(
                    "Association type does not exist: \n" +
                    "   Source Node Type: " + sourceNodeTypeQName + "\n" +
                    "   Association Type: " + assocTypeQName);
            eventResults.add(result);
            return;
        }
        
        // perform required checks
        checkSourceType(eventResults, assocDef, sourceNodeRef, sourceNodeTypeQName);
    }
    
    /**
     * Checks that the source node type is valid for the association. 
     */
    protected void checkSourceType(
            List<IntegrityRecord> eventResults,
            AssociationDefinition assocDef,
            NodeRef sourceNodeRef,
            QName sourceNodeTypeQName)
    {
        // check the association source type
        ClassDefinition sourceDef = assocDef.getSourceClass();
        if (sourceDef instanceof TypeDefinition)
        {
            // the node type must be a match
            if (!dictionaryService.isSubClass(sourceNodeTypeQName, sourceDef.getName()))
            {
                IntegrityRecord result = new IntegrityRecord(
                        "The association source type is incorrect: \n" +
                        "   Association: " + assocDef + "\n" +
                        "   Required Source Type: " + sourceDef.getName() + "\n" +
                        "   Actual Source Type: " + sourceNodeTypeQName);
                eventResults.add(result);
            }
        }
        else if (sourceDef instanceof AspectDefinition)
        {
            // the source must have a relevant aspect
            Set<QName> sourceAspects = nodeService.getAspects(sourceNodeRef);
            boolean found = false;
            for (QName sourceAspectTypeQName : sourceAspects)
            {
                if (dictionaryService.isSubClass(sourceAspectTypeQName, sourceDef.getName()))
                {
                    found = true;
                    break;
                }
            }
            if (!found)
            {
                IntegrityRecord result = new IntegrityRecord(
                        "The association source is missing the aspect required for this association: \n" +
                        "   Association: " + assocDef + "\n" +
                        "   Required Source Aspect: " + sourceDef.getName() + "\n" +
                        "   Actual Source Aspects: " + sourceAspects);
                eventResults.add(result);
            }
        }
        else
        {
            IntegrityRecord result = new IntegrityRecord(
                    "Unknown ClassDefinition subclass on the source definition: \n" +
                    "   Association: " + assocDef + "\n" +
                    "   Source Definition: " + sourceDef.getName());
            eventResults.add(result);
        }
    }
}
