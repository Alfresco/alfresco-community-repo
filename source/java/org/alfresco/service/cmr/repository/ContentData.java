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
package org.alfresco.service.cmr.repository;

import java.io.Serializable;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.util.EqualsHelper;

/**
 * The compound property representing content
 * 
 * @author Derek Hulley
 */
public class ContentData implements Serializable
{
    private static final long serialVersionUID = 8979634213050121462L;

    private static char[] INVALID_CONTENT_URL_CHARS = new char[] {'|'};
    
    private final String contentUrl;
    private final String mimetype;
    private final long size;
    private final String encoding;
    
    /**
     * Construct a content property from a string
     * 
     * @param contentPropertyStr the string representing the content details
     * @return Returns a bean version of the string
     */
    public static ContentData createContentProperty(String contentPropertyStr)
    {
        // get the content url
        int contentUrlIndex = contentPropertyStr.indexOf("contentUrl=");
        if (contentUrlIndex == -1)
        {
            throw new AlfrescoRuntimeException(
                    "ContentData string does not have a content URL: " +
                    contentPropertyStr);
        }
        int mimetypeIndex = contentPropertyStr.indexOf("|mimetype=", contentUrlIndex + 11);
        if (mimetypeIndex == -1)
        {
            throw new AlfrescoRuntimeException(
                    "ContentData string does not have a mimetype: " +
                    contentPropertyStr);
        }
        int sizeIndex = contentPropertyStr.indexOf("|size=", mimetypeIndex + 10);
        if (sizeIndex == -1)
        {
            throw new AlfrescoRuntimeException(
                    "ContentData string does not have a size: " +
                    contentPropertyStr);
        }
        int encodingIndex = contentPropertyStr.indexOf("|encoding=", sizeIndex + 6);
        if (encodingIndex == -1)
        {
            throw new AlfrescoRuntimeException(
                    "ContentData string does not have an encoding: " +
                    contentPropertyStr);
        }
        
        String contentUrl = contentPropertyStr.substring(contentUrlIndex + 11, mimetypeIndex);
        if (contentUrl.length() == 0)
            contentUrl = null;
        String mimetype = contentPropertyStr.substring(mimetypeIndex + 10, sizeIndex);
        if (mimetype.length() == 0)
            mimetype = null;
        String sizeStr = contentPropertyStr.substring(sizeIndex + 6, encodingIndex);
        if (sizeStr.length() == 0)
            sizeStr = "0";
        String encoding = contentPropertyStr.substring(encodingIndex + 10);
        if (encoding.length() == 0)
            encoding = null;
        
        long size = Long.valueOf(sizeStr);
        
        ContentData property = new ContentData(contentUrl, mimetype, size, encoding);
        // done
        return property;
    }
    
    /**
     * Constructs a new instance using the existing one as a template, but replacing the
     * mimetype
     * 
     * @param existing an existing set of content data, null to use default values
     * @param mimetype the mimetype to set
     * @return Returns a new, immutable instance of the data
     */
    public static ContentData setMimetype(ContentData existing, String mimetype)
    {
        ContentData ret = new ContentData(
                existing == null ? null : existing.contentUrl,
                mimetype,
                existing == null ? 0L : existing.size,
                existing == null ? "UTF-8" : existing.encoding);
        // done
        return ret;
    }
    
    /**
     * Create a compound set of data representing a single instance of <i>content</i>.
     * <p>
     * In order to ensure data integrity, the {@link #getMimetype() mimetype}
     * must be set if the {@link #getContentUrl() content URL} is set.
     * 
     * @param contentUrl the content URL.  If this value is non-null, then the
     *      <b>mimetype</b> must be supplied.
     * @param mimetype the content mimetype.  This is mandatory if the <b>contentUrl</b> is specified.
     * @param size the content size.
     * @param encoding the content encoding.
     */
    public ContentData(String contentUrl, String mimetype, long size, String encoding)
    {
        checkContentUrl(contentUrl, mimetype);
        
        this.contentUrl = contentUrl;
        this.mimetype = mimetype;
        this.size = size;
        this.encoding = encoding;
    }
    
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        else if (obj == null)
            return false;
        else if (!(obj instanceof ContentData))
            return false;
        ContentData that = (ContentData) obj;
        return (EqualsHelper.nullSafeEquals(this.contentUrl, that.contentUrl) &&
                EqualsHelper.nullSafeEquals(this.mimetype, that.mimetype) &&
                this.size == that.size &&
                EqualsHelper.nullSafeEquals(this.encoding, that.encoding));
    }
    
    /**
     * @return Returns a string of form: <code>contentUrl=xxx;mimetype=xxx;size=xxx;encoding=xxx</code>
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder(80);
        sb.append("contentUrl=").append(contentUrl == null ? "" : contentUrl)
          .append("|mimetype=").append(mimetype == null ? "" : mimetype)
          .append("|size=").append(size)
          .append("|encoding=").append(encoding == null ? "" : encoding);
        return sb.toString();
    }
    
    /**
     * @return Returns a URL identifying the specific location of the content.
     *      The URL must identify, within the context of the originating content
     *      store, the exact location of the content.
     * @throws ContentIOException
     */
    public String getContentUrl()
    {
        return contentUrl;
    }
    
    /**
     * Checks that the content URL is correct, and also that the mimetype is
     * non-null if the URL is present.
     * 
     * @param contentUrl the content URL to check
     * @param mimetype
     */
    private void checkContentUrl(String contentUrl, String mimetype)
    {
        // check the URL
        if (contentUrl != null && contentUrl.length() > 0)
        {
            for (int i = 0; i < INVALID_CONTENT_URL_CHARS.length; i++)
            {
                for (int j = contentUrl.length() - 1; j > -1; j--)
                {
                    if (contentUrl.charAt(j) == INVALID_CONTENT_URL_CHARS[i])
                    {
                        throw new IllegalArgumentException(
                                "The content URL contains an invalid char: \n" +
                                "   content URL: " + contentUrl + "\n" +
                                "   char: " + INVALID_CONTENT_URL_CHARS[i] + "\n" +
                                "   position: " + j);
                    }
                }
            }
            // check that mimetype is present if URL is present
            if (mimetype == null)
            {
                throw new IllegalArgumentException(
                        "The content mimetype must be set whenever the URL is set: \n" +
                        "   content URL: " + contentUrl + "\n" +
                        "   mimetype: " + mimetype);
            }
        }
    }
    
    /**
     * Gets content's mimetype.
     * 
     * @return Returns a standard mimetype for the content or null if the mimetype
     *      is unkown
     */
    public String getMimetype()
    {
        return mimetype;
    }
    
    /**
     * Get the content's size
     *  
     * @return Returns the size of the content
     */
    public long getSize()
    {
        return size;
    }
    
    /**
     * Gets the content's encoding.
     * 
     * @return Returns a valid Java encoding, typically a character encoding, or
     *      null if the encoding is unkown
     */
    public String getEncoding()
    {
        return encoding;
    }
}
