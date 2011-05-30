package org.alfresco.repo.web.scripts.solr;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.domain.node.ContentDataWithId;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
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
import org.json.JSONException;
import org.springframework.extensions.webscripts.json.JSONWriter;

public class SOLRSerializer
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
    
    public String serialize(QName propName, Serializable value) throws IOException
    {
        if(value == null)
        {
            return null;
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

            @SuppressWarnings("unchecked")
            Collection<Serializable> c = (Collection<Serializable>)value;

            StringWriter body = new StringWriter();
            JSONWriter jsonOut = new JSONWriter(body);
            jsonOut.startObject();
            {
                jsonOut.startArray();
                for(Serializable o : c)
                {
                    jsonOut.writeValue(serializeToString(o));
                }
                jsonOut.endArray();
            }
            jsonOut.endObject();
            
            return body.toString();
        }
        else
        {
            return serializeToString(value);
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
                    return String.valueOf(source.getId());
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

            // TODO should be list of Strings, need to double-check
//            INSTANCE.addConverter(List.class, Path.class, new TypeConverter.Converter<List, Path>()
//            {
//                public Path convert(List source)
//                {
////                    try
////                    {
//                        Path path = new Path();
//                        for(Object pathElementObj : source)
//                        {
//                            String pathElementStr = (String)pathElementObj;
//                            Path.Element pathElement = null;
//                            int idx = pathElementStr.indexOf("|");
//                            if(idx == -1)
//                            {
//                                throw new IllegalArgumentException("Unable to deserialize to Path Element, invalid string " + pathElementStr);
//                            }
//
//                            String prefix = pathElementStr.substring(0, idx+1);
//                            String suffix = pathElementStr.substring(idx+1);
//                            if(prefix.equals("a|"))
//                            {
//                                pathElement = INSTANCE.convert(Path.AttributeElement.class, suffix);
//                            }
//                            else if(prefix.equals("p|"))
//                            {
//                                pathElement = INSTANCE.convert(Path.ParentElement.class, suffix);
//                            }
//                            else if(prefix.equals("c|"))
//                            {
//                                pathElement = INSTANCE.convert(Path.ChildAssocElement.class, suffix);
//                            }
//                            else if(prefix.equals("s|"))
//                            {
//                                pathElement = INSTANCE.convert(Path.SelfElement.class, suffix);
//                            }
//                            else if(prefix.equals("ds|"))
//                            {
//                                pathElement = new Path.DescendentOrSelfElement();
//                            }
////                            else if(prefix.equals("se|"))
////                            {
////                                pathElement = new JCRPath.SimpleElement(QName.createQName(suffix));
////                            }
//                            else
//                            {
//                                throw new IllegalArgumentException("Unable to deserialize to Path, invalid path element string " + pathElementStr);
//                            }
//
//                            path.append(pathElement);
//                        }
//                        return path;
////                    }
////                    catch(JSONException e)
////                    {
////                        throw new IllegalArgumentException(e);
////                    }
//                }
//            });
            
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
