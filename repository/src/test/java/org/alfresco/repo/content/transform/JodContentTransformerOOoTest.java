/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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
package org.alfresco.repo.content.transform;

import static org.junit.Assert.assertNotNull;

import org.alfresco.repo.content.AbstractJodConverterBasedTest;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.thumbnail.ThumbnailDefinition;
import org.alfresco.repo.thumbnail.ThumbnailRegistry;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

/**
 * 
 * @author Neil McErlean
 * @since 3.2 SP1
 *
 * @deprecated The transformations code is being moved out of the codebase and replaced by the new async RenditionService2 or other external libraries.
 */
@Deprecated
public class JodContentTransformerOOoTest extends AbstractJodConverterBasedTest
{
    private static Log log = LogFactory.getLog(JodContentTransformerOOoTest.class);

    /**
     * This test method tests the built-in thumbnail transformations - all for a Word source document.
     * This will include transformations doc-pdf-png and doc-pdf-swf. ALF-2070
     */
    @Test
    public void thumbnailTransformationsUsingJodConverter()
    {
    	// If OpenOffice is not available then we will ignore this test (by passing it).
    	// This is because not all the build servers have OOo installed.
    	if (!isOpenOfficeAvailable())
    	{
    		System.out.println("Did not run " + this.getClass().getSimpleName() + ".thumbnailTransformationsUsingJodConverter" +
    				" because OOo is not available.");
    		return;
    	}
    	
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
            {
                public Void execute() throws Throwable
                {
                    ThumbnailRegistry thumbnailRegistry = thumbnailService.getThumbnailRegistry();
                    for (ThumbnailDefinition thumbDef : thumbnailRegistry.getThumbnailDefinitions())
                    {
                    	if (log.isDebugEnabled())
                    	{
                    		log.debug("Testing thumbnail definition " + thumbDef.getName());
                    	}
                    	
                        NodeRef thumbnail = thumbnailService.createThumbnail(contentNodeRef, ContentModel.PROP_CONTENT,
							        thumbDef.getMimetype(), thumbDef.getTransformationOptions(), thumbDef.getName());
                        
                        assertNotNull("Thumbnail was unexpectedly null.", thumbnail);
                    }
                    
                    return null;
                }
            });
    }
}
