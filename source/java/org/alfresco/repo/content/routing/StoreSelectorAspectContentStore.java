/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.content.routing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.AbstractRoutingContentStore;
import org.alfresco.repo.content.ContentContext;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.NodeContentContext;
import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Implementation of a {@link AbstractRoutingContentStore routing content store} that diverts
 * and moves content based on the <b>cm:storeSelector</b> aspect.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class StoreSelectorAspectContentStore
            extends AbstractRoutingContentStore
            implements InitializingBean,
                        NodeServicePolicies.OnUpdatePropertiesPolicy,
                        NodeServicePolicies.OnAddAspectPolicy
{
    private static final String ERR_INVALID_DEFAULT_STORE = "content.routing.err.invalid_default_store";
    private static final String KEY_CONTENT_MOVE_DETAILS = "StoreSelectorAspectContentStore.ContentMoveDetails";
    
    private static Log logger = LogFactory.getLog(StoreSelectorAspectContentStore.class);

    private ContentMoveTransactionListener transactionListener;
    private NodeService nodeService;
    private PolicyComponent policyComponent;
    private DictionaryService dictionaryService;
    private Map<String, ContentStore> storesByName;
    private List<ContentStore> stores;
    private String defaultStoreName;
    
    public StoreSelectorAspectContentStore()
    {
    }

    /**
     * @param nodeService           the service to access the properties
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param policyComponent       register to receive updates to the <b>cm:storeSelector</b> aspect
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    /**
     * @param dictionaryService     used to check for content property types
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @param storesByName          a map of content stores keyed by a common name
     */
    public void setStoresByName(Map<String, ContentStore> storesByName)
    {
        this.storesByName = storesByName;
        this.stores = new ArrayList<ContentStore>(storesByName.values());
    }

    /**
     * @return                      Returns the stores keyed by store name
     */
    public Map<String, ContentStore> getStoresByName()
    {
        return storesByName;
    }

    /**
     * Set the name of the store to select if the content being created is not associated
     * with any specific value in the <b>cm:storeSelector</b> or if the aspect is not
     * present.
     * 
     * @param defaultStoreName      the name of one of the stores
     * 
     * @see #setStoresByName(Map)
     */
    public void setDefaultStoreName(String defaultStoreName)
    {
        this.defaultStoreName = defaultStoreName;
    }

    /**
     * Checks that the required properties are present
     */
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "storesByName", storesByName);
        PropertyCheck.mandatory(this, "defaultStoreName", defaultStoreName);
        // Check that the default store name is valid
        if (storesByName.get(defaultStoreName) == null)
        {
            AlfrescoRuntimeException.create(ERR_INVALID_DEFAULT_STORE, defaultStoreName, storesByName.keySet());
        }
        // Register to receive change updates relevant to the aspect
        // Register to receive property change updates
        policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onAddAspect"),
                ContentModel.ASPECT_STORE_SELECTOR,
                new JavaBehaviour(this, "onAddAspect"));
        policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateProperties"),
                ContentModel.ASPECT_STORE_SELECTOR,
                new JavaBehaviour(this, "onUpdateProperties"));
        
        // Construct the transaction listener that will be bound in
        transactionListener = new ContentMoveTransactionListener();
    }

    @Override
    protected List<ContentStore> getAllStores()
    {
        return stores;
    }

    @Override
    protected ContentStore selectWriteStore(ContentContext ctx)
    {
        ContentStore store;
        String storeNameProp;
        if (!(ctx instanceof NodeContentContext))
        {
            storeNameProp = "<NodeRef not available>";
            store = storesByName.get(defaultStoreName);
        }
        else
        {
            NodeRef nodeRef = ((NodeContentContext) ctx).getNodeRef();      // Never null
            storeNameProp = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_STORE_NAME);
            if (storeNameProp == null)
            {
                storeNameProp = "<null>";
                store = storesByName.get(defaultStoreName);
            }
            else
            {
                store = storesByName.get(storeNameProp);
                if (store == null)
                {
                    // There was no store with that name
                    storeNameProp = "<unmapped store: " + storeNameProp + ">";
                    store = storesByName.get(defaultStoreName);
                }
            }
        }
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug(
                    "ContentStore selected: \n" +
                    "   Node context:   " + ctx + "\n" +
                    "   Store name:     " + storeNameProp + "\n" +
                    "   Store Selected: " + store);
        }
        return store;
    }
    
    /**
     * Helper method to select a store, taking into account <tt>null</tt> and invalid values.
     */
    private ContentStore selectStore(String storeName)
    {
        if (storeName == null || !storesByName.containsKey(storeName))
        {
            storeName = defaultStoreName;
        }
        return storesByName.get(storeName);
    }
    
    /**
     * Class to carry info into the post-transaction phase
     */
    private static class ContentMoveDetail
    {
        private final ContentStore oldStore;
        private final ContentStore newStore;
        private final String contentUrl;
        private ContentMoveDetail(ContentStore oldStore, ContentStore newStore, String contentUrl)
        {
            this.oldStore = oldStore;
            this.newStore = newStore;
            this.contentUrl = contentUrl;
        }
    }
    /**
     * Ensures that the content is copied between stores only if the transaction is successful.
     * 
     * @author Derek Hulley
     * @since 3.2
     */
    private class ContentMoveTransactionListener extends TransactionListenerAdapter
    {
        @Override
        public void afterCommit()
        {
            List<ContentMoveDetail> contentMoveDetails = TransactionalResourceHelper.getList(KEY_CONTENT_MOVE_DETAILS);
            for (ContentMoveDetail contentMoveDetail : contentMoveDetails)
            {
                moveContent(contentMoveDetail.oldStore, contentMoveDetail.newStore, contentMoveDetail.contentUrl);
            }
        }
    }

    /**
     * Move content from the old store to the new store
     */
    private void scheduleContentMove(ContentStore oldStore, ContentStore newStore, String contentUrl)
    {
        // Add the details of the copy to the transaction
        List<ContentMoveDetail> contentMoveDetails = TransactionalResourceHelper.getList(KEY_CONTENT_MOVE_DETAILS);
        ContentMoveDetail detail = new ContentMoveDetail(oldStore, newStore, contentUrl);
        contentMoveDetails.add(detail);
        // Bind the listener to the transaction
        AlfrescoTransactionSupport.bindListener(transactionListener);
    }
    
    private void moveContent(ContentStore oldStore, ContentStore newStore, String contentUrl)
    {
        ContentReader reader = oldStore.getReader(contentUrl);
        if (!reader.exists())
        {
            // Nothing to copy
            return;
        }
        ContentContext ctx = new ContentContext(null, contentUrl);
        ContentWriter writer = newStore.getWriter(ctx);
        // Copy it
        writer.putContent(reader);
        // Remove the old content
        oldStore.delete(contentUrl);
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug(
                    "Store selector moved content: \n" +
                    "   Old store: " + oldStore + "\n" +
                    "   New Store: " + newStore + "\n" +
                    "   Content:   " + contentUrl);
        }
    }
    
    /**
     * Ensures that all content is moved to the correct store.
     * <p>
     * Spoofs a call to {@link #onUpdateProperties(NodeRef, Map, Map)}.
     */
    public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName)
    {
        Map<QName, Serializable> after = nodeService.getProperties(nodeRef);
        // Pass the call through.  It is only interested in a single property.
        onUpdateProperties(
                nodeRef,
                Collections.<QName, Serializable>emptyMap(),
                after);
    }

    /**
     * Keeps the content in the correct store based on changes to the <b>cm:storeName</b> property
     */
    public void onUpdateProperties(
            NodeRef nodeRef,
            Map<QName, Serializable> before,
            Map<QName, Serializable> after)
    {
        String storeNameBefore = (String) before.get(ContentModel.PROP_STORE_NAME);
        String storeNameAfter = (String) after.get(ContentModel.PROP_STORE_NAME);
        if (EqualsHelper.nullSafeEquals(storeNameBefore, storeNameAfter))
        {
            // We're not interested in the change
            return;
        }
        // Find out which store to move the content to
        ContentStore oldStore = selectStore(storeNameBefore);
        ContentStore newStore = selectStore(storeNameAfter);
        // Don't do anything if the store did not change
        if (oldStore == newStore)
        {
            return;
        }
        // Find all content properties and move the content
        List<String> contentUrls = new ArrayList<String>(1);
        for (QName propertyQName : after.keySet())
        {
            PropertyDefinition propDef = dictionaryService.getProperty(propertyQName);
            if (propDef == null)
            {
                // Ignore
                continue;
            }
            if (!propDef.getDataType().getName().equals(DataTypeDefinition.CONTENT))
            {
                // It is not content
                continue;
            }
            // The property value
            Serializable propertyValue = after.get(propertyQName);
            if (propertyValue == null)
            {
                // Ignore missing values
            }
            // Get the content URLs, being sensitive to collections
            if (propDef.isMultiValued())
            {
                Collection<ContentData> contentValues =
                        DefaultTypeConverter.INSTANCE.getCollection(ContentData.class, propertyValue);
                if (contentValues.size() == 0)
                {
                    // No content
                    continue;
                }
                for (ContentData contentValue : contentValues)
                {
                    String contentUrl = contentValue.getContentUrl();
                    if (contentUrl != null)
                    {
                        contentUrls.add(contentUrl);
                    }
                }
            }
            else
            {
                ContentData contentValue = DefaultTypeConverter.INSTANCE.convert(ContentData.class, propertyValue);
                String contentUrl = contentValue.getContentUrl();
                if (contentUrl != null)
                {
                    contentUrls.add(contentUrl);
                }
            }
        }
        // Move content from the old store to the new store
        for (String contentUrl : contentUrls)
        {
            scheduleContentMove(oldStore, newStore, contentUrl);
        }
    }

    /**
     * A constraint that acts as a list of values, where the values are the store names
     * injected into the {@link StoreSelectorAspectContentStore}.
     * <p>
     * If the store is not active or is incorrectly configured, then this constraint
     * will contain a single value of 'Default'.  Any attempt to set another value will
     * lead to constraint failures.
     * 
     * @author Derek Hulley
     * @since 3.2
     */
    public static class StoreSelectorConstraint extends ListOfValuesConstraint
    {
        private StoreSelectorAspectContentStore store;
        /**
         * Required default constructor
         */
        public StoreSelectorConstraint()
        {
        }

        public void setStore(StoreSelectorAspectContentStore store)
        {
            this.store = store;
        }

        @Override
        public void initialize()
        {
            checkPropertyNotNull("store", store);
            List<String> allowedValues = new ArrayList<String>(store.getStoresByName().keySet());
            super.setAllowedValues(allowedValues);
            // Now initialize as we have set the LOV
            super.initialize();
        }
    }
}
