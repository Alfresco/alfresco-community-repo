package org.alfresco.repo.transfer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class to encapsulate the behaviour of "Alien" nodes.
 */
public class AlienProcessorImpl implements AlienProcessor
{
    private NodeService nodeService;
    private BehaviourFilter behaviourFilter;
    private DictionaryService dictionaryService;
    
    private static final Log log = LogFactory.getLog(AlienProcessorImpl.class);
    
    public void init()
    {
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "behaviourFilter", behaviourFilter);
        PropertyCheck.mandatory(this, "dictionaryService", getDictionaryService());
    }

    public void onCreateChild(ChildAssociationRef childAssocRef, final String repositoryId)
    {
        log.debug("on create child association to transferred node");
        
        ChildAssociationRef currentAssoc = childAssocRef;
           
        if(!childAssocRef.isPrimary())
        {
            log.debug("not a primary assoc - do nothing");
            return;
        }
        
        // TODO Needs to check assoc is a cm:contains or subtype of cm:contains
        if(!childAssocRef.getTypeQName().equals(ContentModel.ASSOC_CONTAINS))
        {
            Collection<QName> subAspects = dictionaryService.getSubAspects(ContentModel.ASSOC_CONTAINS, true);
            if(!subAspects.contains(childAssocRef.getTypeQName()))
            {
                log.debug("not a subtype of cm:contains - do nothing");
                return; 
            }
        }
  
        NodeRef parentNodeRef = currentAssoc.getParentRef();
        NodeRef childNodeRef = currentAssoc.getChildRef();
            
        if(!nodeService.hasAspect(parentNodeRef, TransferModel.ASPECT_TRANSFERRED))
        {
                log.debug("parent was not transferred - do nothing");
                return;
        }   
            
        if(!nodeService.hasAspect(parentNodeRef, TransferModel.ASPECT_ALIEN))
        {
            // parent is not yet an alien invader ...
           String parentFromRepo = (String)nodeService.getProperty(parentNodeRef, TransferModel.PROP_FROM_REPOSITORY_ID);
           {
               if(repositoryId.equalsIgnoreCase(parentFromRepo))
               {
                   log.debug("parent was not alien and this node is from the same repo - do nothing");
                   return;
                }
            }
        }
            
        /**
          * If we get this far then we are going to Make the new child node 
          * an alien node
          */
        setAlien(childNodeRef, repositoryId);
            
        /**
          * Now deal with the parents of this alien node
          */
        while(currentAssoc != null)
        {
            parentNodeRef = currentAssoc.getParentRef();
            childNodeRef = currentAssoc.getChildRef();
        
            if(nodeService.hasAspect(parentNodeRef, TransferModel.ASPECT_TRANSFERRED) || nodeService.hasAspect(parentNodeRef, TransferModel.ASPECT_ALIEN))
            {
                if (!isInvaded(parentNodeRef, repositoryId))
                {
                    if(log.isDebugEnabled())
                    {
                        log.debug("alien invades parent node:" + parentNodeRef + ", repositoryId:" + repositoryId);
                    }
                        
                    final NodeRef newAlien = parentNodeRef; 
                     
                    /**
                     * Parent may be locked or not be editable by the current user 
                     * turn off auditing and lock service for this transaction and 
                     * run as admin.
                     */
                     RunAsWork<Void> actionRunAs = new RunAsWork<Void>()
                     {
                        public Void doWork() throws Exception
                        {
                                getBehaviourFilter().disableBehaviour(newAlien, ContentModel.ASPECT_AUDITABLE);
                                getBehaviourFilter().disableBehaviour(newAlien, ContentModel.ASPECT_LOCKABLE);
                                setAlien(newAlien, repositoryId);
                                return null;
                        }          
                    };
                    AuthenticationUtil.runAs(actionRunAs, AuthenticationUtil.getSystemUserName());
             
                    // Yes the parent has been invaded so step up to the parent's parent             
                    currentAssoc = nodeService.getPrimaryParent(parentNodeRef);
                }
                else
                {
                    log.debug("parent node is already invaded");
                    currentAssoc = null;
                }   
            }
            else
            {
                log.debug("parent is not a transferred node");
                currentAssoc = null;
            }
       }            
    }

    public void beforeDeleteAlien(NodeRef deletedNodeRef)
    {
    log.debug("on delete node - need to check for transferred node");
        
        List<String>stuff = (List<String>)nodeService.getProperty(deletedNodeRef, TransferModel.PROP_INVADED_BY);
        
        Vector<String> exInvaders = new Vector<String>(stuff);
        
        ChildAssociationRef currentAssoc = nodeService.getPrimaryParent(deletedNodeRef);
     
        while(currentAssoc != null && exInvaders != null && exInvaders.size() > 0)
        {
            NodeRef parentNodeRef = currentAssoc.getParentRef();
            NodeRef currentNodeRef = currentAssoc.getChildRef();
            
            /**
             * Does the parent have alien invaders ?
             */
            if(nodeService.hasAspect(parentNodeRef, TransferModel.ASPECT_ALIEN))
            {
                log.debug("parent node is alien - check siblings");
                
                /**
                 * For each invader of the deletedNode
                 */
                Iterator<String> i = exInvaders.listIterator();
                while(i.hasNext())
                {
                    String exInvader = i.next();
                    log.debug("Checking exInvader:" + exInvader);
                    
                    /**
                     * Check the siblings of this node to see whether there are any other alien nodes for this invader.
                     */
                    //TODO replace with a more efficient query
                    List<ChildAssociationRef> refs = nodeService.getChildAssocs(parentNodeRef);
                    
                    for(ChildAssociationRef ref : refs)
                    {
                        NodeRef childRef = ref.getChildRef();
                        List<String>invadedBy = (List<String>)nodeService.getProperty(childRef, TransferModel.PROP_INVADED_BY);
                        
                        if(childRef.equals(currentNodeRef))
                        {
                            // do nothing - this is the node we are working with.
                        }    
                        else 
                        {
                            if(invadedBy != null && invadedBy.contains(exInvader))
                            {
                                // There is a sibling so remove this from the list of ex invaders.
                                log.debug("yes there is a sibling so it remains an invader");
                                i.remove();
                                break;
                            }
                        }
                    } // for each child assoc
                    
                } // for each invader
                
                log.debug("end of checking siblings");
                
                if(exInvaders.size() > 0)
                {
                    log.debug("removing invaders from parent node:" + parentNodeRef);
                    List<String> parentInvaders = (List<String>)nodeService.getProperty(parentNodeRef, TransferModel.PROP_INVADED_BY);
                    
                    final List<String> newInvaders = new ArrayList<String>(10);
                    for(String invader : parentInvaders)
                    {
                        if(exInvaders.contains(invader))
                        {
                            log.debug("removing invader:" + invader);
                        }
                        else
                        {
                            newInvaders.add(invader);
                        }
                    }
                        
                    final NodeRef oldAlien = parentNodeRef;
                  
                    /**
                     * Parent may be locked or not be editable by the current user 
                     * turn off auditing and lock service for this transaction and 
                     * run as admin.
                     */
                    RunAsWork<Void> actionRunAs = new RunAsWork<Void>()
                    {
                        public Void doWork() throws Exception
                        {
                            behaviourFilter.disableBehaviour(oldAlien, ContentModel.ASPECT_AUDITABLE);
                            behaviourFilter.disableBehaviour(oldAlien, ContentModel.ASPECT_LOCKABLE);
                            if(newInvaders.size() > 0)
                            {
                                nodeService.setProperty(oldAlien, TransferModel.PROP_INVADED_BY, (Serializable)newInvaders);
                            }
                            else
                            {
                                log.debug("parent node is no longer alien nodeRef" + oldAlien);
                                nodeService.removeAspect(oldAlien, TransferModel.ASPECT_ALIEN);
                            }
                            return null;
                        }          
                    };
                    AuthenticationUtil.runAs(actionRunAs, AuthenticationUtil.getSystemUserName());
                }
                
                /**
                 * Now step up to the parent's parent
                 */
                 currentAssoc = nodeService.getPrimaryParent(parentNodeRef);
            }
            else
            {
                log.debug("parent is not an alien node");
                currentAssoc = null;
            }
        } // end of while              
    }

    public boolean isAlien(NodeRef nodeRef)
    {
        return nodeService.hasAspect(nodeRef, TransferModel.ASPECT_ALIEN);
    }

    public void pruneNode(NodeRef parentNodeRef, String fromRepositoryId)
    {
        Stack<NodeRef> nodesToPrune = new Stack<NodeRef>();
        Stack<NodeRef> foldersToRecalculate = new Stack<NodeRef>(); 
        nodesToPrune.add(parentNodeRef);
                
        while(!nodesToPrune.isEmpty())
        {
            /**
             *  for all alien children
             * 
             *  if from the repo with no (other) aliens - delete
             *  
             *  if from the repo with multiple alien invasions - leave alone but process children
             */
            NodeRef currentNodeRef = nodesToPrune.pop();
            
            log.debug("pruneNode:" + currentNodeRef);
            
            if(getNodeService().hasAspect(currentNodeRef, TransferModel.ASPECT_ALIEN))
            {
                // Yes this is an alien node
                List<String>invadedBy = (List<String>)getNodeService().getProperty(currentNodeRef, TransferModel.PROP_INVADED_BY);
                if(invadedBy.contains(fromRepositoryId))
                {
                    if(invadedBy.size() == 1)
                    {
                        // we are invaded by a single repository which must be fromRepositoryId
                        log.debug("pruned - deleted node:" + currentNodeRef);
                        getNodeService().deleteNode(currentNodeRef);
                    }
                    else
                    {
                        log.debug("folder has multiple invaders");
                        // multiple invasion - so it must be a folder
                        //TODO replace with a more efficient query
                        List<ChildAssociationRef> refs = getNodeService().getChildAssocs(parentNodeRef);
                        for(ChildAssociationRef ref : refs)
                        {
                            if(log.isDebugEnabled())
                            {
                                log.debug("will need to check child:" + ref);
                            }
                            nodesToPrune.push(ref.getChildRef()); 
                            
                            /**
                             * This folder can't be deleted so its invaded flag needs to be re-calculated 
                             */
                            if(!foldersToRecalculate.contains(ref.getParentRef()))
                            {
                                foldersToRecalculate.push(ref.getParentRef());
                            }
                        }
                    }
                }
                else
                {
                    /**
                     * Current node has been invaded by another repository  
                     *
                     * Need to check fromRepositoryId since its children may need to be pruned
                     */
                    getNodeService().hasAspect(currentNodeRef, TransferModel.ASPECT_TRANSFERRED);
                    {
                        String fromRepoId = (String)getNodeService().getProperty(currentNodeRef, TransferModel.PROP_FROM_REPOSITORY_ID);
                        if(fromRepositoryId.equalsIgnoreCase(fromRepoId))
                        {
                            log.debug("folder is from the transferring repository");
                            // invaded from somewhere else - so it must be a folder
                            List<ChildAssociationRef> refs = getNodeService().getChildAssocs(currentNodeRef);
                            for(ChildAssociationRef ref : refs)
                            {
                                if(log.isDebugEnabled())
                                {
                                    log.debug("will need to check child:" + ref);
                                }
                                nodesToPrune.push(ref.getChildRef()); 
                                
                                /**
                                 * This folder can't be deleted so its invaded flag needs to be re-calculated 
                                 */
                                if(!foldersToRecalculate.contains(ref.getParentRef()))
                                {
                                    foldersToRecalculate.push(ref.getParentRef());
                                }
                            }
                        }
                    }        
                }
            }
            else
            {
                // Current node does not contain alien nodes so it can be deleted.
                getNodeService().hasAspect(currentNodeRef, TransferModel.ASPECT_TRANSFERRED);
                {
                    String fromRepoId = (String)getNodeService().getProperty(currentNodeRef, TransferModel.PROP_FROM_REPOSITORY_ID);
                    if(fromRepositoryId.equalsIgnoreCase(fromRepoId))
                    {
                        // we are invaded by a single repository
                        log.debug("pruned - deleted non alien node:" + currentNodeRef);
                        getNodeService().deleteNode(currentNodeRef);
                    }
                }
            }
        }
        
        /**
         * Now ripple the "invadedBy" flag upwards.
         */
        
        while(!foldersToRecalculate.isEmpty())
        {
            NodeRef folderNodeRef = foldersToRecalculate.pop();
            
            log.debug("recalculate invadedBy :" + folderNodeRef);
            
            List<String>folderInvadedBy = (List<String>)getNodeService().getProperty(folderNodeRef, TransferModel.PROP_INVADED_BY);
            
            boolean stillInvaded = false;
            //TODO need a more efficient query here
            List<ChildAssociationRef> refs = getNodeService().getChildAssocs(folderNodeRef);
            for(ChildAssociationRef ref : refs)
            {
                NodeRef childNode = ref.getChildRef();
                List<String>childInvadedBy = (List<String>)getNodeService().getProperty(childNode, TransferModel.PROP_INVADED_BY);
                
                if(childInvadedBy.contains(fromRepositoryId))
                {
                    log.debug("folder is still invaded");
                    stillInvaded = true;
                    break;
                }
            }
            
            if(!stillInvaded)
            {
                List<String> newInvadedBy = new ArrayList<String>(folderInvadedBy);
                folderInvadedBy.remove(fromRepositoryId);
                getNodeService().setProperty(folderNodeRef, TransferModel.PROP_INVADED_BY, (Serializable)newInvadedBy);
            }
        }
        log.debug("pruneNode: end");        
    }
    
    
    /**
     * Is this node invaded ?
     * @param nodeRef
     * @param invader
     * @return true, this node has been invaded by the invader
     */
    private boolean isInvaded(NodeRef nodeRef, String invader)
    {
        List<String>invadedBy = (List<String>)nodeService.getProperty(nodeRef, TransferModel.PROP_INVADED_BY);
        
        if(invadedBy == null)
        {
            return false;
        }
        
        return invadedBy.contains(invader);
    }
    
    /**
     * Mark the specified node as an alien node, invadedby the invader.
     * @param newAlien
     * @param invader
     */
    private void setAlien(NodeRef newAlien, String invader)
    {
        // Introduce a Multi-valued property
        List<String> invadedBy = (List<String>)nodeService.getProperty(newAlien, 
                TransferModel.PROP_INVADED_BY);
        
        if(invadedBy == null)
        {
            nodeService.setProperty(newAlien, TransferModel.PROP_ALIEN, Boolean.TRUE);
            invadedBy = new ArrayList<String>(1);
        }
        invadedBy.add(invader);
        
        /**
         * Set the invaded by property
         */
        nodeService.setProperty(newAlien, TransferModel.PROP_INVADED_BY, (Serializable) invadedBy);
        
//        /**
//         * Experiment with a residual property
//         */ 
//        nodeService.setProperty(newAlien, QName.createQName(TransferModel.TRANSFER_MODEL_1_0_URI, 
//                "invader" + invader), Boolean.TRUE);
  
    }
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public NodeService getNodeService()
    {
        return nodeService;
    }

    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }

    public BehaviourFilter getBehaviourFilter()
    {
        return behaviourFilter;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public DictionaryService getDictionaryService()
    {
        return dictionaryService;
    }
}
