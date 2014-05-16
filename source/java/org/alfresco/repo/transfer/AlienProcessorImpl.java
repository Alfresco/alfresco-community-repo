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
import org.alfresco.service.descriptor.DescriptorService;
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
    private DescriptorService descriptorService;
    
    private static final Log log = LogFactory.getLog(AlienProcessorImpl.class);
    
    public void init()
    {
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "behaviourFilter", behaviourFilter);
        PropertyCheck.mandatory(this, "dictionaryService", getDictionaryService());
        PropertyCheck.mandatory(this, "descriptorService", descriptorService);
    }

    public void onCreateChild(ChildAssociationRef childAssocRef, final String repositoryId, boolean isNewNode)
    {
        log.debug("on create child association to transferred node");
        
        ChildAssociationRef currentAssoc = childAssocRef;
        NodeRef parentNodeRef = currentAssoc.getParentRef();
        NodeRef childNodeRef = currentAssoc.getChildRef();
                  
        if(!childAssocRef.isPrimary())
        {
            log.debug("not a primary assoc - do nothing");
            return;
        }
                
        /**
         * check assoc is a cm:contains or subtype of cm:contains
         */
        if(!childAssocRef.getTypeQName().equals(ContentModel.ASSOC_CONTAINS))
        {
            Collection<QName> subAspects = dictionaryService.getSubAspects(ContentModel.ASSOC_CONTAINS, true);
            if(!subAspects.contains(childAssocRef.getTypeQName()))
            {
                log.debug("not a subtype of cm:contains - do nothing");
                return; 
            }
        }
            
        if(!(nodeService.hasAspect(parentNodeRef, TransferModel.ASPECT_TRANSFERRED) || nodeService.hasAspect(parentNodeRef, TransferModel.ASPECT_ALIEN)))
        {
            log.debug("parent was not transferred or alien - do nothing");
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
 
    public void beforeDeleteAlien(NodeRef deletedNodeRef, ChildAssociationRef oldAssoc)
    {
        log.debug("before delete node - need to check for alien invaders");

        List<String>stuff = (List<String>)nodeService.getProperty(deletedNodeRef, TransferModel.PROP_INVADED_BY);
        if (stuff == null) return;
        Vector<String> exInvaders = new Vector<String>(stuff);
        
        /**
         * some fudge to get this to run after the node has been moved.
         */
        ChildAssociationRef currentAssoc;
        if(oldAssoc != null)
        {
            currentAssoc = oldAssoc;
        }
        else
        {
            currentAssoc = nodeService.getPrimaryParent(deletedNodeRef);
        }
     
        while(currentAssoc != null && exInvaders != null && exInvaders.size() > 0)
        {
            NodeRef parentNodeRef = currentAssoc.getParentRef();
            NodeRef currentNodeRef;
            
            if(currentAssoc == oldAssoc)
            { 
                currentNodeRef = deletedNodeRef;
            }
            else
            {
                currentNodeRef = currentAssoc.getChildRef();   
            }
            
            /**
             * Does the parent have alien invaders ?
             */
            if(nodeService.hasAspect(parentNodeRef, TransferModel.ASPECT_ALIEN))
            {
                log.debug("parent node is invaded by aliens");
                
                /**
                 * Remove the parent's origin from the list of exInvaders since the parent also invades.
                 */
                String parentRepoId; 
                if(nodeService.hasAspect(parentNodeRef, TransferModel.ASPECT_TRANSFERRED))
                {
                    parentRepoId = (String)nodeService.getProperty(parentNodeRef, TransferModel.PROP_FROM_REPOSITORY_ID);
                }                
                else
                {
                    parentRepoId = descriptorService.getCurrentRepositoryDescriptor().getId(); 
                }
                
                exInvaders.remove(parentRepoId);
                
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
                    //List<ChildAssociationRef> refs = nodeService.getChildAssocs(parentNodeRef);
                    List<ChildAssociationRef> refs = nodeService.getChildAssocsByPropertyValue(parentNodeRef, TransferModel.PROP_INVADED_BY, exInvader);
                    
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
    
    
    public void afterMoveAlien(ChildAssociationRef newAssocRef)
    {
        log.debug("after move alien: newAssocRef");
        
        NodeRef parentNodeRef = newAssocRef.getParentRef();
        NodeRef childNodeRef = newAssocRef.getChildRef();
        
        List<String> childInvadedBy = (List<String>)nodeService.getProperty(childNodeRef, TransferModel.PROP_INVADED_BY);
        
        if(nodeService.hasAspect(parentNodeRef, TransferModel.ASPECT_TRANSFERRED) || nodeService.hasAspect(parentNodeRef, TransferModel.ASPECT_ALIEN))
        {
            List<String>aliensToAdd = new ArrayList<String>();
            
            log.debug("new parent is transferred or alien");
                          
            /**
             * check assoc is a cm:contains or subtype of cm:contains
             */
            if(!newAssocRef.getTypeQName().equals(ContentModel.ASSOC_CONTAINS))
            {
                Collection<QName> subAspects = dictionaryService.getSubAspects(ContentModel.ASSOC_CONTAINS, true);
                if(!subAspects.contains(newAssocRef.getTypeQName()))
                {
                    log.debug("not a subtype of cm:contains - may need to uninvade");
                   
                    String parentRepoId = descriptorService.getCurrentRepositoryDescriptor().getId(); 
                    retreatDownwards(childNodeRef, parentRepoId);            
                    
                    return; 
                }
            }
                                
            if(nodeService.hasAspect(parentNodeRef, TransferModel.ASPECT_ALIEN))
            {
                // parent is already alien
                List<String>parentInvadedBy = (List<String>)nodeService.getProperty(parentNodeRef, TransferModel.PROP_INVADED_BY);
                for(String invader : childInvadedBy)
                {
                    if(!parentInvadedBy.contains(invader))
                    {
                        aliensToAdd.add(invader);
                    }
                }
            }
            else
            {
                // parent is transfered but does not yet contain aliens
               String parentFromRepo = (String)nodeService.getProperty(parentNodeRef, TransferModel.PROP_FROM_REPOSITORY_ID);
               {
                   for(String invader : childInvadedBy)
                   {
                       if(invader.equalsIgnoreCase(parentFromRepo))
                       {
                           // The invader is the same repo
                           log.debug("child node is from the same repo as a non invaded node");
                           retreatDownwards(childNodeRef, parentFromRepo);
                       }
                       else
                       {
                           aliensToAdd.add(invader);
                       }
                   }
               }
            }
            
            /**
              * Now deal with the parents of this alien node
              */
            ChildAssociationRef currentAssoc = newAssocRef;
            while(currentAssoc != null && aliensToAdd.size() > 0)
            {
                parentNodeRef = currentAssoc.getParentRef();
                childNodeRef = currentAssoc.getChildRef();
            
                // if parent node is transferred or alien
                if(nodeService.hasAspect(parentNodeRef, TransferModel.ASPECT_TRANSFERRED) || nodeService.hasAspect(parentNodeRef, TransferModel.ASPECT_ALIEN))
                {
                    Iterator<String> i = aliensToAdd.iterator();
                    
                    // for each alien repo id to add
                    while(i.hasNext())
                    {
                        String alienRepoId = (String)i.next();
                    
                        if (!isInvaded(parentNodeRef, alienRepoId))
                        {
                            if(log.isDebugEnabled())
                            {
                                log.debug("alien invades parent node:" + parentNodeRef + ", repositoryId:" + alienRepoId);
                            }
                            
                            final NodeRef newAlien = parentNodeRef; 
                            final String fAlien = alienRepoId;
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
                                    setAlien(newAlien, fAlien);
                                    return null;
                                }          
                            };
                            AuthenticationUtil.runAs(actionRunAs, AuthenticationUtil.getSystemUserName());
                        }
                        else
                        {
                            log.debug("parent node is already invaded by:" + alienRepoId);
                            i.remove();
                        }
                        
                        // Yes the parent has been invaded so step up to the parent's parent             
                        currentAssoc = nodeService.getPrimaryParent(parentNodeRef);
                    }
                }
                else
                {
                    log.debug("parent is not a transferred node");
                    currentAssoc = null;
                }
            }            
        }
        else
        {
            log.debug("parent was not transferred or alien");
            
            String parentRepoId = descriptorService.getCurrentRepositoryDescriptor().getId(); 
            retreatDownwards(childNodeRef, parentRepoId);
            
            return;
        }   
    } // after move alien 
    
    /**
     * Top down un-invasion
     * <p>
     * Steps down the tree retreating from all the invaded nodes.
     * <p>
     * The retreat will stop is there is a "sub-invasion".
     * <p>   
     * @param nodeRef the top of the tree
     * @param repoId the repository that is retreating.
     */
    private void retreatDownwards(NodeRef nodeRef, String fromRepositoryId)
    {
        Stack<NodeRef> nodesToRetreat = new Stack<NodeRef>();
        nodesToRetreat.add(nodeRef);
        
        /**
         * Now go and do the retreat.        
         */
        while(!nodesToRetreat.isEmpty())
        {
            if(log.isDebugEnabled())
            {
                log.debug("retreat :" + nodeRef + ", repoId:" + fromRepositoryId);
            }
            
            /**
             *  for the current node and all alien children
             *  
             *  if they are "from" the retreating repository then 
             */
            NodeRef currentNodeRef = nodesToRetreat.pop();
            
            log.debug("retreatNode:" + currentNodeRef);
            
            if(getNodeService().hasAspect(currentNodeRef, TransferModel.ASPECT_ALIEN))
            {
                // Yes this is an alien node
                List<String>invadedBy = (List<String>)getNodeService().getProperty(currentNodeRef, TransferModel.PROP_INVADED_BY);
                
                String parentRepoId; 
                if(nodeService.hasAspect(currentNodeRef, TransferModel.ASPECT_TRANSFERRED))
                {
                    log.debug("node is transferred");
                    parentRepoId = (String)nodeService.getProperty(currentNodeRef, TransferModel.PROP_FROM_REPOSITORY_ID);
                }                
                else
                {
                    log.debug("node is local");
                    parentRepoId = descriptorService.getCurrentRepositoryDescriptor().getId(); 
                }
                
                if(fromRepositoryId.equalsIgnoreCase(parentRepoId))
                {
                    // This node is "owned" by the retreating repo
                    // Yes we are invaded by fromRepositoryId
                    if(invadedBy.size() == 1)
                    {
                        // we are invaded by a single repository which must be fromRepositoryId
                        log.debug("no longe alien:" + currentNodeRef);
                        getNodeService().removeAspect(currentNodeRef, TransferModel.ASPECT_ALIEN);
                    }
                    else
                    {
                       invadedBy.remove(parentRepoId);
                       getNodeService().setProperty(currentNodeRef, TransferModel.PROP_INVADED_BY, (Serializable)invadedBy);
                    }
                    
                    //List<ChildAssociationRef> refs = getNodeService().getChildAssocs(currentNodeRef);
                    List<ChildAssociationRef> refs = nodeService.getChildAssocsByPropertyValue(currentNodeRef, TransferModel.PROP_INVADED_BY, fromRepositoryId);
                    for(ChildAssociationRef ref : refs)
                    {
                        if(log.isDebugEnabled())
                        {
                            log.debug("will need to check child:" + ref);
                        }
                        nodesToRetreat.push(ref.getChildRef());        
                    }        
                }
            } 
        }
    } // retreatDownwards
   
    public boolean isAlien(NodeRef nodeRef)
    {
        return nodeService.hasAspect(nodeRef, TransferModel.ASPECT_ALIEN);
    }

    public void pruneNode(NodeRef nodeToPrune, String fromRepositoryId)
    {
        Stack<NodeRef> nodesToPrune = new Stack<NodeRef>();
        nodesToPrune.add(nodeToPrune);
        
        ChildAssociationRef startingParent = nodeService.getPrimaryParent(nodeToPrune);
        
        Stack<NodeRef> foldersToRecalculate = new Stack<NodeRef>();
        
        /**
         * Now go and do the pruning.        
         */
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
                    // Yes we are invaded by fromRepositoryId
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
                        List<ChildAssociationRef> refs = nodeService.getChildAssocsByPropertyValue(currentNodeRef, TransferModel.PROP_INVADED_BY, fromRepositoryId);
                        for(ChildAssociationRef ref : refs)
                        {
                            if(log.isDebugEnabled())
                            {
                                log.debug("will need to check child:" + ref);
                            }
                            nodesToPrune.push(ref.getChildRef());        
                        }
                        
                        /**
                         * Yes we might do something to the children of this node.
                         */
                        if(!foldersToRecalculate.contains(currentNodeRef))
                        {
                            foldersToRecalculate.push(currentNodeRef);
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
                                if(!foldersToRecalculate.contains(currentNodeRef))
                                {
                                    foldersToRecalculate.push(currentNodeRef);
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
         * Now recalculate the "invadedBy" flag for those folders we could not delete.
         */
        while(!foldersToRecalculate.isEmpty())
        {
            NodeRef folderNodeRef = foldersToRecalculate.pop();
            
            log.debug("recalculate invadedBy :" + folderNodeRef);
            
            recalcInvasion(folderNodeRef, fromRepositoryId);
        }
        
        /**
         * Now ripple up the invaded flag - may be a alien retreat.
         */
        log.debug("now ripple upwards");
        
        ChildAssociationRef ripple = startingParent;
        while(ripple != null)
        {
            if(log.isDebugEnabled())
            {
                log.debug("Checking parent:" + ripple);
            }
            
            if(nodeService.hasAspect(ripple.getParentRef(), TransferModel.ASPECT_ALIEN))
            {
                if(recalcInvasion(ripple.getParentRef(), fromRepositoryId))
                {
                    log.debug("parent is still invaded");
                    ripple = null;   
                }
                else
                {
                    log.debug("parent is no longer invaded");
                    ripple = nodeService.getPrimaryParent(ripple.getParentRef());            
                }
            }
            else
            {
                ripple = null;
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
     * Mark the specified node as an alien node, invaded by the specified invader.
     * @param newAlien node that has been invaded.
     * @param invader the repository id of the invading repo.
     */
    private void setAlien(NodeRef newAlien, String invader)
    {
        // Introduce a Multi-valued property
        List<String> invadedBy = (List<String>)nodeService.getProperty(newAlien, 
                TransferModel.PROP_INVADED_BY);
        
        if(invadedBy == null)
        {
            invadedBy = new ArrayList<String>(1);
        }
        
        if(!invadedBy.contains(invader))
        {
            invadedBy.add(invader);
        }
        
        /**
         * Set the invaded by property
         */
        nodeService.setProperty(newAlien, TransferModel.PROP_INVADED_BY, (Serializable) invadedBy);  
    }
    
    /**
     * Determine whether the specified node is invaded by the specified repository
     * @param folderNodeRef the node to re-calculate
     * @param fromRepositoryId the repository who is transferring.
     * 
     * @return true - still invaded, false, no longer invaded
     */
    private boolean recalcInvasion(NodeRef folderNodeRef, String fromRepositoryId)
    {
        List<String>folderInvadedBy = (List<String>)nodeService.getProperty(folderNodeRef, TransferModel.PROP_INVADED_BY);
             
        boolean stillInvaded = false;
        
        //TODO need a more efficient query here
        List<ChildAssociationRef> refs = nodeService.getChildAssocs(folderNodeRef);
        for(ChildAssociationRef ref : refs)
        {
            NodeRef childNode = ref.getChildRef();
            List<String>childInvadedBy = (List<String>)getNodeService().getProperty(childNode, TransferModel.PROP_INVADED_BY);
            
            if(childInvadedBy != null && childInvadedBy.contains(fromRepositoryId))
            {
                log.debug("folder is still invaded");
                stillInvaded = true;
                break;
            }
        }
        
        if(!stillInvaded)
        {
            log.debug("folder is no longer invaded by this repo:" + folderNodeRef);
            folderInvadedBy.remove(fromRepositoryId);
            if(folderInvadedBy.size() > 0)
            {
                if(log.isDebugEnabled())
                {
                    log.debug("still invaded by:" + folderInvadedBy);
                }
                getNodeService().setProperty(folderNodeRef, TransferModel.PROP_INVADED_BY, (Serializable)folderInvadedBy);
            }
            else
            {
                log.debug("no longer alien:" + folderNodeRef);
                getNodeService().removeAspect(folderNodeRef, TransferModel.ASPECT_ALIEN);
            }
        }
        
        return stillInvaded;
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
    
    public void setDescriptorService(DescriptorService descriptorService)
    {
        this.descriptorService = descriptorService;
    }

    public DescriptorService getDescriptorService()
    {
        return descriptorService;
    }
}
