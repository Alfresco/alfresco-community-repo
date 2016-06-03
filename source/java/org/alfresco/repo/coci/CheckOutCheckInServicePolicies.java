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
         * @param nodeRef NodeRef
         * @param destinationParentNodeRef NodeRef
         * @param destinationAssocTypeQName QName
         * @param destinationAssocQName QName
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
         * @param workingCopy - working copy
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
         * @param workingCopyNodeRef NodeRef
         * @param versionProperties Map<String,Serializable>
         * @param contentUrl String
         * @param keepCheckedOut boolean
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
        * @param nodeRef NodeRef
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
        * @param workingCopyNodeRef - working copy nodeRef
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
        * @param nodeRef NodeRef
        */
       void onCancelCheckOut(NodeRef nodeRef);
   }
}
