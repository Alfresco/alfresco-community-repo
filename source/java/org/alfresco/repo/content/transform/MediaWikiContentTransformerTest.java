/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
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
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.util.TempFileProvider;

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
    
    public void testIsTransformable() throws Exception
    {
        // check reliability
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_TEXT_MEDIAWIKI, MimetypeMap.MIMETYPE_HTML, new TransformationOptions()));
        assertFalse(transformer.isTransformable(MimetypeMap.MIMETYPE_HTML, MimetypeMap.MIMETYPE_TEXT_MEDIAWIKI, new TransformationOptions()));
    }
    
    public void testMediaWikiToHTML() throws Exception
    {
       File input = TempFileProvider.createTempFile("mediaWikiTest", ".mw");
       FileOutputStream fos = new FileOutputStream(input);
       fos.write(WIKI_TEXT.getBytes());
       fos.close();
       
       File output = TempFileProvider.createTempFile("mediaWikiTest", ".htm");
       
       ContentReader contentReader = new FileContentReader(input);
       contentReader.setMimetype(MimetypeMap.MIMETYPE_TEXT_MEDIAWIKI);
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
