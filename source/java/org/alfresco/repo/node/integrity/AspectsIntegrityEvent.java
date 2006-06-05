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
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Event raised to check nodes' aspects
 * 
 * @author Derek Hulley
 */
public class AspectsIntegrityEvent extends AbstractIntegrityEvent
{
    private static Log logger = LogFactory.getLog(AspectsIntegrityEvent.class);
    
    protected AspectsIntegrityEvent(
            NodeService nodeService,
            DictionaryService dictionaryService,
            NodeRef nodeRef)
    {
        super(nodeService, dictionaryService, nodeRef, null, null);
    }
    
    public void checkIntegrity(List<IntegrityRecord> eventResults)
    {
        NodeRef nodeRef = getNodeRef();
        if (!nodeService.exists(nodeRef))
        {
            // node has gone
            if (logger.isDebugEnabled())
            {
                logger.debug("Event ignored - node gone: " + this);
            }
            eventResults.clear();
            return;
        }
        else
        {
            checkMandatoryAspects(getNodeRef(), eventResults);
        }
    }

    /**
     * Checks that the node has the required mandatory aspects applied
     */
    private void checkMandatoryAspects(NodeRef nodeRef, List<IntegrityRecord> eventResults)
    {
        Set<QName> aspects = nodeService.getAspects(nodeRef);
        
        // get the node type
        QName nodeTypeQName = nodeService.getType(nodeRef);
        // get the aspects that should exist
        TypeDefinition typeDef = dictionaryService.getType(nodeTypeQName);
        List<AspectDefinition> mandatoryAspectDefs = typeDef.getDefaultAspects();
        
        // check
        for (AspectDefinition aspect : mandatoryAspectDefs)
        {
            if (aspects.contains(aspect.getName()))
            {
                // it's fine
                continue;
            }
            IntegrityRecord result = new IntegrityRecord(
                    "Mandatory aspect not set: \n" +
                    "   Node: " + nodeRef + "\n" +
                    "   Type: " + nodeTypeQName + "\n" +
                    "   Aspect: " + aspect.getName());
            eventResults.add(result);
            // next one
            continue;
        }
        // done
    }
}
