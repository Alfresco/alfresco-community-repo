/*
 * Copyright (C) 2005 Jesper Steen Møller
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
package org.alfresco.repo.content.metadata;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.namespace.QName;

/**
 * @author Jesper Steen Møller
 */
public class HtmlMetadataExtracterTest extends AbstractMetadataExtracterTest
{
    private HtmlMetadataExtracter extracter;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        extracter = new HtmlMetadataExtracter();
        extracter.setDictionaryService(dictionaryService);
        extracter.register();
    }

    /**
     * @return Returns the same transformer regardless - it is allowed
     */
    protected MetadataExtracter getExtracter()
    {
        return extracter;
    }

    public void testSupports() throws Exception
    {
        for (String mimetype : HtmlMetadataExtracter.MIMETYPES)
        {
            boolean supports = extracter.isSupported(mimetype);
            assertTrue("Mimetype should be supported: " + mimetype, supports);
        }
    }

    public void testHtmlExtraction() throws Exception
    {
        testExtractFromMimetype(MimetypeMap.MIMETYPE_HTML);
    }

    /** Extractor only does the usual basic three properties */
    public void testFileSpecificMetadata(String mimetype, Map<QName, Serializable> properties) {}
}
