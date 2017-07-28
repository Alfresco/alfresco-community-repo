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
import java.util.Arrays;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * A {@link VirtualProtocol} extension that uses a scripted processor virtual
 * template in order to process a so-called vanilla JSON static template
 * definition on template execution.<br>
 * Vanilla references store have an extra {@link ResourceParameter} for the
 * vanilla-JSON template.
 * 
 * @author Bogdan Horje
 */
public class VanillaProtocol extends VirtualProtocol
{
    /**
     * 
     */
    private static final long serialVersionUID = -7192024582935232081L;

    public static final int VANILLA_TEMPLATE_PARAM_INDEX = 2;

    public VanillaProtocol()
    {
        super("vanilla");
    }

    @Override
    public <R> R dispatch(ProtocolMethod<R> method, Reference reference) throws ProtocolMethodException
    {
        return method.execute(this,
                              reference);
    }

    public Reference newReference(String vanillaProcessorClasspath, String templatePath, NodeRef actualNodeRef,
                NodeRef templateRef)
    {
        return this
                    .newReference(new ClasspathResource(vanillaProcessorClasspath),
                                  templatePath,
                                  actualNodeRef,
                                  Arrays
                                              .<Parameter> asList(new ResourceParameter(new RepositoryResource(new RepositoryNodeRef(templateRef)))));
    }

    public Reference newReference(Encoding encoding, Resource virtualTemplateResource, String templatePath,
                Resource actualResource, Resource vanillTemplateResource, List<Parameter> extraParameters)
    {
        List<Parameter> parameters = new ArrayList<>(2);
        parameters.add(new ResourceParameter(vanillTemplateResource));
        parameters.addAll(extraParameters);
        return this.newReference(encoding,
                                 virtualTemplateResource,
                                 templatePath,
                                 actualResource,
                                 parameters);
    }

    public Resource getVanillaTemplateResource(Reference reference)
    {
        ResourceParameter vanillaTemplateParamter = (ResourceParameter) reference
                    .getParameters()
                        .get(VANILLA_TEMPLATE_PARAM_INDEX);
        Resource resource = vanillaTemplateParamter.getValue();

        return resource;
    }

    public Reference newReference(String vanillaProcessorClasspath, String templatePath, NodeRef actualNodeRef,
                String templateSysPath) throws ProtocolMethodException
    {
        Resource templateResource = createSystemPathResource(templateSysPath);

        if (templateResource != null)
        {
            return this.newReference(new ClasspathResource(vanillaProcessorClasspath),
                                     templatePath,
                                     actualNodeRef,
                                     Arrays.<Parameter> asList(new ResourceParameter(templateResource)));
        }
        else
        {
            throw new ProtocolMethodException("Invalid template system path : " + templatePath);
        }
    }
}
