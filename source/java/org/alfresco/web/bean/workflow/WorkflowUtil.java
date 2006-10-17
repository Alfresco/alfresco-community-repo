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
package org.alfresco.web.bean.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
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
   public static void approve(NodeRef ref, NodeService nodeService, CopyService copyService)
      throws AlfrescoRuntimeException
   {
      Node docNode = new Node(ref);
      
      if (docNode.hasAspect(ContentModel.ASPECT_SIMPLE_WORKFLOW) == false)
      {
         throw new AlfrescoRuntimeException("Cannot approve a node that is not part of a workflow.");
      }
      
      // get the simple workflow aspect properties
      Map<String, Object> props = docNode.getProperties();
      
      Boolean approveMove = (Boolean)props.get(ContentModel.PROP_APPROVE_MOVE.toString());
      NodeRef approveFolder = (NodeRef)props.get(ContentModel.PROP_APPROVE_FOLDER.toString());
      
      // first we need to take off the simpleworkflow aspect
      nodeService.removeAspect(ref, ContentModel.ASPECT_SIMPLE_WORKFLOW);
      
      if (approveMove.booleanValue())
      {
         // move the node to the specified folder
         String qname = QName.createValidLocalName(docNode.getName());
         nodeService.moveNode(ref, approveFolder, ContentModel.ASSOC_CONTAINS,
               QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, qname));
      }
      else
      {
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
      
      if (docNode.hasAspect(ContentModel.ASPECT_SIMPLE_WORKFLOW) == false)
      {
         throw new AlfrescoRuntimeException("Cannot reject a node that is not part of a workflow.");
      }
      
      // get the simple workflow aspect properties
      Map<String, Object> props = docNode.getProperties();
      
      String rejectStep = (String)props.get(ContentModel.PROP_REJECT_STEP.toString());
      Boolean rejectMove = (Boolean)props.get(ContentModel.PROP_REJECT_MOVE.toString());
      NodeRef rejectFolder = (NodeRef)props.get(ContentModel.PROP_REJECT_FOLDER.toString());
      
      if (rejectStep == null && rejectMove == null && rejectFolder == null)
      {
         throw new AlfrescoRuntimeException("The workflow does not have a reject step defined.");
      }
      
      // first we need to take off the simpleworkflow aspect
      nodeService.removeAspect(ref, ContentModel.ASPECT_SIMPLE_WORKFLOW);
      
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
   public static Map<QName, Serializable> prepareTaskParams(Node node)
   {
      Map<QName, Serializable> params = new HashMap<QName, Serializable>();
      
      // marshal the properties and associations captured by the property sheet
      // back into a Map to pass to the workflow service

      // go through all the properties in the transient node and add them to
      // params map
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
         List<NodeRef> targets = new ArrayList<NodeRef>(addedAssocs.size());
         for (AssociationRef assoc : addedAssocs.values())
         {
            targets.add(assoc.getTargetRef());
         }
         
         // add the targets for this particular association
         if (targets.size() > 0)
         {
             params.put(assocQName, (Serializable)targets);
         }
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
