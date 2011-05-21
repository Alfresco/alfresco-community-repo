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
package org.alfresco.repo.site;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies.OnMoveNodePolicy;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * Site aspect behaviour bean.
 * 
 * Renames are not allowed, because the relationship between a site and 
 * its authorities is based on a pattern using the name.
 * 
 * @author Nick Burch
 */
public class SiteAspect implements NodeServicePolicies.OnMoveNodePolicy
{
   /** Services */
   private DictionaryService dictionaryService;
   private PolicyComponent policyComponent;
   private NodeService nodeService;
   
   /**
    * Set the dictionary service
    * 
    * @param dictionaryService   dictionary service
    */
   public void setDictionaryService(DictionaryService dictionaryService)
   {
       this.dictionaryService = dictionaryService;
   }
   
   /**
    * Set the policy component
    * 
    * @param policyComponent   policy component
    */
   public void setPolicyComponent(PolicyComponent policyComponent)
   {
       this.policyComponent = policyComponent;
   }
   
   /**
    * Set the node service
    * 
    * @param nodeService   node service
    */
   public void setNodeService(NodeService nodeService)
   {
       this.nodeService = nodeService;
   }
   
   /**
    * Initialise method
    */
   public void init()
   {
       this.policyComponent.bindClassBehaviour(
               OnMoveNodePolicy.QNAME, 
               SiteModel.TYPE_SITE, 
               new JavaBehaviour(this, "onMoveNode", Behaviour.NotificationFrequency.EVERY_EVENT));
       
       this.policyComponent.bindClassBehaviour(OnMoveNodePolicy.QNAME,
               SiteModel.ASPECT_SITE_CONTAINER,
               new JavaBehaviour(this, "onMoveNode", Behaviour.NotificationFrequency.EVERY_EVENT));
   }

   /**
    * Deny renames.
    */
   public void onMoveNode(ChildAssociationRef oldChildAssocRef,
         ChildAssociationRef newChildAssocRef) 
   {
      NodeRef oldParent = oldChildAssocRef.getParentRef();
      NodeRef newParent = newChildAssocRef.getParentRef();
      
      // Deny renames
      if (oldParent.equals(newParent))
      {
          QName type = nodeService.getType((oldChildAssocRef.getChildRef()));
          if (dictionaryService.isSubClass(type, SiteModel.TYPE_SITE))
          {
              throw new SiteServiceException("Sites can not be renamed.");
          }
          else
          {
              throw new SiteServiceException("Site containers can not be renamed.");
          }
      }
   }
}
