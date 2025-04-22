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
package org.alfresco.service.cmr.repository;

import java.io.Serializable;
import java.util.Locale;
import java.util.StringTokenizer;

import org.springframework.extensions.surf.util.I18NUtil;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.util.EqualsHelper;

/**
 * The compound property representing content
 * 
 * @author Derek Hulley
 */
@AlfrescoPublicApi
public class ContentData implements Serializable
{
    private static final long serialVersionUID = 8979634213050121462L;

    private static char[] INVALID_CONTENT_URL_CHARS = new char[]{'|'};

    private final String contentUrl;
    private final String mimetype;
    private final long size;
    private final String encoding;
    private final Locale locale;

    /**
     * Construct a content property from a string
     * 
     * @param contentPropertyStr
     *            the string representing the content details
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
     * Constructs a new instance using the existing one as a template, but replacing the mimetype
     * 
     * @param existing
     *            an existing set of content data, null to use default values
     * @param mimetype
     *            the mimetype to set
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
     * Constructs a new instance using the existing one as a template, but replacing the encoding.
     * 
     * @param existing
     *            an existing set of content data, null to use default values
     * @param encoding
     *            the encoding to set
     * @return Returns a new, immutable instance of the data
     */
    public static ContentData setEncoding(ContentData existing, String encoding)
    {
        ContentData ret = new ContentData(
                existing == null ? null : existing.contentUrl,
                existing == null ? null : existing.mimetype,
                existing == null ? 0L : existing.size,
                encoding,
                existing == null ? null : existing.locale);
        // done
        return ret;
    }

    /**
     * Helper method to determine if the data represents any physical content or not.
     * <p>
     * This method only cares if there is a binary (content URL) and makes no assumptions about the length of the binary.
     * 
     * @param contentData
     *            the content to check (may be <tt>null</tt>)
     * @return <tt>true</tt> if the content URL is non-null i.e. there is a binary available
     */
    public static boolean hasContent(ContentData contentData)
    {
        if (contentData == null)
        {
            return false;
        }
        return contentData.contentUrl != null;
    }

    /**
     * Copy constructor for derived class
     * 
     * @param original
     *            the object to copy
     */
    protected ContentData(ContentData original)
    {
        this.contentUrl = original.contentUrl;
        this.encoding = original.encoding;
        this.locale = original.locale;
        this.mimetype = original.mimetype;
        this.size = original.size;
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
     * In order to ensure data integrity, the {@link #getMimetype() mimetype} must be set if the {@link #getContentUrl() content URL} is set.
     * 
     * @param contentUrl
     *            the content URL. If this value is non-null, then the <b>mimetype</b> must be supplied.
     * @param mimetype
     *            the content mimetype. This is mandatory if the <b>contentUrl</b> is specified.
     * @param size
     *            the content size.
     * @param encoding
     *            the content encoding. This is mandatory if the <b>contentUrl</b> is specified.
     * @param locale
     *            the locale of the content (may be <tt>null</tt>). If <tt>null</tt>, the {@link I18NUtil#getLocale() default locale} will be used.
     */
    public ContentData(String contentUrl, String mimetype, long size, String encoding, Locale locale)
    {
        if (contentUrl != null && (mimetype == null || mimetype.length() == 0))
        {
            mimetype = MimetypeMap.MIMETYPE_BINARY;
        }
        checkContentUrl(contentUrl, mimetype, encoding);
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
                EqualsHelper.nullSafeEquals(this.mimetype, that.mimetype, true) &&
                this.size == that.size &&
                EqualsHelper.nullSafeEquals(this.encoding, that.encoding, true) &&
                EqualsHelper.nullSafeEquals(this.locale, that.locale));
    }

    /**
     * @return Returns a string of form: <code>contentUrl=xxx|mimetype=xxx|size=xxx|encoding=xxx|locale=xxx</code>
     */
    public String toString()
    {
        return getInfoUrl();
    }

    /**
     * @return Returns a URL containing information on the content including the mimetype, locale, encoding and size, the string is returned in the form: <code>contentUrl=xxx|mimetype=xxx|size=xxx|encoding=xxx|locale=xxx</code>
     */
    public String getInfoUrl()
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
     * @return Returns a URL identifying the specific location of the content. The URL must identify, within the context of the originating content store, the exact location of the content.
     * @throws ContentIOException
     */
    public String getContentUrl()
    {
        return contentUrl;
    }

    /**
     * Checks that the content URL is correct, and also that the mimetype is non-null if the URL is present.
     * 
     * @param contentUrl
     *            the content URL to check
     * @param mimetype
     *            the encoding must be present if the content URL is present
     * @param encoding
     *            the encoding must be valid and present if the content URL is present
     */
    private void checkContentUrl(String contentUrl, String mimetype, String encoding)
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
                throw new IllegalArgumentException("\n" +
                        "The content mimetype must be set whenever the URL is set: \n" +
                        "   content URL: " + contentUrl + "\n" +
                        "  and mimetype cannot be 'null'.");
            }
        }
    }

    /**
     * Gets content's mimetype.
     * 
     * @return Returns a standard mimetype for the content or null if the mimetype is unkown
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
     * @return Returns a valid Java encoding, typically a character encoding, or null if the encoding is unkown
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

    /**
     * @return hashCode
     */
    public int hashCode()
    {
        if (contentUrl != null)
        {
            return contentUrl.hashCode();
        }
        return 0;
    }
}
