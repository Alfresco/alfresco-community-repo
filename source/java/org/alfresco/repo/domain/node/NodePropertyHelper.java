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
package org.alfresco.repo.domain.node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.domain.contentdata.ContentDataDAO;
import org.alfresco.repo.domain.locale.LocaleDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.datatype.TypeConversionException;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class provides services for translating exploded properties
 * (as persisted in <b>alf_node_properties</b>) in the public form, which is a
 * <tt>Map</tt> of values keyed by their <tt>QName</tt>.
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public class NodePropertyHelper
{
    private static final Log logger = LogFactory.getLog(NodePropertyHelper.class);
    
    private final DictionaryService dictionaryService;
    private final QNameDAO qnameDAO;
    private final LocaleDAO localeDAO;
    private final ContentDataDAO contentDataDAO;

    /**
     * Construct the helper with the appropriate DAOs and services
     */
    public NodePropertyHelper(
            DictionaryService dictionaryService,
            QNameDAO qnameDAO,
            LocaleDAO localeDAO,
            ContentDataDAO contentDataDAO)
    {
        this.dictionaryService = dictionaryService;
        this.qnameDAO = qnameDAO;
        this.localeDAO = localeDAO;
        this.contentDataDAO = contentDataDAO;
    }

    public Map<NodePropertyKey, NodePropertyValue> convertToPersistentProperties(Map<QName, Serializable> in)
    {
        // Get the locale ID (the default will be overridden where necessary)
        Long propertylocaleId = localeDAO.getOrCreateDefaultLocalePair().getFirst();

        Map<NodePropertyKey, NodePropertyValue> propertyMap = new HashMap<NodePropertyKey, NodePropertyValue>(
                in.size() + 5);
        for (Map.Entry<QName, Serializable> entry : in.entrySet())
        {
            Serializable value = entry.getValue();
            // Get the qname ID
            QName propertyQName = entry.getKey();
            Long propertyQNameId = qnameDAO.getOrCreateQName(propertyQName).getFirst();
            // Get the property definition, if available
            PropertyDefinition propertyDef = dictionaryService.getProperty(propertyQName);
            // Add it to the map
            addValueToPersistedProperties(
                    propertyMap,
                    propertyDef,
                    NodePropertyHelper.IDX_NO_COLLECTION,
                    propertyQNameId,
                    propertylocaleId,
                    value);
        }
        // Done
        return propertyMap;
    }

    /**
     * The collection index used to indicate that the value is not part of a collection. All values from zero up are
     * used for real collection indexes.
     */
    private static final int IDX_NO_COLLECTION = -1;

    /**
     * A method that adds properties to the given map. It copes with collections.
     * 
     * @param propertyDef the property definition (<tt>null</tt> is allowed)
     * @param collectionIndex the index of the property in the collection or <tt>-1</tt> if we are not yet processing a
     *            collection
     */
    private void addValueToPersistedProperties(
            Map<NodePropertyKey, NodePropertyValue> propertyMap,
            PropertyDefinition propertyDef,
            int collectionIndex,
            Long propertyQNameId,
            Long propertyLocaleId,
            Serializable value)
    {
        if (value == null)
        {
            // The property is null. Null is null and cannot be massaged any other way.
            NodePropertyValue npValue = makeNodePropertyValue(propertyDef, null);
            NodePropertyKey npKey = new NodePropertyKey();
            npKey.setListIndex(collectionIndex);
            npKey.setQnameId(propertyQNameId);
            npKey.setLocaleId(propertyLocaleId);
            // Add it to the map
            propertyMap.put(npKey, npValue);
            // Done
            return;
        }

        // Get or spoof the property datatype
        QName propertyTypeQName;
        if (propertyDef == null) // property not recognised
        {
            // allow it for now - persisting excess properties can be useful sometimes
            propertyTypeQName = DataTypeDefinition.ANY;
        }
        else
        {
            propertyTypeQName = propertyDef.getDataType().getName();
        }

        // A property may appear to be multi-valued if the model definition is loose and
        // an unexploded collection is passed in. Otherwise, use the model-defined behaviour
        // strictly.
        boolean isMultiValued;
        if (propertyTypeQName.equals(DataTypeDefinition.ANY))
        {
            // It is multi-valued if required (we are not in a collection and the property is a new collection)
            isMultiValued = (value != null) && (value instanceof Collection<?>)
                    && (collectionIndex == IDX_NO_COLLECTION);
        }
        else
        {
            isMultiValued = propertyDef.isMultiValued();
        }

        // Handle different scenarios.
        // - Do we need to explode a collection?
        // - Does the property allow collections?
        if (collectionIndex == IDX_NO_COLLECTION && isMultiValued && !(value instanceof Collection<?>))
        {
            // We are not (yet) processing a collection but the property should be part of a collection
            addValueToPersistedProperties(
                    propertyMap,
                    propertyDef,
                    0,
                    propertyQNameId,
                    propertyLocaleId,
                    value);
        }
        else if (collectionIndex == IDX_NO_COLLECTION && value instanceof Collection<?>)
        {
            // We are not (yet) processing a collection and the property is a collection i.e. needs exploding
            // Check that multi-valued properties are supported if the property is a collection
            if (!isMultiValued)
            {
                throw new DictionaryException("A single-valued property of this type may not be a collection: \n" +
                        "   Property: " + propertyDef + "\n" +
                        "   Type: " + propertyTypeQName + "\n" +
                        "   Value: " + value);
            }
            // We have an allowable collection.
            @SuppressWarnings("unchecked")
            Collection<Object> collectionValues = (Collection<Object>) value;
            // Persist empty collections directly. This is handled by the NodePropertyValue.
            if (collectionValues.size() == 0)
            {
                NodePropertyValue npValue = makeNodePropertyValue(null,
                        (Serializable) collectionValues);
                NodePropertyKey npKey = new NodePropertyKey();
                npKey.setListIndex(NodePropertyHelper.IDX_NO_COLLECTION);
                npKey.setQnameId(propertyQNameId);
                npKey.setLocaleId(propertyLocaleId);
                // Add it to the map
                propertyMap.put(npKey, npValue);
            }
            // Break it up and recurse to persist the values.
            collectionIndex = -1;
            for (Object collectionValueObj : collectionValues)
            {
                collectionIndex++;
                if (collectionValueObj != null && !(collectionValueObj instanceof Serializable))
                {
                    throw new IllegalArgumentException("Node properties must be fully serializable, "
                            + "including values contained in collections. \n" + "   Property: " + propertyDef + "\n"
                            + "   Index:    " + collectionIndex + "\n" + "   Value:    " + collectionValueObj);
                }
                Serializable collectionValue = (Serializable) collectionValueObj;
                try
                {
                    addValueToPersistedProperties(
                            propertyMap,
                            propertyDef,
                            collectionIndex,
                            propertyQNameId,
                            propertyLocaleId,
                            collectionValue);
                }
                catch (Throwable e)
                {
                    throw new AlfrescoRuntimeException("Failed to persist collection entry: \n" + "   Property: "
                            + propertyDef + "\n" + "   Index:    " + collectionIndex + "\n" + "   Value:    "
                            + collectionValue, e);
                }
            }
        }
        else
        {
            // We are either processing collection elements OR the property is not a collection
            // Collections of collections are only supported by type d:any
            if (value instanceof Collection<?> && !propertyTypeQName.equals(DataTypeDefinition.ANY))
            {
                throw new DictionaryException(
                        "Collections of collections (Serializable) are only supported by type 'd:any': \n"
                                + "   Property: " + propertyDef + "\n" + "   Type: " + propertyTypeQName + "\n"
                                + "   Value: " + value);
            }
            // Handle MLText
            if (value instanceof MLText)
            {
                // This needs to be split up into individual strings
                MLText mlTextValue = (MLText) value;
                for (Map.Entry<Locale, String> mlTextEntry : mlTextValue.entrySet())
                {
                    Locale mlTextLocale = mlTextEntry.getKey();
                    String mlTextStr = mlTextEntry.getValue();
                    // Get the Locale ID for the text
                    Long mlTextLocaleId = localeDAO.getOrCreateLocalePair(mlTextLocale).getFirst();
                    // This is persisted against the current locale, but as a d:text instance
                    NodePropertyValue npValue = new NodePropertyValue(DataTypeDefinition.TEXT, mlTextStr);
                    NodePropertyKey npKey = new NodePropertyKey();
                    npKey.setListIndex(collectionIndex);
                    npKey.setQnameId(propertyQNameId);
                    npKey.setLocaleId(mlTextLocaleId);
                    // Add it to the map
                    propertyMap.put(npKey, npValue);
                }
            }
            else
            {
                NodePropertyValue npValue = makeNodePropertyValue(propertyDef, value);
                NodePropertyKey npKey = new NodePropertyKey();
                npKey.setListIndex(collectionIndex);
                npKey.setQnameId(propertyQNameId);
                npKey.setLocaleId(propertyLocaleId);
                // Add it to the map
                propertyMap.put(npKey, npValue);
            }
        }
    }

    /**
     * Helper method to convert the <code>Serializable</code> value into a full, persistable {@link NodePropertyValue}.
     * <p>
     * Where the property definition is null, the value will take on the {@link DataTypeDefinition#ANY generic ANY}
     * value.
     * <p>
     * Collections are NOT supported. These must be split up by the calling code before calling this method. Map
     * instances are supported as plain serializable instances.
     * 
     * @param propertyDef the property dictionary definition, may be null
     * @param value the value, which will be converted according to the definition - may be null
     * @return Returns the persistable property value
     */
    public NodePropertyValue makeNodePropertyValue(PropertyDefinition propertyDef, Serializable value)
    {
        // get property attributes
        final QName propertyTypeQName;
        if (propertyDef == null) // property not recognised
        {
            // allow it for now - persisting excess properties can be useful sometimes
            propertyTypeQName = DataTypeDefinition.ANY;
        }
        else
        {
            propertyTypeQName = propertyDef.getDataType().getName();
        }
        try
        {
            NodePropertyValue propertyValue = new NodePropertyValue(propertyTypeQName, value);
            // done
            return propertyValue;
        }
        catch (TypeConversionException e)
        {
            throw new TypeConversionException(
                    "The property value is not compatible with the type defined for the property: \n" +
                    "   property: " + (propertyDef == null ? "unknown" : propertyDef) + "\n" +
                    "   value: " + value + "\n" +
                    "   value type: " + value.getClass(),
                    e);
        }
    }

    public Serializable getPublicProperty(
            Map<NodePropertyKey, NodePropertyValue> propertyValues,
            QName propertyQName)
    {
        // Get the qname ID
        Pair<Long, QName> qnamePair = qnameDAO.getQName(propertyQName);
        if (qnamePair == null)
        {
            // There is no persisted property with that QName, so we can't match anything
            return null;
        }
        Long qnameId = qnamePair.getFirst();
        // Now loop over the properties and extract those with the given qname ID
        SortedMap<NodePropertyKey, NodePropertyValue> scratch = new TreeMap<NodePropertyKey, NodePropertyValue>();
        for (Map.Entry<NodePropertyKey, NodePropertyValue> entry : propertyValues.entrySet())
        {
            NodePropertyKey propertyKey = entry.getKey();
            if (propertyKey.getQnameId().equals(qnameId))
            {
                scratch.put(propertyKey, entry.getValue());
            }
        }
        // If we found anything, then collapse the properties to a Serializable
        if (scratch.size() > 0)
        {
            PropertyDefinition propertyDef = dictionaryService.getProperty(propertyQName);
            Serializable collapsedValue = collapsePropertiesWithSameQName(propertyDef, scratch);
            return collapsedValue;
        }
        else
        {
            return null;
        }
    }

    public Map<QName, Serializable> convertToPublicProperties(Map<NodePropertyKey, NodePropertyValue> propertyValues)
    {
        Map<QName, Serializable> propertyMap = new HashMap<QName, Serializable>(propertyValues.size(), 1.0F);
        // Shortcut
        if (propertyValues.size() == 0)
        {
            return propertyMap;
        }
        // We need to process the properties in order
        SortedMap<NodePropertyKey, NodePropertyValue> sortedPropertyValues = new TreeMap<NodePropertyKey, NodePropertyValue>(
                propertyValues);
        // A working map. Ordering is important.
        SortedMap<NodePropertyKey, NodePropertyValue> scratch = new TreeMap<NodePropertyKey, NodePropertyValue>();
        // Iterate (sorted) over the map entries and extract values with the same qname
        Long currentQNameId = Long.MIN_VALUE;
        Iterator<Map.Entry<NodePropertyKey, NodePropertyValue>> iterator = sortedPropertyValues.entrySet().iterator();
        while (true)
        {
            Long nextQNameId = null;
            NodePropertyKey nextPropertyKey = null;
            NodePropertyValue nextPropertyValue = null;
            // Record the next entry's values
            if (iterator.hasNext())
            {
                Map.Entry<NodePropertyKey, NodePropertyValue> entry = iterator.next();
                nextPropertyKey = entry.getKey();
                nextPropertyValue = entry.getValue();
                nextQNameId = nextPropertyKey.getQnameId();
            }
            // If the QName is going to change, and we have some entries to process, then process them.
            if (scratch.size() > 0 && (nextQNameId == null || !nextQNameId.equals(currentQNameId)))
            {
                QName currentQName = qnameDAO.getQName(currentQNameId).getSecond();
                PropertyDefinition currentPropertyDef = dictionaryService.getProperty(currentQName);
                // We have added something to the scratch properties but the qname has just changed
                Serializable collapsedValue = null;
                // We can shortcut if there is only one value
                if (scratch.size() == 1)
                {
                    // There is no need to collapse list indexes
                    collapsedValue = collapsePropertiesWithSameQNameAndListIndex(currentPropertyDef, scratch);
                }
                else
                {
                    // There is more than one value so the list indexes need to be collapsed
                    collapsedValue = collapsePropertiesWithSameQName(currentPropertyDef, scratch);
                }
                boolean forceCollection = false;
                // If the property is multi-valued then the output property must be a collection
                if (currentPropertyDef != null && currentPropertyDef.isMultiValued())
                {
                    forceCollection = true;
                }
                else if (scratch.size() == 1 && scratch.firstKey().getListIndex().intValue() > -1)
                {
                    // This is to handle cases of collections where the property is d:any but not
                    // declared as multiple.
                    forceCollection = true;
                }
                if (forceCollection && collapsedValue != null && !(collapsedValue instanceof Collection<?>))
                {
                    // Can't use Collections.singletonList: ETHREEOH-1172
                    ArrayList<Serializable> collection = new ArrayList<Serializable>(1);
                    collection.add(collapsedValue);
                    collapsedValue = collection;
                }
                // Store the value
                propertyMap.put(currentQName, collapsedValue);
                // Reset
                scratch.clear();
            }
            if (nextQNameId != null)
            {
                // Add to the current entries
                scratch.put(nextPropertyKey, nextPropertyValue);
                currentQNameId = nextQNameId;
            }
            else
            {
                // There is no next value to process
                break;
            }
        }
        // Done
        return propertyMap;
    }

    private Serializable collapsePropertiesWithSameQName(
            PropertyDefinition propertyDef,
            SortedMap<NodePropertyKey, NodePropertyValue> sortedPropertyValues)
    {
        Serializable result = null;
        Collection<Serializable> collectionResult = null;
        // A working map. Ordering is not important for this map.
        Map<NodePropertyKey, NodePropertyValue> scratch = new HashMap<NodePropertyKey, NodePropertyValue>(3);
        // Iterate (sorted) over the map entries and extract values with the same list index
        Integer currentListIndex = Integer.MIN_VALUE;
        Iterator<Map.Entry<NodePropertyKey, NodePropertyValue>> iterator = sortedPropertyValues.entrySet().iterator();
        while (true)
        {
            Integer nextListIndex = null;
            NodePropertyKey nextPropertyKey = null;
            NodePropertyValue nextPropertyValue = null;
            // Record the next entry's values
            if (iterator.hasNext())
            {
                Map.Entry<NodePropertyKey, NodePropertyValue> entry = iterator.next();
                nextPropertyKey = entry.getKey();
                nextPropertyValue = entry.getValue();
                nextListIndex = nextPropertyKey.getListIndex();
            }
            // If the list index is going to change, and we have some entries to process, then process them.
            if (scratch.size() > 0 && (nextListIndex == null || !nextListIndex.equals(currentListIndex)))
            {
                // We have added something to the scratch properties but the index has just changed
                Serializable collapsedValue = collapsePropertiesWithSameQNameAndListIndex(propertyDef, scratch);
                // Store. If there is a value already, then we must build a collection.
                if (result == null)
                {
                    result = collapsedValue;
                }
                else if (collectionResult != null)
                {
                    // We have started a collection, so just add the value to it.
                    collectionResult.add(collapsedValue);
                }
                else
                {
                    // We already had a result, and now have another. A collection has not been
                    // started. We start a collection and explicitly keep track of it so that
                    // we don't get mixed up with collections of collections (ETHREEOH-2064).
                    collectionResult = new ArrayList<Serializable>(20);
                    collectionResult.add(result); // Add the first result
                    collectionResult.add(collapsedValue); // Add the new value
                    result = (Serializable) collectionResult;
                }
                // Reset
                scratch.clear();
            }
            if (nextListIndex != null)
            {
                // Add to the current entries
                scratch.put(nextPropertyKey, nextPropertyValue);
                currentListIndex = nextListIndex;
            }
            else
            {
                // There is no next value to process
                break;
            }
        }
        // Make sure that multi-valued properties are returned as a collection
        if (propertyDef != null && propertyDef.isMultiValued() && result != null && !(result instanceof Collection<?>))
        {
            // Can't use Collections.singletonList: ETHREEOH-1172
            ArrayList<Serializable> collection = new ArrayList<Serializable>(1);
            collection.add(result);
            result = collection;
        }
        // Done
        return result;
    }

    /**
     * At this level, the properties have the same qname and list index. They can only be separated by locale.
     * Typically, MLText will fall into this category as only.
     * <p>
     * If there are multiple values then they can only be separated by locale. If they are separated by locale, then
     * they have to be text-based. This means that the only way to store them is via MLText. Any other multi-locale
     * properties cannot be deserialized.
     */
    private Serializable collapsePropertiesWithSameQNameAndListIndex(
            PropertyDefinition propertyDef,
            Map<NodePropertyKey, NodePropertyValue> propertyValues)
    {
        int propertyValuesSize = propertyValues.size();
        Serializable value = null;
        if (propertyValuesSize == 0)
        {
            // Nothing to do
            return value;
        }
        
        // Do we definitely have MLText?
        boolean isMLText = (propertyDef != null && propertyDef.getDataType().getName().equals(DataTypeDefinition.MLTEXT));
        
        // Determine the default locale ID.  The chance of it being null is vanishingly small, but ...
        Pair<Long, Locale> defaultLocalePair = localeDAO.getDefaultLocalePair();
        Long defaultLocaleId = (defaultLocalePair == null) ? null : defaultLocalePair.getFirst();
        
        Integer listIndex = null;
        for (Map.Entry<NodePropertyKey, NodePropertyValue> entry : propertyValues.entrySet())
        {
            NodePropertyKey propertyKey = entry.getKey();
            NodePropertyValue propertyValue = entry.getValue();

            // Check that the client code has gathered the values together correctly
            if (listIndex == null)
            {
                listIndex = propertyKey.getListIndex();
            }
            else if (!listIndex.equals(propertyKey.getListIndex()))
            {
                throw new IllegalStateException("Expecting to collapse properties with same list index: " + propertyValues);
            }
            
            // Get the locale of the current value
            Long localeId = propertyKey.getLocaleId();
            boolean isDefaultLocale = EqualsHelper.nullSafeEquals(defaultLocaleId, localeId);
            
            // Get the local entry value
            Serializable entryValue = makeSerializableValue(propertyDef, propertyValue);
            
            // A default locale indicates a simple value i.e. the entry represents the whole value,
            // unless the dictionary specifically declares it to be d:mltext
            if (isDefaultLocale && !isMLText)
            {
                // Check and warn if there are other values
                if (propertyValuesSize > 1)
                {
                    logger.warn(
                            "Found localized properties along with a 'null' value in the default locale. \n" +
                            "   The localized values will be ignored; 'null' will be returned: \n" +
                            "   Default locale ID: " + defaultLocaleId + "\n" +
                            "   Property:          " + propertyDef + "\n" +
                            "   Values:            " + propertyValues);
                }
                // The entry could be null or whatever value came out
                value = entryValue;
                break;
            }
            else
            {
                // Non-default locales indicate MLText ONLY.
                Locale locale = localeDAO.getLocalePair(localeId).getSecond();
                // Note that we force a non-null value here as a null MLText object is persisted
                // just like any other null i.e. with the default locale.
                if (value == null)
                {
                    value = new MLText();
                }       // We break for other entry values, so no need to check the non-null case
                // Put the current value into the MLText object
                if (entryValue == null || entryValue instanceof String)
                {
                    // Can put in nulls and Strings
                    ((MLText)value).put(locale, (String)entryValue);    // We've checked the casts
                }
                else
                {
                    // It's a non-null non-String ... can't be added to MLText!
                    logger.warn(
                            "Found localized non-String properties. \n" +
                            "   The non-String values will be ignored: \n" +
                            "   Default locale ID: " + defaultLocaleId + "\n" +
                            "   Property:          " + propertyDef + "\n" +
                            "   Values:            " + propertyValues);
                }
            }
        }
        // Done
        return value;
    }

    /**
     * Extracts the externally-visible property from the persistable value.
     * 
     * @param propertyDef       the model property definition - may be <tt>null</tt>
     * @param propertyValue     the persisted property
     * @return                  Returns the value of the property in the format dictated by the property definition,
     *                          or null if the property value is null
     */
    public Serializable makeSerializableValue(PropertyDefinition propertyDef, NodePropertyValue propertyValue)
    {
        if (propertyValue == null)
        {
            return null;
        }
        // get property attributes
        final QName propertyTypeQName;
        if (propertyDef == null)
        {
            // allow this for now
            propertyTypeQName = DataTypeDefinition.ANY;
        }
        else
        {
            propertyTypeQName = propertyDef.getDataType().getName();
        }
        try
        {
            Serializable value = propertyValue.getValue(propertyTypeQName);
            // Handle conversions to and from ContentData
            if (value instanceof ContentDataId)
            {
                // ContentData used to be persisted as a String and then as a Long.
                // Now it has a special type to denote the ID
                Long contentDataId = ((ContentDataId) value).getId();
                ContentData contentData = contentDataDAO.getContentData(contentDataId).getSecond();
                value = new ContentDataWithId(contentData, contentDataId);
            }
            else if ((value instanceof Long) && propertyTypeQName.equals(DataTypeDefinition.CONTENT))
            {
                Long contentDataId = (Long) value;
                ContentData contentData = contentDataDAO.getContentData(contentDataId).getSecond();
                value = new ContentDataWithId(contentData, contentDataId);
            }
            // done
            return value;
        }
        catch (TypeConversionException e)
        {
            throw new TypeConversionException(
                    "The property value is not compatible with the type defined for the property: \n" +
                    "   property: " + (propertyDef == null ? "unknown" : propertyDef) + "\n" +
                    "   property value: " + propertyValue, e);
        }
    }
}
