/* 
 * Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see http://www.gnu.org/licenses/.
 */

package org.alfresco.repo.virtual.ref;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;

public class ZeroReferenceParser implements ReferenceParser, ZeroEncoding
{

    /**
     * 
     */
    private static final long serialVersionUID = 795566334039858555L;

    /**
     * Parses a String representation of a {@link Reference} and returns the
     * corresponding {@link Reference}. Parsing is done with the help of the
     * {@link Cursor} class, which keeps track of the String elements that make
     * up the {@link Reference} String representation. This method instantiates
     * and initiates the cursor and checks the validity of the referenceString
     * and relies upon the {@link ZeroReferenceParser#parseReference} for the
     * actual parsing.
     *
     * @param referenceString the String representation of a {@link Reference}.
     * @return the newly created {@link Reference}
     * @throws ReferenceParseException
     */
    @Override
    public Reference parse(String referenceString) throws ReferenceParseException
    {
        String[] referenceTokens = referenceString.split(DELIMITER);
        final Cursor cursor = new Cursor(referenceTokens,
                                         0);

        if (referenceTokens.length < 2)
        {
            throw new ReferenceParseException("Invalid reference " + referenceString);
        }
        return this.parseReference(cursor);
    }

    /**
     * Parses a String representation of a {@link Reference} and returns the
     * corresponding {@link Reference}. Parsing is done with the help of the
     * {@link Cursor} class, which keeps track of the String elements that make
     * up the {@link Reference} String representation.
     *
     * @param cursor
     * @return A {@link Reference} instance.
     * @throws ReferenceParseException
     */
    private Reference parseReference(Cursor cursor) throws ReferenceParseException
    {
        try
        {
            Protocol protocol = null;
            int protocolResourceEncoding = Integer.parseInt(cursor.tokens[cursor.i]);
            if (protocolResourceEncoding < VIRTUAL_PROTOCOL_CODE)
            {
                protocol = Protocols.fromName("vanilla");
            }
            else if (protocolResourceEncoding < NODE_PROTOCOL_CODE)
            {
                protocol = Protocols.fromName("virtual");
                protocolResourceEncoding -= VIRTUAL_PROTOCOL_CODE;
            }
            else if ((protocolResourceEncoding - NODE_PROTOCOL_CODE) < VIRTUAL_PROTOCOL_CODE)
            {
                protocol = Protocols.fromName("node");
                protocolResourceEncoding -= NODE_PROTOCOL_CODE;
            }
            if (protocol == null)
            {
                throw new ReferenceParseException("Unknown protocol " + protocol);
            }
            cursor.i++;
            final Resource resource = parseResource(protocolResourceEncoding,
                                                    cursor);
            final List<Parameter> parameters = parseParameters(cursor);

            Reference reference = new Reference(Encodings.ZERO.encoding,
                                                protocol,
                                                resource,
                                                parameters);
            return reference;
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            throw new ReferenceParseException("Invalid reference",
                                              e);
        }
    }

    /**
     * Parses a {@link Resource} which is an instance of
     * {@link RepositoryResource} or {@link ClasspathResource} if the current
     * token is contained in the array {@link ZeroEncoding#RESOURCE_PARAMETER}.
     * 
     * @param resourceEncoding
     * @param cursor
     * @return an instance of {@link RepositoryResource} or
     *         {@link ClasspathResource}
     * @throws ReferenceParseException
     */
    private Resource parseResource(int resourceEncoding, Cursor cursor) throws ReferenceParseException
    {
        Resource resource = null;
        if (resourceEncoding == PATH_CODE)
        {
            resource = new RepositoryResource(parseRepositoryPath(cursor));
        }
        else if (resourceEncoding == NODE_CODE)
        {
            resource = new RepositoryResource(parseRepositoryNode(cursor));
        }
        else if (resourceEncoding == CLASSPATH_RESOURCE_CODE)
        {
            resource = parseClasspathResource(cursor);
        }
        if (resource == null)
        {
            throw new ReferenceParseException("Unknown resource encoding " + resourceEncoding);
        }
        return resource;
    }

    /**
     * Parses a {@link Resource} reference which is an instance of
     * {@link RepositoryResource} or {@link ClasspathResource}, depending on the
     * {@link ZeroEncoding#RESOURCE_PARAMETER}
     * 
     * @param cursor
     * @return A {@link Resource} reference.
     * @throws ReferenceParseException
     */
    private Resource parseResource(Cursor cursor) throws ReferenceParseException
    {
        return this.parseResource(Byte.parseByte(cursor.tokens[cursor.i - 1]),
                                  cursor);
    }

    /**
     * Creates a list of {@link Parameter}s by parsing the individual parameters
     * given by the cursor's tokens. Stops parsing parameters upon encountering
     * {@link ZeroEncoding#REFERENCE_DELIMITER}, which signals the end of a
     * {@link ReferenceParameter}
     * 
     * @param cursor
     * @return a list of {@link Parameter}s 
     * @throws ReferenceParseException
     */
    private List<Parameter> parseParameters(Cursor cursor) throws ReferenceParseException
    {
        List<Parameter> parameters = new ArrayList<>();
        while (cursor.i < cursor.tokens.length)
        {
            if (Arrays.asList(RESOURCE_PARAMETER).contains(cursor.tokens[cursor.i]))
            {
                cursor.i++;
                parameters.add(parseResourceParameter(cursor));
            }
            else if (STRING_PARAMETER.equals(cursor.tokens[cursor.i]))
            {
                cursor.i++;
                parameters.add(parseStringParameter(cursor));
            }
            else if (REFERENCE_PARAMETER.equals(cursor.tokens[cursor.i]))
            {
                cursor.i++;
                parameters.add(parseReferenceParameter(cursor));
            }
            else if (REFERENCE_DELIMITER.equals(cursor.tokens[cursor.i]))
            {
                cursor.i++;
                break;
            }
            else
            {
                throw new ReferenceParseException("Invalid parameter " + cursor.tokens[cursor.i]);
            }
        }
        return parameters;
    }

    /**
     * Creates a {@link Parameter} reference that is an instance of
     * {@link StringParameter} by parsing the resource given by the cursor's
     * current token.
     * 
     * @param cursor
     * @return a {@link Parameter} reference that is instance of
     *         {@link StringParameter} from the cursor parameter
     */
    private Parameter parseStringParameter(Cursor cursor)
    {
        StringParameter paramenter = new StringParameter(cursor.tokens[cursor.i]);
        cursor.i++;
        return paramenter;
    }

    /**
     * Creates a {@link Parameter} reference that is an instance of
     * {@link ResourceParameter} by parsing the resource given by the cursor's
     * current token.
     * 
     * @param cursor
     * @return a {@link Parameter} reference that is instance of
     *         {@link ResourceParameter} from the cursor parameter
     * @throws ReferenceParseException
     */
    private Parameter parseResourceParameter(Cursor cursor) throws ReferenceParseException
    {
        Resource resource = parseResource(cursor);
        return new ResourceParameter(resource);
    }

    /**
     * Creates a {@link RepositoryPath} reference from current token of the cursor.
     * 
     * @param cursor
     * @return A {@link RepositoryPath} reference.
     */
    private RepositoryPath parseRepositoryPath(Cursor cursor)
    {
        String path = cursor.tokens[cursor.i];
        cursor.i++;
        return new RepositoryPath(path);
    }

    /**
     * Creates a {@link RepositoryNodeRef} reference from the default
     * {@link StoreRef}, SpacesStore and from the node id given by the cursor's
     * current token.
     * 
     * @param cursor
     * @return A {@link RepositoryNodeRef} reference.
     */
    private RepositoryNodeRef parseRepositoryNode(Cursor cursor)
    {
        String id = cursor.tokens[cursor.i];
        cursor.i++;

        return new RepositoryNodeRef(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
                                                 id));
    }

    /**
     * Creates a {@link ClasspathResource} reference from the current token of the
     * cursor parameter.
     * 
     * @param cursor
     * @return A {@link ClasspathResource} reference. 
     */
    private ClasspathResource parseClasspathResource(Cursor cursor)
    {
        String classpath = cursor.tokens[cursor.i];
        cursor.i++;
        return new ClasspathResource(classpath);
    }

    /**
     * Creates a {@link ReferenceParameter} reference from the cursor's tokens up
     * to the * token which signifies the end of the current
     * {@link ReferenceParameter}. 
     * 
     * @param cursor
     * @return A {@link ReferenceParameter} reference.
     */
    private ReferenceParameter parseReferenceParameter(Cursor cursor) throws ReferenceParseException
    {
        Reference reference = parseReference(cursor);
        ReferenceParameter parameter = new ReferenceParameter(reference);
        return parameter;
    }
}
