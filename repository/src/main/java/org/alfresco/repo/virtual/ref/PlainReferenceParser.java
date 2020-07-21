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

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Parses string references into {@link Reference} objects based on Plain
 * encodings grammar.
 * <p>
 * For grammar examples see {@link Encodings#PLAIN} encoding definition.
 */
public class PlainReferenceParser implements ReferenceParser, PlainEncoding
{
    /**
     * 
     */
    private static final long serialVersionUID = -7053644289373735564L;

    

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

    private Reference parseReference(Cursor cursor) throws ReferenceParseException
    {
        try
        {

            final String protocolString = cursor.tokens[cursor.i];
            final Protocol protocol = Protocols.fromName(protocolString);
            if (protocol == null)
            {
                throw new ReferenceParseException("Unknown protocol " + protocolString);
            }
            cursor.i++;

            final Resource resource = parseResource(cursor);
            final List<Parameter> parameters = parseParameters(cursor);

            return new Reference(Encodings.PLAIN.encoding,
                                 protocol,
                                 resource,
                                 parameters);
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            throw new ReferenceParseException("Invalid reference",
                                              e);
        }
    }

    /**
     * Obtains a list of {@link Parameter} from the cursor parameter
     * 
     * @param cursor
     * @return a list of {@link Parameter} from the cursor parameter
     * @throws ReferenceParseException
     */
    private List<Parameter> parseParameters(Cursor cursor) throws ReferenceParseException
    {
        List<Parameter> parameters = new ArrayList<>();
        while (cursor.i < cursor.tokens.length)
        {
            if (RESOURCE_PARAMETER.equals(cursor.tokens[cursor.i]))
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
     * Obtains a {@link Parameter} reference that is instance of
     * {@link StringParameter} from the cursor parameter
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
     * Obtains a {@link Parameter} reference that is instance of
     * {@link ResourceParameter} from the cursor parameter
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
     * Obtains {@link Resource} reference witch is an instance of
     * {@link RepositoryResource} if current token is
     * {@link PlainEncoding#REPOSITORY} or an instance of
     * {@link ClasspathResource} if current token is
     * {@link PlainEncoding#CLASSPATH}
     * 
     * @param cursor
     * @return {@link Resource} reference witch an instance of
     *         {@link RepositoryResource} if current token is
     *         {@link PlainEncoding#REPOSITORY} or an instance of
     *         {@link ClasspathResource} if current token is
     *         {@link PlainEncoding#CLASSPATH}
     * @throws ReferenceParseException
     */
    private Resource parseResource(Cursor cursor) throws ReferenceParseException
    {
        if (REPOSITORY.equals(cursor.tokens[cursor.i]))
        {
            cursor.i++;
            return parseRepositoryResource(cursor);
        }
        else if (CLASSPATH.equals(cursor.tokens[cursor.i]))
        {
            cursor.i++;
            return parseClasspathResource(cursor);
        }
        else
        {
            throw new ReferenceParseException("Invalid resource " + cursor.tokens[cursor.i]);
        }
    }

    /**
     * Obtains {@link RepositoryResource} reference from the cursor parameter
     * 
     * @param cursor
     * @return A {@link RepositoryResource} reference from the cursor parameter
     * @throws ReferenceParseException
     */
    private RepositoryResource parseRepositoryResource(Cursor cursor) throws ReferenceParseException
    {
        return new RepositoryResource(parseRepositoryLocation(cursor));
    }

    /**
     * Obtains {@link RepositoryLocation} reference witch is an instance of
     * {@link RepositoryPath} if current token is {@link PlainEncoding#PATH} or
     * an instance of {@link RepositoryNodeRef} if current token is
     * {@link PlainEncoding#NODE}
     * 
     * @param cursor
     * @return A {@link RepositoryLocation} reference witch an instance of
     *         {@link RepositoryPath} if current token is
     *         {@link PlainEncoding#PATH} or an instance of
     *         {@link RepositoryNodeRef} if current token is
     *         {@link PlainEncoding#NODE}
     * @throws ReferenceParseException
     */
    private RepositoryLocation parseRepositoryLocation(Cursor cursor) throws ReferenceParseException
    {
        if (PATH.equals(cursor.tokens[cursor.i]))
        {
            cursor.i++;
            return parseRepositoryPath(cursor);
        }
        else if (NODE.equals(cursor.tokens[cursor.i]))
        {
            cursor.i++;
            return parseRepositoryNode(cursor);
        }
        else
        {
            throw new ReferenceParseException("Invalid location " + cursor.tokens[cursor.i]);
        }
    }

    /**
     * Obtains {@link RepositoryPath} reference from current token of the cursor
     * parameter
     * 
     * @param cursor
     * @return A {@link RepositoryPath} reference from current token of the
     *         cursor parameter
     */
    private RepositoryPath parseRepositoryPath(Cursor cursor)
    {
        String path = cursor.tokens[cursor.i];
        cursor.i++;
        return new RepositoryPath(path);
    }

    /**
     * Obtains {@link RepositoryNodeRef} reference from store protocol, store
     * identifier and node id obtained from cursor's tokens starting with
     * current token of the cursor.
     * 
     * @param cursor
     * @return A {@link RepositoryNodeRef} reference from store protocol, store
     *         identifier and node id obtained from cursor's tokens starting
     *         with current token of the cursor.
     */
    private RepositoryNodeRef parseRepositoryNode(Cursor cursor)
    {
        String storeProtocol = cursor.tokens[cursor.i];
        String storeIdentifier = cursor.tokens[cursor.i + 1];
        String id = cursor.tokens[cursor.i + 2];
        cursor.i += 3;

        return new RepositoryNodeRef(new NodeRef(storeProtocol,
                                                 storeIdentifier,
                                                 id));
    }

    /**
     * Obtains {@link ClasspathResource} reference from current token of the
     * cursor parameter
     * 
     * @param cursor
     * @return A {@link ClasspathResource} reference from current token of the
     *         cursor parameter
     */
    private ClasspathResource parseClasspathResource(Cursor cursor)
    {
        String classpath = cursor.tokens[cursor.i];
        cursor.i++;
        return new ClasspathResource(classpath);
    }

    private ReferenceParameter parseReferenceParameter(Cursor cursor) throws ReferenceParseException
    {
        Reference reference = parseReference(cursor);
        ReferenceParameter parameter = new ReferenceParameter(reference);
        return parameter;
    }

}
