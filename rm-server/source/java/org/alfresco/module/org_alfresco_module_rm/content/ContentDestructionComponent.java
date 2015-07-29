/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.content;

import java.util.Collection;

import org.alfresco.module.org_alfresco_module_rm.classification.ContentClassificationService;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.util.AuthenticationUtil;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * Content destruction component.
 * <p>
 * Listens for the destruction of sensitive nodes (classified content and records) and schedules
 * all their content for immediate destruction.
 * <p>
 * If enabled, the content is also cleansed before destruction.
 * 
 * @author Roy Wetherall
 * @since 3.0.a
 */
@BehaviourBean
public class ContentDestructionComponent implements NodeServicePolicies.BeforeDeleteNodePolicy
{
    /** authentication utils */
    private AuthenticationUtil authenticationUtil;
    
    /** content classification service */
    private ContentClassificationService contentClassificationService;
    
    /** record service */
    private RecordService recordService;
    
    /** eager content store cleaner */
    private EagerContentStoreCleaner eagerContentStoreCleaner;
    
    /** dictionary service */
    private DictionaryService dictionaryService;
    
    /** node service */
    private NodeService nodeService;
    
    /** indicates whether cleansing is enabled or not */
    private boolean cleansingEnabled = false;
    
    /**
     * @param authenticationUtil    authentication utils
     */
    public void setAuthenticationUtil(AuthenticationUtil authenticationUtil)
    {
        this.authenticationUtil = authenticationUtil;
    }
    
    /**
     * @param contentClassificationService  content classification service
     */
    public void setContentClassificationService(ContentClassificationService contentClassificationService)
    {
        this.contentClassificationService = contentClassificationService;
    }
    
    /**
     * @param recordService record service
     */
    public void setRecordService(RecordService recordService)
    {
        this.recordService = recordService;
    }
    
    /**
     * @param eagerContentStoreCleaner  eager content store cleaner
     */
    public void setEagerContentStoreCleaner(EagerContentStoreCleaner eagerContentStoreCleaner)
    {
        this.eagerContentStoreCleaner = eagerContentStoreCleaner;
    }
    
    /**
     * @param dictionaryService dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * @param cleansingEnabled  true if cleansing enabled, false otherwise
     */
    public void setCleansingEnabled(boolean cleansingEnabled)
    {
        this.cleansingEnabled = cleansingEnabled;
    }
    
    /**
     * @return  true if cleansing is enabled, false otherwise
     */
    public boolean isCleansingEnabled()
    {
        return cleansingEnabled;
    }
    
    /**
     * System behaviour implementation that listens for sensitive nodes 
     * and schedules them for immediate destruction.
     * <p>
     * Note that the content destruction and cleansing takes place on transaction
     * commit.  If the transaction is rolled back after this behaviour is encountered
     * then the content will not be destroyed or cleansed.
     * 
     * @param nodeRef node reference about to be deleted
     */
    @Override
    @Behaviour
    (
       isService = true,
       kind = BehaviourKind.CLASS
    )
    public void beforeDeleteNode(final NodeRef nodeRef)
    {
        authenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            public Void doWork() throws Exception
            {
                // if enable and content is classified or a record
                if (contentClassificationService.isClassified(nodeRef) ||
                    recordService.isRecord(nodeRef))
                {
                    // then register all content for destruction
                    registerAllContentForDestruction(nodeRef);
                }                
                return null;
            }
        });
    }
    
    /**
     * Registers all content on the given node for destruction.
     * 
     * @param nodeRef   node reference
     */
    private void registerAllContentForDestruction(NodeRef nodeRef)
    {
        // get node type
        QName nodeType = nodeService.getType(nodeRef);
        
        // get type properties
        Collection<QName> nodeProperties = dictionaryService.getAllProperties(nodeType);
        for (QName nodeProperty : nodeProperties)
        {
            // get property definition
            PropertyDefinition propertyDefinition = dictionaryService.getProperty(nodeProperty);
            
            // if content property
            if (propertyDefinition != null && 
                DataTypeDefinition.CONTENT.equals(propertyDefinition.getDataType().getName()))
            {
                // get content data
                ContentData dataContent = (ContentData)nodeService.getProperty(nodeRef, nodeProperty);
                
                // if enabled cleanse content 
                if (isCleansingEnabled())
                {
                    // register for cleanse then immediate destruction
                    eagerContentStoreCleaner.registerOrphanedContentUrlForCleansing(dataContent.getContentUrl());
                }
                else
                {
                    // register for immediate destruction
                    eagerContentStoreCleaner.registerOrphanedContentUrl(dataContent.getContentUrl(), true);
                }
            }
        }         
    }
}
