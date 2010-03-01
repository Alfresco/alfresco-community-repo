/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
