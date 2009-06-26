/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
package org.alfresco.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import com.beetstra.jutf7.CharsetProvider;

/**
 * 
 * @author Mike Shavnev
 *
 */
public class Utf7 
{
    public static final String UTF7   = "UTF-7";
    public static final String UTF7_OPTIONAL = "X-UTF-7-OPTIONAL";
    public static final String UTF7_MODIFIED = "X-MODIFIED-UTF-7";
    
    
    
    /**
     * Convert string from UTF-7 characters
     * 
     * @param string Input string for decoding
     * @return Decoded string
     */
    public static String decode(String string, String charsetName)
    {
        if (string.length() <= 1)
        {
            return string;
        }
        CharsetProvider provider = new CharsetProvider();
        Charset charset = provider.charsetForName(charsetName);
        CharBuffer charBuffer = charset.decode(ByteBuffer.wrap(string.getBytes()));
        return charBuffer.toString();
    }

    /**
     * Convert string to UTF-7 characters
     * 
     * @param string Input string for decoding
     * @return Encoded string
     */
    public static String encode(String string, String charsetName)
    {
        if (string.length() <= 1)
        {
            return string;
        }
        CharsetProvider provider = new CharsetProvider();
        Charset charset = provider.charsetForName(charsetName);
        ByteBuffer byteBuffer = charset.encode(string);
        return new String(byteBuffer.array()).substring(0, byteBuffer.limit());
    }
}
