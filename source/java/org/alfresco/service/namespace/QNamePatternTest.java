/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.service.namespace;


import junit.framework.TestCase;

/**
 * Tests the various implementations of the
 * {@link org.alfresco.service.namespace.QNamePattern}.
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
