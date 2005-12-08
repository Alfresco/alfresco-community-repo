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
package org.alfresco.service.cmr.view;

import java.io.InputStream;
import java.io.Reader;


/**
 * Contract for a custom import package handler.
 * 
 * @author David Caruana
 */
public interface ImportPackageHandler
{
    /**
     * Start the Import
     */
    public void startImport();
    
    /**
     * Get the package data stream
     * 
     * @return  the reader
     */
    public Reader getDataStream();
    
    /**
     * Call-back for handling the import of content stream.
     * 
     * @param content content descriptor
     * @return the input stream onto the content
     */
    public InputStream importStream(String content);
    
    /**
     * End the Import
     */
    public void endImport();
    
}
