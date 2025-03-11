/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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

import java.util.regex.Pattern;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.util.Pair;

/**
 * Creates string-pair hashes of {@link NodeRef}s where the first string is a stored hash combination for {@link NodeRef} store elements (protocol and id) and the second is a radix 36 encoded {@link NodeRef} id.
 */
public class NodeRefRadixHasher implements NodeRefHasher
{
    public static final NodeRefRadixHasher RADIX_36_HASHER = new NodeRefRadixHasher(36);

    static final Pattern UUID_PATTERN = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    static final String NOT_UUID_FORMAT_MARKER = "X";

    private final StoreRefHasher storeRefHasher;
    private final NodeIdHasher uuidNodeIdHasher;
    private final NodeIdHasher notUuidNodeIdHasher;

    public NodeRefRadixHasher()
    {
        this(16);
    }

    public NodeRefRadixHasher(int radix)
    {
        super();
        this.storeRefHasher = new StoredStoreRefHasher();
        this.uuidNodeIdHasher = new UuidNodeIdRadixHasher(radix);
        this.notUuidNodeIdHasher = new NotUuidNodeIdRadixHasher(radix);
    }

    @Override
    public Pair<String, String> hash(NodeRef nodeRef)
    {
        StoreRef storeRef = nodeRef.getStoreRef();
        String storeHash = storeRefHasher.hash(storeRef);

        String id = nodeRef.getId();
        String idHash = idToIdHash(id);

        return new Pair<>(storeHash, idHash);
    }

    private String idToIdHash(String id)
    {
        if (UUID_PATTERN.matcher(id).matches())
        {
            return uuidNodeIdHasher.hash(id);
        }
        return NOT_UUID_FORMAT_MARKER + notUuidNodeIdHasher.hash(id);
    }

    @Override
    public NodeRef lookup(Pair<String, String> hash)
    {
        String storeHash = hash.getFirst();
        StoreRef storeRef = storeRefHasher.lookup(storeHash);

        String hashId = hash.getSecond();
        String id = hashIdToId(hashId);

        return new NodeRef(storeRef, id);
    }

    private String hashIdToId(String hashId)
    {
        if (hashId.startsWith(NOT_UUID_FORMAT_MARKER))
        {
            String hashIdWithoutMarker = hashId.substring(NOT_UUID_FORMAT_MARKER.length());
            return notUuidNodeIdHasher.lookup(hashIdWithoutMarker);
        }
        return uuidNodeIdHasher.lookup(hashId);
    }
}
