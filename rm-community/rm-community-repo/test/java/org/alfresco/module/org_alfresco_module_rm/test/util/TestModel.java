 
package org.alfresco.module.org_alfresco_module_rm.test.util;

import org.alfresco.service.namespace.QName;

/**
 * @author Roy Wetherall
 * @since 2.3
 */
public interface TestModel
{
    public static final String TEST_URI = "http://www.alfresco.org/model/rmtest/1.0";
    public static final String TEST_PREFIX = "rmt";
    
    public static final QName ASPECT_RECORD_METADATA = QName.createQName(TEST_URI, "recordMetaData");
    public static final QName PROPERTY_RECORD_METADATA = QName.createQName(TEST_URI, "recordMetaDataProperty");
}
