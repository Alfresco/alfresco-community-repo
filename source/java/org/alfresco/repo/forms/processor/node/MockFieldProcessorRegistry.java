/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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

package org.alfresco.repo.forms.processor.node;

import static org.alfresco.repo.forms.processor.node.FormFieldConstants.ASSOC;
import static org.alfresco.repo.forms.processor.node.FormFieldConstants.PROP;

import org.alfresco.repo.forms.processor.FieldProcessor;
import org.alfresco.repo.forms.processor.workflow.PackageItemsFieldProcessor;
import org.alfresco.repo.forms.processor.workflow.TransitionFieldProcessor;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespaceService;

/**
 * 
 * @since 3.4
 * @author Nick Smith
 *
 */
public class MockFieldProcessorRegistry extends ContentModelFieldProcessorRegistry
{
    public MockFieldProcessorRegistry(NamespaceService namespaceService, DictionaryService dictionaryService)
    {
        register(PROP, makePropertyFieldProcessor(namespaceService, dictionaryService));
        register(ASSOC, makeAssociationFieldProcessor(namespaceService, dictionaryService));
        register(EncodingFieldProcessor.KEY, makeEncodingFieldProcessor());
        register(MimetypeFieldProcessor.KEY, makeMimetypeFieldProcessor());
        register(SizeFieldProcessor.KEY, makeSizeFieldProcessor());
        register(TransitionFieldProcessor.KEY, makeTransitionFieldProcessor());
        register(PackageItemsFieldProcessor.KEY, makePackageItemFieldProcessor());
        setDefaultProcessor(makeDefaultFieldProcessor(namespaceService, dictionaryService));
    }

    /**
     * @return
     */
    private FieldProcessor makePackageItemFieldProcessor()
    {
        // TODO Auto-generated method stub
        return new PackageItemsFieldProcessor();
    }

    /**
     * @return
     */
    private FieldProcessor makeTransitionFieldProcessor()
    {
        return new TransitionFieldProcessor();
    }

    private FieldProcessor makeDefaultFieldProcessor(NamespaceService namespaceService,
            DictionaryService dictionaryService)
    {
        DefaultFieldProcessor processor = new DefaultFieldProcessor();
        processor.setDictionaryService(dictionaryService);
        processor.setNamespaceService(namespaceService);
        try 
        {
            processor.afterPropertiesSet();
        }
        catch (Exception e) 
        {
            throw new RuntimeException(e);
        }
        return processor;
    }

    private EncodingFieldProcessor makeEncodingFieldProcessor()
    {
        return new EncodingFieldProcessor();
    }

    private MimetypeFieldProcessor makeMimetypeFieldProcessor()
    {
        return new MimetypeFieldProcessor();
    }

    private SizeFieldProcessor makeSizeFieldProcessor()
    {
        return new SizeFieldProcessor();
    }

    private PropertyFieldProcessor makePropertyFieldProcessor(NamespaceService namespaceService,
            DictionaryService dictionaryService)
    {
        PropertyFieldProcessor processor = new PropertyFieldProcessor();
        processor.setDictionaryService(dictionaryService);
        processor.setNamespaceService(namespaceService);
        return processor;
    }

    private AssociationFieldProcessor makeAssociationFieldProcessor(NamespaceService namespaceService,
            DictionaryService dictionaryService)
    {
        AssociationFieldProcessor processor = new AssociationFieldProcessor();
        processor.setDictionaryService(dictionaryService);
        processor.setNamespaceService(namespaceService);
        return processor;
    }

}
