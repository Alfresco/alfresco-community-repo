package org.alfresco.repo.urlshortening;

import junit.framework.TestCase;

public class BitlyUrlShortenerTest extends TestCase
{
    private BitlyUrlShortenerImpl shortener = new BitlyUrlShortenerImpl();
    
    public void testShorten()
    {
        String url = "http://www.alfresco.com/";
        String shortUrl = shortener.shortenUrl(url);
        assertNotNull(shortUrl);
        assertFalse(shortUrl.isEmpty());
        assertFalse(url.equals(shortUrl));
        assertTrue(shortUrl.length()<=20);
    }
}
