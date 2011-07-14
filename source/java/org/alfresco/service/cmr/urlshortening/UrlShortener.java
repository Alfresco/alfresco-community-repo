package org.alfresco.service.cmr.urlshortening;

public interface UrlShortener
{
    String shortenUrl(String longUrl);
    
    int getUrlLength();
}
