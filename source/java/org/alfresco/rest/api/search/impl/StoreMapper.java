/*-
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.rest.api.search.impl;

import org.alfresco.repo.version.Version2Model;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Maps to and from a StoreRef for the json public api.
 *
 * @author Gethin James
 */
public class StoreMapper
{

    public static final String LIVE_NODES = "nodes";
    public static final String VERSIONS = "versions";
    public static final String DELETED = "deleted-nodes";

    private static Log logger = LogFactory.getLog(StoreMapper.class);

    public static final StoreRef STORE_REF_VERSION2_SPACESSTORE = new StoreRef("workspace", Version2Model.STORE_ID);

    /**
     * Work out which StoreRef this store belongs to.
     * @param String representing a store
     * @return StoreRef
     */
    public StoreRef getStoreRef(String store)
    {
        if (store != null && !store.isEmpty())
        {
            switch (store.toLowerCase())
            {
                case LIVE_NODES:
                    return StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
                case VERSIONS:
                    return STORE_REF_VERSION2_SPACESSTORE;
                case DELETED:
                    return StoreRef.STORE_REF_ARCHIVE_SPACESSTORE;
            }
        }
        throw new InvalidArgumentException(InvalidArgumentException.DEFAULT_MESSAGE_ID,
                    new Object[] { ": scope allowed values: nodes,deleted-nodes,versions" });
    }

    /**
     * Work out which store this noderef belongs to.
     * @param nodeRef
     * @return String representing a store
     */
    public String getStore(NodeRef nodeRef)
    {
        if (nodeRef != null)
        {
            if (StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.equals(nodeRef.getStoreRef()))
            {
                return LIVE_NODES;
            }

            if (STORE_REF_VERSION2_SPACESSTORE.equals(nodeRef.getStoreRef()))
            {
                return VERSIONS;
            }

            if (StoreRef.STORE_REF_ARCHIVE_SPACESSTORE.equals(nodeRef.getStoreRef()))
            {
                return DELETED;
            }
        }

        logger.warn("Unknown store ref: "+nodeRef);
        return null;
    }
}
