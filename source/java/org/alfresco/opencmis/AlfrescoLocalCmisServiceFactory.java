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
            AlfrescoCmisService cmisService = new AlfrescoCmisServiceImpl(CMIS_CONNECTOR);
            wrapperService = new CmisServiceWrapper<AlfrescoCmisService>(cmisService,
                    CMIS_CONNECTOR.getTypesDefaultMaxItems(), CMIS_CONNECTOR.getTypesDefaultDepth(),
                    CMIS_CONNECTOR.getObjectsDefaultMaxItems(), CMIS_CONNECTOR.getObjectsDefaultDepth());
            THREAD_LOCAL_SERVICE.set(wrapperService);
        }
        wrapperService.getWrappedService().open(context);
        return wrapperService;
    }
}
