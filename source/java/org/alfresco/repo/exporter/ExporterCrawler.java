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
