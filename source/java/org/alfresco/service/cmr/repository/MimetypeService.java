/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.service.cmr.repository;

import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.NotAuditable;
import org.alfresco.service.PublicService;


/**
 * This service interface provides support for Mimetypes.
 * 
 * @author Derek Hulley
 *
 */
@PublicService
public interface MimetypeService
{
    /**
     * Get the extension for the specified mimetype  
     * 
     * @param mimetype a valid mimetype
     * @return Returns the default extension for the mimetype
     * @throws AlfrescoRuntimeException if the mimetype doesn't exist
     */
    @NotAuditable
    public String getExtension(String mimetype);

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
     * Get all mimetypes
     * 
     * @return all mimetypes
     */
    @NotAuditable
    public List<String> getMimetypes();

    /**
     * Provides a non-null best guess of the appropriate mimetype given a
     * filename.
     * 
     * @param filename the name of the file with an optional file extension
     * @return Returns the best guess mimetype or the mimetype for
     *      straight binary files if no extension could be found.
     */
    @NotAuditable
    public String guessMimetype(String filename);
}
