package org.alfresco.rest.framework.resource.content;


/**
 * Basic information about content.  Typically taken from a HTTPServletRequest.
 * You may choose to trust it but there is no guarantee that it accurately describes the content.
 */
public interface BasicContentInfo {
    public String getMimeType();
    public String getEncoding();
    
}
