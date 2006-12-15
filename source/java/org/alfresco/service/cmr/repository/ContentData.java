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
import java.util.Locale;
import java.util.StringTokenizer;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
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
    private final Locale locale;
    
    /**
     * Construct a content property from a string
     * 
     * @param contentPropertyStr the string representing the content details
     * @return Returns a bean version of the string
     */
    public static ContentData createContentProperty(String contentPropertyStr)
    {
        String contentUrl = null;
        String mimetype = null;
        long size = 0L;
        String encoding = null;
        Locale locale = null;
        // now parse the string
        StringTokenizer tokenizer = new StringTokenizer(contentPropertyStr, "|");
        while (tokenizer.hasMoreTokens())
        {
            String token = tokenizer.nextToken();
            if (token.startsWith("contentUrl="))
            {
                contentUrl = token.substring(11);
                if (contentUrl.length() == 0)
                {
                    contentUrl = null;
                }
            }
            else if (token.startsWith("mimetype="))
            {
                mimetype = token.substring(9);
                if (mimetype.length() == 0)
                {
                    mimetype = null;
                }
            }
            else if (token.startsWith("size="))
            {
                String sizeStr = token.substring(5);
                if (sizeStr.length() > 0)
                {
                    size = Long.parseLong(sizeStr);
                }
            }
            else if (token.startsWith("encoding="))
            {
                encoding = token.substring(9);
                if (encoding.length() == 0)
                {
                    encoding = null;
                }
            }
            else if (token.startsWith("locale="))
            {
                String localeStr = token.substring(7);
                if (localeStr.length() > 0)
                {
                    locale = I18NUtil.parseLocale(localeStr);
                }
            }
        }
        
        ContentData property = new ContentData(contentUrl, mimetype, size, encoding, locale);
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
                existing == null ? "UTF-8" : existing.encoding,
                existing == null ? null : existing.locale);
        // done
        return ret;
    }
    
    /**
     * Create a content data using the {@link I18NUtil#getLocale() default locale}.
     * 
     * @see #ContentData(String, String, long, String, Locale)
     */
    public ContentData(String contentUrl, String mimetype, long size, String encoding)
    {
        this(contentUrl, mimetype, size, encoding, null);
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
     * @param encoding the content encoding (may be <tt>null</tt>).
     * @param locale the locale of the content (may be <tt>null</tt>).  If <tt>null</tt>, the
     *      {@link I18NUtil#getLocale() default locale} will be used.
     */
    public ContentData(String contentUrl, String mimetype, long size, String encoding, Locale locale)
    {
        checkContentUrl(contentUrl, mimetype);
        this.contentUrl = contentUrl;
        this.mimetype = mimetype;
        this.size = size;
        this.encoding = encoding;
        if (locale == null)
        {
            locale = I18NUtil.getLocale();
        }
        this.locale = locale;
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
                EqualsHelper.nullSafeEquals(this.encoding, that.encoding) &&
                EqualsHelper.nullSafeEquals(this.locale, that.locale));
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
          .append("|encoding=").append(encoding == null ? "" : encoding)
          .append("|locale=").append(locale == null ? "" : DefaultTypeConverter.INSTANCE.convert(String.class, locale));
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
    
    /**
     * Get the content's locale.
     * 
     * @return Returns a locale, or null if the locale is unknown
     */
    public Locale getLocale()
    {
        return locale;
    }
}
