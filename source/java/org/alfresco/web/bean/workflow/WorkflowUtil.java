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
package org.alfresco.web.bean.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Helper class for common Workflow functionality.
 * <p>
 * This class should be replaced with calls to a WorkflowService once it is available.
 * 
 * @author Kevin Roast
 */
public class WorkflowUtil
{
   private static Log logger = LogFactory.getLog(WorkflowUtil.class);
   
   /**
    * Execute the Approve step for the Simple Workflow on a node.
    *  
    * @param ref           NodeRef to the node with the workflow
    * @param nodeService   NodeService instance
    * @param copyService   CopyService instance
    * 
    * @throws AlfrescoRuntimeException
    */
   public static void approve(final NodeRef ref, final NodeService nodeService, final CopyService copyService)
      throws AlfrescoRuntimeException
   {
      Node docNode = new Node(ref);
      
      if (docNode.hasAspect(ApplicationModel.ASPECT_SIMPLE_WORKFLOW) == false)
      {
         throw new AlfrescoRuntimeException("Cannot approve a node that is not part of a workflow.");
      }
      
      // get the simple workflow aspect properties
      Map<String, Object> props = docNode.getProperties();
      
      Boolean approveMove = (Boolean)props.get(ApplicationModel.PROP_APPROVE_MOVE.toString());
      NodeRef approveFolder = (NodeRef)props.get(ApplicationModel.PROP_APPROVE_FOLDER.toString());
      
      if (approveMove.booleanValue())
      {
         // first we need to take off the simpleworkflow aspect
         nodeService.removeAspect(ref, ApplicationModel.ASPECT_SIMPLE_WORKFLOW);
         
         // move the node to the specified folder
         String qname = QName.createValidLocalName(docNode.getName());
         nodeService.moveNode(ref, approveFolder, ContentModel.ASSOC_CONTAINS,
               QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, qname));
      }
      else
      {
         // first we need to take off the simpleworkflow aspect
         // NOTE: run as system to allow Consumers to copy an item
         AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
         {
            public String doWork() throws Exception
            {
               nodeService.removeAspect(ref, ApplicationModel.ASPECT_SIMPLE_WORKFLOW);
               return null;
            }
         }, AuthenticationUtil.getSystemUserName());
         
         // copy the node to the specified folder
         String name = docNode.getName();
         String qname = QName.createValidLocalName(name);
         NodeRef newNode = copyService.copy(ref, approveFolder, ContentModel.ASSOC_CONTAINS,
               QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, qname), true);
         
         // the copy service does not copy the name of the node so we
         // need to update the property on the copied item
         nodeService.setProperty(newNode, ContentModel.PROP_NAME, name);
      }
      
      if (logger.isDebugEnabled())
      {
         String movedCopied = approveMove ? "moved" : "copied";
         logger.debug("Node has been approved and " + movedCopied + " to folder with id of " + 
               approveFolder.getId());
      }
   }
   
   /**
    * Execute the Reject step for the Simple Workflow on a node.
    *  
    * @param ref           NodeRef to the node with the workflow
    * @param nodeService   NodeService instance
    * @param copyService   CopyService instance
    * 
    * @throws AlfrescoRuntimeException
    */
   public static void reject(NodeRef ref, NodeService nodeService, CopyService copyService)
      throws AlfrescoRuntimeException
   {
      Node docNode = new Node(ref);
      
      if (docNode.hasAspect(ApplicationModel.ASPECT_SIMPLE_WORKFLOW) == false)
      {
         throw new AlfrescoRuntimeException("Cannot reject a node that is not part of a workflow.");
      }
      
      // get the simple workflow aspect properties
      Map<String, Object> props = docNode.getProperties();
      
      String rejectStep = (String)props.get(ApplicationModel.PROP_REJECT_STEP.toString());
      Boolean rejectMove = (Boolean)props.get(ApplicationModel.PROP_REJECT_MOVE.toString());
      NodeRef rejectFolder = (NodeRef)props.get(ApplicationModel.PROP_REJECT_FOLDER.toString());
      
      if (rejectStep == null && rejectMove == null && rejectFolder == null)
      {
         throw new AlfrescoRuntimeException("The workflow does not have a reject step defined.");
      }
      
      // first we need to take off the simpleworkflow aspect
      nodeService.removeAspect(ref, ApplicationModel.ASPECT_SIMPLE_WORKFLOW);
      
      if (rejectMove != null && rejectMove.booleanValue())
      {
         // move the document to the specified folder
         String qname = QName.createValidLocalName(docNode.getName());
         nodeService.moveNode(ref, rejectFolder, ContentModel.ASSOC_CONTAINS,
               QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, qname));
      }
      else
      {
         // copy the document to the specified folder
         String name = docNode.getName();
         String qname = QName.createValidLocalName(name);
         NodeRef newNode = copyService.copy(ref, rejectFolder, ContentModel.ASSOC_CONTAINS,
               QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, qname), true);
         
         // the copy service does not copy the name of the node so we
         // need to update the property on the copied item
         nodeService.setProperty(newNode, ContentModel.PROP_NAME, name);
      }
      
      if (logger.isDebugEnabled())
      {
         String movedCopied = rejectMove ? "moved" : "copied";
         logger.debug("Node has been rejected and " + movedCopied + " to folder with id of " + 
               rejectFolder.getId());
      }
   }
   
   /**
    * Prepares the given node for persistence in the workflow engine.
    * 
    * @param node The node to package up for persistence
    * @return The map of data representing the node
    */
   @SuppressWarnings("unchecked")
   public static Map<QName, Serializable> prepareTaskParams(Node node)
   {
      Map<QName, Serializable> params = new HashMap<QName, Serializable>();
      
      // marshal the properties and associations captured by the property sheet
      // back into a Map to pass to the workflow service

      // go through all the properties in the transient node and add them to params map
      Map<String, Object> props = node.getProperties();
      for (String propName : props.keySet())
      {
         QName propQName = Repository.resolveToQName(propName);
         params.put(propQName, (Serializable)props.get(propName));
      }
      
      // go through any associations that have been added to the start task
      // and build a list of NodeRefs representing the targets
      Map<String, Map<String, AssociationRef>> assocs = node.getAddedAssociations();
      for (String assocName : assocs.keySet())
      {
         QName assocQName = Repository.resolveToQName(assocName);
         
         // get the associations added and create list of targets
         Map<String, AssociationRef> addedAssocs = assocs.get(assocName);
         List<AssociationRef> originalAssocRefs = (List<AssociationRef>) node.getAssociations().get(assocName);
         List<NodeRef> targets = new ArrayList<NodeRef>(addedAssocs.size());
         
         if (originalAssocRefs != null)
         {
             for (AssociationRef assoc : originalAssocRefs)
             {
                targets.add(assoc.getTargetRef());
             }
         }
         
         for (AssociationRef assoc : addedAssocs.values())
         {
            targets.add(assoc.getTargetRef());
         }
         
         params.put(assocQName, (Serializable)targets);
      }
      
      // go through the removed associations and either setup or adjust the 
      // parameters map accordingly
      assocs = node.getRemovedAssociations();
      
      for (String assocName : assocs.keySet())
      {
         QName assocQName = Repository.resolveToQName(assocName);
         
         // get the associations removed and create list of targets
         Map<String, AssociationRef> removedAssocs = assocs.get(assocName);         
         List<NodeRef> targets = (List<NodeRef>)params.get(assocQName);
         
         if (targets == null)
         {
             // if there weren't any assocs of this type added get the current
             // set of assocs from the node
             List<AssociationRef> originalAssocRefs = (List<AssociationRef>)node.getAssociations().get(assocName);
             targets = new ArrayList<NodeRef>(originalAssocRefs.size());
             
             for (AssociationRef assoc : originalAssocRefs)
             {
                targets.add(assoc.getTargetRef());
             }
         }
         
         // remove the assocs the user deleted
         for (AssociationRef assoc : removedAssocs.values())
         {
            targets.remove(assoc.getTargetRef());
         }
         
         params.put(assocQName, (Serializable)targets);
      }
      
      // TODO: Deal with child associations if and when we need to support
      //       them for workflow tasks, for now warn that they are being used
      Map childAssocs = node.getAddedChildAssociations();
      if (childAssocs.size() > 0)
      {
         if (logger.isWarnEnabled())
            logger.warn("Child associations are present but are not supported for workflow tasks, ignoring...");
      }
      
      return params;
   }
}
