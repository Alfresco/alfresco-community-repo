package org.alfresco.repo.webdav;


/**
 * WebDAVMethods that are able to post activity data must implement this interface
 * in order that the CloudWebDAVServlet will supply the object with an
 * ActivityPoster collaborator.
 * 
 * @author Matt Ward
 */
public interface ActivityPostProducer
{
    void setActivityPoster(WebDAVActivityPoster activityPoster);
}
