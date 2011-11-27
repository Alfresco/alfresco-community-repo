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
 * Factory for local OpenCMIS service objects.
 * 
 * @author florian.mueller
 */
public class AlfrescoLocalCmisServiceFactory extends AbstractServiceFactory
{
    private static ThreadLocal<CmisServiceWrapper<AlfrescoCmisService>> THREAD_LOCAL_SERVICE = new ThreadLocal<CmisServiceWrapper<AlfrescoCmisService>>();

    private static CMISConnector CMIS_CONNECTOR;

    @Override
    public void init(Map<String, String> parameters)
    {
    }

    /**
     * Sets the CMIS connector.
     */
    public static void setCmisConnector(CMISConnector connector)
    {
        CMIS_CONNECTOR = connector;
    }

    @Override
    public void destroy()
    {
        THREAD_LOCAL_SERVICE = null;
    }

    @Override
    public CmisService getService(CallContext context)
    {
        CmisServiceWrapper<AlfrescoCmisService> wrapperService = THREAD_LOCAL_SERVICE.get();
        if (wrapperService == null)
        {
            wrapperService = new CmisServiceWrapper<AlfrescoCmisService>(new AlfrescoCmisServiceImpl(CMIS_CONNECTOR),
                    CMIS_CONNECTOR.getTypesDefaultMaxItems(), CMIS_CONNECTOR.getTypesDefaultDepth(),
                    CMIS_CONNECTOR.getObjectsDefaultMaxItems(), CMIS_CONNECTOR.getObjectsDefaultDepth());
            THREAD_LOCAL_SERVICE.set(wrapperService);
        }
        return wrapperService;
    }
}
