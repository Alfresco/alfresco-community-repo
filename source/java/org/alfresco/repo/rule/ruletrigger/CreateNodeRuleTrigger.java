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
package org.alfresco.repo.rule.ruletrigger;

import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * We use this specialised trigger for create node beaucse of a problem with the CIFS integration.
 * <p>
 * The create node trigger will only be fired if the object is NOT a sub-type of content.
 * 
 * @author Roy Wetherall
 */
public class CreateNodeRuleTrigger extends SingleChildAssocRefPolicyRuleTrigger
{
    /**
     * The logger
     */
    private static Log logger = LogFactory.getLog(CreateNodeRuleTrigger.class);
    
    DictionaryService dictionaryService;
    
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    public void policyBehaviour(ChildAssociationRef childAssocRef)
    {
        // Only fire the rule if the node is question has no potential to contain content
        // TODO we need to find a better way to do this .. how can this be resolved in CIFS??
        boolean triggerRule = false;
        QName type = this.nodeService.getType(childAssocRef.getChildRef());
        ClassDefinition classDefinition = this.dictionaryService.getClass(type);
        if (classDefinition != null)
        {
            for (PropertyDefinition propertyDefinition : classDefinition.getProperties().values())
            {
                if (propertyDefinition.getDataType().getName().equals(DataTypeDefinition.CONTENT) == true)
                {
                    triggerRule = true;
                    break;
                }
            }
        }
        
        if (triggerRule == false)
        {
            if (logger.isDebugEnabled() == true)
            {
                logger.debug(
                        "Create node rule trigger fired for parent node " + 
                        this.nodeService.getType(childAssocRef.getParentRef()).toString() + " " + childAssocRef.getParentRef() + 
                        " and child node " +
                        this.nodeService.getType(childAssocRef.getChildRef()).toString() + " " + childAssocRef.getChildRef());
            }
            
            triggerRules(childAssocRef.getParentRef(), childAssocRef.getChildRef());
        }
        
        // Reguadless of whether the rule is triggered, mark this transaction as having created this node
        AlfrescoTransactionSupport.bindResource(childAssocRef.getChildRef().toString(), childAssocRef.getChildRef().toString());         
    }
}
