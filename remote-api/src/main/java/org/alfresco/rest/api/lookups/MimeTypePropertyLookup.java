/*-
 * #%L
 * Alfresco Remote API
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
package org.alfresco.rest.api.lookups;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Looks up mimetype values
 * Pass in a mimetype value and a display string is returned.
 * @author Gethin James
 */
public class MimeTypePropertyLookup implements PropertyLookup<String>
{
    private Set<String> supported = new HashSet<>();
    private ServiceRegistry serviceRegistry;

    @Override
    public String lookup(String propertyValue)
    {
        Map<String,String> mimetypes = serviceRegistry.getMimetypeService().getDisplaysByMimetype();
        return mimetypes.get(propertyValue);
    }

    @Override
    public Set<String> supports()
    {
        return supported;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    public void setSupported(List<String> supported)
    {
        this.supported.add(QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "content.mimetype").toString());
        this.supported.addAll(supported);
    }

}
