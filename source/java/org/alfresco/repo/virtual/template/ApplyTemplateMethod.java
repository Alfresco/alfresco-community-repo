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


package org.alfresco.repo.virtual.template;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.repo.virtual.VirtualContext;
import org.alfresco.repo.virtual.ref.AbstractProtocolMethod;
import org.alfresco.repo.virtual.ref.GetActualNodeRefMethod;
import org.alfresco.repo.virtual.ref.GetTemplatePathMethod;
import org.alfresco.repo.virtual.ref.GetVanillaScriptInputStreamMethod;
import org.alfresco.repo.virtual.ref.NodeProtocol;
import org.alfresco.repo.virtual.ref.ProtocolMethodException;
import org.alfresco.repo.virtual.ref.Reference;
import org.alfresco.repo.virtual.ref.Resource;
import org.alfresco.repo.virtual.ref.ResourceProcessingError;
import org.alfresco.repo.virtual.ref.VanillaProtocol;
import org.alfresco.repo.virtual.ref.VirtualProtocol;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Creates a {@link VirtualFolderDefinition} by executing the template indicated
 * by a virtualized entity reference.
 * 
 * @author Bogdan Horje
 */
public class ApplyTemplateMethod extends AbstractProtocolMethod<VirtualFolderDefinition>
{
    public static final String VANILLA_JSON_PARAM_NAME = "vanillaJSON";

    private ActualEnvironment environment;

    private static Log logger = LogFactory.getLog(ApplyTemplateMethod.class);

    public ApplyTemplateMethod(ActualEnvironment environment)
    {
        super();
        this.environment = environment;
    }

    public VirtualFolderDefinition execute(VirtualProtocol virtualProtocol, Reference reference)
                throws ProtocolMethodException
    {
        VirtualContext context = createVirtualContext(reference);
        return execute(virtualProtocol,
                       reference,
                       context);
    }

    private VirtualContext createVirtualContext(Reference reference) throws ProtocolMethodException
    {
        return new VirtualContext(environment,
                                  reference.execute(new GetActualNodeRefMethod(environment)));
    }

    public VirtualFolderDefinition execute(VirtualProtocol virtualProtocol, Reference reference, VirtualContext context)
                throws ProtocolMethodException
    {
        Resource resource = reference.getResource();

        try
        {
            VirtualFolderDefinition theStructure = resource.processWith(new TemplateResourceProcessor(context));
            String path = reference.execute(new GetTemplatePathMethod());

            if (!path.isEmpty())
            {
                String[] pathElements = path.split(PATH_SEPARATOR);
                int startIndex = path.startsWith(PATH_SEPARATOR) ? 1 : 0;
                for (int i = startIndex; i < pathElements.length; i++)
                {
                    theStructure = theStructure.findChildById(pathElements[i]);
                    if (theStructure == null)
                    {

                        throw new ProtocolMethodException("Invalid template path in " + reference.toString());

                    }
                }
            }

            return theStructure;
        }
        catch (ResourceProcessingError e)
        {
            throw new ProtocolMethodException(e);
        }
    }

    @Override
    public VirtualFolderDefinition execute(VanillaProtocol vanillaProtocol, Reference reference)
                throws ProtocolMethodException
    {
        InputStream vanillaIS = reference.execute(new GetVanillaScriptInputStreamMethod(environment));
        try
        {
            String vanillaJSON = IOUtils.toString(vanillaIS,
                                                  StandardCharsets.UTF_8);
            VirtualContext context = createVirtualContext(reference);
            context.setParameter(VANILLA_JSON_PARAM_NAME,
                                 vanillaJSON);
            return execute(vanillaProtocol,
                           reference,
                           context);

        }
        catch (IOException e)
        {
            throw new ProtocolMethodException(e);
        }
        finally
        {
            try
            {
                if (vanillaIS != null)
                    vanillaIS.close();
            }
            catch (IOException ioe)
            {
                logger.warn("Failed to close input stream : " + ioe);
            }
        }

    }

    /**
     * Creates an empty {@link VirtualFolderDefinition} parameterized with a
     * {@link NullFilingRule} as this method is called for non-virtual nodes.
     * 
     * @param protocol
     * @param reference
     * @return The empty {@link VirtualFolderDefinition}.
     */
    @Override
    public VirtualFolderDefinition execute(NodeProtocol protocol, Reference reference) throws ProtocolMethodException
    {
        VirtualFolderDefinition virtualStructure = new VirtualFolderDefinition();
        virtualStructure.setFilingRule(new NodeFilingRule(environment));
        return virtualStructure;
    }
}
