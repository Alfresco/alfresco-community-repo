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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.alfresco.repo.version.common.VersionUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A generic, immutable virtualized artefact reference.<br>
 * Refers virtualized artefacts through :<br>
 * <ul>
 * <li>a {@link Protocol} - defines what kind of virtualzied artefact is
 * referred by this reference as well as what virtualization process was applied
 * </li>
 * <li>a {@link Resource} - identifies the main resource used in the
 * virtualization (egg. a classpath location or repository node reference)</li>
 * <li>a list of {@link ResourceParameter}s - used in customizing the
 * virtualization process (egg. a resource pointing to the actual node of a
 * semi-virtual folder reference)</li>
 * </ul>
 * 
 * @author Bogdan Horje
 */
public class Reference
{
    private static Log logger = LogFactory.getLog(Reference.class);

    private static final Character VIRTUAL_TOKEN = 'v';

    /**
     * Quick Reference compliance check of a {@link NodeRef}.<br>
     * NodeRef Reference representations validated by this method should produce
     * valid Reference objects based on the given {@link NodeRef} when passed to
     * the {@link #fromNodeRef(NodeRef)} method.
     * 
     * @param nodeRef
     * @return <code>true</code> if the given {@link NodeRef} is a valid
     *         Reference representation<br>
     *         <code>false</code> otherwise
     */
    public static final boolean isReference(NodeRef nodeRef)
    {
        if (nodeRef != null)
        {
            String id = nodeRef.getId();
            if (id != null)
            {
                char zeroChar = id.charAt(0);
                return VIRTUAL_TOKEN.equals(zeroChar);
            }
            else
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("NodeRef with null ID.");
                }
            }
        }

        return false;
    }

    /**
     * {@link NodeRef} {@link Reference} representation decoder/converter
     * method.<br>
     * Creates a {@link Reference} representation based on the ID of the given
     * {@link NodeRef}.<br>
     * It expects a {@link #VIRTUAL_TOKEN} prefixed encoded string. The encoded
     * string must start with a valid {@link Encoding} token. The Reference
     * representation structure is (no delimiters between the 3 elements):
     * VIRTUAL_TOKEN ENCODING_TOKEN referenceString Given that a valid encoding
     * was detected {@link Encoding#urlNative} information is used to obtain a
     * reference string. The reference string is parsed using the encoding
     * configured parser.
     * 
     * @param nodeRef
     * @return the {@link Reference} object corresponding to the given
     *         {@link NodeRef}
     * @throws ReferenceParseException if an error occurs during the reference
     *             string parsing
     * @throws ReferenceEncodingException if the {@link NodeRef} ID has an
     *             invalid virtual token prefix or it uses an invalid encoding
     *             token
     */
    public static final Reference fromNodeRef(NodeRef nodeRef) throws ReferenceParseException,
                ReferenceEncodingException
    {
        String id = nodeRef.getId();
        if (id.startsWith("" + VIRTUAL_TOKEN))
        {
            char token = id.charAt(1);
            Encoding encoding = Encodings.fromToken(token);
            if (encoding == null)
            {
                throw new ReferenceEncodingException("Invalid encoding token " + token + " in " + id);
            }
            else
            {
                String referenceString = id.substring(2);
                if (!encoding.urlNative)
                {
                    referenceString = new String(org.apache.commons.codec.binary.Base64.decodeBase64(referenceString));
                }

                Reference reference = encoding.parser.parse(referenceString);
                return reference.propagateNodeRefMutations(nodeRef);
            }
        }
        else
        {
            throw new ReferenceEncodingException("Invalid node ID format " + id);
        }
    }

    // Average reference length log trace

    private static long _trace_refLength = 0;

    private static long _trace_refCount = 0;

    private static final long _trace_refBatchSize = 4096*2;

    private static synchronized void _trace_avg_ref_length(long refLength)
    {
        _trace_refLength += refLength;
        _trace_refCount++;
        if (_trace_refBatchSize > 0 && _trace_refCount % _trace_refBatchSize == 0)
        {
            logger.trace("Average reference encoding size : " + (_trace_refLength / _trace_refCount));
            _trace_refCount = 0;
            _trace_refLength = 0;
        }
    }

    private Encoding encoding;

    private Protocol protocol;

    private Resource resource;

    private List<Parameter> parameters;

    /**
     * Constructor
     * 
     * @param encoding the default {@link Encoding} of the new resource - to be
     *            used where an encoding is required and none is specified
     * @param protocol
     * @param resource
     * @param parameters resource parameters - a copy of the provided list will
     *            be stored by this reference
     */
    public Reference(Encoding encoding, Protocol protocol, Resource resource, List<? extends Parameter> parameters)
    {
        this.encoding = encoding;
        this.protocol = protocol;
        this.resource = resource;
        this.parameters = new LinkedList<>(parameters);
    }

    public Reference(Encoding encoding, Protocol protocol, Resource resource)
    {
        this(encoding,
             protocol,
             resource,
             Collections.<Parameter> emptyList());
    }

    /**
     * @return a {@link String} representation of this reference using its
     *         default {@link Encoding}
     * @throws ReferenceEncodingException
     */
    public String encode() throws ReferenceEncodingException
    {
        return encode(this.encoding);
    }

    /**
     * @param anEncoding
     * @return a {@link String} representation of this reference using the given
     *         {@link Encoding}
     * @throws ReferenceEncodingException
     */
    public String encode(Encoding anEncoding) throws ReferenceEncodingException
    {
        return anEncoding.stringifier.stringify(this);
    }

    /**
     * @return the default {@link Encoding} of this reference
     */
    public Encoding getEncoding()
    {
        return this.encoding;
    }

    public Protocol getProtocol()
    {
        return this.protocol;
    }

    public Resource getResource()
    {
        return this.resource;
    }

    public List<Parameter> getParameters()
    {
        return parameters;
    }

    /**
     * @return a {@link NodeRef} representation of this resource using the
     *         {@link StoreRef#STORE_REF_WORKSPACE_SPACESSTORE} and the default
     *         encoding of this resource
     * @throws ReferenceEncodingException
     */
    public NodeRef toNodeRef() throws ReferenceEncodingException
    {
        return toNodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    }

    /**
     * @param storeRef
     * @return a {@link NodeRef} representation of this resource using the given
     *         {@link StoreRef} and the default encoding of this resource
     * @throws ReferenceEncodingException
     */
    public NodeRef toNodeRef(StoreRef storeRef) throws ReferenceEncodingException
    {
        return toNodeRef(storeRef,
                         this.encoding);
    }

    /**
     * @param storeRef
     * @param encoding
     * @return a {@link NodeRef} representation of this resource using the given
     *         {@link StoreRef} and {@link Encoding}
     * @throws ReferenceEncodingException
     */
    public NodeRef toNodeRef(StoreRef storeRef, Encoding encoding) throws ReferenceEncodingException
    {
        String id = encode(encoding);
        // TODO: move non-native encoding to encoding object itself
        if (!encoding.urlNative)
        {
            id = new String(org.apache.commons.codec.binary.Base64.encodeBase64(id.getBytes(),
                                                                                false));
        }
        StringBuilder idBuilder = new StringBuilder();
        idBuilder.append(VIRTUAL_TOKEN);
        idBuilder.append(encoding.token);
        idBuilder.append(id);

        NodeRef theNode = new NodeRef(storeRef,
                                      idBuilder.toString());

        if (logger.isTraceEnabled())
        {
            _trace_avg_ref_length(theNode.toString().length());
        }

        return theNode;
    }

    /**
     * Double-dispatches {@link ProtocolMethod}s.<br>
     * Uses {@link Protocol#dispatch(ProtocolMethod, Reference)} to trigger
     * concrete protocol based double dispatch
     * <code>ProtocolMethod::execute</code> invocation on the given method
     * object.
     * 
     * @param method
     * @return the dispatched method execution result
     * @throws ProtocolMethodException
     */
    public <R> R execute(ProtocolMethod<R> method) throws ProtocolMethodException
    {
        return this.protocol.dispatch(method,
                                      this);
    }

    /**
     * Despite claimed {@link NodeRef} opacity Alfresco sometimes alters
     * NodeRefs representation to achieve functionality. For example see
     * {@link VersionUtil#convertNodeRef(NodeRef)}.<br>
     * We say that altered {@link NodeRef}s have suffered mutations and we try
     * to detect those mutations and create a correspondent reference.
     * 
     * @param mutatedNodeRef
     * @return a mutated version of this {@link Reference} corresponding to the
     *         given mutated node or
     *         <code>this<code> Reference if no mutations are detected
     */
    public Reference propagateNodeRefMutations(NodeRef mutatedNodeRef)
    {
        return protocol.propagateNodeRefMutations(mutatedNodeRef,
                                                  this);
    }

    @Override
    public String toString()
    {
        try
        {
            return encode();
        }
        catch (ReferenceEncodingException e)
        {
            logger.error("Invalid reference",
                         e);
            return super.toString();
        }
    }

    @Override
    public int hashCode()
    {
        return resource != null ? resource.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (obj == null)
        {
            return false;
        }
        else if (!(obj instanceof Reference))
        {
            return false;
        }

        Reference other = (Reference) obj;

        if (resource == null)
        {
            return other.resource == null;
        }
        else
        {
            if (!this.resource.equals(other.resource))
            {
                return false;
            }
            else
            {
                if (parameters == null)
                {
                    return other.parameters == null;
                }
                else
                {
                    return parameters.equals(other.parameters);
                }
            }
        }
    }
}
