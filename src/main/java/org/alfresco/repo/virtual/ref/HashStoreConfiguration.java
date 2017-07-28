/*
 * #%L
 * Alfresco Repository
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

package org.alfresco.repo.virtual.ref;

import org.alfresco.repo.version.Version2Model;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.version.VersionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Global hash store configuration.<br>
 * Used for custom global {@link HashStore}s required for {@link Reference}
 * string encoding.
 */
public class HashStoreConfiguration
{
    private static Log logger = LogFactory.getLog(HashStoreConfiguration.class);

    public static class HashStoreConfigurationBean
    {
        public void setClasspathsHashes(String classpathsHashes)
        {
            String[] hashes = classpathsHashes.split(",");
            for (int i = 0; i < hashes.length; i++)
            {
                String[] pathHash = hashes[i].split("->");
                if (pathHash.length != 2)
                {
                    logger.error("Invalid classpath hash configuration " + hashes[i]);
                }
                else
                {
                    HashStoreConfiguration.getInstance().getClasspathHashStore().put(pathHash[0],
                                                                                     pathHash[1]);
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Configured classpath hash " + pathHash[0] + " -> " + pathHash[1]);
                    }
                }
            }
        }
    }

    private static HashStoreConfiguration instance = null;

    public static synchronized HashStoreConfiguration getInstance()
    {
        if (instance == null)
        {
            instance = new HashStoreConfiguration();
        }

        return instance;
    }

    private final HashStore classpathStore;

    private HashStore storeProtocolStore;

    private HashStore storeIdStore;

    private HashStoreConfiguration()
    {
        classpathStore = new HashStore();

        storeProtocolStore = new HashStore();

        storeIdStore = new HashStore();

        storeProtocolStore.put(StoreRef.PROTOCOL_WORKSPACE,
                               "1");
        storeProtocolStore.put(StoreRef.PROTOCOL_ARCHIVE,
                               "2");
        storeProtocolStore.put(StoreRef.PROTOCOL_AVM,
                               "3");
        storeProtocolStore.put(StoreRef.PROTOCOL_DELETED,
                               "4");
        storeProtocolStore.put(StoreRef.PROTOCOL_TEST,
                               "5");
        storeProtocolStore.put(VersionService.VERSION_STORE_PROTOCOL,
                               "6");
        storeIdStore.put("SpacesStore",
                         "1");
        storeIdStore.put(VersionModel.STORE_ID,
                         "2");
        storeIdStore.put(Version2Model.STORE_ID,
                         "3");
    }

    public HashStore getClasspathHashStore()
    {
        return classpathStore;
    }

    public HashStore getStoreIdStore()
    {
        return this.storeIdStore;
    }

    public HashStore getStoreProtocolStore()
    {
        return this.storeProtocolStore;
    }
}
