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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.AbstractRoutingContentStore;
import org.alfresco.repo.content.ContentContext;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.NodeContentContext;
import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
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
public class StoreSelectorAspectContentStore extends AbstractRoutingContentStore implements InitializingBean
{
    private static final String ERR_INVALID_DEFAULT_STORE = "content.routing.err.invalid_default_store";
    
    private static Log logger = LogFactory.getLog(StoreSelectorAspectContentStore.class);

    private NodeService nodeService;
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
