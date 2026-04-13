/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.encoding;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

import com.glaforge.i18n.io.CharsetToolkit;

/**
 * Uses the <a href="http://glaforge.free.fr/wiki/index.php?wiki=GuessEncoding">Guess Encoding</a>
 * library.
 * 
 * @since 2.1
 * @author Derek Hulley
 */
public class GuessEncodingCharsetFinder extends AbstractCharactersetFinder
{
    /** Dummy charset to detect the default guess */
    private static final Charset DUMMY_CHARSET = new DummyCharset();

    @Override
    protected Charset detectCharsetImpl(byte[] buffer) throws Exception
    {
        CharsetToolkit charsetToolkit = new CharsetToolkit(buffer, DUMMY_CHARSET);
        charsetToolkit.setEnforce8Bit(true);            // Force the default instead of a guess
        Charset charset = charsetToolkit.guessEncoding();
        if (charset == DUMMY_CHARSET)
        {
            return null;
        }
        else
        {
            return charset;
        }
    }
    
    /**
     * A dummy charset to detect a default hit.
     */
    public static class DummyCharset extends Charset
    {
        DummyCharset()
        {
            super("dummy", new String[] {});
        }

        @Override
        public boolean contains(Charset cs)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public CharsetDecoder newDecoder()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public CharsetEncoder newEncoder()
        {
            throw new UnsupportedOperationException();
        }
        
    }
}
