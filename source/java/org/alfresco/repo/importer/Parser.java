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

import java.io.Reader;


/**
 * This interface represents the contract between the importer service and a 
 * parser (which is responsible for parsing the input stream and extracting
 * node descriptions).
 * 
 * The parser interacts with the passed importer to import nodes into the
 * Repository.
 *  
 * @author David Caruana
 */
public interface Parser
{
    /**
     * Parse nodes from specified input stream and import via the provided importer
     * 
     * @param viewReader
     * @param importer
     */
    public void parse(Reader viewReader, Importer importer);

}
