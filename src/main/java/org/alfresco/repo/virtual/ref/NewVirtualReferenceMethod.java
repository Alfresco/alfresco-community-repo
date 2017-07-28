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

import org.alfresco.service.cmr.repository.NodeRef;

public class NewVirtualReferenceMethod extends AbstractProtocolMethod<Reference>
{
    private String templateSysPath = null;

    private NodeRef templateRef = null;

    private String templatePath = null;

    private NodeRef actualNodeRef = null;

    /**
     * classpath to the javascript processor of vanilla JSON templates used to construct virtual folders
     */
    private String vanillaProcessorClasspath = null;

    /**
     * @param templateSysPath
     * @param templatePath
     * @param actualNodeRef
     * @param vanillaProcessorClasspath
     * @deprecated In future system paths will be replaced with actual resources
     *             or string encoded references
     */
    public NewVirtualReferenceMethod(String templateSysPath, String templatePath, NodeRef actualNodeRef,
                String vanillaProcessorClasspath)
    {
        super();
        this.templateSysPath = templateSysPath;
        this.templatePath = templatePath;
        this.actualNodeRef = actualNodeRef;
        this.vanillaProcessorClasspath = vanillaProcessorClasspath;
    }

    public NewVirtualReferenceMethod(NodeRef templateRef, String templatePath, NodeRef actualNodeRef,
                String vanillaProcessorClasspath)
    {
        super();
        this.templateRef = templateRef;
        this.templatePath = templatePath;
        this.actualNodeRef = actualNodeRef;
        this.vanillaProcessorClasspath = vanillaProcessorClasspath;
    }

    @Override
    public Reference execute(VirtualProtocol virtualProtocol, Reference reference) throws ProtocolMethodException
    {
        if (templateRef != null)
        {
            return virtualProtocol.newReference(templateRef,
                                                templatePath,
                                                actualNodeRef);
        }
        else
        {
            return virtualProtocol.newReference(templateSysPath,
                                                templatePath,
                                                actualNodeRef);
        }
    }

    @Override
    public Reference execute(VanillaProtocol vanillaProtocol, Reference reference) throws ProtocolMethodException
    {
        if (templateRef != null)
        {
            return vanillaProtocol.newReference(vanillaProcessorClasspath,
                                                templatePath,
                                                actualNodeRef,
                                                templateRef);

        }
        else
        {
            return vanillaProtocol.newReference(vanillaProcessorClasspath,
                                                templatePath,
                                                actualNodeRef,
                                                templateSysPath);
        }
    }
}
