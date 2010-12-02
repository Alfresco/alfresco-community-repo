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

package org.alfresco.repo.forms.processor.node;

import static org.alfresco.repo.forms.processor.node.FormFieldConstants.ASSOC;
import static org.alfresco.repo.forms.processor.node.FormFieldConstants.PROP;

import org.alfresco.repo.forms.processor.FieldProcessor;
import org.alfresco.repo.forms.processor.workflow.MessageFieldProcessor;
import org.alfresco.repo.forms.processor.workflow.PackageItemsFieldProcessor;
import org.alfresco.repo.forms.processor.workflow.TaskOwnerFieldProcessor;
import org.alfresco.repo.forms.processor.workflow.TransitionFieldProcessor;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespaceService;

/**
 * Mock implementation of a FieldProcessorRegistry.
 * 
 * @since 3.4
 * @author Nick Smith
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
        register(MessageFieldProcessor.KEY, makeMessageFieldProcessor());
        register(TaskOwnerFieldProcessor.KEY, makeTaskOwnerFieldProcessor());
        setDefaultProcessor(makeDefaultFieldProcessor(namespaceService, dictionaryService));
    }

    private FieldProcessor makePackageItemFieldProcessor()
    {
        return new PackageItemsFieldProcessor();
    }

    private FieldProcessor makeTransitionFieldProcessor()
    {
        return new TransitionFieldProcessor();
    }
    
    private FieldProcessor makeMessageFieldProcessor()
    {
        return new MessageFieldProcessor();
    }
    
    private FieldProcessor makeTaskOwnerFieldProcessor()
    {
        return new TaskOwnerFieldProcessor();
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
