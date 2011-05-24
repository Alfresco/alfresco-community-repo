/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.opencmis;

import java.util.Map;

import org.apache.chemistry.opencmis.commons.impl.server.AbstractServiceFactory;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.server.support.CmisServiceWrapper;

/**
 * Factory for OpenCMIS service objects.
 * 
 * @author florian.mueller
 */
public class AlfrescoCmisServiceFactory extends AbstractServiceFactory
{
    private ThreadLocal<CmisServiceWrapper<AlfrescoCmisService>> threadLocalService = new ThreadLocal<CmisServiceWrapper<AlfrescoCmisService>>();

    private CMISConnector connector;

    @Override
    public void init(Map<String, String> parameters)
    {
    }

    /**
     * Sets the CMIS connector.
     */
    public void setCmisConnector(CMISConnector connector)
    {
        this.connector = connector;
    }

    @Override
    public void destroy()
    {
        threadLocalService = null;
    }

    @Override
    public CmisService getService(CallContext context)
    {
        CmisServiceWrapper<AlfrescoCmisService> wrapperService = threadLocalService.get();
        if (wrapperService == null)
        {
            wrapperService = new CmisServiceWrapper<AlfrescoCmisService>(new AlfrescoCmisService(connector),
                    connector.getTypesDefaultMaxItems(), connector.getTypesDefaultDepth(),
                    connector.getObjectsDefaultMaxItems(), connector.getObjectsDefaultDepth());
            threadLocalService.set(wrapperService);
        }

        wrapperService.getWrappedService().beginCall(context);

        return wrapperService;
    }
}
