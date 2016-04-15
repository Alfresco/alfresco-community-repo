package org.alfresco.repo.importer.view;

import org.alfresco.repo.importer.Importer;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.QName;


/**
 * Maintains state about the currently imported element.
 * 
 * @author David Caruana
 *
 */
public class ElementContext
{
    // Dictionary Service
    private DictionaryService dictionary;
    
    // Element Name
    private QName elementName;
    
    // Importer
    private Importer importer;
    
    
    /**
     * Construct
     * 
     * @param elementName QName
     * @param dictionary DictionaryService
     * @param importer Importer
     */
    public ElementContext(QName elementName, DictionaryService dictionary, Importer importer)
    {
        this.elementName = elementName;
        this.dictionary = dictionary;
        this.importer = importer;
    }
    
    /**
     * @return  the element name
     */
    public QName getElementName()
    {
        return elementName;
    }
    
    /**
     * @return  the dictionary service
     */
    public DictionaryService getDictionaryService()
    {
        return dictionary;
    }
    
    /**
     * @return  the importer
     */
    public Importer getImporter()
    {
        return importer;
    }
}
