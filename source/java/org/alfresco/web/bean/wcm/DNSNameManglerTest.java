/*
 * Copyright (C) 2006 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */

package org.alfresco.web.bean.wcm;

import junit.framework.TestCase;

/**
 * Test the DNSNameMangler.
 * @author britt
 */
public class DNSNameManglerTest extends TestCase
{
    /**
     * Test it.
     */
    public void testIt()
    {
        try
        {
            String mangled = DNSNameMangler.MakeDNSName("website", "britt", "main");
            System.out.println(mangled);
            assertTrue(mangled.length() <= 59);
            mangled = DNSNameMangler.MakeDNSName("website", "Foodle Dee dOO", "main");
            System.out.println(mangled);
            assertTrue(mangled.length() <= 59);
            mangled = DNSNameMangler.MakeDNSName("website-thinkl$", "winky_froo", "orkle");
            System.out.println(mangled);
            assertTrue(mangled.length() <= 59);
            mangled = DNSNameMangler.MakeDNSName("fork", "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxZZxxxxxxxxxxxxxxxxxxxxxxxxxxxxx", "winkle");
            System.out.println(mangled);
            assertTrue(mangled.length() <= 59);
            mangled = DNSNameMangler.MakeDNSName("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx", "Frederick", "preview");
            System.out.println(mangled);
            assertTrue(mangled.length() <= 59);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }
}
