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

import org.alfresco.repo.virtual.ref.ReferenceParser.Cursor;

/**
 * Custom parser for hash encoded strings of {@link Reference}s having the
 * protocol set to {@link NodeProtocol}.
 */
public class NodeHashReferenceParser extends ProtocolHashParser
{
    private HashReferenceParser referenceParser;

    public NodeHashReferenceParser(HashStore classpathHashStore, HashReferenceParser referenceParser)
    {
        super(classpathHashStore);
        this.referenceParser = referenceParser;
    }

    @Override
    public Reference parse(Cursor cursor) throws ReferenceParseException
    {
        if (!NODE_PROTOCOL_CODE.equals(cursor.currentToken()))
        {
            throw new ReferenceParseException("Node token \"" + NODE_PROTOCOL_CODE + "\" expected instead of \""
                        + cursor.currentToken() + "\"");
        }
        cursor.i++;

        Resource resource = parseResource(cursor);

        Reference parentReference = referenceParser.parse(cursor);

        return NodeProtocol.newReference(Encodings.HASH.encoding,
                                         resource,
                                         parentReference);

    }

}
