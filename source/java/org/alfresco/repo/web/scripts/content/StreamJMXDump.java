package org.alfresco.repo.web.scripts.content;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.management.MBeanServerConnection;

import org.alfresco.repo.management.JmxDumpUtil;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.util.FileCopyUtils;

/**
 * WebScript java backed bean implementation to stream the JMX property
 * dump as a .zip content file. 
 * 
 * @author Kevin Roast
 */
public class StreamJMXDump extends StreamContent
{
    /** Logger */
    private static Log logger = LogFactory.getLog(StreamJMXDump.class);
    
    /** The MBean server. */
    private MBeanServerConnection mbeanServer;
    
    
    /**
     * @param mBeanServer       MBeanServerConnection bean
     */
    public void setMBeanServer(MBeanServerConnection mBeanServer)
    {
        this.mbeanServer = mBeanServer;
    }
    
    /**
     * @see org.springframework.extensions.webscripts.WebScript#execute(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.WebScriptResponse)
     */
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException
    {
        PrintWriter tempFileOut = null;
        ZipOutputStream zout = null;
        try
        {
            // content type and caching
            res.setContentType("application/zip");
            Cache cache = new Cache();
            cache.setNeverCache(true);
            cache.setMustRevalidate(true);
            cache.setMaxAge(0L);
            res.setCache(cache);
            
            Date date = new Date();
            String attachFileName = "jmxdump_" + (date.getYear()+1900) + '_' + (date.getMonth()+1) + '_' + (date.getDate());
            String headerValue = "attachment; filename=\"" + attachFileName + ".zip\"";
            
            // set header based on filename - will force a Save As from the browse if it doesn't recognize it
            // this is better than the default response of the browser trying to display the contents
            res.setHeader("Content-Disposition", headerValue);
            
            // write JMX data to temp file
            File tempFile = TempFileProvider.createTempFile("jmxdump", ".txt");
            tempFileOut = new PrintWriter(tempFile);
            JmxDumpUtil.dumpConnection(mbeanServer, tempFileOut);
            tempFileOut.flush();
            tempFileOut.close();
            tempFileOut = null;
            
            // zip output
            zout = new ZipOutputStream(res.getOutputStream());
            ZipEntry zipEntry = new ZipEntry(attachFileName + ".txt");
            zout.putNextEntry(zipEntry);
            FileCopyUtils.copy(new FileInputStream(tempFile), zout);
            zout = null;
        } 
        catch (IOException ioe)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "Could not output JMX dump: " + ioe.getMessage(), ioe);
        }
        finally
        {
            if (tempFileOut != null) tempFileOut.close();
            try
            {
                if (zout != null) zout.close();
            }
            catch (IOException e1) {}
        }
    }
}