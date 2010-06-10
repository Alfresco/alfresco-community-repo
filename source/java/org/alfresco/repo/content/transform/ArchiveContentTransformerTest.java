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

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.TransformationOptions;

/**
 * Test class for ArchiveContentTransformer.
 * 
 * @see org.alfresco.repo.content.transform.ArchiveContentTransformer
 * 
 * @author Neil McErlean
 */
public class ArchiveContentTransformerTest extends AbstractContentTransformerTest
{
    private ContentTransformer transformer;
    
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        
        transformer = new ArchiveContentTransformer();
    }

    protected ContentTransformer getTransformer(String sourceMimetype, String targetMimetype)
    {
        return transformer;
    }
    
    public void testIsTransformable() throws Exception
    {
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_ZIP, MimetypeMap.MIMETYPE_TEXT_PLAIN, new TransformationOptions()));
    }

    @Override
	protected boolean isQuickPhraseExpected(String targetMimetype)
	{
    	// The Zip transformer produces names of the entries, not their contents.
		return false;
	}

    @Override
	protected boolean isQuickWordsExpected(String targetMimetype)
    {
    	// The Zip transformer produces names of the entries, not their contents.
    	return false;
    }
}
