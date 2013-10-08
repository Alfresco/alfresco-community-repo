package org.alfresco.repo.web.scripts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.StringBuilderWriter;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Runtime;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.WrappingWebScriptResponse;

/**
 * Transactional Buffered Response
 */
public class BufferedResponse implements WrappingWebScriptResponse
{
    // Logger
    protected static final Log logger = LogFactory.getLog(BufferedResponse.class);

    private WebScriptResponse res;
    private int bufferSize;
    private ByteArrayOutputStream outputStream = null;
    private StringBuilderWriter outputWriter = null;
    

    /**
     * Construct
     * 
     * @param res
     * @param bufferSize
     */
    public BufferedResponse(WebScriptResponse res, int bufferSize)
    {
        this.res = res;
        this.bufferSize = bufferSize;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.web.scripts.WrappingWebScriptResponse#getNext()
     */
    public WebScriptResponse getNext()
    {
        return res;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptResponse#addHeader(java.lang.String, java.lang.String)
     */
    public void addHeader(String name, String value)
    {
        res.addHeader(name, value);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptResponse#encodeScriptUrl(java.lang.String)
     */
    public String encodeScriptUrl(String url)
    {
        return res.encodeScriptUrl(url);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptResponse#getEncodeScriptUrlFunction(java.lang.String)
     */
    public String getEncodeScriptUrlFunction(String name)
    {
        return res.getEncodeScriptUrlFunction(name);
    }

    /* (non-Javadoc)
     * @see org.springframework.extensions.webscripts.WebScriptResponse#encodeResourceUrl(java.lang.String)
     */
    public String encodeResourceUrl(String url)
    {
        return res.encodeResourceUrl(url);
    }

    /* (non-Javadoc)
     * @see org.springframework.extensions.webscripts.WebScriptResponse#getEncodeResourceUrlFunction(java.lang.String)
     */
    public String getEncodeResourceUrlFunction(String name)
    {
        return res.getEncodeResourceUrlFunction(name);
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptResponse#getOutputStream()
     */
    public OutputStream getOutputStream() throws IOException
    {
        if (outputStream == null)
        {
            if (outputWriter != null)
            {
                throw new AlfrescoRuntimeException("Already buffering output writer");
            }
            this.outputStream = new ByteArrayOutputStream(bufferSize);
        }
        return outputStream;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptResponse#getRuntime()
     */
    public Runtime getRuntime()
    {
        return res.getRuntime();
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptResponse#getWriter()
     */
    public Writer getWriter() throws IOException
    {
        if (outputWriter == null)
        {
            if (outputStream != null)
            {
                throw new AlfrescoRuntimeException("Already buffering output stream");
            }
            outputWriter = new StringBuilderWriter(bufferSize);
        }
        return outputWriter;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptResponse#reset()
     */
    public void reset()
    {
        if (outputStream != null)
        {
            outputStream.reset();
        }
        else if (outputWriter != null)
        {
            outputWriter = null;
        }
        res.reset();
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptResponse#setCache(org.alfresco.web.scripts.Cache)
     */
    public void setCache(Cache cache)
    {
        res.setCache(cache);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptResponse#setContentType(java.lang.String)
     */
    public void setContentType(String contentType)
    {
        res.setContentType(contentType);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptResponse#setContentEncoding(java.lang.String)
     */
    public void setContentEncoding(String contentEncoding)
    {
        res.setContentEncoding(contentEncoding);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptResponse#setHeader(java.lang.String, java.lang.String)
     */
    public void setHeader(String name, String value)
    {
        res.setHeader(name, value);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptResponse#setStatus(int)
     */
    public void setStatus(int status)
    {
        res.setStatus(status);
    }

    /**
     * Write buffered response to underlying response
     */
    public void writeResponse()
    {
        try
        {
            if (logger.isDebugEnabled() && outputStream != null)
            {
                logger.debug("Writing Transactional response: size=" + outputStream.size());
            }
            
            if (outputWriter != null)
            {
                outputWriter.flush();
                res.getWriter().write(outputWriter.toString());
            }
            else if (outputStream != null)
            {
                if (logger.isDebugEnabled())
                    logger.debug("Writing Transactional response: size=" + outputStream.size());
                
                outputStream.flush();
                outputStream.writeTo(res.getOutputStream());
            }
        }
        catch (IOException e)
        {
            throw new AlfrescoRuntimeException("Failed to commit buffered response", e);
        }
    }
}
