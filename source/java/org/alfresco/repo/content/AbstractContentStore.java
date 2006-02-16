/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.repo.content;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.util.GUID;

/**
 * Base class providing support for different types of content stores.
 * <p>
 * Since content URLs have to be consistent across all stores for
 * reasons of replication and backup, the most important functionality
 * provided is the generation of new content URLs and the checking of
 * existing URLs.
 * 
 * @author Derek Hulley
 */
public abstract class AbstractContentStore implements ContentStore
{
    /**
     * Simple implementation that uses the
     * {@link ContentReader#exists() reader's exists} method as its implementation.
     */
    public boolean exists(String contentUrl) throws ContentIOException
    {
        ContentReader reader = getReader(contentUrl);
        return reader.exists();
    }

    /**
     * Creates a new content URL.  This must be supported by all
     * stores that are compatible with Alfresco.
     * 
     * @return Returns a new and unique content URL
     */
    public static String createNewUrl()
    {
        Calendar calendar = new GregorianCalendar();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;  // 0-based
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        // create the URL
        StringBuilder sb = new StringBuilder(20);
        sb.append(STORE_PROTOCOL)
          .append(year).append('/')
          .append(month).append('/')
          .append(day).append('/')
          .append(GUID.generate()).append(".bin");
        String newContentUrl = sb.toString();
        // done
        return newContentUrl;
    }
    
    /**
     * This method can be used to ensure that URLs conform to the
     * required format.  If subclasses have to parse the URL,
     * then a call to this may not be required - provided that
     * the format is checked.
     * <p>
     * The protocol part of the URL (including legacy protocols)
     * is stripped out and just the relative path is returned.
     * 
     * @param contentUrl a URL of the content to check
     * @return Returns the relative part of the URL
     * @throws RuntimeException if the URL is not correct
     */
    public static String getRelativePart(String contentUrl) throws RuntimeException
    {
        int index = 0;
        if (contentUrl.startsWith(STORE_PROTOCOL))
        {
            index = 8;
        }
        else if (contentUrl.startsWith("file://"))
        {
            index = 7;
        }
        else
        {
            throw new AlfrescoRuntimeException(
                    "All content URLs must start with " + STORE_PROTOCOL + ": \n" +
                    "   the invalid url is: " + contentUrl);
        }
        
        // extract the relative part of the URL
        String path = contentUrl.substring(index);
        // more extensive checks can be added in, but it seems overkill
        if (path.length() < 10)
        {
            throw new AlfrescoRuntimeException(
                    "The content URL is invalid: \n" +
                    "   content url: " + contentUrl);
        }
        return path;
    }

    public final Set<String> getUrls() throws ContentIOException
    {
        return getUrls(null, null);
    }
}
