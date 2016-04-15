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
package org.alfresco.repo.content.metadata;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.api.AlfrescoPublicApi;    
import org.alfresco.repo.content.ContentWorker;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.namespace.QName;

/**
 * Interface for writing metadata properties back into the content file.
 *
 * @author Ray Gauss II
 *
 */
@AlfrescoPublicApi
public interface MetadataEmbedder extends ContentWorker {

    /**
     * Determines if the extracter works against the given mimetype.
     *
     * @param mimetype      the document mimetype
     * @return Returns      <tt>true</tt> if the mimetype is supported, otherwise <tt>false</tt>.
     */
    public boolean isEmbeddingSupported(String mimetype);
    
    /**
     * Embeds the given properties into the file specified by the given content writer.
     *      * <p>
     * The embedding viability can be determined by an up front call to
     * {@link #isEmbeddingSupported(String)}.
     * <p>
     * The source mimetype <b>must</b> be available on the
     * {@link org.alfresco.service.cmr.repository.ContentAccessor#getMimetype()} method
     * of the writer.
     * 
     * @param properties the model properties to embed
     * @param reader the reader for the original source content file
     * @param writer the writer for the content after metadata has been embedded
     * @throws ContentIOException
     */
    public void embed(Map<QName, Serializable> properties, ContentReader reader, ContentWriter writer) throws ContentIOException;


}
