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
package org.alfresco.repo.admin.patch.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.alfresco.email.server.AliasableAspect;
import org.alfresco.email.server.EmailServerModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Patch to duplicate the AliasableAspect into the attributes service.
 * 
 * Inbound email.
 * 
 * @author mrogers
 *
 */
public class AliasableAspectPatch extends AbstractPatch
{
    private static final String MSG_SUCCESS = "patch.emailAliasableAspect.result";
    
    private AttributeService attributeService;
    private NodeDAO nodeDAO;
    private PatchDAO patchDAO;
    private QNameDAO qnameDAO;
    private BehaviourFilter behaviourFilter;
    
    private final int batchThreads = 3;
    private final int batchSize = 40;
    private final long count = batchThreads * batchSize;
    
    private static Log logger = LogFactory.getLog(AliasableAspectPatch.class);
    

    @Override
    protected String applyInternal() throws Exception
    {                      
        BatchProcessWorkProvider<NodeRef> workProvider = new BatchProcessWorkProvider<NodeRef>()
        {
            final List<NodeRef> result = new ArrayList<NodeRef>();
            
            Long aspectQNameId = 0L;
            long maxNodeId = getPatchDAO().getMaxAdmNodeID();
            
            long minSearchNodeId = 1;
            long maxSearchNodeId = count;
            
            Pair<Long, QName> val = getQnameDAO().getQName(EmailServerModel.ASPECT_ALIASABLE );

            public int getTotalEstimatedWorkSize()
            {
                return result.size();
            }

            public Collection<NodeRef> getNextWork()
            {
                if(val != null)
                {
                    Long aspectQNameId = val.getFirst();
                
                    result.clear();
                
                    while (result.isEmpty() && minSearchNodeId < maxNodeId)
                    {                    
                        List<Long> nodeids = getPatchDAO().getNodesByAspectQNameId(aspectQNameId, minSearchNodeId, maxSearchNodeId);
                
                        for(Long nodeid : nodeids)
                        {
                            NodeRef.Status status = getNodeDAO().getNodeIdStatus(nodeid);
                            if(!status.isDeleted())
                            {
                                result.add(status.getNodeRef());
                            }
                        }
                        minSearchNodeId = minSearchNodeId + count;
                        maxSearchNodeId = maxSearchNodeId + count;
                    }
                }

                return result;
            }
        };

        RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
        // Configure the helper to run in read-only mode
        // MNT-10764
        txnHelper.setForceWritable(true);
        
        BatchProcessor<NodeRef> batchProcessor = new BatchProcessor<NodeRef>(
                "AliasableAspectPatch", 
                txnHelper,
                workProvider,
                batchThreads, 
                batchSize, 
                applicationEventPublisher, 
                logger, 
                1000);

        BatchProcessWorker<NodeRef> worker = new BatchProcessWorker<NodeRef>()
        {

            public void afterProcess() throws Throwable
            {
            }

            public void beforeProcess() throws Throwable
            {
            }

            public String getIdentifier(NodeRef entry)
            {
                return entry.toString();
            }

            public void process(NodeRef entry) throws Throwable
            {
                String alias = (String)nodeService.getProperty(entry, EmailServerModel.PROP_ALIAS);
                if(alias != null)
                {
                    NodeRef existing = (NodeRef) getAttributeService().getAttribute(AliasableAspect.ALIASABLE_ATTRIBUTE_KEY_1,
                            AliasableAspect.ALIASABLE_ATTRIBUTE_KEY_2,
                            AliasableAspect.normaliseAlias(alias));
                    
                    if(existing != null)
                    {
                        if(!existing.equals(entry))
                        {
                            // alias is used by more than one node - warning of some sort?
                            if(logger.isWarnEnabled())
                            {
                                logger.warn("Email alias is not unique, alias:" + alias + " nodeRef:" + entry);
                            }
                            
                            try
                            {
                                behaviourFilter.disableBehaviour(EmailServerModel.ASPECT_ALIASABLE);
                                nodeService.removeAspect(entry,  EmailServerModel.ASPECT_ALIASABLE);
                        
                            }
                            finally
                            {
                                behaviourFilter.enableBehaviour(EmailServerModel.ASPECT_ALIASABLE);
                            }
                        }
                        
                        // else do nothing - attribute already exists.
                    }
                    else
                    {
                        if(logger.isDebugEnabled())
                        {
                            logger.debug("creating email alias attribute for " + alias);
                        }
                        getAttributeService().createAttribute(entry, AliasableAspect.ALIASABLE_ATTRIBUTE_KEY_1, AliasableAspect.ALIASABLE_ATTRIBUTE_KEY_2, AliasableAspect.normaliseAlias(alias));
                    }
                }
            }

        };

        // Now set the batch processor to work
        
        batchProcessor.process(worker, true);
       
        return I18NUtil.getMessage(MSG_SUCCESS);
    }


    public void setAttributeService(AttributeService attributeService)
    {
        this.attributeService = attributeService;
    }


    public AttributeService getAttributeService()
    {
        return attributeService;
    }


    public void setNodeDAO(NodeDAO nodeDAO)
    {
        this.nodeDAO = nodeDAO;
    }


    public NodeDAO getNodeDAO()
    {
        return nodeDAO;
    }


    public void setPatchDAO(PatchDAO patchDAO)
    {
        this.patchDAO = patchDAO;
    }


    public PatchDAO getPatchDAO()
    {
        return patchDAO;
    }


    public void setQnameDAO(QNameDAO qnameDAO)
    {
        this.qnameDAO = qnameDAO;
    }


    public QNameDAO getQnameDAO()
    {
        return qnameDAO;
    }


    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }


    public BehaviourFilter getBehaviourFilter()
    {
        return behaviourFilter;
    }
    

}
