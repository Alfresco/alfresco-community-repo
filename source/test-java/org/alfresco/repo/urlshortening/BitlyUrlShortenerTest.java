package org.alfresco.repo.urlshortening;

import junit.framework.TestCase;

public class BitlyUrlShortenerTest extends TestCase
{
    private BitlyUrlShortenerImpl shortener;
    
    public void testShorten()
    {
        String url = "http://www.alfresco.com/";
        String shortUrl = shortener.shortenUrl(url);
        assertNotNull(shortUrl);
        assertFalse(shortUrl.isEmpty());
        assertFalse(url.equals(shortUrl));
        assertTrue(shortUrl.length()<=20);
    }
    
    /**
    * {@inheritDoc}
    */
    @Override
    protected void setUp() throws Exception
    {
        this.shortener = new BitlyUrlShortenerImpl();;
        shortener.setApiKey("R_ca15c6c89e9b25ccd170bafd209a0d4f");
        shortener.setUrlLength(20);
        shortener.setUsername("brianalfresco");
    }
}
