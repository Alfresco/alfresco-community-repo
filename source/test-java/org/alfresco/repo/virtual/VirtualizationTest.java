
package org.alfresco.repo.virtual;

import org.alfresco.util.ApplicationContextHelper;

/**
 * Virtualization tests constant interface.
 * 
 * @author Bogdan Horje
 */
public interface VirtualizationTest
{
    static String VIRTUALIZATION_CONFIG_TEST_BOOTSTRAP_BEAN_ID = "virtualizationConfigTestBootstrap";

    static final String[] CONFIG_LOCATIONS = new String[] { ApplicationContextHelper.CONFIG_LOCATIONS[0],
                "classpath:**/virtualization-test-context.xml" };
}
