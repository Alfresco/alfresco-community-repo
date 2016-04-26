package org.alfresco.repo.forms.processor;

import java.util.List;
import java.util.Map;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.Item;

/**
 * Interface definition of a form processor which is responsible
 * for generating a Form representation of a data source, for example a
 * repository node, a task or an XML schema and for persisting the
 * form data back to the data source.
 *
 * @author Gavin Cornwell
 */
@AlfrescoPublicApi
public interface FormProcessor
{
    /**
     * Determines whether this form processor is applicable for
     * the supplied item
     * 
     * @param item The item the form is being generated for
     * @return true if the processor is applicable
     */
    public boolean isApplicable(Item item);
    
    /**
     * Determines whether this form processor is active
     * 
     * @return true if the processor is active
     */
    public boolean isActive();
    
    /**
     * Returns a Form representation for an item
     * 
     * @param item The item to generate a Form object for
     * @param fields Restricted list of fields to include, null
     *               indicates all possible fields for the item 
     *               should be included
     * @param forcedFields List of field names from 'fields' list
     *                     that should be forcibly included, it is
     *                     up to the form processor implementation
     *                     to determine how to enforce this 
     * @param context Map representing optional context that
     *                can be used during retrieval of the form
     * @return The Form representation
     */
    public Form generate(Item item, List<String> fields, List<String> forcedFields, 
                Map<String, Object> context);
    
    /**
     * Persists the given object representing the form data
     * for an item
     *
     * @param item The item to generate a Form object for
     * @param data An object representing the data of the form
     * @return The object persisted
     */
    public Object persist(Item item, FormData data);
}
