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
package org.alfresco.repo.content.selector;

import org.alfresco.repo.content.ContentWorker;
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
