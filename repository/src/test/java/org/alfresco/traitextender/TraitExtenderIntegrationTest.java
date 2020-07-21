/*
 * #%L
 * Alfresco Repository
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

package org.alfresco.traitextender;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.traitextender.Extender;
import org.alfresco.traitextender.ExtensionPoint;
import org.alfresco.traitextender.InstanceExtensionFactory;
import org.alfresco.traitextender.RegistryExtensionBundle;
import org.alfresco.traitextender.SingletonExtensionFactory;

import junit.framework.TestCase;

public class TraitExtenderIntegrationTest extends TestCase
{
    private RegistryExtensionBundle extensionBundle;

    private RegistryExtensionBundle singletonExtensionBundle;

    private RegistryExtensionBundle publicExtensionBundle;

    @Override
    protected void setUp() throws Exception
    {
        Extender.getInstance().stopAll();

        extensionBundle = new RegistryExtensionBundle("extensionBundle");
        extensionBundle
                    .register(new ExtensionPoint<TestExtension, TestTrait>(TestExtension.class,
                                                                           TestTrait.class),
                              new InstanceExtensionFactory<TestExtensionImpl, TestTrait, TestExtension>(TestExtensionImpl.class,
                                                                                                        TestTrait.class,
                                                                                                        TestExtension.class));

        TestSingletonExtensionImpl sigletonExtension = new TestSingletonExtensionImpl("s1");

        singletonExtensionBundle = new RegistryExtensionBundle("singletonExtensionBundle");
        singletonExtensionBundle
                    .register(new ExtensionPoint<TestExtension, TestTrait>(TestExtension.class,
                                                                           TestTrait.class),
                              new SingletonExtensionFactory<TestExtension, TestSingletonExtensionImpl, TestTrait>(sigletonExtension,
                                                                                                                  TestExtension.class));

        publicExtensionBundle = new RegistryExtensionBundle("publicExtensionBundle");
        publicExtensionBundle
                    .register(new ExtensionPoint<TestPublicExtension, TestPublicTrait>(TestPublicExtension.class,
                                                                                       TestPublicTrait.class),
                              new InstanceExtensionFactory<TestPublicExtensionImpl, TestPublicTrait, TestPublicExtension>(TestPublicExtensionImpl.class,
                                                                                                                          TestPublicTrait.class,
                                                                                                                          TestPublicExtension.class));
    }

    public void testIntegration()
    {
        Extender.getInstance().start(extensionBundle);

        assertEquals("TestService.privateServiceMethod1(testIntegration) TestExtensionImpl.privateServiceMethod1(testIntegration)",
                     new TestService("psm1").publicServiceMethod1("testIntegration"));
    }
    
    public void testIntegration_overrideExtensible_1()
    {
        Extender.getInstance().start(extensionBundle);

        String expectedSuffix = new TestService("psm1").publicServiceMethod3("testIntegration");
        assertEquals("x"+expectedSuffix,
                     new TestServiceExtension("psm1").publicServiceMethod3("testIntegration"));
    }

    public void testIntegration_stoppedBundle()
    {
        final TestService preStopService = new TestService("psm1");
        Extender.getInstance().start(extensionBundle);

        assertEquals("TestService.privateServiceMethod1(testIntegration) TestExtensionImpl.privateServiceMethod1(testIntegration)",
                     preStopService.publicServiceMethod1("testIntegration"));

        Extender.getInstance().stop(extensionBundle);
        final TestService postStopService = new TestService("psm1");

        assertEquals("TestService.privateServiceMethod1(testIntegration)",
                     postStopService.publicServiceMethod1("testIntegration"));
        assertEquals("TestService.privateServiceMethod1(testIntegration)",
                     preStopService.publicServiceMethod1("testIntegration"));
    }

    public void testIntegration_singletonExtension()
    {
        Extender.getInstance().start(singletonExtensionBundle);

        assertEquals("psm1 TestSingletonExtensionImpl.publicServiceMethod2(testIntegration)@s1",
                     new TestService("psm1").publicServiceMethod2("testIntegration"));

        assertEquals("psm2 TestSingletonExtensionImpl.publicServiceMethod2(testIntegration)@s1",
                     new TestService("psm2").publicServiceMethod2("testIntegration"));
    }

    public void testIntegration_publicTrait()
    {
        Extender.getInstance().start(publicExtensionBundle);

        assertEquals("EPM1PM1testIntegration",
                     new TestPublicService().publicMethod1("testIntegration"));

        assertEquals("EPM2PM2testIntegration",
                     new TestPublicService().publicMethod2("testIntegration"));
    }

    public void testIntegration_publicOverridenExtensible_1()
    {
        Extender.getInstance().start(publicExtensionBundle);

        assertEquals("XEPM1PM1testIntegration",
                     new TestPublicServiceExtension().publicMethod1("testIntegration"));
    }

    public void testIntegration_publicOverridenExtensible_2()
    {
        Extender.getInstance().start(publicExtensionBundle);
        
        assertEquals("XEPM2PM2testIntegration",
                     new TestPublicServiceExtension().publicMethod2("testIntegration"));
    }

    public void testIntegration_bypass()
    {

        Extender.getInstance().start(singletonExtensionBundle);

        assertEquals("PSM3TestService.privateServiceMethod1(bypass) TestSingletonExtensionImpl.privateServiceMethod1(bypass)@s1 TestSingletonExtensionImpl.publicServiceMethod3(bypass)@s1",
                     new TestService("SBP").publicServiceMethod3("bypass"));
    }

    public void testIntegration_exceptionHandling()
    {
        Extender.getInstance().start(publicExtensionBundle);

        try
        {
            new TestPublicService().publicMethod3(true,
                                                  false);
            fail("An exception was expected!");
        }
        catch (TestException e)
        {
            // void - success
        }
        catch (Exception e)
        {
            fail(TestException.class + " wa expected but got " + e);
        }
    }

    public void testIntegration_runtimeExceptionHandling()
    {
        Extender.getInstance().start(publicExtensionBundle);

        try
        {
            new TestPublicService().publicMethod4(true,
                                                  false);
            fail("An exception was expected!");
        }
        catch (TestRuntimeException e)
        {
            // void - success
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail(TestRuntimeException.class + " wa expected but got " + e.getClass());
        }

        try
        {
            new TestPublicService().publicMethod4(false,
                                                  true);
            fail("An exception was expected!");
        }
        catch (TestRuntimeException e)
        {
            // void - success
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail(TestRuntimeException.class + " wa expected but got " + e.getClass());
        }
    }

    private void assertNoThreadTraitSideEffect()
    {
        final TestService s1 = new TestService("psm1");
        final TestService s2 = new TestService("psm2");

        final Integer s1Id = System.identityHashCode(s1);
        final Integer s2Id = System.identityHashCode(s2);

        final List<Integer> traitIdentities = new ArrayList<Integer>();
        s1.publicServiceMethod3(s2,
                                traitIdentities);

        assertEquals(6,
                     traitIdentities.size());
        final Integer s1TraitId = traitIdentities.get(0);
        final Integer s2TraitId = traitIdentities.get(1);
        assertFalse(s1TraitId.equals(s2TraitId));
        assertEquals(s2TraitId,
                     traitIdentities.get(2));
        assertEquals(s2Id,
                     traitIdentities.get(3));
        assertEquals(s1TraitId,
                     traitIdentities.get(4));
        assertEquals(s1Id,
                     traitIdentities.get(5));
    }

    public void testIntegration_threadTraitSideEffect()
    {

        Extender.getInstance().start(singletonExtensionBundle);
        assertNoThreadTraitSideEffect();
        Extender.getInstance().stop(singletonExtensionBundle);

        Extender.getInstance().start(extensionBundle);
        assertNoThreadTraitSideEffect();
        Extender.getInstance().stop(extensionBundle);

    }
}
