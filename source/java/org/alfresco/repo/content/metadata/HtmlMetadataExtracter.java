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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.content.metadata;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.text.ChangedCharSetException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.namespace.QName;

/**
 * 
 * @author Jesper Steen Møller
 */
public class HtmlMetadataExtracter extends AbstractMetadataExtracter
{
    private static final Set<String> MIMETYPES = new HashSet<String>(5);
    static
    {
        MIMETYPES.add(MimetypeMap.MIMETYPE_HTML);
        MIMETYPES.add(MimetypeMap.MIMETYPE_XHTML);
    }

    public HtmlMetadataExtracter()
    {
        super(MIMETYPES, 1.0, 1000);
    }

    public void extractInternal(ContentReader reader, Map<QName, Serializable> destination) throws Throwable
    {
        final Map<QName, Serializable> tempDestination = new HashMap<QName, Serializable>();
        
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
                    trimPut(ContentModel.PROP_TITLE, title.toString(), tempDestination);
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
                        trimPut(ContentModel.PROP_AUTHOR, valueO, tempDestination);
                    }
                    if (name.equalsIgnoreCase("description") || name.equalsIgnoreCase("dc.description"))
                    {
                        trimPut(ContentModel.PROP_DESCRIPTION, valueO, tempDestination);
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
            tempDestination.clear();
            Reader r = null;
            InputStream cis = null;
            try
            {
                cis = reader.getContentInputStream();
                // TODO: for now, use default charset; we should attempt to map from html meta-data
                r = new InputStreamReader(cis);
                HTMLEditorKit.Parser parser = new ParserDelegator();
                parser.parse(r, callback, tries > 0);
                destination.putAll(tempDestination);
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
    }
}
