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

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Interface for classes that are able to read a text-based input stream and determine
 * the character encoding.
 * <p>
 * There are quite a few libraries that do this, but none are perfect.  It is therefore
 * necessary to abstract the implementation to allow these finders to be configured in
 * as required.
 * <p>
 * Implementations should have a default constructor and be completely thread safe and
 * stateless.  This will allow them to be constructed and held indefinitely to do the
 * decoding work.
 * <p>
 * Where the encoding cannot be determined, it is left to the client to decide what to do.
 * Some implementations may guess and encoding or use a default guess - it is up to the
 * implementation to specify the behaviour.
 * 
 * @since 2.1
 * @author Derek Hulley
 */
public interface CharactersetFinder
{
    /**
     * Attempt to detect the character set encoding for the give input stream.  The input
     * stream will not be altered or closed by this method, and must therefore support
     * marking.  If the input stream available doesn't support marking, then it can be wrapped with
     * a {@link BufferedInputStream}.
     * <p>
     * The current state of the stream will be restored before the method returns.
     * 
     * @param is                an input stream that must support marking
     * @return                  Returns the encoding of the stream,
     *                          or <tt>null</tt> if encoding cannot be identified
     */
    public Charset detectCharset(InputStream is);
    
    /**
     * Attempt to detect the character set encoding for the given buffer.
     * 
     * @param buffer            the first <i>n</i> bytes of the character stream
     * @return                  Returns the encoding of the buffer,
     *                          or <tt>null</tt> if encoding cannot be identified
     */
    public Charset detectCharset(byte[] buffer);
}
