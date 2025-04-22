/*
 * #%L
 * Alfresco Data model classes
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
package org.alfresco.service.namespace;

import junit.framework.TestCase;

/**
 * Tests the various implementations of the {@link org.alfresco.service.namespace.QNamePattern}.
 * 
 * @author Derek Hulley
 */
public class QNamePatternTest extends TestCase
{
    private static final String TEST_NAMESPACE = "http://www.alfresco.org/QNamePatternTest";

    QName check1;
    QName check2;
    QName check3;

    public QNamePatternTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        check1 = QName.createQName(null, "ABC");
        check2 = QName.createQName(TEST_NAMESPACE, "XYZ");
        check3 = QName.createQName(TEST_NAMESPACE, "ABC");
    }

    public void testSimpleQNamePattern() throws Exception
    {
        QNamePattern pattern = QName.createQName(TEST_NAMESPACE, "ABC");

        // check
        assertFalse("Simple match failed: " + check1, pattern.isMatch(check1));
        assertFalse("Simple match failed: " + check2, pattern.isMatch(check2));
        assertTrue("Simple match failed: " + check3, pattern.isMatch(check3));
    }

    public void testRegexQNamePatternMatcher() throws Exception
    {
        QNamePattern pattern = new RegexQNamePattern(".*alfresco.*", "A.?C");

        // check
        assertFalse("Regex match failed: " + check1, pattern.isMatch(check1));
        assertFalse("Regex match failed: " + check2, pattern.isMatch(check2));
        assertTrue("Regex match failed: " + check3, pattern.isMatch(check3));

        assertTrue("All match failed: " + check1, RegexQNamePattern.MATCH_ALL.isMatch(check1));
        assertTrue("All match failed: " + check2, RegexQNamePattern.MATCH_ALL.isMatch(check2));
        assertTrue("All match failed: " + check3, RegexQNamePattern.MATCH_ALL.isMatch(check3));
    }
}
