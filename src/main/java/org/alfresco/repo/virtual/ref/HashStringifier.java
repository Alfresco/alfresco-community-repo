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

import java.util.List;

/**
 * Hash encoded reference stringifier.<br>
 * Delegates to {@link VanillaHashStringifier},
 * {@link VirtualHashStringifier} or {@link NodeHashStringifier} for
 * custom protocol parsing.
 */
public class HashStringifier implements Stringifier
{

    /**
     * 
     */
    private static final long serialVersionUID = 2213824445193662644L;

    private NodeHashStringifier nodeStringifier;

    private VirtualHashStringifier virtualStringifier;

    private VanillaHashStringifier vanillaStringifier;

    public HashStringifier()
    {
        HashStore cpStore = HashStoreConfiguration.getInstance().getClasspathHashStore();
        nodeStringifier = new NodeHashStringifier(cpStore,
                                                  this);
        virtualStringifier = new VirtualHashStringifier(cpStore,
                                                        this);

        vanillaStringifier = new VanillaHashStringifier(cpStore,
                                                        this);
    }

    @Override
    public String stringify(Reference reference) throws ReferenceEncodingException
    {
        final Protocol protocol = reference.getProtocol();

        if (Protocols.NODE.protocol.equals(protocol))
        {
            return nodeStringifier.stringify(reference);
        }
        else if (Protocols.VIRTUAL.protocol.equals(protocol))
        {
            return virtualStringifier.stringify(reference);
        }
        else if (Protocols.VANILLA.protocol.equals(protocol))
        {
            return vanillaStringifier.stringify(reference);
        }
        else
        {
            return stringifyUnknown(reference);
        }
    }

    private String stringifyUnknown(Reference reference) throws ReferenceEncodingException
    {
        throw new ReferenceEncodingException("Could not stringify unknown protocol reference s"
                    + reference.encode(Encodings.PLAIN.encoding));
    }

    @Override
    public String stringify(Resource resource) throws ReferenceEncodingException
    {
        throw new ReferenceEncodingException("Unknown hash protocol.");
    }

    @Override
    public String stringifyResource(Resource resource) throws ReferenceEncodingException
    {
        throw new ReferenceEncodingException("Unknown hash protocol.");
    }

    @Override
    public String stringifyResource(RepositoryResource resource) throws ReferenceEncodingException
    {
        throw new ReferenceEncodingException("Unknown hash protocol.");
    }

    @Override
    public String stringifyResource(ClasspathResource resource) throws ReferenceEncodingException
    {
        throw new ReferenceEncodingException("Unknown hash protocol.");
    }

    @Override
    public String stringify(RepositoryLocation repositoryLocation) throws ReferenceEncodingException
    {
        throw new ReferenceEncodingException("Unknown hash protocol.");
    }

    @Override
    public String stringifyRepositoryLocation(RepositoryLocation repositoryLocation) throws ReferenceEncodingException
    {
        throw new ReferenceEncodingException("Unknown hash protocol.");
    }

    @Override
    public String stringifyRepositoryLocation(RepositoryNodeRef repositoryNodeRef) throws ReferenceEncodingException
    {
        throw new ReferenceEncodingException("Unknown hash protocol.");
    }

    @Override
    public String stringifyRepositoryLocation(RepositoryPath repositoryPath) throws ReferenceEncodingException
    {
        throw new ReferenceEncodingException("Unknown hash protocol.");
    }

    @Override
    public String stringify(List<Parameter> parameters) throws ReferenceEncodingException
    {
        throw new ReferenceEncodingException("Unknown hash protocol.");
    }

    @Override
    public String stringify(Parameter parameter) throws ReferenceEncodingException
    {
        throw new ReferenceEncodingException("Unknown hash protocol.");
    }

    @Override
    public String stringifyParameter(Parameter parameter) throws ReferenceEncodingException
    {
        throw new ReferenceEncodingException("Unknown hash protocol.");
    }

    @Override
    public String stringifyParameter(ResourceParameter resourceParameter) throws ReferenceEncodingException
    {
        throw new ReferenceEncodingException("Unknown hash protocol.");
    }

    @Override
    public String stringifyParameter(StringParameter stringParameter) throws ReferenceEncodingException
    {
        throw new ReferenceEncodingException("Unknown hash protocol.");
    }

    @Override
    public String stringifyParameter(ReferenceParameter referenceParameter) throws ReferenceEncodingException
    {
        throw new ReferenceEncodingException("Unknown hash protocol.");
    }

}
