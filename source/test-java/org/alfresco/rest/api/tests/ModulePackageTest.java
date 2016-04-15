package org.alfresco.rest.api.tests;

import static org.junit.Assert.*;

import org.alfresco.repo.module.ModuleDetailsImpl;
import org.alfresco.repo.module.ModuleDetailsImplTest;
import org.alfresco.rest.api.model.ModulePackage;
import org.alfresco.service.cmr.module.ModuleDetails;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

/**
 * Basic tests for taking ModuleDetailsImpl and representing it as a ModulePackage class.
 * @author Gethin James.
 */
public class ModulePackageTest
{
    static Properties props = ModuleDetailsImplTest.DEFAULT_PROPS;

    @Test
    public void testConstructor() throws IOException
    {
        ModuleDetails details = new ModuleDetailsImpl(props);
        ModulePackage modulePackage = ModulePackage.fromModuleDetails(details);
        assertNotNull(modulePackage);
        assertEquals(props.getProperty(ModuleDetails.PROP_ID), modulePackage.getId());
        assertEquals(props.getProperty(ModuleDetails.PROP_VERSION),modulePackage.getVersion());
        assertEquals(props.getProperty(ModuleDetails.PROP_REPO_VERSION_MIN),modulePackage.getVersionMin());
        assertEquals(props.getProperty(ModuleDetails.PROP_REPO_VERSION_MAX),modulePackage.getVersionMax());
        assertEquals(props.getProperty(ModuleDetails.PROP_INSTALL_STATE),modulePackage.getInstallState().toString());
    }

    @Test
    public void testInvalidConstructor() throws IOException
    {
        ModulePackage modulePackage = ModulePackage.fromModuleDetails(null);
        assertNull(modulePackage);
    }
}
