/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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

package org.alfresco.rest.api.namespace;

import org.springframework.beans.factory.InitializingBean;

import org.alfresco.rest.api.NamespacePrefixes;
import org.alfresco.rest.api.model.NamespacePrefixEntry;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.util.ParameterCheck;

/**
 * REST API v1 endpoint for namespace-prefix mapping. ACS-12299
 */
@EntityResource(name = "namespace-prefix", title = "Namespace Prefixes")
public class NamespacePrefixesEntityResource implements EntityResourceAction.Read<NamespacePrefixEntry>, InitializingBean
{
    private NamespacePrefixes namespacePrefixes;

    public void setNamespacePrefixes(NamespacePrefixes namespacePrefixes)
    {
        this.namespacePrefixes = namespacePrefixes;
    }

    @Override
    public void afterPropertiesSet()
    {
        ParameterCheck.mandatory("namespacePrefixes", namespacePrefixes);
    }

    @Override
    public CollectionWithPagingInfo<NamespacePrefixEntry> readAll(Parameters parameters)
    {
        return namespacePrefixes.getNamespacePrefixes(parameters);
    }
}
