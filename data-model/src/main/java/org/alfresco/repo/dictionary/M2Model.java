/*
 * #%L
 * Alfresco Data model classes
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
package org.alfresco.repo.dictionary;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.zip.CRC32;

import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;


/**
 * Model Definition.
 * 
 * @author David Caruana
 *
 */
public class M2Model
{
    private static final String ERR_PARSE_FAILURE = "d_dictionary.model.err.parse.failure";
    private static final String ERR_CREATE_M2MODEL_FAILURE = "d_dictionary.model.err.create_m2model.failure";

    private String name = null;
    private String description = null;
    private String author = null;
    private Date published = null;
    private String version;
    private String  analyserResourceBundleName = null;

    private List<M2Namespace> namespaces = new ArrayList<M2Namespace>();
    private List<M2Namespace> imports = new ArrayList<M2Namespace>();
    private List<M2DataType> dataTypes = new ArrayList<M2DataType>();
    private List<M2Type> types = new ArrayList<M2Type>();
    private List<M2Aspect> aspects = new ArrayList<M2Aspect>();
    private List<M2Constraint> constraints = new ArrayList<M2Constraint>();

    private M2Model()
    {
    }


    /**
     * Construct an empty model
     * 
     * @param name  the name of the model
     * @return  the model
     */
    public static M2Model createModel(String name)
    {
        M2Model model = new M2Model();
        model.name = name;
        return model;
    }

    
    /**
     * Construct a model from a dictionary xml specification
     * 
     * @param xml  the dictionary xml
     * @return  the model representation of the xml
     */
    public static M2Model createModel(InputStream xml)
    {
        return createModel("default", xml);
    }
    
    public static M2Model createModel(String bindingName, InputStream xml)
    {
        try
        {
            IBindingFactory factory = BindingDirectory.getFactory(bindingName, M2Model.class);
            IUnmarshallingContext context = factory.createUnmarshallingContext();
            Object obj = context.unmarshalDocument(xml, null);
            return (M2Model)obj;
        }
        catch(JiBXException e)
        {
            throw new DictionaryException(ERR_PARSE_FAILURE, e);
        }        
    }

    
    /**
     * Render the model to dictionary XML
     * 
     * @param xml  the dictionary xml representation of the model
     */
    public void toXML(OutputStream xml)
    {
        toXML(null, xml);
    }
    
    public void toXML(ModelDefinition.XMLBindingType bindingType, OutputStream xml)
    {
        try
        {
        	if(bindingType == null)
        	{
        		bindingType = ModelDefinition.XMLBindingType.DEFAULT;
        	}

        	String bindingName = bindingType.toString();
            IBindingFactory factory = (bindingName != null) ? BindingDirectory.getFactory(bindingName, M2Model.class) :
            	BindingDirectory.getFactory("default", M2Model.class);
            IMarshallingContext context = factory.createMarshallingContext();
            context.setIndent(4);
            context.marshalDocument(this, "UTF-8", null, xml);
        }
        catch(JiBXException e)
        {
            throw new DictionaryException(ERR_CREATE_M2MODEL_FAILURE, e);
        }
    }

    public long getChecksum(ModelDefinition.XMLBindingType bindingType)
    {
        final CRC32 crc = new CRC32();

        // construct the crc directly from the model's xml stream
        toXML(bindingType, new OutputStream() {
            public void write(int b) throws IOException
            {
                crc.update(b);
            }
        });

        return crc.getValue();
    }
    
    /**
     * Create a compiled form of this model
     * 
     * @param dictionaryDAO  dictionary DAO
     * @param namespaceDAO  namespace DAO
     * @return  the compiled form of the model
     */
    public CompiledModel compile(DictionaryDAO dictionaryDAO, NamespaceDAO namespaceDAO, boolean enableConstraintClassLoading)
    {
        CompiledModel compiledModel = new CompiledModel(this, dictionaryDAO, namespaceDAO, enableConstraintClassLoading);
        return compiledModel;
    }

    
    public String getName()
    {
        return name;
    }
    
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    
    public String getDescription()
    {
        return description;
    }
    
    
    public void setDescription(String description)
    {
        this.description = description;
    }
    
    
    public String getAuthor()
    {
        return author;
    }
    
    
    public void setAuthor(String author)
    {
        this.author = author;
    }
    
    
    public Date getPublishedDate()
    {
        return published;
    }
    
    
    public void setPublishedDate(Date published)
    {
        this.published = published;
    }
    
    
    public String getVersion()
    {
        return version;
    }
    
    
    public void setVersion(String version)
    {
        this.version = version;
    }

    
    public M2Type createType(String name)
    {
        M2Type type = new M2Type();
        type.setName(name);
        types.add(type);
        return type;
    }
    
    
    public void removeType(String name)
    {
        M2Type type = getType(name);
        if (type != null)
        {
            types.remove(type);
        }
    }
    
    
    public List<M2Type> getTypes()
    {
        return Collections.unmodifiableList(types);
    }

    
    public M2Type getType(String name)
    {
        for (M2Type candidate : types)
        {
            if (candidate.getName().equals(name))
            {
                return candidate;
            }
        }
        return null;
    }
    
    
    public M2Aspect createAspect(String name)
    {
        M2Aspect aspect = new M2Aspect();
        aspect.setName(name);
        aspects.add(aspect);
        return aspect;
    }
    
    
    public void removeAspect(String name)
    {
        M2Aspect aspect = getAspect(name);
        if (aspect != null)
        {
            aspects.remove(aspect);
        }
    }

    
    public List<M2Aspect> getAspects()
    {
        return Collections.unmodifiableList(aspects);
    }

    
    public M2Aspect getAspect(String name)
    {
        for (M2Aspect candidate : aspects)
        {
            if (candidate.getName().equals(name))
            {
                return candidate;
            }
        }
        return null;
    }
    
    
    public M2DataType createPropertyType(String name)
    {
        M2DataType type = new M2DataType();
        type.setName(name);
        dataTypes.add(type);
        return type;
    }
    

    public void removePropertyType(String name)
    {
        M2DataType type = getPropertyType(name);
        if (type != null)
        {
            dataTypes.remove(name);
        }
    }


    public List<M2DataType> getPropertyTypes()
    {
        return Collections.unmodifiableList(dataTypes);
    }

    
    public M2DataType getPropertyType(String name)
    {
        for (M2DataType candidate : dataTypes)
        {
            if (candidate.getName().equals(name))
            {
                return candidate;
            }
        }
        return null;
    }

    
    public M2Namespace createNamespace(String uri, String prefix)
    {
        M2Namespace namespace = new M2Namespace();
        namespace.setUri(uri);
        namespace.setPrefix(prefix);
        namespaces.add(namespace);
        return namespace;
    }
    
    
    public void removeNamespace(String uri)
    {
        M2Namespace namespace = getNamespace(uri);
        if (namespace != null)
        {
            namespaces.remove(namespace);
        }
    }

    
    public List<M2Namespace> getNamespaces()
    {
        return Collections.unmodifiableList(namespaces);
    }


    public M2Namespace getNamespace(String uri)
    {
        for (M2Namespace candidate : namespaces)
        {
            if (candidate.getUri().equals(uri))
            {
                return candidate;
            }
        }
        return null;
    }
    
    
    public M2Namespace createImport(String uri, String prefix)
    {
        M2Namespace namespace = new M2Namespace();
        namespace.setUri(uri);
        namespace.setPrefix(prefix);
        imports.add(namespace);
        return namespace;
    }
    
    
    public void removeImport(String uri)
    {
        M2Namespace namespace = getImport(uri);
        if (namespace != null)
        {
            imports.remove(namespace);
        }
    }


    public List<M2Namespace> getImports()
    {
        return Collections.unmodifiableList(imports);
    }

    
    public M2Namespace getImport(String uri)
    {
        for (M2Namespace candidate : imports)
        {
            if (candidate.getUri().equals(uri))
            {
                return candidate;
            }
        }
        return null;
    }

    public List<M2Constraint> getConstraints()
    {
        return Collections.unmodifiableList(constraints);
    }
    
    public M2Constraint getConstraint(String name)
    {
        for (M2Constraint candidate : constraints)
        {
            if (candidate.getName().equals(name))
            {
                return candidate;
            }
        }
        return null;
    }
    
    public M2Constraint createConstraint(String name, String type)
    {
        M2Constraint constraint = new M2Constraint();
        constraint.setName(name);
        constraint.setType(type);
        constraints.add(constraint);
        return constraint;
    }
    
    public void removeConstraint(String name)
    {
        M2Constraint constraint = getConstraint(name);
        if (constraint != null)
        {
            constraints.remove(constraint);
        }
    }
    
    // Do not delete: referenced by m2binding.xml
    @SuppressWarnings("unused")
    private static List createList()
    {
        return new ArrayList();
    }

    public void setConfigProperties(Properties configProperties)
    {
        if (types != null)
        {
            for (M2Class type : types)
            {
                type.setConfigProperties(configProperties);
            }
        }
        if (aspects != null)
        {
            for (M2Class aspect : aspects)
            {
                aspect.setConfigProperties(configProperties);
            }
        }
    }

}
