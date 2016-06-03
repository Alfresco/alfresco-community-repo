package org.alfresco.repo.webdav;

import javax.servlet.http.HttpServletResponse;

/**
 * Abstract base class for the hierarchical methods COPY and MOVE
 * 
 * @author gavinc
 */
public abstract class HierarchicalMethod extends WebDAVMethod
{
    // Request parameters

    protected String m_strDestinationPath;
    protected boolean m_overwrite = true;

    /**
     * Default constructor
     */
    public HierarchicalMethod()
    {
    }

    /**
     * Return the destination path
     * 
     * @return String
     */
    public final String getDestinationPath()
    {
        return m_strDestinationPath;
    }

    /**
     * Return the overwrite setting
     * 
     * @return boolean
     */
    public final boolean hasOverWrite()
    {
        return m_overwrite;
    }

    /**
     * Parse the request headers
     * 
     * @exception WebDAVServerException
     */
    protected void parseRequestHeaders() throws WebDAVServerException
    {
        // Get the destination path for the copy

        String destURL = m_request.getHeader(WebDAV.HEADER_DESTINATION);

        if (logger.isDebugEnabled())
            logger.debug("Parsing Destination header: " + destURL);

        // Check that the URL is on this server and refers to the WebDAV
        // path, if not then return an error
        getDAVHelper().checkDestinationURL(m_request, destURL);

        
        m_strDestinationPath = getDAVHelper().getDestinationPath(getContextPath(), getServletPath(), destURL);
        
        // Failed to fix the destination path, return an error

        if (m_strDestinationPath == null)
        {
            logger.warn("Failed to parse the Destination header: " + destURL);
            throw new WebDAVServerException(HttpServletResponse.SC_BAD_REQUEST);
        }

        // Check if the copy should overwrite an existing file

        String strOverwrite = m_request.getHeader(WebDAV.HEADER_OVERWRITE);
        if (strOverwrite != null && strOverwrite.equals(WebDAV.F))
        {
            m_overwrite = false;
        }
    }

    /**
     * Parse the request body
     * 
     * @exception WebDAVServerException
     */
    protected void parseRequestBody() throws WebDAVServerException
    {
        // NOTE: Hierarchical methods do have a body to define what should
        // happen
        // to the properties when they are moved or copied, however, this
        // feature is not implemented by many servers, including ours!!
    }
}
