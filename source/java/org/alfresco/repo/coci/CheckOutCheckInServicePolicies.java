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
package org.alfresco.repo.coci;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.policy.ClassPolicy;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Policy interfaces for the check in/check out service
 * 
 * @author Roy Wetherall
 */
public interface CheckOutCheckInServicePolicies
{
    /**
     *
     */
    public interface BeforeCheckOut extends ClassPolicy
    {
        static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "beforeCheckOut");
          
        /**
         * 
         * @param nodeRef
         */
        void beforeCheckOut(
                NodeRef nodeRef,
                NodeRef destinationParentNodeRef,           
                QName destinationAssocTypeQName, 
                QName destinationAssocQName);
    }
    
    /**
     *
     */
    public interface OnCheckOut extends ClassPolicy
    {
        static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "onCheckOut");
          
        /**
         * 
         * @param nodeRef
         */
        void onCheckOut(NodeRef workingCopy);
    }
    
    /**
     *
     */
    public interface BeforeCheckIn extends ClassPolicy
    {
        static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "beforeCheckIn");
        
        /**
         * 
         * @param workingCopyNodeRef
         * @param versionProperties
         * @param contentUrl
         * @param keepCheckedOut
         */
        void beforeCheckIn(
                NodeRef workingCopyNodeRef,
                Map<String,Serializable> versionProperties,
                String contentUrl,
                boolean keepCheckedOut);
    }
    
    /**
     *
     */
   public interface OnCheckIn extends ClassPolicy
   {
       static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "onCheckIn");
       
       /**
        * 
        * @param nodeRef
        */
       void onCheckIn(NodeRef nodeRef);
   }
   
   /**
   *
   */
   public interface BeforeCancelCheckOut extends ClassPolicy
   {
       static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "beforeCancelCheckOut");
         
       /**
        * 
        * @param nodeRef
        */
       void beforeCancelCheckOut(NodeRef workingCopyNodeRef);
   }
   
   /**
   *
   */
   public interface OnCancelCheckOut extends ClassPolicy
   {
       static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "onCancelCheckOut");
         
       /**
        * 
        * @param nodeRef
        */
       void onCancelCheckOut(NodeRef nodeRef);
   }
}
