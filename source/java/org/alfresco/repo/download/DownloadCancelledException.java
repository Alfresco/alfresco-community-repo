package org.alfresco.repo.download;

import org.alfresco.service.cmr.view.ExporterException;

/**
 * Exception thrown by ZipDownloadExporter, if a download is cancelled mid flow. 
 *
 * @author Alex Miller
 */
public class DownloadCancelledException extends ExporterException
{
    private static final long serialVersionUID = 4694929866014032096L;

    public DownloadCancelledException()
    {
        super("Download Cancelled");
    }
}
