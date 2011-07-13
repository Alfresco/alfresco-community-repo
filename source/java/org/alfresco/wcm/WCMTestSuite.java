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
package org.alfresco.wcm;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.alfresco.wcm.asset.AssetServiceImplTest;
import org.alfresco.wcm.preview.PreviewURIServiceImplTest;
import org.alfresco.wcm.sandbox.SandboxServiceImplTest;
import org.alfresco.wcm.webproject.WebProjectServiceImplTest;
import org.alfresco.wcm.webproject.script.ScriptWebProjectsTest;

/**
 * WCM test suite
 * 
 * @author janv
 */
public class WCMTestSuite extends TestSuite
{
    /**
     * Creates the test suite
     * 
     * @return  the test suite
     */
    public static Test suite() 
    {
        TestSuite suite = new TestSuite();
        
        suite.addTestSuite(WCMAspectTest.class);
        suite.addTestSuite(WebProjectServiceImplTest.class);
        suite.addTestSuite(AssetServiceImplTest.class);
        suite.addTestSuite(SandboxServiceImplTest.class);
        suite.addTestSuite(ScriptWebProjectsTest.class);
        suite.addTestSuite(PreviewURIServiceImplTest.class);
        suite.addTestSuite(WCMConcurrentTest.class);
        return suite;
    }
}
