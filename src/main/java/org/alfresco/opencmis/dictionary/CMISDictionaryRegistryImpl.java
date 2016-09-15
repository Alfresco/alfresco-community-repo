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
package org.alfresco.opencmis.dictionary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.opencmis.dictionary.CMISAbstractDictionaryService.DictionaryInitializer;
import org.alfresco.opencmis.mapping.CMISMapping;
import org.alfresco.repo.dictionary.CompiledModel;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.QName;
import org.apache.chemistry.opencmis.commons.data.CmisExtensionElement;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.CmisExtensionElementImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * CMIS Dictionary registry
 * 
 * Index of CMIS Type Definitions
 * 
 * @author sglover
 */
public class CMISDictionaryRegistryImpl implements CMISDictionaryRegistry
{
    public static final String ALFRESCO_EXTENSION_NAMESPACE = "http://www.alfresco.org";
    public static final String MANDATORY_ASPECTS = "mandatoryAspects";
    public static final String MANDATORY_ASPECT = "mandatoryAspect";

    // Logger
    protected static final Log logger = LogFactory.getLog(CMISDictionaryRegistryImpl.class);

	private CMISMapping cmisMapping;
	private DictionaryService dictionaryService;
	private String tenant;

	protected CMISAbstractDictionaryService cmisDictionaryService;
	private String parentTenant;

    private DictionaryInitializer dictionaryInitializer;

    // Type Definitions Index
    private Map<QName, TypeDefinitionWrapper> typeDefsByQName = new HashMap<QName, TypeDefinitionWrapper>();
    private Map<QName, AbstractTypeDefinitionWrapper> assocDefsByQName = new HashMap<QName, AbstractTypeDefinitionWrapper>();

    private Map<String, AbstractTypeDefinitionWrapper> typeDefsByTypeId = new HashMap<String, AbstractTypeDefinitionWrapper>();
    private Map<String, TypeDefinitionWrapper> typeDefsByQueryName = new HashMap<String, TypeDefinitionWrapper>();
    private List<TypeDefinitionWrapper> baseTypes = new ArrayList<TypeDefinitionWrapper>();

    private Map<String, PropertyDefinitionWrapper> propDefbyPropId = new HashMap<String, PropertyDefinitionWrapper>();
    private Map<String, PropertyDefinitionWrapper> propDefbyQueryName = new HashMap<String, PropertyDefinitionWrapper>();

	private Map<String, List<TypeDefinitionWrapper>> children = new HashMap<String, List<TypeDefinitionWrapper>>();

    public CMISDictionaryRegistryImpl(CMISAbstractDictionaryService cmisDictionaryService, CMISMapping cmisMapping,
    		DictionaryService dictionaryService, DictionaryInitializer dictionaryInitializer)
    {
    	this(cmisDictionaryService, "", null, cmisMapping, dictionaryService, dictionaryInitializer);
    }

    /*
     * Testing only.
     */
    CMISDictionaryRegistryImpl()
    {
    }

    public CMISDictionaryRegistryImpl(CMISAbstractDictionaryService cmisDictionaryService, String tenant, String parentTenant,
    		CMISMapping cmisMapping, DictionaryService dictionaryService, DictionaryInitializer dictionaryInitializer)
    {
    	this.cmisDictionaryService = cmisDictionaryService;
    	this.tenant = tenant;
    	this.parentTenant = parentTenant;
    	this.cmisMapping = cmisMapping;
    	this.dictionaryService = dictionaryService;
    	this.dictionaryInitializer = dictionaryInitializer;
    }

    protected CMISDictionaryRegistry getParent()
    {
    	CMISDictionaryRegistry registry = null;
    	if(parentTenant != null)
    	{
    		return cmisDictionaryService.getRegistry(parentTenant);
    	}
    	return registry;
    }

    @Override
    public String getTenant()
    {
		return tenant;
	}

	private List<TypeDefinitionWrapper> getChildrenImpl(String typeId)
	{
		return children.get(typeId);
	}

    @Override
	public List<TypeDefinitionWrapper> getChildren(String typeId)
	{
    	List<TypeDefinitionWrapper> ret = new LinkedList<>();

    	List<TypeDefinitionWrapper> children = getChildrenImpl(typeId);
    	if(children != null)
    	{
    		ret.addAll(children);
    	}

		if(getParent() != null)
		{
			children = getParent().getChildren(typeId);
	    	if(children != null)
	    	{
	    		ret.addAll(children);
	    	}
		}

		return ret;
	}

    @Override
	public void setChildren(String typeId, List<TypeDefinitionWrapper> children)
	{
		this.children.put(typeId, children);
	}

    @Override
	public void addChild(String typeId, TypeDefinitionWrapper child)
	{
    	List<TypeDefinitionWrapper> children = this.children.get(typeId);
    	if(children == null)
    	{
    		children = new LinkedList<TypeDefinitionWrapper>();
    		this.children.put(typeId, children);
    	}
		children.add(child);
	}

	@Override
    public TypeDefinitionWrapper getTypeDefByTypeId(String typeId)
    {
		return getTypeDefByTypeId(typeId, true);
    }

	@Override
    public TypeDefinitionWrapper getTypeDefByTypeId(String typeId, boolean includeParent)
    {
    	TypeDefinitionWrapper typeDef = typeDefsByTypeId.get(typeId);
    	if(typeDef == null && includeParent && getParent() != null)
    	{
    		typeDef = getParent().getTypeDefByTypeId(typeId);
    	}

    	return typeDef;
    }

    @Override
    public TypeDefinitionWrapper getAssocDefByQName(QName qname)
    {
    	TypeDefinitionWrapper typeDef = assocDefsByQName.get(qname);
    	if(typeDef == null && getParent() != null)
    	{
    		typeDef = getParent().getAssocDefByQName(qname);
    	}

    	return typeDef;
    }

    @Override
    public TypeDefinitionWrapper getTypeDefByQueryName(Object queryName)
    {
    	TypeDefinitionWrapper typeDef = typeDefsByQueryName.get(queryName);
    	if(typeDef == null && getParent() != null)
    	{
    		typeDef = getParent().getTypeDefByQueryName(queryName);
    	}

    	return typeDef;
    }

    @Override
    public TypeDefinitionWrapper getTypeDefByQName(QName qname)
    {
    	TypeDefinitionWrapper typeDef = typeDefsByQName.get(qname);
    	if(typeDef == null && getParent() != null)
    	{
    		typeDef = getParent().getTypeDefByQName(qname);
    	}

    	return typeDef;
    }

    @Override
    public PropertyDefinitionWrapper getPropDefByPropId(String propId)
    {
    	PropertyDefinitionWrapper propDef = propDefbyPropId.get(propId);
    	if(propDef == null && getParent() != null)
    	{
    		propDef = getParent().getPropDefByPropId(propId);
    	}

    	return propDef;
    }

    @Override
    public PropertyDefinitionWrapper getPropDefByQueryName(Object queryName)
    {
    	PropertyDefinitionWrapper propDef = propDefbyQueryName.get(queryName);
    	if(propDef == null && getParent() != null)
    	{
    		propDef = getParent().getPropDefByQueryName(queryName);
    	}

    	return propDef;
    }

    private Collection<AbstractTypeDefinitionWrapper> getTypeDefsImpl()
    {
    	return typeDefsByTypeId.values();
    }

    @Override
    public Collection<AbstractTypeDefinitionWrapper> getTypeDefs()
    {
    	return getTypeDefs(true);
    }

    @Override
    public Collection<AbstractTypeDefinitionWrapper> getTypeDefs(boolean includeParent)
    {
    	Collection<AbstractTypeDefinitionWrapper> ret = new LinkedList<>();
    	ret.addAll(getTypeDefsImpl());
    	if(includeParent && getParent() != null)
    	{
    		ret.addAll(getParent().getTypeDefs());
    	}
    	return Collections.unmodifiableCollection(ret);
    }

    private Collection<AbstractTypeDefinitionWrapper> getAssocDefsImpl()
    {
    	return assocDefsByQName.values();
    }

    @Override
    public Collection<AbstractTypeDefinitionWrapper> getAssocDefs()
    {
    	return getAssocDefs(true);
    }

    @Override
    public Collection<AbstractTypeDefinitionWrapper> getAssocDefs(boolean includeParent)
    {
    	Collection<AbstractTypeDefinitionWrapper> ret = new LinkedList<>();
    	ret.addAll(getAssocDefsImpl());
    	if(includeParent && getParent() != null)
    	{
    		ret.addAll(getParent().getAssocDefs());
    	}
    	return Collections.unmodifiableCollection(ret);
    }

    private void addTypeExtensions(TypeDefinitionWrapper td)
    {
        QName classQName = td.getAlfrescoClass();
        ClassDefinition classDef = dictionaryService.getClass(classQName);
        if(classDef != null)
        {
	        // add mandatory/default aspects
	        List<AspectDefinition> defaultAspects = classDef.getDefaultAspects(true);
	        if(defaultAspects != null && defaultAspects.size() > 0)
	        {
		        List<CmisExtensionElement> mandatoryAspectsExtensions = new ArrayList<CmisExtensionElement>();
		        for(AspectDefinition aspectDef : defaultAspects)
		        {
		        	QName aspectQName = aspectDef.getName();
		        	
		        	TypeDefinitionWrapper aspectType = getTypeDefByQName(cmisMapping.getCmisType(aspectQName));
		            if (aspectType == null)
		            {
		                continue;
		            }
	
		        	mandatoryAspectsExtensions.add(new CmisExtensionElementImpl(ALFRESCO_EXTENSION_NAMESPACE, MANDATORY_ASPECT, null, aspectType.getTypeId()));
		        }
	
	            if(!mandatoryAspectsExtensions.isEmpty())
	            {
	                td.getTypeDefinition(true).setExtensions(
	                        Collections.singletonList((CmisExtensionElement) new CmisExtensionElementImpl(
	                                ALFRESCO_EXTENSION_NAMESPACE, MANDATORY_ASPECTS, null, mandatoryAspectsExtensions)));
	            }
	        }
        }
    }

    @Override
    public void addModel(CompiledModel model)
    {
    	Collection<AbstractTypeDefinitionWrapper> types = dictionaryInitializer.createDefinitions(this, model);
    	addTypes(types);
    	for(AbstractTypeDefinitionWrapper type : types)
    	{
    		type.resolveInheritance(cmisMapping, this, dictionaryService);
    	}
    }

    @Override
    public void updateModel(CompiledModel model)
    {
    	// TODO
    }

    @Override
    public void removeModel(CompiledModel model)
    {
    	// TODO
    }

    private void clear()
    {
    	typeDefsByQName.clear();
        assocDefsByQName.clear();
        typeDefsByTypeId.clear();
        typeDefsByQueryName.clear();
        baseTypes.clear();

        propDefbyPropId.clear();
        propDefbyQueryName.clear();

    	children.clear();
    }

    private void addTypes(Collection<AbstractTypeDefinitionWrapper> types)
    {
        // phase 1: construct type definitions and link them together
        for (AbstractTypeDefinitionWrapper objectTypeDef : types)
        {
        	List<TypeDefinitionWrapper> children = objectTypeDef.connectParentAndSubTypes(cmisMapping, this, dictionaryService);
            setChildren(objectTypeDef.getTypeId(), children);
        }

        // phase 2: register base types and inherit property definitions
        for (AbstractTypeDefinitionWrapper typeDef : types)
        {
            if (typeDef.getTypeDefinition(false).getParentTypeId() == null ||
            		!tenant.equals(TenantService.DEFAULT_DOMAIN))
            {
            	if(tenant.equals(TenantService.DEFAULT_DOMAIN))
            	{
            		baseTypes.add(typeDef);
            	}
                typeDef.resolveInheritance(cmisMapping, this, dictionaryService);
            }
        }

        // phase 3: register properties
        for (AbstractTypeDefinitionWrapper typeDef : types)
        {
            registerPropertyDefinitions(typeDef);
        }

        // phase 4: assert valid
        for (AbstractTypeDefinitionWrapper typeDef : types)
        {
            typeDef.assertComplete();

            addTypeExtensions(typeDef);
        }
    }

    public void init()
    {
    	long start = System.currentTimeMillis();

        if (logger.isDebugEnabled())
        {
            logger.debug("Creating type definitions...");
        }

    	Collection<AbstractTypeDefinitionWrapper> types = dictionaryInitializer.createDefinitions(this);
    	addTypes(types);

    	long end = System.currentTimeMillis();

        if (logger.isInfoEnabled())
        {
            logger.info("Initialized CMIS Dictionary " + cmisMapping.getCmisVersion() + " tenant " + tenant + " in " + (end - start) + "ms. Types:"
            		+ typeDefsByTypeId.size() + ", Base Types:" + baseTypes.size());
        }
    }

    private List<TypeDefinitionWrapper> getBaseTypesImpl()
    {
    	return baseTypes;
    }

    @Override
    public List<TypeDefinitionWrapper> getBaseTypes()
    {
    	return getBaseTypes(true);
    }

    @Override
    public List<TypeDefinitionWrapper> getBaseTypes(boolean includeParent)
    {
    	List<TypeDefinitionWrapper> ret = new LinkedList<TypeDefinitionWrapper>();

    	List<TypeDefinitionWrapper> baseTypes = getBaseTypesImpl();
    	if(baseTypes != null)
    	{
    		ret.addAll(baseTypes);
    	}

    	if(includeParent && getParent() != null)
    	{
    		baseTypes = getParent().getBaseTypes();
        	if(baseTypes != null)
        	{
        		ret.addAll(baseTypes);
        	}
    	}

        return Collections.unmodifiableList(ret);
    }

    /**
     * Register type definition.
     * 
     * @param typeDef AbstractTypeDefinitionWrapper
     */
    @Override
    public void registerTypeDefinition(AbstractTypeDefinitionWrapper typeDef)
    {
        TypeDefinitionWrapper existingTypeDef = typeDefsByTypeId.get(typeDef.getTypeId());
        if (existingTypeDef != null)
        {
//            throw new AlfrescoRuntimeException("Type " + typeDef.getTypeId() + " already registered");
            if(logger.isWarnEnabled())
            {
                logger.warn("Type " + typeDef.getTypeId() + " already registered");
            }
        }

        typeDefsByTypeId.put(typeDef.getTypeId(), typeDef);
        QName typeQName = typeDef.getAlfrescoName();
        if (typeQName != null)
        {
            if ((typeDef instanceof RelationshipTypeDefintionWrapper) && !typeDef.isBaseType())
            {
                assocDefsByQName.put(typeQName, typeDef);
            } else
            {
                typeDefsByQName.put(typeQName, typeDef);
            }
        }

        typeDefsByQueryName.put(typeDef.getTypeDefinition(false).getQueryName(), typeDef);

        if (logger.isDebugEnabled())
        {
            logger.debug("Registered type " + typeDef.getTypeId() + " (scope=" + typeDef.getBaseTypeId() + ")");
            logger.debug(" QName: " + typeDef.getAlfrescoName());
            logger.debug(" Table: " + typeDef.getTypeDefinition(false).getQueryName());
            logger.debug(" Action Evaluators: " + typeDef.getActionEvaluators().size());
        }
    }

    /**
     * Register property definitions.
     * 
     * @param typeDef AbstractTypeDefinitionWrapper
     */
    public void registerPropertyDefinitions(AbstractTypeDefinitionWrapper typeDef)
    {
        for (PropertyDefinitionWrapper propDef : typeDef.getProperties(false))
        {
            if (propDef.getPropertyDefinition().isInherited())
            {
                continue;
            }

            propDefbyPropId.put(propDef.getPropertyId(), propDef);
            propDefbyQueryName.put(propDef.getPropertyDefinition().getQueryName(), propDef);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("DictionaryRegistry[");
        builder.append("Types=").append(typeDefsByTypeId.size()).append(", ");
        builder.append("Base Types=").append(baseTypes.size()).append(", ");
        builder.append("]");
        return builder.toString();
    }
}