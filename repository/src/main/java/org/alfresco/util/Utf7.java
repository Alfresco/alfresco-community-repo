/*
 * #%L
 * Alfresco Repository
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
