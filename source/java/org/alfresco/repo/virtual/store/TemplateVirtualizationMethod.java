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

package org.alfresco.repo.virtual.store;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.repo.virtual.ActualEnvironmentException;
import org.alfresco.repo.virtual.VirtualizationException;
import org.alfresco.repo.virtual.ref.Encodings;
import org.alfresco.repo.virtual.ref.NewVirtualReferenceMethod;
import org.alfresco.repo.virtual.ref.Protocol;
import org.alfresco.repo.virtual.ref.ProtocolMethodException;
import org.alfresco.repo.virtual.ref.Protocols;
import org.alfresco.repo.virtual.ref.Reference;
import org.alfresco.repo.virtual.ref.VirtualProtocol;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Base implementation for virtualization rules defined using template located
 * in the content repository or in a system path. <br>
 * System paths are custom string references of a resource that can be located
 * either in the repository or in the java classpath - <b>system paths are
 * deprecated and they will be replaced by {@link Encodings#PLAIN} encoded
 * {@link Reference} strings</b>.<br>
 * Templates are programmatic or declarative definitions of the rules
 * implemented by this virtualization method.<br>
 * Supported template formats include <b>JavaScript defined templates</b> and
 * <b>JSON defined templates</b> (JSON templates are also referred as <b>vanilla
 * templates</b>). The extension present in the name of the template is used to
 * determine the nature of the template (*.js for JavaScript and *.json for
 * JSON). <br>
 * JSON templates processing is done using a configurable JavaScriot processor
 * script (actually a JavaScript template that gets an extra parameter
 * containing the JSON template) that resides in the Java class path. <br>
 * Templates are processed in order to virtualize {@link NodeRef}s using
 * {@link NewVirtualReferenceMethod} protocol reference constructor visitor.
 * <br>
 * 
 * @author Bogdan Horje
 */
public abstract class TemplateVirtualizationMethod implements VirtualizationMethod
{
    protected static final String PATH_SEPARATOR = "/";

    private String vanillaProcessorClasspath;

    public TemplateVirtualizationMethod()
    {
        super();
    }

    public void setVanillaProcessor(String vanillaProcessorClasspath)
    {
        this.vanillaProcessorClasspath = vanillaProcessorClasspath;
    }

    /**
     * @param env the environment in which the virtualization takes place
     * @param actualNodeRef the node that is virtualized using the given
     *            template
     * @param templateSystemPath system path string of the template used in
     *            virtualizing the given NodeRef
     * @return a {@link Reference} correspondent of the given {@link NodeRef}
     *         according to the rules defined by the given template
     * @throws VirtualizationException
     * @deprecated all template system path functionality should be replaced by
     *             plain encoded references
     */
    protected Reference newVirtualReference(ActualEnvironment env, NodeRef actualNodeRef, String templateSystemPath)
                throws VirtualizationException
    {

        final char systemToken = templateSystemPath.charAt(0);
        if (systemToken == VirtualProtocol.NODE_TEMPLATE_PATH_TOKEN)
        {
            // create node based reference
            return newVirtualReference(env,
                                       actualNodeRef,
                                       new NodeRef(templateSystemPath.substring(1)));
        }

        String templateName = retrieveTemplateContentName(env,
                                                          templateSystemPath);
        if (!templateName.isEmpty())
        {
            Protocol protocol = protocolFormName(templateName);

            return protocol.dispatch(new NewVirtualReferenceMethod(templateSystemPath,
                                                                   PATH_SEPARATOR,
                                                                   actualNodeRef,
                                                                   vanillaProcessorClasspath),
                                     null);
        }
        else
        {
            // default branch - invalid virtual node
            throw new VirtualizationException("Invalid virtualization : missing template name for "
                        + templateSystemPath);
        }
    }

    /**
     * @param env the environment in which the virtualization takes place
     * @param actualNodeRef the node that is virtualized using the given
     *            template
     * @param templateRef {@link NodeRef} of the template used in virtualizing
     *            the given NodeRef
     * @return a {@link Reference} correspondent of the given {@link NodeRef}
     *         according to the rules defined by the given template
     * @throws VirtualizationException
     */
    protected Reference newVirtualReference(ActualEnvironment env, NodeRef actualNodeRef, NodeRef templateRef)
                throws VirtualizationException
    {
        String templateName = retrieveTemplateContentName(env,
                                                          templateRef);
        if (templateName != null)
        {
            Protocol protocol = protocolFormName(templateName);

            return newVirtualReference(protocol,
                                       templateRef,
                                       actualNodeRef);
        }
        else
        {
            // default branch - invalid virtual node
            throw new VirtualizationException("Invalid virtualization : missing template name for " + templateRef);
        }
    }

    /**
     * @param protocol {@link Protocol} to be used in virtualizing the given
     *            <code>actulalNodeRef</code>
     * @param templateRef {@link NodeRef} of the template used in virtualizing
     *            the given NodeRef
     * @param actualNodeRef the node that is virtualized using the given
     *            template
     * @return a {@link Reference} correspondent of the given {@link NodeRef}
     *         according to the rules defined by the given template
     * @throws ProtocolMethodException
     */
    protected Reference newVirtualReference(Protocol protocol, NodeRef templateRef, NodeRef actualNodeRef)
                throws ProtocolMethodException
    {
        return protocol.dispatch(new NewVirtualReferenceMethod(templateRef,
                                                               PATH_SEPARATOR,
                                                               actualNodeRef,
                                                               vanillaProcessorClasspath),
                                 null);
    }

    /**
     * @param env
     * @param sysPath
     * @return template name for the given system path
     * @throws ActualEnvironmentException
     * @deprecated all template system path functionality should be replaced by
     *             plain encoded references
     */
    private String retrieveTemplateContentName(ActualEnvironment env, String sysPath) throws ActualEnvironmentException
    {
        int index = sysPath.lastIndexOf(PATH_SEPARATOR);
        if (index < 0)
        {
            index = 1;
        }
        return sysPath.substring(index);
    }

    private String retrieveTemplateContentName(ActualEnvironment env, NodeRef templateRef)
                throws ActualEnvironmentException
    {
        String templateName = (String) env.getProperty(templateRef,
                                                       ContentModel.PROP_NAME);
        return templateName;
    }

    private final Protocol protocolFormName(String name) throws VirtualizationException
    {

        if (name.toUpperCase().endsWith(".JS"))
        {
            return Protocols.VIRTUAL.protocol;
        }
        else if (name.toUpperCase().endsWith(".JSON"))
        {
            return Protocols.VANILLA.protocol;
        }
        else
        {
            throw new VirtualizationException("Invalid template script file .extension for " + name);
        }
    }

}
