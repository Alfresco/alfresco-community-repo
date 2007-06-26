/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.repo.content.encoding;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

import org.alfresco.encoding.CharactersetFinder;
import org.alfresco.service.cmr.repository.MimetypeService;

/**
 * Utility bean to guess the charset given a stream and a mimetype.
 * 
 * @since 2.1
 * @author Derek Hulley
 */
public class ContentCharsetFinder
{
    private Charset defaultCharset = Charset.defaultCharset();
    private MimetypeService mimetypeService;
    private List<CharactersetFinder> charactersetFinders;

    /**
     * Override the system default charset.  Where the characterset cannot be determined for
     * a mimetype and input stream, this mimetype will be used.  The default is 'UTF-8'.
     * 
     * @param defaultCharset            the default characterset
     */
    public void setDefaultCharset(String defaultCharset)
    {
        this.defaultCharset = Charset.forName(defaultCharset);
    }
    
    /**
     * Set the mimetype service that will help determine if a particular mimetype can be
     * treated as encoded text or not.
     */
    public void setMimetypeService(MimetypeService mimetypeService)
    {
        this.mimetypeService = mimetypeService;
    }
    
    /**
     * Set the list of characterset finder to execute, in order, for text based content.
     * @param charactersetFinders       a list of finders
     */
    public void setCharactersetFinders(List<CharactersetFinder> charactersetFinders)
    {
        this.charactersetFinders = charactersetFinders;
    }
    
    /**
     * Gets the characterset from the stream, if the mimetype is text and the text
     * has enough information to give the encoding away.  Otherwise, the default
     * is returned.
     * 
     * @param is                a stream that will not be affected by the call, but must
     *                          support marking
     * @param mimetype          the mimetype of the stream data - <tt>null</tt> if not known
     * @return                  returns a characterset and never <tt>null</tt>
     */
    public Charset getCharset(InputStream is, String mimetype)
    {
        if (mimetype == null)
        {
            return defaultCharset;
        }
        // Is it text?
        if (!mimetypeService.isText(mimetype))
        {
            return defaultCharset;
        }
        // Try the finders
        Charset charset = null;
        for (CharactersetFinder finder : charactersetFinders)
        {
            charset = finder.detectCharset(is);
            if (charset != null)
            {
                break;
            }
        }
        // Done
        if (charset == null)
        {
            return defaultCharset;
        }
        else
        {
            return charset;
        }
    }
}
