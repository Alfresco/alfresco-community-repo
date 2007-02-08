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

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.namespace.QName;

/**
 * 
 * @author Jesper Steen Møller
 */
public interface MetadataExtracter
{
    /**
     * Provides the approximate accuracy with which this extracter can extract
     * metadata for the mimetype.
     * <p>
     * 
     * @param sourceMimetype the source mimetype
     * @return Returns a score 0.0 to 1.0. 0.0 indicates that the extraction
     *         cannot be performed at all. 1.0 indicates that the extraction can
     *         be performed perfectly.
     */
    public double getReliability(String sourceMimetype);

    /**
     * Provides an estimate, usually a worst case guess, of how long an
     * extraction will take.
     * <p>
     * This method is used to determine, up front, which of a set of equally
     * reliant transformers will be used for a specific extraction.
     * 
     * @return Returns the approximate number of milliseconds per transformation
     */
    public long getExtractionTime();

    /**
     * Extracts the metadata from the content provided by the reader and source
     * mimetype to the supplied map.
     * <p>
     * The extraction viability can be determined by an up front call to
     * {@link #getReliability(String)}.
     * <p>
     * The source mimetype <b>must</b> be available on the
     * {@link org.alfresco.service.cmr.repository.ContentAccessor#getMimetype()} method
     * of the reader.
     * 
     * @param reader the source of the content
     * @param destination the destination of the extraction
     * @throws ContentIOException if an IO exception occurs
     */
    public void extract(ContentReader reader, Map<QName, Serializable> destination) throws ContentIOException;

}
