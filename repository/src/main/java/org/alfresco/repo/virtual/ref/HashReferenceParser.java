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

/**
 * Hash encoded reference string parser.<br>
 * Delegates to {@link VirtualHashReferenceParser},
 * {@link VanillaHashReferenceParser} or {@link NodeHashReferenceParser} for
 * custom protocol parsing.
 */
public class HashReferenceParser implements ReferenceParser, HashEncodingArtefact
{
    /**
     * 
     */
    private static final long serialVersionUID = -2569625423953183530L;

    private NodeHashReferenceParser nodeReferenceParser;

    private VirtualHashReferenceParser virtualReferenceParser;

    private VanillaHashReferenceParser vanillaReferenceParser;

    public HashReferenceParser()
    {
        HashStore cpStore = HashStoreConfiguration.getInstance().getClasspathHashStore();

        nodeReferenceParser = new NodeHashReferenceParser(cpStore,
                                                          this);

        virtualReferenceParser = new VirtualHashReferenceParser(cpStore);

        vanillaReferenceParser = new VanillaHashReferenceParser(cpStore);
    }

    @Override
    public Reference parse(String referenceString) throws ReferenceParseException
    {
        String[] tokens = referenceString.split("-");
        Cursor c = new Cursor(tokens,
                              0);

        return parse(c);
    }

    public Reference parse(Cursor c)
    {
        if (NODE_PROTOCOL_CODE.equals(c.currentToken()))
        {
            return nodeReferenceParser.parse(c);
        }
        else if (VANILLA_PROTOCOL_CODE.equals(c.currentToken()))
        {
            return vanillaReferenceParser.parse(c);
        }
        else if (VIRTUAL_PROTOCOL_CODE.equals(c.currentToken()))
        {
            return virtualReferenceParser.parse(c);
        }
        else
        {
            throw new ReferenceEncodingException("Unknown reference code " + c.currentToken());
        }

    }
}
