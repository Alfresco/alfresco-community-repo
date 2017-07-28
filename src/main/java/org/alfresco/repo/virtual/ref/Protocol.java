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

import java.io.Serializable;
import java.util.ArrayList;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * A {@link Reference} model element that defines the type of the referred
 * artefact and the virtualization process that was applied when creating a
 * reference.<br>
 * Protocols define the semantics of all elements of a reference. Protocol
 * implementations add syntactic processing methods.<br>
 * The high level semantic functionality of a protocol is implemented with
 * {@link ProtocolMethod}s.
 * 
 * @author Bogdan Horje
 */
public class Protocol implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = -6969613804420028905L;

    public final String name;

    protected static final Encoding DEFAULT_ENCODING = Encodings.HASH.encoding;

    public Protocol(String name)
    {
        super();
        this.name = name;
    }

    public Parameter getParameter(Reference reference, int index)
    {
        return reference.getParameters().get(index);
    }

    public Reference replaceParameter(Reference reference, int index, String stringParameter)
    {
        return replaceParameter(reference,
                                index,
                                new StringParameter(stringParameter));
    }

    public Reference replaceParameter(Reference reference, int index, Parameter parameter)
    {
        ArrayList<Parameter> parameters = new ArrayList<Parameter>(reference.getParameters());
        parameters.set(index,
                       parameter);
        return new Reference(reference.getEncoding(),
                             reference.getProtocol(),
                             reference.getResource(),
                             parameters);
    }

    public Reference addParameter(Reference reference, Resource resource)
    {
        return addParameter(reference,
                            new ResourceParameter(resource));
    }

    public Reference addParameter(Reference reference, Parameter parameter)
    {
        ArrayList<Parameter> parameters = new ArrayList<Parameter>(reference.getParameters());
        parameters.add(parameter);
        return new Reference(reference.getEncoding(),
                             reference.getProtocol(),
                             reference.getResource(),
                             parameters);
    }

    /**
     * {@link ProtocolMethod} double-dispatch/visitor protocol type selector.
     * Subclasses should override this method to select the appropriate type
     * bound method.<br>
     * This particular implementation calls the default
     * {@link ProtocolMethod#execute(Protocol, Reference)} default method
     * implementation.
     * 
     * @param method
     * @param reference
     * @return the value returned by the type bond method
     * @throws ProtocolMethodException
     */
    public <R> R dispatch(ProtocolMethod<R> method, Reference reference) throws ProtocolMethodException
    {
        return method.execute(this,
                              reference);
    }

    @Override
    public String toString()
    {
        return name;
    }

    /**
     * {@link Protocol} delegate of
     * {@link Reference#propagateNodeRefMutations(NodeRef)}
     * 
     * @param mutatedNodeRef
     * @param reference
     * @return a mutated version of the given {@link Reference} corresponding to
     *         the given mutated node or the given {@link Reference} if no
     *         mutations are detected
     */
    public Reference propagateNodeRefMutations(NodeRef mutatedNodeRef, Reference reference)
    {
        // nothing to propagate by default
        return reference;
    }
}
