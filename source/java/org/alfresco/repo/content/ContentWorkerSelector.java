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
package org.alfresco.repo.content;

import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;

/**
 * An interface instances that are able to identify content based on the
 * {@linkplain ContentReader content reader}.  This is specifically
 * aimed at extractors, transformers, injectors and similar classes.
 * <p>
 * The notion of supplying some type of worker looks a bit odd here, but
 * really an instance of this type will act as an optional factory.  Also,
 * in the context of the calling class, the context and the generics will
 * identify exactly which type is returned by the factory.
 * 
 * @since 2.1
 * @author Derek Hulley
 */
public interface ContentWorkerSelector<W extends ContentWorker>
{
    /**
     * Provides an worker appropriate to the given content, if possible.  The reader
     * should only be used if absolutely required.  The caller should always request
     * {@linkplain ContentReader#getReader() a new reader} or check the
     * {@linkplain ContentReader#isClosed() reader's state}.
     * 
     * @param reader        the content reader, providing the actual stream metadata
     *                      and even the stream, if required.
     * @return              Return a worker that can operate on the content, or <tt>null</tt>
     *                      if this identifier doesn't support the content.
     * @throws ContentIOException
     *                      if the search fails
     */
    W getWorker(ContentReader reader);
}
