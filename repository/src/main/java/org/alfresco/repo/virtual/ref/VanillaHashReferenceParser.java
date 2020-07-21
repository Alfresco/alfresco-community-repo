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
/**
 * Custom parser for hash encoded strings of {@link Reference}s having the
 * protocol set to {@link VanillaProtocol}.
 */
public class VanillaHashReferenceParser extends VirtualHashReferenceParser
{

    public VanillaHashReferenceParser(HashStore classpathHashStore)
    {
        super(classpathHashStore,
              VANILLA_PROTOCOL_CODE);
    }

    @Override
    protected Reference parseVirtualExtension(Cursor c, Resource templateResource, String templatePath,
                Resource actualNodeResource)
    {
        Resource vanillaTemplateResource = parseResource(c);
        // TODO :parse vanilla template
        List<Parameter> extraParameters = Collections.<Parameter> emptyList();
        return ((VanillaProtocol) Protocols.VANILLA.protocol).newReference(Encodings.HASH.encoding,
                                                                           templateResource,
                                                                           templatePath,
                                                                           actualNodeResource,
                                                                           vanillaTemplateResource,
                                                                           extraParameters);
    }
}
