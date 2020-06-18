/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
/*
 * Copyright (C) 2005 - 2020 Jesper Steen Møller
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

import java.io.File;
import java.io.Serializable;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.AbstractContentTransformerTest;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;

/**
 * @deprecated extractor has been moved to a T-Engine.
 *
 * @author Jesper Steen Møller
 */
@Deprecated
public class HtmlMetadataExtracterTest extends AbstractMetadataExtracterTest
{
    private static final String QUICK_TITLE_JAPANESE = "確認した結果を添付しますので、確認してください";
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

    public void testHtmlExtractionJapanese() throws Exception
    {
        String mimetype = MimetypeMap.MIMETYPE_HTML;

        File japaneseHtml = AbstractContentTransformerTest.loadNamedQuickTestFile("quick.japanese.html");
        Map<QName, Serializable> properties = extractFromFile(japaneseHtml, mimetype);

        assertFalse("extractFromMimetype should return at least some properties, none found for " + mimetype,
                properties.isEmpty());

        // Title and description
        assertEquals(
                "Property " + ContentModel.PROP_TITLE + " not found for mimetype " + mimetype,
                QUICK_TITLE_JAPANESE,
                DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(ContentModel.PROP_TITLE)));
    }

    /** Extractor only does the usual basic three properties */
    public void testFileSpecificMetadata(String mimetype, Map<QName, Serializable> properties) {}
}
