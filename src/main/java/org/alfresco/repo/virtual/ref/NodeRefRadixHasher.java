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

import java.math.BigInteger;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.util.Pair;
import org.apache.commons.lang.StringUtils;

import com.sun.star.uno.RuntimeException;

/**
 * Creates string-pair hashes of {@link NodeRef}s where the first string is a
 * stored hash combination for {@link NodeRef} store elements (protocol and id)
 * and the second is a radix 36 encoded {@link NodeRef} id.
 */
public class NodeRefRadixHasher implements NodeRefHasher
{
    public static final NodeRefRadixHasher RADIX_36_HASHER = new NodeRefRadixHasher(36);

    private HashStore storeProtocolStore;

    private HashStore storeIdStore;

    private int radix;

    public NodeRefRadixHasher()
    {
        this(16);
    }

    public NodeRefRadixHasher(int radix)
    {
        super();
        this.radix = radix;
        this.storeProtocolStore = HashStoreConfiguration.getInstance().getStoreProtocolStore();
        this.storeIdStore = HashStoreConfiguration.getInstance().getStoreIdStore();
    }

    @Override
    public Pair<String, String> hash(NodeRef nodeRef)
    {
        String uuid = nodeRef.getId();

        if (uuid.length() != 36)
        {
            throw new RuntimeException("Invalid noderf id length " + uuid);
        }

        String bigInt16String = uuid.replaceAll("-",
                                                "");
        if (bigInt16String.length() != 32)
        {
            throw new RuntimeException("Invalid noderf id format " + uuid);
        }

        BigInteger bigIntId = new BigInteger(bigInt16String,
                                             16);
        StoreRef storeRef = nodeRef.getStoreRef();
        String storeProtocolHash = storeProtocolStore.hash(storeRef.getProtocol());
        String storeIdHash = storeIdStore.hash(storeRef.getIdentifier());
        if (storeProtocolHash == null || storeIdHash == null)
        {
            throw new RuntimeException("Missing hash for " + storeRef);
        }
        String storeHash = storeProtocolHash + storeIdHash;
        return new Pair<String, String>(storeHash,
                                        bigIntId.toString(radix));

    }

    @Override
    public NodeRef lookup(Pair<String, String> hash)
    {
        String storeHash = hash.getFirst();
        String storeProtocolHash = storeHash.substring(0,
                                                       1);
        String storeIdHash = storeHash.substring(1,
                                                 2);

        String storeProtocol = storeProtocolStore.lookup(storeProtocolHash);
        String storeId = storeIdStore.lookup(storeIdHash);
        if (storeProtocol == null || storeId == null)
        {
            throw new RuntimeException("Lookup found no protocol or id for " + storeHash);
        }
        BigInteger nodeId = new BigInteger(hash.getSecond(),
                                           radix);
        String nodeIdHexa = nodeId.toString(16);
        nodeIdHexa = StringUtils.leftPad(nodeIdHexa,
                                         32,
                                         "0");
        int leadZeros = 32 - nodeIdHexa.length();
        if (leadZeros > 0)
        {
        }
        String groups[] = new String[5];
        groups[0] = nodeIdHexa.substring(0,
                                         8);
        groups[1] = nodeIdHexa.substring(8,
                                         12);
        groups[2] = nodeIdHexa.substring(12,
                                         16);
        groups[3] = nodeIdHexa.substring(16,
                                         20);
        groups[4] = nodeIdHexa.substring(20,
                                         32);
        StringBuilder idBuilder = new StringBuilder(groups[0]);
        for (int i = 1; i < groups.length; i++)
        {
            idBuilder.append("-");
            idBuilder.append(groups[i]);
        }
        return new NodeRef(storeProtocol,
                           storeId,
                           idBuilder.toString());
    }
}
