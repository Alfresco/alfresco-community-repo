/*
 * #%L
 * Alfresco Data model classes
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
package org.alfresco.service.cmr.repository;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.repo.content.encoding.ContentCharsetFinder;
import org.alfresco.service.NotAuditable;


/**
 * This service interface provides support for Mimetypes.
 * 
 * @author Derek Hulley
 */
@AlfrescoPublicApi
public interface MimetypeService
{
    /**
     * Get the extension for the specified mimetype  
     * 
     * @param mimetype a valid mimetype
     * @return Returns the default extension for the mimetype
     */
    @NotAuditable
    public String getExtension(String mimetype);

    /**
     * Get the mimetype for the specified extension
     * 
     * @param extension a valid file extension
     * @return Returns a valid mimetype if found, or null if does not exist
     */
    @NotAuditable
    public String getMimetype(String extension);

    /**
     * Get all human readable mimetype descriptions indexed by mimetype extension
     * 
     * @return the map of displays indexed by extension
     */
    @NotAuditable
    public Map<String, String> getDisplaysByExtension();

    /**
     * Get all human readable mimetype descriptions indexed by mimetype
     *
     * @return the map of displays indexed by mimetype
     */
    @NotAuditable
    public Map<String, String> getDisplaysByMimetype();

    /**
     * Get all mimetype extensions indexed by mimetype
     * 
     * @return the map of extension indexed by mimetype
     */
    @NotAuditable
    public Map<String, String> getExtensionsByMimetype();

    /**
     * Get all mimetypes indexed by extension
     * 
     * @return the map of mimetypes indexed by extension
     */
    @NotAuditable
    public Map<String, String> getMimetypesByExtension();
    
    /**
     * Check if a given mimetype represents a text format.
     * 
     * @param mimetype      the mimetype to check
     * @return              Returns <tt>true</tt> if it is text
     */
    @NotAuditable
    public boolean isText(String mimetype);

    /**
     * Get all mimetypes
     * 
     * @return all mimetypes
     */
    @NotAuditable
    public List<String> getMimetypes();

    /**
     * Provides a non-null best guess of the appropriate mimetype given a
     * filename, based entirely on the file extension. Because file
     * extensions can be wrong, you are strongly advised to use
     * {@link #guessMimetype(String, ContentReader)} instead.
     * 
     * @param filename the name of the file with an optional file extension
     * @return Returns the best guess mimetype or the mimetype for
     *      straight binary files if no extension could be found.
     */
    @NotAuditable
    public String guessMimetype(String filename);
    
    /**
     * Provides a non-null best guess of the appropriate mimetype for a
     * given file. Uses a mixture of file name and file content
     * logic to identify the mimetype for the file, and will usually
     * manage to identify files with incorrect extensions. 
     * 
     * @param filename the name of the file with an optional file extension
     * @param reader a reader for the content of the file 
     * @return Returns the best guess mimetype or the mimetype for
     *      straight binary files if no extension could be found.
     */
    @NotAuditable
    public String guessMimetype(String filename, ContentReader reader);
    
    /**
     * Provides a non-null best guess of the appropriate mimetype for a
     * given file. Uses a mixture of file name and file content
     * logic to identify the mimetype for the file, and will usually
     * manage to identify files with incorrect extensions. 
     * 
     * @param filename the name of the file with an optional file extension
     * @param input an input stream for the content 
     * @return Returns the best guess mimetype or the mimetype for
     *      straight binary files if no extension could be found.
     */
    @NotAuditable
    public String guessMimetype(String filename, InputStream input);

    /**
     * Use detection heuristics to check if the mime type of the document really 
     *  matches what it claims to be.
     * This is typically used when a transformation or metadata extractions fails, 
     *  and you want to know if someone has renamed a file and consequently it has 
     *  the wrong mime type. 
     * @return Null if the mime type seems ok, otherwise the mime type it probably is
     */
    @NotAuditable
    public String getMimetypeIfNotMatches(ContentReader reader);

    /**
     * Provides the system default charset finder.
     * 
     * @return      Returns a character set finder that can be used to decode
     *              streams in order to get the encoding.
     * 
     * @since 2.1
     */
    @NotAuditable
    public ContentCharsetFinder getContentCharsetFinder();
    
    /**
     * Returns a collection of mimetypes ordered by extension.
     * @param extension to restrict the collection to one entry
     */
    @NotAuditable
    public Collection<String> getMimetypes(String extension);
}
