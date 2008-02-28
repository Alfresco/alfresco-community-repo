/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.content.transform;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;

import de.schlichtherle.io.FileOutputStream;

/**
 * @see org.alfresco.repo.content.transform.MediaWikiContentTransformer
 * 
 * @author Roy Wetherall
 */
public class MediaWikiContentTransformerTest extends AbstractContentTransformerTest
{
    private ContentTransformer transformer;
    
    private static final String WIKI_TEXT = 
        "== This is a title ==\n" +
        "\n" + 
        "'''This is bold'''  and some ''italics on the same line''\n" +
        "\n" + 
        "Here is a link to the main page .... [[Main Page]]\n" +
        "\n" + 
        "*and\n" + 
        "*what\n" +
        "*about\n" +
        "*a list\n" +
        "\n" + 
        "  Some indented text that should apear different\n" +
        "\n" + 
        "What about an external link [http://www.alfresco.com Alfresco]\n" +
        "\n" + 
        "<nowiki>This markup should be ignored [[Main Page]]</nowiki>\n" +
        "\n" + 
        "----\n" +
        "\n" + 
        "Lets put some text at the end :)\n";

    
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        transformer = new MediaWikiContentTransformer();
    }
    
    protected ContentTransformer getTransformer(String sourceMimetype, String targetMimetype)
    {
        return transformer;
    }

    public void testSetUp() throws Exception
    {
        assertNotNull(transformer);
    }
    
    public void checkReliability() throws Exception
    {
        // check reliability
        double reliability = transformer.getReliability(MimetypeMap.MIMETYPE_TEXT_PLAIN, MimetypeMap.MIMETYPE_HTML);
        assertEquals("Reliability incorrect", 1.0, reliability);   // plain text to html is 100%
        
        // check other way around
        reliability = transformer.getReliability(MimetypeMap.MIMETYPE_HTML, MimetypeMap.MIMETYPE_TEXT_PLAIN);
        assertEquals("Reliability incorrect", 0.0, reliability);   // html to plain text is 0%
    }
    
    public void testMediaWikiToHTML() throws Exception
    {
       File input = File.createTempFile("mediaWikiTest", ".txt");
       FileOutputStream fos = new FileOutputStream(input);
       fos.write(WIKI_TEXT.getBytes());
       fos.close();
       
       File output = File.createTempFile("mediaWikiTest", ".htm");
       
       ContentReader contentReader = new FileContentReader(input);
       contentReader.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
       contentReader.setEncoding("UTF-8");
       
       ContentWriter contentWriter = new FileContentWriter(output);
       contentWriter.setMimetype(MimetypeMap.MIMETYPE_HTML);
       contentWriter.setEncoding("UTF-8");
       
       transformer.transform(contentReader, contentWriter);
       
       String line = null;
       BufferedReader reader = new BufferedReader(new FileReader(output));
       while ((line = reader.readLine()) != null) 
       {
           System.out.println(line);
       }
    }
}
