/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.model.security;

import java.util.HashSet;
import java.util.Set;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Protected model artifact class.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
@AlfrescoPublicApi
public abstract class ProtectedModelArtifact
{
    /** Model security service */
    private ModelSecurityService modelSecurityService;

    /** Namespace service */
    private NamespaceService namespaceService;

    /** Qualified name of the model artifact */
    private QName name;

    /** Set of capabilities */
    private Set<Capability> capabilities;

    /** Capability names */
    private Set<String> capabilityNames;

    /**
     * @param namespaceService  namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    /**
     * @param modelSecurityService  model security service
     */
    public void setModelSecurityService(ModelSecurityService modelSecurityService)
    {
        this.modelSecurityService = modelSecurityService;
    }

    /**
     * Init method
     */
    public void init()
    {
        modelSecurityService.register(this);
    }

    /**
     * @param name  artifact name (in cm:content form)
     */
    public void setName(String name)
    {
        this.name = QName.createQName(name, namespaceService);
    }

    /**
     * @return  artifact QName
     */
    public QName getQName()
    {
        return name;
    }

    /**
     * @param capabilities  capabilities
     */
    public void setCapabilities(Set<Capability> capabilities)
    {
        this.capabilities = capabilities;
    }

    /**
     * @return  capabilities
     */
    public Set<Capability> getCapabilities()
    {
        return capabilities;
    }

    /**
     * @return  capability names
     */
    public Set<String> getCapilityNames()
    {
        if (capabilityNames == null && capabilities != null)
        {
            capabilityNames = new HashSet<>(capabilities.size());
            for (Capability capability : capabilities)
            {
                capabilityNames.add(capability.getName());
            }
        }

        return capabilityNames;
    }
}
