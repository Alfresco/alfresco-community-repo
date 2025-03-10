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
        String nodeIdHexa = nodeId.toString(16);
        nodeIdHexa = StringUtils.leftPad(nodeIdHexa, 32, "0");
        String[] groups = new String[5];
        groups[0] = nodeIdHexa.substring(0, 8);
        groups[1] = nodeIdHexa.substring(8, 12);
        groups[2] = nodeIdHexa.substring(12, 16);
        groups[3] = nodeIdHexa.substring(16, 20);
        groups[4] = nodeIdHexa.substring(20, 32);
        StringBuilder idBuilder = new StringBuilder(groups[0]);
        for (int i = 1; i < groups.length; i++)
        {
            idBuilder.append("-");
            idBuilder.append(groups[i]);
        }
        return idBuilder.toString();
    }

    @Override
    public String hash(String uuid)
    {
        String uuidWithoutDashes = uuid.replaceAll("-", "");
        BigInteger bigIntId = new BigInteger(uuidWithoutDashes, 16);
        return bigIntId.toString(radix);
    }
}
