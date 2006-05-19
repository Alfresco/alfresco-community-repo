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

package org.alfresco.repo.avm.hibernate;

import org.hibernate.Session;

/**
 * This is a helper class that knows how to issue identifiers.
 * @author britt
 */
public class Issuer
{
    /**
     * The name of this issuer.  Used as the primary key in it's
     * mapping.
     */
    private String fName;
    
    /**
     * The next number to issue.
     */
    private long fNext;
    
    /**
     * The version (for concurrency control).
     */
    private long fVers;
    
    /**
     * Anonymous constructor.
     */
    public Issuer()
    {
    }
    
    /**
     * Rich constructor.
     * @param name The name of this issuer.
     * @param next The next number to issue.
     */
    public Issuer(String name, long next, Session session)
    {
        fName = name;
        fNext = next;
        session.save(this);
    }
    
    // Bean methods.
    
    /**
     * Set the name of this issuer.
     * @param name The name to set.
     */
    public void setName(String name)
    {
        fName = name;
    }
    
    /**
     * Get the name of this issuer.
     * @return The name of this issuer.
     */
    public String getName()
    {
        return fName;
    }
    
    /**
     * Set the next number.
     * @param next The next number.
     */
    public void setNext(long next)
    {
        fNext = next;
    }
    
    /**
     * Get the next number.
     * @return The next number.
     */
    public long getNext()
    {
        return fNext;
    }
    
    /**
     * Issue the next number.
     * @return A serial number.
     */
    public long issue()
    {
        return fNext++;
    }

    /**
     * @return the vers
     */
    public long getVers()
    {
        return fVers;
    }

    /**
     * @param vers the vers to set
     */
    public void setVers(long vers)
    {
        fVers = vers;
    }
}
