/*
 * Copyright (C) 2005 Jesper Steen Møller
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
package org.alfresco.repo.content.metadata;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.text.ChangedCharSetException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;

/**
 * Extracts the following values from HTML documents:
 * <pre>
 *   <b>author:</b>                 --      cm:author
 *   <b>title:</b>                  --      cm:title
 *   <b>description:</b>            --      cm:description
 * </pre>
 * 
 * @author Jesper Steen Møller
 * @author Derek Hulley
 */
public class HtmlMetadataExtracter extends AbstractMappingMetadataExtracter
{
    private static final String KEY_AUTHOR = "author";
    private static final String KEY_TITLE = "title";
    private static final String KEY_DESCRIPTION= "description";

    private static final Set<String> MIMETYPES = new HashSet<String>(5);
    static
    {
        MIMETYPES.add(MimetypeMap.MIMETYPE_HTML);
        MIMETYPES.add(MimetypeMap.MIMETYPE_XHTML);
    }

    public HtmlMetadataExtracter()
    {
        super(MIMETYPES);
    }

    @Override
    protected Map<String, Serializable> extractRaw(ContentReader reader) throws Throwable
    {
        final Map<String, Serializable> rawProperties = newRawMap();
        
        HTMLEditorKit.ParserCallback callback = new HTMLEditorKit.ParserCallback()
        {
            StringBuffer title = null;
            boolean inHead = false;

            public void handleText(char[] data, int pos)
            {
                if (title != null)
                {
                    title.append(data);
                }
            }

            public void handleComment(char[] data, int pos)
            {
                // Perhaps sniff for Office 9+ metadata in here?
            }

            public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos)
            {
                if (HTML.Tag.HEAD.equals(t))
                {
                    inHead = true;
                }
                else if (HTML.Tag.TITLE.equals(t) && inHead)
                {
                    title = new StringBuffer();
                }
                else
                    handleSimpleTag(t, a, pos);
            }

            public void handleEndTag(HTML.Tag t, int pos)
            {
                if (HTML.Tag.HEAD.equals(t))
                {
                    inHead = false;
                }
                else if (HTML.Tag.TITLE.equals(t) && title != null)
                {
                    putRawValue(KEY_TITLE, title.toString(), rawProperties);
                    title = null;
                }
            }

            public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos)
            {
                if (HTML.Tag.META.equals(t))
                {
                    Object nameO = a.getAttribute(HTML.Attribute.NAME);
                    Object valueO = a.getAttribute(HTML.Attribute.CONTENT);
                    if (nameO == null || valueO == null)
                        return;

                    String name = nameO.toString();

                    if (name.equalsIgnoreCase("creator") || name.equalsIgnoreCase("author")
                            || name.equalsIgnoreCase("dc.creator"))
                    {
                        putRawValue(KEY_AUTHOR, valueO.toString(), rawProperties);
                    }
                    else if (name.equalsIgnoreCase("description") || name.equalsIgnoreCase("dc.description"))
                    {
                        putRawValue(KEY_DESCRIPTION, valueO.toString(), rawProperties);
                    }
                }
            }

            public void handleError(String errorMsg, int pos)
            {
            }
        };

        String charsetGuess = "UTF-8";
        int tries = 0;
        while (tries < 3)
        {
            rawProperties.clear();
            Reader r = null;
            InputStream cis = null;
            try
            {
                cis = reader.getContentInputStream();
                // TODO: for now, use default charset; we should attempt to map from html meta-data
                r = new InputStreamReader(cis);
                HTMLEditorKit.Parser parser = new ParserDelegator();
                parser.parse(r, callback, tries > 0);
                break;
            }
            catch (ChangedCharSetException ccse)
            {
                tries++;
                charsetGuess = ccse.getCharSetSpec();
                int begin = charsetGuess.indexOf("charset=");
                if (begin > 0)
                    charsetGuess = charsetGuess.substring(begin + 8, charsetGuess.length());
                reader = reader.getReader();
            }
            finally
            {
                if (r != null)
                    r.close();
                if (cis != null)
                    cis.close();
            }
        }
        // Done
        return rawProperties;
    }
}
