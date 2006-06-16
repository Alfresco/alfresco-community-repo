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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * This is a helper class that knows how to issue identifiers.
 * @author britt
 */
class Issuer
{
    /**
     * The path to this issuers persistent storage.
     */
    private String fPath;
    
    /**
     * The next number to issue.
     */
    private long fNext;
    
    /**
     * Constructor for an already existing Issuer.
     * @param path The path to this issuers persistent store.
     */
    public Issuer(String path)
    {
        fPath = path;
        try
        {
            DataInputStream in = new DataInputStream(new FileInputStream(fPath + ".new"));
            fNext = in.readLong();
            fNext += 257;
            in.close();
            save();
            return;
        }
        catch (IOException ie)
        {
            // Do nothing.
        }
        try
        {
            DataInputStream in = new DataInputStream(new FileInputStream(fPath));
            fNext = in.readLong();
            fNext += 257;
            in.close();
            save();
            return;
        }
        catch (IOException ie)
        {
            // Do nothing.
        }
        // Last resort.
        try
        {
            DataInputStream in = new DataInputStream(new FileInputStream(fPath + ".old"));
            fNext = in.readLong();
            fNext += 513;
            in.close();
            save();
            return;
        }
        catch (IOException ie)
        {
            // TODO Log this situation.
            throw new AVMException("Could not restore issuer" + fPath, ie);
        }
    }
            
    /**
     * Rich constructor.
     * @param path The path to this issuers persistent store.
     * @param next The next number to issue.
     */
    public Issuer(String path, long next)
    {
        fPath = path;
        fNext = next;
        save();
    }
    
    /**
     * Issue the next number.
     * @return A serial number.
     */
    public synchronized long issue()
    {
        long val = fNext++;
        if (fNext % 128 == 0)
        {
            save();
        }
        return val;
    }
    
    /**
     * Persist this issuer.
     */
    public void save()
    {
        while (true)
        {
            try
            {
                FileOutputStream fileOut = new FileOutputStream(fPath + ".new");
                DataOutputStream out = new DataOutputStream(fileOut);
                out.writeLong(fNext);
                out.flush();
                // Force data to physical storage.
                fileOut.getChannel().force(true);
                out.close();
                File from = new File(fPath);
                File to = new File(fPath + ".old");
                from.renameTo(to);
                from = new File(fPath + ".new");
                to = new File(fPath);
                from.renameTo(to);
                break;
            }
            catch (IOException ie)
            {
                // TODO Log this situation.
            }
        }
    }                
}
