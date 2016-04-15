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
