/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2025 - 2025 Alfresco Software Limited
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

import org.apache.commons.lang3.StringUtils;

public class UuidNodeIdRadixHasher implements NodeIdHasher
{
    private final int radix;

    public UuidNodeIdRadixHasher(int radix)
    {
        this.radix = radix;
    }

    @Override
    public String lookup(String hashUuid)
    {
        BigInteger nodeId = new BigInteger(hashUuid, radix);
        String nodeIdHex = nodeId.toString(16);
        String paddedNodeIdHex = StringUtils.leftPad(nodeIdHex, 32, "0");
        return String.join("-",
                paddedNodeIdHex.substring(0, 8),
                paddedNodeIdHex.substring(8, 12),
                paddedNodeIdHex.substring(12, 16),
                paddedNodeIdHex.substring(16, 20),
                paddedNodeIdHex.substring(20, 32));
    }

    @Override
    public String hash(String uuid)
    {
        String uuidWithoutDashes = uuid.replaceAll("-", "");
        BigInteger bigIntUuidWithoutDashesHex = new BigInteger(uuidWithoutDashes, 16);
        return bigIntUuidWithoutDashesHex.toString(radix);
    }
}
