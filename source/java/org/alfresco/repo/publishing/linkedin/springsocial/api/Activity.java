package org.alfresco.repo.publishing.linkedin.springsocial.api;

public interface Activity
{
    public String getContentType();
    
    public void setContentType(String value);
    
    public String getBody();
    
    public void setBody(String value);
    
    public String getLocale();
    
    public void setLocale(String value);
}
