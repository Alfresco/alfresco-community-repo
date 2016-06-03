package org.alfresco.repo.webdav;

/**
 * Implements the WebDAV HEAD method
 * 
 * @author gavinc
 */
public class HeadMethod extends GetMethod
{
    /**
     * Default constructor
     */
    public HeadMethod()
    {
        // Do everything the GET request does apart from returning the content

        m_returnContent = false;
    }
}
