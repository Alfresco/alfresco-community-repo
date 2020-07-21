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
import java.util.Collections;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ParameterCheck;
import org.springframework.core.io.ClassPathResource;

/**
 * A protocol for encoding virtual artefacts.<br>
 * Virtual artefacts are generated using a <b>virtual folder
 * template</b>indicated by the main {@link Reference} resource.<br>
 * The virtual folder template defines a hierarchical structure of virtual
 * nodes. <br>
 * The <b>template path</b> (see {@link #getTemplatePath(Reference)}) indicates
 * a path in this structure.<br>
 * Virtual folders templates can be applied on <b>actual repository nodes</b>
 * (see {@link #getActualNodeLocation(Reference)}) which should be passed as
 * parameters to the virtual folder template.<br>
 * The protocol implementation also handles virtual protocol {@link Reference}
 * creation and template path navigation.
 */
public class VirtualProtocol extends Protocol
{
    /**
     * 
     */
    private static final long serialVersionUID = -520071882362365522L;

    /**
     * Actual node {@link Parameter} index.
     */
    public static final int ACTUAL_NODE_LOCATION_PARAM_INDEX = 1;

    /**
     * Template path {@link Parameter} index.
     */
    public static final int TEMPLATE_PATH_PARAM_INDEX = 0;

    /**
     * Repository node path system path token.
     */
    public static final Character NODE_TEMPLATE_PATH_TOKEN = 'N';

    /**
     * Classpath system path token.
     */
    public static final Character CLASS_TEMPLATE_PATH_TOKEN = 'C';

    public VirtualProtocol()
    {
        this("virtual");
    }

    public VirtualProtocol(String name)
    {
        super(name);
    }

    @Override
    public <R> R dispatch(ProtocolMethod<R> method, Reference reference) throws ProtocolMethodException
    {
        return method.execute(this,
                              reference);
    }

    /**
     * @param reference
     * @return the inner template path referenced by the given {@link Reference}
     * @see VirtualProtocol#TEMPLATE_PATH_PARAM_INDEX
     */
    public String getTemplatePath(Reference reference)
    {
        StringParameter parameter = (StringParameter) getParameter(reference,
                                                                   TEMPLATE_PATH_PARAM_INDEX);
        return parameter.getValue();
    }

    /**
     * @param reference
     * @param path
     * @return a {@link Reference} copy of the given reference parameter with
     *         the template path set to the given path parameter value
     * @see VirtualProtocol#TEMPLATE_PATH_PARAM_INDEX
     */
    public Reference replaceTemplatePath(Reference reference, String path)
    {
        return replaceParameter(reference,
                                TEMPLATE_PATH_PARAM_INDEX,
                                path);
    }

    /**
     * @param reference
     * @return the repository location of the actual node that the virtual
     *         template should be applied on
     * @see VirtualProtocol#ACTUAL_NODE_LOCATION_PARAM_INDEX
     */
    public RepositoryLocation getActualNodeLocation(Reference reference)
    {
        ResourceParameter parameter = (ResourceParameter) getParameter(reference,
                                                                       ACTUAL_NODE_LOCATION_PARAM_INDEX);
        RepositoryResource repoResource = (RepositoryResource) parameter.getValue();
        return repoResource.getLocation();
    }

    /**
     * @param templateNodeRef {@link NodeRef} of the template content holding
     *            repository node
     * @param templatePath
     * @param actualNodeRef
     * @return a new virtual protocol {@link Reference} with the given virtual
     *         protocol reference elements
     */
    public Reference newReference(NodeRef templateNodeRef, String templatePath, NodeRef actualNodeRef)
    {
        ParameterCheck.mandatoryString("templatePath", templatePath);
        return this.newReference(new RepositoryResource(new RepositoryNodeRef(templateNodeRef)),
                                 templatePath,
                                 actualNodeRef,
                                 Collections.<Parameter> emptyList());
    }

    /**
     * @param templateResource template content holding resource
     * @param templatePath
     * @param actualNodeRef
     * @return a new virtual protocol {@link Reference} with the given virtual
     *         protocol reference elements
     */
    public Reference newReference(Resource templateResource, String templatePath, NodeRef actualNodeRef,
                List<Parameter> extraParameters)
    {
        ParameterCheck.mandatoryString("templatePath", templatePath);
        ArrayList<Parameter> parameters = new ArrayList<Parameter>();
        parameters.add(new StringParameter(templatePath));
        parameters.add(new ResourceParameter(new RepositoryResource(new RepositoryNodeRef(actualNodeRef))));
        parameters.addAll(extraParameters);
        return new Reference(DEFAULT_ENCODING,
                             this,
                             templateResource,
                             parameters);
    }

    public Reference newReference(Encoding encoding, Resource templateResource, String templatePath,
                Resource actualNodeResource, List<Parameter> extraParameters)
    {
        ParameterCheck.mandatoryString("templatePath", templatePath);
        ArrayList<Parameter> parameters = new ArrayList<Parameter>(3);
        parameters.add(new StringParameter(templatePath));
        parameters.add(new ResourceParameter(actualNodeResource));
        parameters.addAll(extraParameters);
        return new Reference(encoding,
                             this,
                             templateResource,
                             parameters);
    }

    /**
     * Creates a resource based on the given template system-path that is used
     * in creating a new virtual protocol reference.
     * 
     * @param templateSysPath a template-system-path for the template holding
     *            content<br>
     *            Template-system-paths are classpaths paths or repository paths
     *            prefixed with
     *            {@link VirtualProtocol#CLASS_TEMPLATE_PATH_TOKEN} or
     *            {@link VirtualProtocol#NODE_TEMPLATE_PATH_TOKEN} respectively.
     * @param templatePath
     * @param actualNodeRef
     * @return a new virtual protocol {@link Reference} with the given virtual
     *         protocol reference elements
     * @throws ProtocolMethodException
     * @deprecated In future system paths will be replaced with actual resources
     *             or string encoded references
     */
    public Reference newReference(String templateSysPath, String templatePath, NodeRef actualNodeRef)
                throws ProtocolMethodException
    {
        Resource templateResource = createSystemPathResource(templateSysPath);

        if (templateResource != null)
        {
            return this.newReference(templateResource,
                                     templatePath,
                                     actualNodeRef,
                                     Collections.<Parameter> emptyList());
        }
        else
        {
            throw new ProtocolMethodException("Invalid template system path : " + templatePath);
        }
    }

    /**
     * System path resource factory method.
     * 
     * @param templateSysPath a classpath or a repository path prefixed with
     *            {@link VirtualProtocol#CLASS_TEMPLATE_PATH_TOKEN} or
     *            {@link VirtualProtocol#NODE_TEMPLATE_PATH_TOKEN} respectively.
     * @return a {@link ClassPathResource} or a {@link RepositoryResource} for
     *         the given system path
     * @deprecated In future system paths will be replaced with actual resources
     *             or string encoded references
     */
    protected Resource createSystemPathResource(String templateSysPath)
    {
        final char systemToken = templateSysPath.charAt(0);

        Resource templateResource = null;

        if (systemToken == NODE_TEMPLATE_PATH_TOKEN)
        {
            templateResource = new RepositoryResource(new RepositoryPath(templateSysPath.substring(1)));
        }
        else if (systemToken == CLASS_TEMPLATE_PATH_TOKEN)
        {
            templateResource = new ClasspathResource(templateSysPath.substring(1));
        }

        return templateResource;
    }
}
