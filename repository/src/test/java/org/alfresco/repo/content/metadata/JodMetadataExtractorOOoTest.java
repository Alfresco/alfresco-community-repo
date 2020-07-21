/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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
package org.alfresco.repo.content.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.content.AbstractJodConverterBasedTest;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.metadata.OpenOfficeMetadataWorker;
import org.alfresco.repo.content.transform.AbstractContentTransformerTest;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @deprecated OOTB extractors are being moved to T-Engines.
 *
 * @author Neil McErlean
 * @since 3.2 SP1
 */
@Deprecated
public class JodMetadataExtractorOOoTest extends AbstractJodConverterBasedTest
{
    protected static final String QUICK_TITLE = "The quick brown fox jumps over the lazy dog";
    protected static final String QUICK_DESCRIPTION = "Gym class featuring a brown fox and lazy dog";
    protected static final String QUICK_CREATOR = "Nevin Nollop";
    protected static final String QUICK_CREATOR_EMAIL = "nevin.nollop@alfresco.com";
    protected static final String QUICK_PREVIOUS_AUTHOR = "Derek Hulley";
    
    @Test
    @Ignore("The test was never run and fails on remote transformer")
    public void metadataExtractionUsingJodConverter() throws Exception
    {
    	// If OpenOffice is not available then we will ignore this test (by passing it).
    	// This is because not all the build servers have OOo installed.
    	if (!isOpenOfficeAvailable())
    	{
    		System.out.println("Did not run " + this.getClass().getSimpleName() + "thumbnailTransformationsUsingJodConverter" +
			                   " because OOo is not available.");
    		return;
    	}

    	Map<QName, Serializable> properties = extractFromMimetype();
        assertFalse("extractFromMimetype should return at least some properties, none found", properties.isEmpty());
        String mimetype = MimetypeMap.MIMETYPE_WORD;

        // One of Creator or Author
        if (properties.containsKey(ContentModel.PROP_CREATOR))
        {
            assertEquals("Property " + ContentModel.PROP_CREATOR
                    + " not found for mimetype " + mimetype, QUICK_CREATOR,
                    DefaultTypeConverter.INSTANCE.convert(String.class,
                            properties.get(ContentModel.PROP_CREATOR)));
        } else if (properties.containsKey(ContentModel.PROP_AUTHOR))
        {
            assertEquals("Property " + ContentModel.PROP_AUTHOR
                    + " not found for mimetype " + mimetype, QUICK_CREATOR,
                    DefaultTypeConverter.INSTANCE.convert(String.class,
                            properties.get(ContentModel.PROP_AUTHOR)));
        } else
        {
            fail("Expected one Property out of " + ContentModel.PROP_CREATOR
                    + " and " + ContentModel.PROP_AUTHOR
                    + " but found neither of them.");
        }
        
        // Title and description
        assertEquals("Property " + ContentModel.PROP_TITLE
                + " not found for mimetype " + mimetype, QUICK_TITLE,
                DefaultTypeConverter.INSTANCE.convert(String.class, properties
                        .get(ContentModel.PROP_TITLE)));
        assertEquals("Property " + ContentModel.PROP_DESCRIPTION
                + " not found for mimetype " + mimetype, QUICK_DESCRIPTION,
                DefaultTypeConverter.INSTANCE.convert(String.class, properties
                        .get(ContentModel.PROP_DESCRIPTION)));
    }
    
    protected Map<QName, Serializable> extractFromMimetype() throws Exception
    {
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        
        // attempt to get a source file for each mimetype
        File sourceFile = AbstractContentTransformerTest.loadQuickTestFile("doc");
        if (sourceFile == null)
        {
            throw new FileNotFoundException("No quick.doc file found for test");
        }

        // construct a reader onto the source file
        ContentReader sourceReader = new FileContentReader(sourceFile);
        sourceReader.setMimetype(MimetypeMap.MIMETYPE_WORD);
        
        OpenOfficeMetadataWorker worker = (OpenOfficeMetadataWorker) ctx.getBean("extracter.worker.JodConverter");
        
        Set<String> supportedTypes = new HashSet<String>();
        supportedTypes.add(MimetypeMap.MIMETYPE_WORD);
        JodConverterMetadataExtracter extracter = new JodConverterMetadataExtracter(supportedTypes);
        extracter.setMimetypeService(serviceRegistry.getMimetypeService());
        extracter.setDictionaryService(serviceRegistry.getDictionaryService());
        extracter.setWorker(worker);

        extracter.init();

        extracter.extract(sourceReader, properties);
        return properties;
    }
}
