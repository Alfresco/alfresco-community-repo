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

package org.alfresco.repo.forms.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.forms.Field;
import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.Item;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract base class for all FormProcessor implementations that wish to use
 * the filter mechanism.
 * 
 * @author Gavin Cornwell
 * @author Nick Smith
 */
public abstract class FilteredFormProcessor<ItemType, PersistType> extends AbstractFormProcessor
{
    private static final Log logger = LogFactory.getLog(FilteredFormProcessor.class);

    private List<String> ignoredFields = null;
    
    protected FilterRegistry<ItemType, PersistType> filterRegistry;

    protected FieldProcessorRegistry fieldProcessorRegistry;

    /**
     * Sets the filter registry
     * 
     * @param filterRegistry The FilterRegistry instance
     */
    public void setFilterRegistry(FilterRegistry<ItemType, PersistType> filterRegistry)
    {
        this.filterRegistry = filterRegistry;

        if (logger.isDebugEnabled())
            logger.debug("Set filter registry: " + this.filterRegistry + " for processor: " + this);
    }

    /**
     * @param ignoredFields the ignoredFields to set
     */
    public void setIgnoredFields(List<String> ignoredFields)
    {
        this.ignoredFields = ignoredFields;
    }

    /**
     * {@inheritDoc}
     */
    public Form generate(Item item, List<String> fields, List<String> forcedFields, Map<String, Object> context)
    {
        // get the typed object representing the item
        ItemType typedItem = getTypedItem(item);

        // create an empty Form
        Form form = new Form(item);

        // inform all regsitered filters the form is about to be generated
        if (this.filterRegistry != null)
        {
            for (Filter<ItemType, PersistType> filter : this.filterRegistry.getFilters())
            {
                filter.beforeGenerate(typedItem, fields, forcedFields, form, context);
            }
        }

        // perform the actual generation of the form
        internalGenerate(typedItem, fields, forcedFields, form, context);

        // inform all regsitered filters the form has been generated
        if (this.filterRegistry != null)
        {
            for (Filter<ItemType, PersistType> filter : this.filterRegistry.getFilters())
            {
                filter.afterGenerate(typedItem, fields, forcedFields, form, context);
            }
        }
        return form;
    }

    /**
     * Persists the given form data for the given item, completed by calling
     * each applicable registered handler
     * 
     * @see org.alfresco.repo.forms.processor.FormProcessor#persist(org.alfresco.repo.forms.Item,
     *      org.alfresco.repo.forms.FormData)
     * @param item The item to save the form for
     * @param data The object representing the form data
     * @return The object persisted
     */
    public Object persist(Item item, FormData data)
    {
        // get the typed object representing the item
        ItemType typedItem = getTypedItem(item);

        // inform all regsitered filters the form is about to be persisted
        if (this.filterRegistry != null)
        {
            for (Filter<ItemType, PersistType> filter : this.filterRegistry.getFilters())
            {
                filter.beforePersist(typedItem, data);
            }
        }

        // perform the actual persistence of the form
        PersistType persistedObject = internalPersist(typedItem, data);

        // inform all regsitered filters the form has been persisted
        if (this.filterRegistry != null)
        {
            for (Filter<ItemType, PersistType> filter : this.filterRegistry.getFilters())
            {
                filter.afterPersist(typedItem, data, persistedObject);
            }
        }

        return persistedObject;
    }

    /**
     * Generates the form.
     * 
     * @param item The object to generate a form for
     * @param fields Restricted list of fields to include
     * @param forcedFields List of fields to forcibly include
     * @param form The form object being generated
     * @param context Map representing optional context that can be used during
     *            retrieval of the form
     */
    protected void internalGenerate(ItemType item, List<String> fields, List<String> forcedFields, Form form, Map<String, Object> context)
    {
        Log log = getLogger();
        if (log.isDebugEnabled()) log.debug("Generating form for: " + item);
    
        // generate the form type and URI for the item.
        Item formItem = form.getItem();
        formItem.setType(getItemType(item));
        formItem.setUrl(getItemURI(item));
    
        Object itemData = makeItemData(item);
        FormCreationData data = new FormCreationDataImpl(itemData, forcedFields, context); 
        populateForm(form, fields, data);
        if (log.isDebugEnabled()) //
            log.debug("Generated form: " + form);
    }

    /**
     * This method generates all the fields to be added and adds them to the Form, together with the associated field data.
     * @param form The {@link Form} to which the fields are added. 
     * @param fields The names of all the fields to be added.
     * @param data {@link FormCreationData} used to generate all the fields.
     */
    protected void populateForm(Form form, List<String> fields, FormCreationData data)
    {
        List<Field> fieldsToAdd;
        if (fields != null && fields.size() > 0)
        {
            fieldsToAdd = generateSelectedFields(fields, data);
        }
        else
        {
            fieldsToAdd = generateDefaultFields(data, getIgnoredFields());
        }
        form.addFields(fieldsToAdd);
    }

    private List<String> getIgnoredFields()
    {
        if (ignoredFields != null)
        {
            return ignoredFields;
        }
        
        return getDefaultIgnoredFields();
    }

    /**
     * Generates a list of default fields to add if no field names are specified.
     * @param data Used for field creation.
     * @param fieldsToIgnore TODO
     * @return a {@link List} of {@link Field Fields} which may be empty.
     */
    protected List<Field> generateDefaultFields(FormCreationData data, List<String> fieldsToIgnore)
    {
        return Collections.emptyList();
    }

    protected List<Field> generateSelectedFields(List<String> fields, FormCreationData data)
    {
        List<Field> fieldData = new ArrayList<Field>(fields.size());
        for (String fieldName : fields)
        {
            Field field = fieldProcessorRegistry.buildField(fieldName, data);
            if (field == null)
            {
                if (getLogger().isDebugEnabled()) 
                {
                    String msg = "Ignoring unrecognised field \"" + fieldName + "\"";
                    getLogger().debug(msg);
                }
            }
            else
            {
                fieldData.add(field);
            }
        }
        return fieldData;
    }

    
    /**
     * Sets the field processor registry.
     * 
     * @param fieldProcessorRegistry
     *            The {@link FieldProcessorRegistry} to use.
     */
    public void setFieldProcessorRegistry(FieldProcessorRegistry fieldProcessorRegistry)
    {
        this.fieldProcessorRegistry = fieldProcessorRegistry;
    }

    /**
     * Creates a data object used by the {@link FormProcessor} and {@link FieldProcessor FieldProcessors} to create {@link Field Fields}
     * @return Object
     */
    protected abstract Object makeItemData(ItemType item);

    /**
     * Returns a typed Object representing the given item.
     * <p>
     * Subclasses that represent a form type will return a typed object that is
     * then passed to each of it's handlers, the handlers can therefore safely
     * cast the Object to the type they expect.
     * 
     * @param item The item to get a typed object for
     * @return The typed object
     */
    protected abstract ItemType getTypedItem(Item item);

    /**
     * Retrieves a logger instance to log to.
     * 
     * @return Log instance to log to.
     */
    protected abstract Log getLogger();

    /**
     * Returns a {@link String} describing the type fo the specified item.
     * @param item ItemType
     * @return String
     */
    protected abstract String getItemType(ItemType item);

    /**
     * Returns the URI location of the specified item.
     * @param item ItemType
     * @return String
     */
    protected abstract String getItemURI(ItemType item);
    
    /**
     * Persists the form data.
     * 
     * @param item The object to persist the form for
     * @param data The data to persist
     * @return The object that got created or modified
     */
    protected abstract PersistType internalPersist(ItemType item, FormData data);

    /**
     * When a {@link Form} is generated with no field names specifically set then a default {@link Form} is created.
     * The default {@link Form} contains all the properties and associations related to the {@link Item}, excluding a 
     * blacklist of ignored fields which defaults to the return value of this method.
     * The default ignored values can be overridden by setting the property <code>ignoredFields</code>.
     * 
     * @return the names of all the fields to be excluded from the default {@link Form} if no <code>defaultFields</code> property is explicitly set.
     */
    protected abstract List<String> getDefaultIgnoredFields();
}
