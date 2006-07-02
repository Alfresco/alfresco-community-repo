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

/**
 * This is a helper class that knows how to issue identifiers.
 * @author britt
 */
class Issuer
{
    /**
     * The next number to issue.
     */
    private long fNext;
    
    /**
     * Rich constructor.
     * @param next The next number to issue.
     */
    public Issuer(long next)
    {
        fNext = next;
    }
    
    /**
     * Issue the next number.
     * @return A serial number.
     */
    public synchronized long issue()
    {
        return fNext++;
    }
}
