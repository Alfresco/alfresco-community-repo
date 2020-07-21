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

import java.util.Collections;
import java.util.List;


import org.alfresco.repo.virtual.ref.ReferenceParser.Cursor;
import org.alfresco.util.Pair;
/**
 * Custom parser for hash encoded strings of {@link Reference}s having the
 * protocol set to {@link VirtualProtocol}.
 */
public class VirtualHashReferenceParser extends ProtocolHashParser
{
    private String protocolCode;

    private NumericPathHasher numericPathHasher = new NumericPathHasher();

    public VirtualHashReferenceParser(HashStore classpathHashStore)
    {
        this(classpathHashStore,
             VIRTUAL_PROTOCOL_CODE);
    }

    public VirtualHashReferenceParser(HashStore classpathHashStore, String protocolCode)
    {
        super(classpathHashStore);
        this.protocolCode = protocolCode;
    }

    @Override
    public Reference parse(Cursor cursor) throws ReferenceParseException
    {
        if (!protocolCode.equals(cursor.currentToken()))
        {
            throw new ReferenceParseException("Node token \"" + protocolCode + "\" expected instead of \""
                        + cursor.currentToken() + "\"");
        }
        cursor.i++;

        Resource templateResource = parseResource(cursor);

        Resource actualNodeResource = parseResource(cursor);

        String pathToken = cursor.nextToken();
        String pathCode = pathToken.substring(0,
                                              1);
        String templatePath;

        if (HASHED_NUMERIC_PATH_CODE.equals(pathCode))
        {
            String pathHash = pathToken.substring(1);
            templatePath = numericPathHasher.lookup(new Pair<String, String>(pathHash,
                                                                             null));
        }
        else if (NUMERIC_ROOT_PATH_CODE.equals(pathCode))
        {
            templatePath = "/";
        }
        else if (NUMERIC_PATH_CODE.equals(pathCode))
        {
            String pathNonHashed = pathToken.substring(1);
            templatePath = numericPathHasher.lookup(new Pair<String, String>(null,
                                                                             pathNonHashed));
        }
        else if (MIXED_NUMERIC_PATH_CODE.equals(pathCode))
        {
            String pathHash = pathToken.substring(1);
            String pathNonHashed = cursor.nextToken();
            templatePath = numericPathHasher.lookup(new Pair<String, String>(pathHash,
                                                                             pathNonHashed));
        }
        else
        {
            throw new ReferenceParseException("Pnvalid path token code " + pathCode);
        }

        return parseVirtualExtension(cursor,
                                     templateResource,
                                     templatePath,
                                     actualNodeResource);

    }

    protected Reference parseVirtualExtension(Cursor c, Resource templateResource, String templatePath,
                Resource actualNodeResource)
    {
        List<Parameter> extraParameters = Collections.<Parameter> emptyList();
        return ((VirtualProtocol) Protocols.VIRTUAL.protocol).newReference(Encodings.HASH.encoding,
                                                                           templateResource,
                                                                           templatePath,
                                                                           actualNodeResource,
                                                                           extraParameters);
    }
}
