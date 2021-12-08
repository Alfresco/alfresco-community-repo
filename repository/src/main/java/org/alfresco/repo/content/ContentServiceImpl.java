/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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
package org.alfresco.repo.content;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentServicePolicies.OnContentPropertyUpdatePolicy;
import org.alfresco.repo.content.ContentServicePolicies.OnContentReadPolicy;
import org.alfresco.repo.content.ContentServicePolicies.OnContentUpdatePolicy;
import org.alfresco.repo.content.cleanup.EagerContentStoreCleaner;
import org.alfresco.repo.content.directurl.DirectAccessUrlDisabledException;
import org.alfresco.repo.content.directurl.SystemWideDirectUrlConfig;
import org.alfresco.repo.content.filestore.FileContentStore;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.ClassPolicyDelegate;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.DirectAccessUrl;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.MimetypeServiceAware;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.usage.ContentQuotaException;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Service implementation acting as a level of indirection between the client
 * and the underlying content store.
 * <p>
 * Note: This class was formerly the {@link RoutingContentService} but the
 * 'routing' functionality has been pushed into the {@link AbstractRoutingContentStore store}
 * implementations.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class ContentServiceImpl implements ContentService, ApplicationContextAware
{
    private static final Log logger = LogFactory.getLog(ContentServiceImpl.class);
    
    private DictionaryService dictionaryService;
    private NodeService nodeService;
    private MimetypeService mimetypeService;
    private RetryingTransactionHelper transactionHelper;
    private ApplicationContext applicationContext;

    /** The cleaner that will ensure that rollbacks clean up after themselves */
    private EagerContentStoreCleaner eagerContentStoreCleaner;
    /** the store to use.  Any multi-store support is provided by the store implementation. */
    private ContentStore store;
    /** the store for all temporarily created content */
    private ContentStore tempStore;
    /** Should we consider zero byte content to be the same as no content? */
    private boolean ignoreEmptyContent;

    private SystemWideDirectUrlConfig systemWideDirectUrlConfig;

    /** pre-configured allow list of media/mime types, eg. specific types of images & also pdf */
    private Set<String> nonAttachContentTypes = Collections.emptySet();

    /**
     * The policy component
     */
    private PolicyComponent policyComponent;
    
    /*
     * Policies delegates
     */
    ClassPolicyDelegate<ContentServicePolicies.OnContentUpdatePolicy> onContentUpdateDelegate;
    ClassPolicyDelegate<ContentServicePolicies.OnContentPropertyUpdatePolicy> onContentPropertyUpdateDelegate;
    ClassPolicyDelegate<ContentServicePolicies.OnContentReadPolicy> onContentReadDelegate;
    
    public void setRetryingTransactionHelper(RetryingTransactionHelper helper)
    {
        this.transactionHelper = helper;
    }
    
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setMimetypeService(MimetypeService mimetypeService)
    {
        this.mimetypeService = mimetypeService;
    }

    public void setEagerContentStoreCleaner(EagerContentStoreCleaner eagerContentStoreCleaner)
    {
        this.eagerContentStoreCleaner = eagerContentStoreCleaner;
    }

    public void setStore(ContentStore store)
    {
        this.store = store;
    }

    public void setSystemWideDirectUrlConfig(SystemWideDirectUrlConfig systemWideDirectUrlConfig)
    {
        this.systemWideDirectUrlConfig = systemWideDirectUrlConfig;
    }

    public void setNonAttachContentTypes(String nonAttachAllowListStr)
    {
        if ((nonAttachAllowListStr != null) && (! nonAttachAllowListStr.isEmpty()))
        {
            nonAttachContentTypes = Set.of(nonAttachAllowListStr.trim().split("\\s*,\\s*"));
        }
    }

    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    /**
     * @deprecated The transformations code is being moved out of the codebase and replaced by the new async RenditionService2 or other external libraries.
     */
    public void setIgnoreEmptyContent(boolean ignoreEmptyContent)
    {
        this.ignoreEmptyContent = ignoreEmptyContent;
    }

    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;
    }

    /**
     * Service initialise 
     */
    public void init()
    {
        // Set up a temporary store
        this.tempStore = new FileContentStore(this.applicationContext, TempFileProvider.getTempDir().getAbsolutePath());

        // Bind on update properties behaviour
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
                this,
                new JavaBehaviour(this, "onUpdateProperties"));
        
        // Register on content update policy
        this.onContentUpdateDelegate = this.policyComponent.registerClassPolicy(OnContentUpdatePolicy.class);
        this.onContentPropertyUpdateDelegate = this.policyComponent.registerClassPolicy(OnContentPropertyUpdatePolicy.class);
        this.onContentReadDelegate = this.policyComponent.registerClassPolicy(OnContentReadPolicy.class);
    }
    
    /**
     * Update properties policy behaviour
     * 
     * @param nodeRef    the node reference
     * @param before    the before values of the properties
     * @param after        the after values of the properties
     */
    public void onUpdateProperties(
            NodeRef nodeRef,
            Map<QName, Serializable> before,
            Map<QName, Serializable> after)
    {
        // ALF-254: empty files (0 bytes) do not trigger content rules
        if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_NO_CONTENT))
        {
            return;
        }
    	
        // Don't duplicate work when firing multiple policies
        Set<QName> types = null;
        OnContentPropertyUpdatePolicy propertyPolicy = null;            // Doesn't change for the node instance
        // Variables to control firing of node-level policies (any content change)
        boolean fire = false;
        boolean isNewContent = false;
        // check if any of the content properties have changed
        for (QName propertyQName : after.keySet())
        {
            // is this a content property?
            PropertyDefinition propertyDef = dictionaryService.getProperty(propertyQName);
            if (propertyDef == null)
            {
                // the property is not recognised
                continue;
            }
            else if (!propertyDef.getDataType().getName().equals(DataTypeDefinition.CONTENT))
            {
                // not a content type
                continue;
            }
            else if (propertyDef.isMultiValued())
            {
                // We don't fire notifications for multi-valued content properties
                continue;
            }
            
            try
            {
                ContentData beforeValue = (ContentData) before.get(propertyQName);
                ContentData afterValue = (ContentData) after.get(propertyQName);
                boolean hasContentBefore = ContentData.hasContent(beforeValue)
                        && (!ignoreEmptyContent || beforeValue.getSize() > 0);
                boolean hasContentAfter = ContentData.hasContent(afterValue)
                        && (!ignoreEmptyContent || afterValue.getSize() > 0);
                
                // There are some shortcuts here
                if (!hasContentBefore && !hasContentAfter)
                {
                    // Really, nothing happened
                    continue;
                }
                else if (EqualsHelper.nullSafeEquals(beforeValue, afterValue))
                {
                    // Still, nothing happening
                    continue;
                }
                
                // Check for new content
                isNewContent = isNewContent || !hasContentBefore && hasContentAfter;
                
                // Make it clear when there's no content before or after
                if (!hasContentBefore)
                {
                    beforeValue = null;
                }
                if (!hasContentAfter)
                {
                    afterValue = null;
                }

                // So debug ...
                if (logger.isDebugEnabled())
                {
                    String name = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
                    logger.debug(
                            "Content property updated: \n" +
                            "   Node Name:   " + name + "\n" +
                            "   Property:    " + propertyQName + "\n" +
                            "   Is new:      " + isNewContent + "\n" +
                            "   Before:      " + beforeValue + "\n" +
                            "   After:       " + afterValue);
                }
                
                // Fire specific policy
                types = getTypes(nodeRef, types);
                if (propertyPolicy == null)
                {
                    propertyPolicy = onContentPropertyUpdateDelegate.get(nodeRef, types);
                }
                propertyPolicy.onContentPropertyUpdate(nodeRef, propertyQName, beforeValue, afterValue);
                
                // We also fire an event if *any* content property is changed
                fire = true;
            }
            catch (ClassCastException e)
            {
                // properties don't conform to model
                continue;
            }
        }
        // fire?
        if (fire)
        {
            // Fire the content update policy
            types = getTypes(nodeRef, types);
            OnContentUpdatePolicy policy = onContentUpdateDelegate.get(nodeRef, types);
            policy.onContentUpdate(nodeRef, isNewContent);
        }
    }
    
        
    /**
     * Helper method to lazily populate the types associated with a node
     * 
     * @param nodeRef           the node
     * @param types             any existing types
     * @return                  the types - either newly populated or just what was passed in
     */
    private Set<QName> getTypes(NodeRef nodeRef, Set<QName> types)
    {
        if (types != null)
        {
            return types;
        }
        types = new HashSet<QName>(this.nodeService.getAspects(nodeRef));
        types.add(this.nodeService.getType(nodeRef));
        return types;
    }

    @Override
    public long getStoreFreeSpace()
    {
        return store.getSpaceFree();
    }

    @Override
    public long getStoreTotalSpace()
    {
        return store.getSpaceTotal();
    }

    /** {@inheritDoc} */
    public ContentReader getRawReader(String contentUrl)
    {
        ContentReader reader = null;
        try
        {
            reader = store.getReader(contentUrl);
        }
        catch (UnsupportedContentUrlException e)
        {
            // The URL is not supported, so we spoof it
            reader = new EmptyContentReader(contentUrl);
        }
        if (reader == null)
        {
            throw new AlfrescoRuntimeException("ContentStore implementations may not return null ContentReaders");
        }
        // set extra data on the reader
        reader.setMimetype(MimetypeMap.MIMETYPE_BINARY);
        reader.setEncoding("UTF-8");
        reader.setLocale(I18NUtil.getLocale());
        
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug(
                    "Direct request for reader: \n" +
                    "   Content URL: " + contentUrl + "\n" +
                    "   Reader:      " + reader);
        }
        return reader;
    }

    public ContentReader getReader(NodeRef nodeRef, QName propertyQName)
    {
        return getReader(nodeRef, propertyQName, true);
    }
        
    @SuppressWarnings("unchecked")
    private ContentReader getReader(NodeRef nodeRef, QName propertyQName, boolean fireContentReadPolicy)
    {
        ContentData contentData = getContentData(nodeRef, propertyQName);

        // check that the URL is available
        if (contentData == null || contentData.getContentUrl() == null)
        {
            // there is no URL - the interface specifies that this is not an error condition
            return null;                                
        }
        String contentUrl = contentData.getContentUrl();
        
        // The context of the read is entirely described by the URL
        ContentReader reader = store.getReader(contentUrl);
        if (reader == null)
        {
            throw new AlfrescoRuntimeException("ContentStore implementations may not return null ContentReaders");
        }
        
        // set extra data on the reader
        reader.setMimetype(contentData.getMimetype());
        reader.setEncoding(contentData.getEncoding());
        reader.setLocale(contentData.getLocale());
        
        // Fire the content read policy
        if (reader != null && fireContentReadPolicy == true)
        {
            // Fire the content update policy
            Set<QName> types = new HashSet<QName>(this.nodeService.getAspects(nodeRef));
            types.add(this.nodeService.getType(nodeRef));
            OnContentReadPolicy policy = this.onContentReadDelegate.get(nodeRef, types);
            policy.onContentRead(nodeRef);
        }
        
        // we don't listen for anything
        // result may be null - but interface contract says we may return null
        return reader;
    }

    private ContentData getContentData(NodeRef nodeRef, QName propertyQName)
    {
        ContentData contentData = null;
        Serializable propValue = nodeService.getProperty(nodeRef, propertyQName);
        if (propValue instanceof Collection)
        {
            Collection<Serializable> colPropValue = (Collection<Serializable>)propValue;
            if (colPropValue.size() > 0)
            {
                propValue = colPropValue.iterator().next();
            }
        }

        if (propValue instanceof ContentData)
        {
            contentData = (ContentData)propValue;
        }

        if (contentData == null)
        {
            PropertyDefinition contentPropDef = dictionaryService.getProperty(propertyQName);
            
            // if no value or a value other content, and a property definition has been provided, ensure that it's CONTENT or ANY            
            if (contentPropDef != null && 
                (!(contentPropDef.getDataType().getName().equals(DataTypeDefinition.CONTENT) ||
                   contentPropDef.getDataType().getName().equals(DataTypeDefinition.ANY))))
            {
                throw new InvalidTypeException("The node property must be of type content: \n" +
                        "   node: " + nodeRef + "\n" +
                        "   property name: " + propertyQName + "\n" +
                        "   property type: " + ((contentPropDef == null) ? "unknown" : contentPropDef.getDataType()),
                        propertyQName);
            }
        }
        return contentData;
    }

    public ContentWriter getWriter(NodeRef nodeRef, QName propertyQName, boolean update)
    {
        if (nodeRef == null)
        {
            ContentContext ctx = new ContentContext(null, null);
            // for this case, we just give back a valid URL into the content store
            ContentWriter writer = store.getWriter(ctx);
            // Register the new URL for rollback cleanup
            eagerContentStoreCleaner.registerNewContentUrl(writer.getContentUrl());
            // done
            return writer;
        }
        
        // check for an existing URL - the get of the reader will perform type checking
        ContentReader existingContentReader = getReader(nodeRef, propertyQName, false);
        
        // get the content using the (potentially) existing content - the new content
        // can be wherever the store decides.
        ContentContext ctx = new NodeContentContext(existingContentReader, null, nodeRef, propertyQName);
        ContentWriter writer = store.getWriter(ctx);
        // Register the new URL for rollback cleanup
        eagerContentStoreCleaner.registerNewContentUrl(writer.getContentUrl());

        Serializable contentValue = nodeService.getProperty(nodeRef, propertyQName);

        // set extra data on the reader if the property is pre-existing
        if (contentValue != null && contentValue instanceof ContentData)
        {
            ContentData contentData = (ContentData)contentValue;
            writer.setMimetype(contentData.getMimetype());
            writer.setEncoding(contentData.getEncoding());
            writer.setLocale(contentData.getLocale());
        }
        
        // attach a listener if required
        if (update)
        {
            // need a listener to update the node when the stream closes
            WriteStreamListener listener = new WriteStreamListener(nodeService, nodeRef, propertyQName, writer);
            listener.setRetryingTransactionHelper(transactionHelper);
            writer.addListener(listener);
            
        }
        
        // supply the writer with a copy of the mimetype service if needed
        if (writer instanceof MimetypeServiceAware)
        {
            ((MimetypeServiceAware)writer).setMimetypeService(mimetypeService);
        }
        
        // give back to the client
        return writer;
    }

    /**
     * @return Returns a writer to an anonymous location
     */
    public ContentWriter getTempWriter()
    {
        // there is no existing content and we don't specify the location of the new content
        return tempStore.getWriter(ContentContext.NULL_CONTEXT);
    }

    /**
     * Ensures that, upon closure of the output stream, the node is updated with
     * the latest URL of the content to which it refers.
     * <p>
     * 
     * @author Derek Hulley
     */
    private static class WriteStreamListener extends AbstractContentStreamListener
    {
        private NodeService nodeService;
        private NodeRef nodeRef;
        private QName propertyQName;
        private ContentWriter writer;
        
        public WriteStreamListener(
                NodeService nodeService,
                NodeRef nodeRef,
                QName propertyQName,
                ContentWriter writer)
        {
            this.nodeService = nodeService;
            this.nodeRef = nodeRef;
            this.propertyQName = propertyQName;
            this.writer = writer;
        }
        
        public void contentStreamClosedImpl() throws ContentIOException
        {
            try
            {
                // set the full content property
                ContentData contentData = writer.getContentData();
                nodeService.setProperty(nodeRef, propertyQName, contentData);
                // done
                if (logger.isDebugEnabled())
                {
                    logger.debug("Stream listener updated node: \n" +
                            "   node: " + nodeRef + "\n" +
                            "   property: " + propertyQName + "\n" +
                            "   value: " + contentData);
                }
            }
            catch (ContentQuotaException qe)
            {
                throw qe;
            }
            catch (Throwable e)
            {
                throw new ContentIOException("Failed to set content property on stream closure: \n" +
                        "   node: " + nodeRef + "\n" +
                        "   property: " + propertyQName + "\n" +
                        "   writer: " + writer + "\n" + 
                        e.toString(),
                        e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isContentDirectUrlEnabled()
    {
        return systemWideDirectUrlConfig.isEnabled() && store.isContentDirectUrlEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isContentDirectUrlEnabled(NodeRef nodeRef, QName propertyQName)
    {
        boolean contentDirectUrlEnabled = false;

        // TODO: update this
        if (systemWideDirectUrlConfig.isEnabled())
        {
            ContentData contentData = getContentData(nodeRef, propertyQName);

            // check that the URL is available
            if (contentData == null || contentData.getContentUrl() == null)
            {
                throw new IllegalArgumentException("The supplied nodeRef " + nodeRef + " has no content.");
            }

            contentDirectUrlEnabled = (store.isContentDirectUrlEnabled(contentData.getContentUrl()));
        }

        return contentDirectUrlEnabled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DirectAccessUrl requestContentDirectUrl(NodeRef nodeRef, QName propertyQName, boolean attachment, Long validFor)
    {
        if (!systemWideDirectUrlConfig.isEnabled())
        {
            throw new DirectAccessUrlDisabledException("Direct access url isn't available.");
        }

        ContentData contentData = getContentData(nodeRef, propertyQName);
        // check that the content & URL is available
        if (contentData == null || contentData.getContentUrl() == null)
        {
            throw new IllegalArgumentException("The supplied nodeRef " + nodeRef + " has no content.");
        }

        String contentUrl = contentData.getContentUrl();
        String contentMimetype = contentData.getMimetype();
        String fileName = getFileName(nodeRef);

        validFor = adjustValidFor(validFor);
        attachment = adjustAttachment(nodeRef, contentMimetype, attachment);

        DirectAccessUrl directAccessUrl = null;
        if (store.isContentDirectUrlEnabled())
        {
            try
            {
                directAccessUrl = store.requestContentDirectUrl(contentUrl, attachment, fileName, contentMimetype, validFor);
            }
            catch (UnsupportedOperationException ex)
            {
                // expected exception
            }
        }
        return directAccessUrl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Experimental
    public Map<String, String> getStorageProperties(NodeRef nodeRef, QName propertyQName)
    {
        final ContentData contentData = getContentDataOrThrowError(nodeRef, propertyQName);
        return store.getStorageProperties(contentData.getContentUrl());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean requestSendContentToArchive(NodeRef nodeRef, QName propertyQName,
                                               Map<String, Serializable> archiveParams)
    {
        final ContentData contentData = getContentDataOrThrowError(nodeRef, propertyQName);
        return store.requestSendContentToArchive(contentData.getContentUrl(), archiveParams);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean requestRestoreContentFromArchive(NodeRef nodeRef, QName propertyQName, Map<String, Serializable> restoreParams)
    {
        final ContentData contentData = getContentDataOrThrowError(nodeRef, propertyQName);
        return store.requestRestoreContentFromArchive(contentData.getContentUrl(), restoreParams);
    }

    protected String getFileName(NodeRef nodeRef)
    {
        String fileName = null;

        try
        {
            fileName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
        }
        catch (InvalidNodeRefException ex)
        {
        }

        return fileName;
    }

    private Long adjustValidFor(Long validFor)
    {
        if (validFor == null || validFor > systemWideDirectUrlConfig.getDefaultExpiryTimeInSec())
        {
            validFor = systemWideDirectUrlConfig.getDefaultExpiryTimeInSec();
        }
         return validFor;
    }

    private boolean adjustAttachment(NodeRef nodeRef, String mimeType, boolean attachmentIn)
    {
        boolean attachment = true;
        if (! attachmentIn)
        {
            if ((nonAttachContentTypes != null) && (nonAttachContentTypes.contains(mimeType)))
            {
                attachment = false;
            }
            else
            {
                logger.warn("Ignored attachment=false for " + nodeRef.getId() + " since " + mimeType + " is not in the whitelist for non-attach content types");
            }
        }
        return attachment;
    }

    private ContentData getContentDataOrThrowError(NodeRef nodeRef, QName propertyQName)
    {
        final ContentData contentData = getContentData(nodeRef, propertyQName);

        if (contentData == null || contentData.getContentUrl() == null)
        {
            throw new IllegalArgumentException("The supplied nodeRef " + nodeRef + " and property name: " + propertyQName + " has no content.");
        }
        return contentData;
    }
}
