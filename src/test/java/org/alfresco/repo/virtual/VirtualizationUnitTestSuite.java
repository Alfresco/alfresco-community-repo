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

package org.alfresco.repo.virtual;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestSuite;

public class VirtualizationUnitTestSuite
{
    /**
     * Creates the test suite
     *
     * @return the test suite
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        unitTests(suite);
        return suite;
    }

    static void unitTests(TestSuite suite)
    {
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.virtual.page.PageCollatorTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.virtual.ref.GetChildByIdMethodTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.virtual.ref.GetParentReferenceMethodTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.virtual.ref.NewVirtualReferenceMethodTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.virtual.ref.PlainReferenceParserTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.virtual.ref.PlainStringifierTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.virtual.ref.ProtocolTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.virtual.ref.ReferenceTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.virtual.ref.ResourceParameterTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.virtual.ref.StringParameterTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.virtual.ref.VirtualProtocolTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.virtual.store.ReferenceComparatorTest.class));

        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.virtual.ref.ZeroReferenceParserTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.virtual.ref.ZeroStringifierTest.class));

        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.virtual.ref.HashStringifierTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.virtual.ref.NodeRefRadixHasherTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.virtual.ref.NumericPathHasherTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.virtual.ref.StoredPathHasherTest.class));

        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.virtual.template.VirtualQueryImplTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.virtual.store.TypeVirtualizationMethodTest.Unit.class));
    }
}
