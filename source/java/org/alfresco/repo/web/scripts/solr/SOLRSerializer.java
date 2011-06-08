/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts.solr;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.domain.node.ContentDataWithId;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.Path.AttributeElement;
import org.alfresco.service.cmr.repository.Path.ChildAssocElement;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.TypeConverter;
import org.alfresco.service.cmr.repository.datatype.TypeConverter.Converter;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * SOLR conversions of values to JSON-compatible <tt>String</tt>.
 * 
 * @since 4.0
 */
/* package */ class SOLRSerializer
{
    protected static final Log logger = LogFactory.getLog(SOLRSerializer.class);
    
    private Set<QName> NUMBER_TYPES;

    private DictionaryService dictionaryService;
    private NamespaceService namespaceService;
    
    private SOLRTypeConverter typeConverter;

    public void init()
    {
        PropertyCheck.mandatory(this, "dictionaryService", dictionaryService);
        PropertyCheck.mandatory(this, "namespaceService", namespaceService);

        NUMBER_TYPES = new HashSet<QName>(4);
        NUMBER_TYPES.add(DataTypeDefinition.DOUBLE);
        NUMBER_TYPES.add(DataTypeDefinition.FLOAT);
        NUMBER_TYPES.add(DataTypeDefinition.INT);
        NUMBER_TYPES.add(DataTypeDefinition.LONG);

        typeConverter = new SOLRTypeConverter(namespaceService);
    }
    
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    public <T> T serializeValue(Class<T> targetClass, Object value) throws JSONException
    {
        return typeConverter.INSTANCE.convert(targetClass, value);
    }
    
    public String serializeToString(Serializable value)
    {
        return typeConverter.INSTANCE.convert(String.class, value);
    }
    
    @SuppressWarnings("unchecked")
    public PropertyValue serialize(QName propName, Serializable value) throws IOException, JSONException
    {
        if(value == null)
        {
            return new PropertyValue(false, "null");
        }

        PropertyDefinition propertyDef = dictionaryService.getProperty(propName);
        if(propertyDef == null)
        {
            throw new IllegalArgumentException("Could not find property definition for property " + propName);
        }
        
        boolean isMulti = propertyDef.isMultiValued();
        if(isMulti)
        {
            if(!(value instanceof Collection))
            {
                throw new IllegalArgumentException("Multi value: expected a collection, got " + value.getClass().getName());
            }

            Collection<Serializable> c = (Collection<Serializable>)value;

            JSONArray body = new JSONArray();
            for(Serializable o : c)
            {
                body.put(serializeValue(String.class, o));
            }
            
            return new PropertyValue(false, body.toString());
        }
        else
        {
            boolean encodeString = true;
            DataTypeDefinition dataType = propertyDef.getDataType();
            QName dataTypeName = dataType.getName();
            if(dataTypeName.equals(DataTypeDefinition.MLTEXT))
            {
                encodeString = false;
            }
            else if(dataTypeName.equals(DataTypeDefinition.CONTENT))
            {
                encodeString = false;
            }
            else
            {
                encodeString = true;
            }
            return new PropertyValue(encodeString, serializeValue(String.class, value));
        }
    }
    
    @SuppressWarnings("rawtypes")
    private class SOLRTypeConverter
    {
        private NamespaceService namespaceService;
        TypeConverter INSTANCE = new TypeConverter();
        
        @SuppressWarnings("unchecked")
        SOLRTypeConverter(NamespaceService namespaceService)
        {
            this.namespaceService = namespaceService;

            // add all default converters to this converter
            // TODO find a better way of doing this
            Map<Class<?>, Map<Class<?>, Converter<?, ?>>> converters = DefaultTypeConverter.INSTANCE.getConverters();
            for(Class<?> source : converters.keySet())
            {
                Map<Class<?>, Converter<?, ?>> converters1 = converters.get(source);
                for(Class dest : converters1.keySet())
                {
                    Converter converter = converters1.get(dest);
                    INSTANCE.addConverter(source, dest, converter);
                }
            }

            // MLText
            INSTANCE.addConverter(MLText.class, String.class, new TypeConverter.Converter<MLText, String>()
            {
                public String convert(MLText source)
                {
                    try
                    {
                        JSONArray array = new JSONArray();
                        for(Locale locale : source.getLocales())
                        {
                            JSONObject json = new JSONObject();
                            json.put("locale", DefaultTypeConverter.INSTANCE.convert(String.class, locale));
                            json.put("value", source.getValue(locale));
                            array.put(json);
                        }

                        return array.toString(3);
                    }
                    catch(JSONException e)
                    {
                        throw new AlfrescoRuntimeException("Unable to serialize content data to JSON", e);
                    }
                }
            });
            
            // QName
            INSTANCE.addConverter(QName.class, String.class, new TypeConverter.Converter<QName, String>()
            {
                public String convert(QName source)
                {
                    return source.toPrefixString(SOLRTypeConverter.this.namespaceService);
                }
            });
            
            // content
            INSTANCE.addConverter(ContentDataWithId.class, String.class, new TypeConverter.Converter<ContentDataWithId, String>()
            {
                public String convert(ContentDataWithId source)
                {
                    JSONObject json = new JSONObject();
                    try
                    {
                        json.put("contentId", String.valueOf(source.getId()));
                        String locale = INSTANCE.convert(String.class, source.getLocale());
                        json.put("locale", locale == null ? JSONObject.NULL : locale);
                        String encoding = source.getEncoding();
                        json.put("encoding", encoding == null ? JSONObject.NULL : encoding);
                        String mimetype = source.getMimetype();
                        json.put("mimetype", mimetype == null ? JSONObject.NULL : mimetype);
                        json.put("size", String.valueOf(source.getSize()));
                        return json.toString(3);
                    }
                    catch(JSONException e)
                    {
                        throw new AlfrescoRuntimeException("Unable to serialize content data to JSON", e);
                    }
                }
            });
                    
            // node refs
            INSTANCE.addConverter(NodeRef.class, String.class, new TypeConverter.Converter<NodeRef, String>()
            {
                public String convert(NodeRef source)
                {
                    return source.toString();
                }
            });
            
            INSTANCE.addConverter(String.class, NodeRef.class, new TypeConverter.Converter<String, NodeRef>()
            {
                public NodeRef convert(String source)
                {
                    return new NodeRef(source);
                }
            });
            
            // paths
            INSTANCE.addConverter(AttributeElement.class, String.class, new TypeConverter.Converter<AttributeElement, String>()
            {
                public String convert(AttributeElement source)
                {
                    return source.toString();
                }
            });
            
            INSTANCE.addConverter(ChildAssocElement.class, String.class, new TypeConverter.Converter<ChildAssocElement, String>()
            {
                public String convert(ChildAssocElement source)
                {
                    return source.getRef().toString();
                }
            });
            
            INSTANCE.addConverter(Path.DescendentOrSelfElement.class, String.class, new TypeConverter.Converter<Path.DescendentOrSelfElement, String>()
            {
                public String convert(Path.DescendentOrSelfElement source)
                {
                    return source.toString();
                }
            });
            
            INSTANCE.addConverter(Path.ParentElement.class, String.class, new TypeConverter.Converter<Path.ParentElement, String>()
            {
                public String convert(Path.ParentElement source)
                {
                    return source.toString();
                }
            });
            
            INSTANCE.addConverter(Path.SelfElement.class, String.class, new TypeConverter.Converter<Path.SelfElement, String>()
            {
                public String convert(Path.SelfElement source)
                {
                    return source.toString();
                }
            });
            
            INSTANCE.addConverter(String.class, AttributeElement.class, new TypeConverter.Converter<String, AttributeElement>()
            {
                public AttributeElement convert(String source)
                {
                    return new Path.AttributeElement(source);
                }
            });
            
            INSTANCE.addConverter(String.class, ChildAssocElement.class, new TypeConverter.Converter<String, ChildAssocElement>()
            {
                public ChildAssocElement convert(String source)
                {
                    return new Path.ChildAssocElement(INSTANCE.convert(ChildAssociationRef.class, source));
                }
            });
            
            INSTANCE.addConverter(String.class, Path.DescendentOrSelfElement.class, new TypeConverter.Converter<String, Path.DescendentOrSelfElement>()
            {
                public Path.DescendentOrSelfElement convert(String source)
                {
                    return new Path.DescendentOrSelfElement();
                }
            });
            
            INSTANCE.addConverter(String.class, Path.ParentElement.class, new TypeConverter.Converter<String, Path.ParentElement>()
            {
                public Path.ParentElement convert(String source)
                {
                    return new Path.ParentElement();
                }
            });
            
            INSTANCE.addConverter(String.class, Path.SelfElement.class, new TypeConverter.Converter<String, Path.SelfElement>()
            {
                public Path.SelfElement convert(String source)
                {
                    return new Path.SelfElement();
                }
            });
            

            INSTANCE.addConverter(Path.class, List.class, new TypeConverter.Converter<Path, List>()
            {
                public List convert(Path source)
                {
                    List<String> pathArray = new ArrayList<String>(source.size());
                    for(Path.Element element : source)
                    {
                        pathArray.add(INSTANCE.convert(String.class, element));
                    }
                    return pathArray;
                }
            });
            
            INSTANCE.addConverter(Path.class, String.class, new TypeConverter.Converter<Path, String>()
            {
                public String convert(Path source)
                {
                    return source.toString();
                }
            });
            
            // associations
            INSTANCE.addConverter(ChildAssociationRef.class, String.class, new TypeConverter.Converter<ChildAssociationRef, String>()
            {
                public String convert(ChildAssociationRef source)
                {
                    return source.toString();
                }
            });

            INSTANCE.addConverter(String.class, ChildAssociationRef.class, new TypeConverter.Converter<String, ChildAssociationRef>()
            {
                public ChildAssociationRef convert(String source)
                {
                    return new ChildAssociationRef(source);
                }
            });

            INSTANCE.addConverter(AssociationRef.class, String.class, new TypeConverter.Converter<AssociationRef, String>()
            {
                public String convert(AssociationRef source)
                {
                    return source.toString();
                }
            });

            INSTANCE.addConverter(String.class, AssociationRef.class, new TypeConverter.Converter<String, AssociationRef>()
            {
                public AssociationRef convert(String source)
                {
                    return new AssociationRef(source);
                }
            });
            
            INSTANCE.addConverter(Number.class, String.class, new TypeConverter.Converter<Number, String>()
            {
                public String convert(Number source)
                {
                    return source.toString();
                }
            });

            INSTANCE.addConverter(Boolean.class, String.class, new TypeConverter.Converter<Boolean, String>()
            {
                public String convert(Boolean source)
                {
                    return source.toString();
                }
            });

        }
    }
}
