package org.alfresco.repo.content;

import org.alfresco.api.AlfrescoPublicApi;    
import org.alfresco.repo.policy.ClassPolicy;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Content service policies interface
 * 
 * @author Roy Wetherall
 * @author Derek Hulley
 */
public interface ContentServicePolicies
{
    /** @deprecated Use {@link OnContentUpdatePolicy#QNAME} */
    public static final QName ON_CONTENT_UPDATE = QName.createQName(NamespaceService.ALFRESCO_URI, "onContentUpdate");
    /** @deprecated Use {@link OnContentPropertyUpdatePolicy#QNAME} */
    public static final QName ON_CONTENT_PROPERTY_UPDATE = QName.createQName(NamespaceService.ALFRESCO_URI, "onContentPropertyUpdate");
    /** @deprecated Use {@link OnContentReadPolicy#QNAME} */
    public static final QName ON_CONTENT_READ = QName.createQName(NamespaceService.ALFRESCO_URI, "onContentRead");
    
	/**
	 * Policy that is raised once per node when any of the content properties on the node are
	 * changed; the specific properties are irrelevant.  This is primarily useful to determine
	 * when a new file is introduced into the system.
	 */
	@AlfrescoPublicApi
	public interface OnContentUpdatePolicy extends ClassPolicy
	{
	    public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "onContentUpdate");
		/**
		 * @param nodeRef	the node reference
		 */
		public void onContentUpdate(NodeRef nodeRef, boolean newContent);
	}
    
    /**
     * Policy that is raised for each content property change.  Any policy implementations must be aware
     * that the transaction in which this is called could still roll back; no filesystem changes should
     * occur against the source content until after the transaction has <u>successfully</u> completed.
     * 
     * @since 3.2
     */
    @AlfrescoPublicApi
    public interface OnContentPropertyUpdatePolicy extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "onContentPropertyUpdate");
        /**
         * @param nodeRef           the node reference
         * @param propertyQName     the name of the property that changed
         * @param beforeValue       the value of the content data prior to the change.
         *                          Note that this value may be <tt>null</tt> or any of it's member
         *                          values may be <tt>null</tt> according to the contract of the
         *                          {@link ContentData} class.
         * @param afterValue        the value of the content data after the change
         * 
         * @see ContentData#hasContent(ContentData)
         * @see RoutingContentService#onUpdateProperties(NodeRef, java.util.Map, java.util.Map)
         * @since 3.2
         */
        public void onContentPropertyUpdate(
                NodeRef nodeRef,
                QName propertyQName,
                ContentData beforeValue,
                ContentData afterValue);
    }
    
    /**
     * On content read policy interface.
     * 
     * This policy is fired when a content reader is requested for a node that has content.
     */
    @AlfrescoPublicApi
    public interface OnContentReadPolicy extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "onContentRead");
        /**
         * @param nodeRef   the node reference
         */
        public void onContentRead(NodeRef nodeRef);
    }
}
