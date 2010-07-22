package org.alfresco.repo.forms.processor.node;

import static org.alfresco.repo.forms.processor.node.FormFieldConstants.ASSOC;
import static org.alfresco.repo.forms.processor.node.FormFieldConstants.PROP;
import static org.alfresco.repo.forms.processor.node.FormFieldConstants.TRANSIENT_ENCODING;
import static org.alfresco.repo.forms.processor.node.FormFieldConstants.TRANSIENT_MIMETYPE;
import static org.alfresco.repo.forms.processor.node.FormFieldConstants.TRANSIENT_SIZE;

import org.alfresco.repo.forms.processor.FieldProcessor;
import org.alfresco.repo.forms.processor.workflow.PackageItemsFieldProcessor;
import org.alfresco.repo.forms.processor.workflow.TransitionFieldProcessor;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespaceService;

public class MockFieldProcessorRegistry extends ContentModelFieldProcessorRegistry
{
    public MockFieldProcessorRegistry(NamespaceService namespaceService, DictionaryService dictionaryService)
    {
        register(PROP, makePropertyFieldProcessor(namespaceService, dictionaryService));
        register(ASSOC, makeAssociationFieldProcessor(namespaceService, dictionaryService));
        register(TRANSIENT_ENCODING, makeEncodingFieldProcessor());
        register(TRANSIENT_MIMETYPE, makeMimetypeFieldProcessor());
        register(TRANSIENT_SIZE, makeSizeFieldProcessor());
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

    private TransientEncodingFieldProcessor makeEncodingFieldProcessor()
    {
        return new TransientEncodingFieldProcessor();
    }

    private TransientMimetypeFieldProcessor makeMimetypeFieldProcessor()
    {
        return new TransientMimetypeFieldProcessor();
    }

    private TransientSizeFieldProcessor makeSizeFieldProcessor()
    {
        return new TransientSizeFieldProcessor();
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
