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

package org.alfresco.repo.avm;

import junit.framework.TestCase;

/**
 * Simple sanity test for Issuer;
 * @author britt
 */
public class IssuerTest extends TestCase
{
    /**
     * Sanity check issuer logic.
     */
    public void testSanity()
    {
        Issuer issuer = new Issuer("test", 0L);
        for (int i = 0; i < 500; i++)
        {
            issuer.issue();
        }
        issuer = new Issuer("test");
        assertTrue(issuer.issue() >= 500);
    }
}
