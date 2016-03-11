package org.alfresco.module.org_alfresco_module_rm.recorded.version.config;

import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseWebScriptUnitTest;
import org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionModel;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Base test class for the recorded version config tests
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public abstract class BaseRecordedVersionConfigTest extends BaseWebScriptUnitTest implements RecordableVersionModel
{
    /** Recorded version config web script root folder */
    protected static final String RECORDED_VERSION_CONFIG_WEBSCRIPT_ROOT = "alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/action/";

    /** Node ref for test document */
    protected NodeRef testdoc;

    /** Setup web script parameters */
    protected Map<String, String> buildParameters()
    {
        testdoc = generateCmContent("testdoc.txt");

        return buildParameters
        (
                "store_type",       testdoc.getStoreRef().getProtocol(),
                "store_id",         testdoc.getStoreRef().getIdentifier(),
                "id",               testdoc.getId()
        );
    }
}
