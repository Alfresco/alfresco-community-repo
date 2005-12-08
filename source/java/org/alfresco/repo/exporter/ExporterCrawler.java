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
package org.alfresco.repo.exporter;

import org.alfresco.service.cmr.view.Exporter;
import org.alfresco.service.cmr.view.ExporterCrawlerParameters;


/**
 * Responsible for crawling Repository contents and invoking the exporter
 * with the contents found.
 *  
 * @author David Caruana
 *
 */
public interface ExporterCrawler
{
    /**
     * Crawl Repository and export items found
     *
     * @param parameters  crawler parameters
     * @param exporter  exporter to export via
     */
    public void export(ExporterCrawlerParameters parameters, Exporter exporter);
}
