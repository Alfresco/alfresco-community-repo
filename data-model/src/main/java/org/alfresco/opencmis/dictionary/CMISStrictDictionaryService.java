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

import java.util.Collection;
import java.util.LinkedList;

import org.alfresco.opencmis.mapping.CMISMapping;
import org.alfresco.repo.dictionary.CompiledModel;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * CMIS Dictionary which provides Types that strictly conform to the CMIS
 * specification.
 * 
 * That is, only maps types to one of root Document, Folder, Relationship and
 * Policy.   
 * 
 * And Item which is pretty much anything that is not a Document, Folder, Relationship or Policy.
 * 
 * @author steveglover
 * @author davidc
 * @author mrogers
 */
public class CMISStrictDictionaryService extends CMISAbstractDictionaryService
{
	private Log logger = LogFactory.getLog(CMISStrictDictionaryService.class);
    
    public static final String DEFAULT = "DEFAULT_DICTIONARY";

    private DictionaryInitializer coreDictionaryInitializer;
    private DictionaryInitializer tenantDictionaryInitializer;

    public void init()
    {
    	this.coreDictionaryInitializer = new DictionaryInitializer()
		{
			@Override
		    public Collection<AbstractTypeDefinitionWrapper> createDefinitions(CMISDictionaryRegistry cmisRegistry)
		    {
				Collection<AbstractTypeDefinitionWrapper> ret = new LinkedList<>();
				ret.addAll(createTypeDefs(cmisRegistry, dictionaryService.getAllTypes(true)));

				Collection<QName> assocQNames = dictionaryService.getAllAssociations(true);

		        // register base type
		        String typeId = cmisMapping.getCmisTypeId(BaseTypeId.CMIS_RELATIONSHIP, CMISMapping.RELATIONSHIP_QNAME);
		        ClassDefinition classDef = dictionaryService.getClass(CMISMapping.RELATIONSHIP_QNAME);

		        // from Thor
		        if (classDef == null)
		        {
		            if (assocQNames.size() != 0)
		            {
		                logger.warn("Unexpected - no class for "+CMISMapping.RELATIONSHIP_QNAME+" - cannot create assocDefs for: "+assocQNames);
		            }
		        }
		        else
		        {
			        RelationshipTypeDefintionWrapper objectTypeDef = new RelationshipTypeDefintionWrapper(cmisMapping,
			                accessorMapping, luceneBuilderMapping, typeId, dictionaryService, classDef);
			        cmisRegistry.registerTypeDefinition(objectTypeDef);
			        ret.add(objectTypeDef);

					ret.addAll(createAssocDefs(cmisRegistry, assocQNames));
		        }

		        ret.addAll(createTypeDefs(cmisRegistry, dictionaryService.getAllAspects(true)));
				return ret;
		    }

			@Override
		    public Collection<AbstractTypeDefinitionWrapper> createDefinitions(CMISDictionaryRegistry cmisRegistry,
		    		CompiledModel model)
		    {
				Collection<AbstractTypeDefinitionWrapper> ret = new LinkedList<>();
				
				model.getClass(model.getTypes().iterator().next().getName());

				for(TypeDefinition typeDef : model.getTypes())
				{
					QName classQName = typeDef.getName();
					AbstractTypeDefinitionWrapper objectTypeDef = createTypeDef(classQName);
		            if(objectTypeDef != null)
		            {
		                cmisRegistry.registerTypeDefinition(objectTypeDef);
		                ret.add(objectTypeDef);
		            }
				}

				for(AssociationDefinition assocDef : model.getAssociations())
				{
					QName classQName = assocDef.getName();
					RelationshipTypeDefintionWrapper assocTypeDef =  createAssocDef(classQName);
		        	if(assocTypeDef != null)
		        	{	
			            cmisRegistry.registerTypeDefinition(assocTypeDef);
				        ret.add(assocTypeDef);
		        	}
				}

				for(AspectDefinition aspectDef : model.getAspects())
				{
					QName classQName = aspectDef.getName();
					AbstractTypeDefinitionWrapper objectTypeDef = createTypeDef(classQName);
		            if(objectTypeDef != null)
		            {
		                cmisRegistry.registerTypeDefinition(objectTypeDef);
		                ret.add(objectTypeDef);
		            }
				}

				return ret;
		    }
		};

    	this.tenantDictionaryInitializer = new DictionaryInitializer()
		{
			@Override
		    public Collection<AbstractTypeDefinitionWrapper> createDefinitions(CMISDictionaryRegistry cmisRegistry)
		    {
				Collection<AbstractTypeDefinitionWrapper> ret = new LinkedList<>();
				ret.addAll(createTypeDefs(cmisRegistry, dictionaryService.getAllTypes(false)));
				ret.addAll(createAssocDefs(cmisRegistry, dictionaryService.getAllAssociations(false)));
				ret.addAll(createTypeDefs(cmisRegistry, dictionaryService.getAllAspects(false)));
				return ret;
		    }

			@Override
		    public Collection<AbstractTypeDefinitionWrapper> createDefinitions(CMISDictionaryRegistry cmisRegistry,
		    		CompiledModel model)
		    {
				Collection<AbstractTypeDefinitionWrapper> ret = new LinkedList<>();

				for(TypeDefinition typeDef : model.getTypes())
				{
					QName classQName = typeDef.getName();
					AbstractTypeDefinitionWrapper objectTypeDef = createTypeDef(classQName);
		            if(objectTypeDef != null)
		            {
		                cmisRegistry.registerTypeDefinition(objectTypeDef);
		                ret.add(objectTypeDef);
		            }
				}

				for(AssociationDefinition assocDef : model.getAssociations())
				{
					QName classQName = assocDef.getName();
					RelationshipTypeDefintionWrapper assocTypeDef =  createAssocDef(classQName);
		        	if(assocTypeDef != null)
		        	{	
			            cmisRegistry.registerTypeDefinition(assocTypeDef);
				        ret.add(assocTypeDef);
		        	}
				}

				for(AspectDefinition aspectDef : model.getAspects())
				{
					QName classQName = aspectDef.getName();
					AbstractTypeDefinitionWrapper objectTypeDef = createTypeDef(classQName);
		            if(objectTypeDef != null)
		            {
		                cmisRegistry.registerTypeDefinition(objectTypeDef);
		                ret.add(objectTypeDef);
		            }
				}

				return ret;
		    }
		};
    }

    /**
     * Create Type Definitions
     * 
     * @param classQName QName
     */
    private AbstractTypeDefinitionWrapper createTypeDef(QName classQName)
    {
    	AbstractTypeDefinitionWrapper objectTypeDef = null;

        // skip items that are remapped to CMIS model
        if(!cmisMapping.isRemappedType(classQName))
        {
	        // create appropriate kind of type definition
	        ClassDefinition classDef = dictionaryService.getClass(classQName);
	        String typeId = null;
	        if (cmisMapping.isValidCmisDocument(classQName))
	        {
	            typeId = cmisMapping.getCmisTypeId(BaseTypeId.CMIS_DOCUMENT, classQName);
	            objectTypeDef = new DocumentTypeDefinitionWrapper(cmisMapping, accessorMapping, luceneBuilderMapping, typeId, dictionaryService, classDef);
	        }
	        else if (cmisMapping.isValidCmisFolder(classQName))
	        {
	            typeId = cmisMapping.getCmisTypeId(BaseTypeId.CMIS_FOLDER, classQName);
	            objectTypeDef = new FolderTypeDefintionWrapper(cmisMapping, accessorMapping, luceneBuilderMapping, typeId, dictionaryService, classDef);
	        }
	        else if (cmisMapping.getCmisVersion().equals(CmisVersion.CMIS_1_1) && cmisMapping.isValidCmisSecondaryType(classQName))
	        {
	            typeId = cmisMapping.getCmisTypeId(BaseTypeId.CMIS_SECONDARY, classQName);
	            objectTypeDef = new SecondaryTypeDefinitionWrapper(cmisMapping, accessorMapping, luceneBuilderMapping, typeId, dictionaryService, classDef);
	        }
	        else if (cmisMapping.isValidCmisPolicy(classQName))
	        {
	            typeId = cmisMapping.getCmisTypeId(BaseTypeId.CMIS_POLICY, classQName);
	            objectTypeDef = new PolicyTypeDefintionWrapper(cmisMapping, accessorMapping, luceneBuilderMapping, typeId, dictionaryService, classDef);
	        }
	        else if (cmisMapping.isValidCmisItem(classQName))
	        {
	            typeId = cmisMapping.getCmisTypeId(BaseTypeId.CMIS_ITEM, classQName);
	            objectTypeDef = new ItemTypeDefinitionWrapper(cmisMapping, accessorMapping, luceneBuilderMapping, typeId, dictionaryService, classDef);
	        }
        }

        return objectTypeDef;
    }

    private Collection<AbstractTypeDefinitionWrapper> createTypeDefs(CMISDictionaryRegistry registry,
    		Collection<QName> classQNames)
    {
    	Collection<AbstractTypeDefinitionWrapper> ret = new LinkedList<>();

        for (QName classQName : classQNames)
        {
        	AbstractTypeDefinitionWrapper objectTypeDef = createTypeDef(classQName);
            if (objectTypeDef != null)
            {
                registry.registerTypeDefinition(objectTypeDef);
                ret.add(objectTypeDef);
            }
        }

        return ret;
    }

    private RelationshipTypeDefintionWrapper createAssocDef(QName classQName)
    {
    	RelationshipTypeDefintionWrapper assocTypeDef = null;

        if(cmisMapping.isValidCmisRelationship(classQName))
        {
	        // create appropriate kind of type definition
	        AssociationDefinition assocDef = dictionaryService.getAssociation(classQName);
	        String typeId = cmisMapping.getCmisTypeId(BaseTypeId.CMIS_RELATIONSHIP, classQName);
	        assocTypeDef = new RelationshipTypeDefintionWrapper(cmisMapping, accessorMapping, luceneBuilderMapping, 
	                typeId, dictionaryService, assocDef);
        }

        return assocTypeDef;
    }

    /**
     * Create Relationship Definitions
     * 
     * @param registry CMISDictionaryRegistry
     * @param classQNames Collection<QName
     */
    private Collection<RelationshipTypeDefintionWrapper> createAssocDefs(CMISDictionaryRegistry registry,
    		Collection<QName> classQNames)
    {
    	Collection<RelationshipTypeDefintionWrapper> ret = new LinkedList<>();

        // register all other relationships
        for (QName classQName : classQNames)
        {
        	RelationshipTypeDefintionWrapper assocTypeDef = createAssocDef(classQName);
        	if(assocTypeDef != null)
        	{	
	            registry.registerTypeDefinition(assocTypeDef);
		        ret.add(assocTypeDef);
        	}
        }

        return ret;
    }

	@Override
	protected DictionaryInitializer getCoreDictionaryInitializer()
	{
		return coreDictionaryInitializer;
	}

	@Override
	protected DictionaryInitializer getTenantDictionaryInitializer()
	{
		return tenantDictionaryInitializer;
	}
}
