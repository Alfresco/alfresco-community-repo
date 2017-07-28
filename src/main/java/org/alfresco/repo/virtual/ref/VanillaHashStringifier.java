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
 * Custom stringifier for hash encoded strings of {@link Reference}s having the
 * protocol set to {@link VanillaProtocol}.
 */
public class VanillaHashStringifier extends VirtualHashStringifier
{
    private static final long serialVersionUID = -2087786593789426927L;

    public VanillaHashStringifier(HashStore classpathHashStore, Stringifier referenceDispatcher)
    {
        super(classpathHashStore,
              referenceDispatcher);
    }

    @Override
    public String stringify(Reference reference) throws ReferenceEncodingException
    {
        final Protocol protocol = reference.getProtocol();
        if (!Protocols.VANILLA.protocol.equals(protocol))
        {
            throw new ReferenceEncodingException("Unsupported protocol " + protocol + "."
                        + Protocols.VIRTUAL.protocol.name + " exoected ");
        }

        String virtualString = stringifyVirtualReference(reference);

        ResourceParameter vanillaTemplateParam = (ResourceParameter) reference
                    .getParameters()
                        .get(VanillaProtocol.VANILLA_TEMPLATE_PARAM_INDEX);

        String vanillaString = stringify(vanillaTemplateParam.getValue());

        return VANILLA_PROTOCOL_CODE + "-" + virtualString + "-" + vanillaString;
    }
}
