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
package org.alfresco.repo.importer;

import java.io.InputStream;

import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;


/**
 * Content Handler that interacts with an Alfresco Importer
 * 
 * @author David Caruana
 */
public interface ImportContentHandler extends ContentHandler, ErrorHandler
{
    /**
     * Sets the Importer
     * 
     * @param importer
     */
    public void setImporter(Importer importer);

    /**
     * Call-back for importing content streams
     * 
     * @param content  content stream identifier
     * @return  the input stream
     */
    public InputStream importStream(String content);
}
