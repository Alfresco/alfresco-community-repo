/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.dictionary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;


/**
 * Data Dictionary Service Implementation
 * 
 * @author David Caruana
 */
public class DictionaryComponent implements DictionaryService
{
    private DictionaryDAO dictionaryDAO;


    // TODO: Check passed arguments are valid
    
    /**
     * Sets the Meta Model DAO
     * 
     * @param metaModelDAO  meta model DAO
     */
    public void setDictionaryDAO(DictionaryDAO dictionaryDAO)
    {
        this.dictionaryDAO = dictionaryDAO;
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.DictionaryService#getAllModels()
     */
    public Collection<QName> getAllModels()
    {
        return dictionaryDAO.getModels();
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.DictionaryService#getModel(org.alfresco.repo.ref.QName)
     */
    public ModelDefinition getModel(QName model)
    {
        return dictionaryDAO.getModel(model);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.DictionaryService#getAllPropertyTypes()
     */
    public Collection<QName> getAllDataTypes()
    {
        Collection<QName> propertyTypes = new ArrayList<QName>();
        for (QName model : getAllModels())
        {
            propertyTypes.addAll(getAspects(model));
        }
        return propertyTypes;
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.DictionaryService#getPropertyTypes(org.alfresco.repo.ref.QName)
     */
    public Collection<QName> getDataTypes(QName model)
    {
        Collection<DataTypeDefinition> propertyTypes = dictionaryDAO.getDataTypes(model);
        Collection<QName> qnames = new ArrayList<QName>(propertyTypes.size());
        for (DataTypeDefinition def : propertyTypes)
        {
            qnames.add(def.getName());
        }
        return qnames;
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.DictionaryService#getAllTypes()
     */
    public Collection<QName> getAllTypes()
    {
        Collection<QName> types = new ArrayList<QName>();
        for (QName model : getAllModels())
        {
            types.addAll(getTypes(model));
        }
        return types;
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.DictionaryService#getTypes(org.alfresco.repo.ref.QName)
     */
    public Collection<QName> getTypes(QName model)
    {
        Collection<TypeDefinition> types = dictionaryDAO.getTypes(model);
        Collection<QName> qnames = new ArrayList<QName>(types.size());
        for (TypeDefinition def : types)
        {
            qnames.add(def.getName());
        }
        return qnames;
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.DictionaryService#getAllAspects()
     */
    public Collection<QName> getAllAspects()
    {
        Collection<QName> aspects = new ArrayList<QName>();
        for (QName model : getAllModels())
        {
            aspects.addAll(getAspects(model));
        }
        return aspects;
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.DictionaryService#getAspects(org.alfresco.repo.ref.QName)
     */
    public Collection<QName> getAspects(QName model)
    {
        Collection<AspectDefinition> aspects = dictionaryDAO.getAspects(model);
        Collection<QName> qnames = new ArrayList<QName>(aspects.size());
        for (AspectDefinition def : aspects)
        {
            qnames.add(def.getName());
        }
        return qnames;
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.DictionaryService#isSubClass(org.alfresco.repo.ref.QName, org.alfresco.repo.ref.QName)
     */
    public boolean isSubClass(QName className, QName ofClassName)
    {
        // Validate arguments
        ParameterCheck.mandatory("className", className);
        ParameterCheck.mandatory("ofClassName", ofClassName);
        ClassDefinition classDef = getClass(className);
        if (classDef == null)
        {
            throw new InvalidTypeException(className);
        }
        ClassDefinition ofClassDef = getClass(ofClassName);
        if (ofClassDef == null)
        {
            throw new InvalidTypeException(ofClassName);
        }
        
        // Only check if both ends are either a type or an aspect
        boolean subClassOf = false;
        if (classDef.isAspect() == ofClassDef.isAspect())
        {
            while (classDef != null)
            {
                if (classDef.equals(ofClassDef))
                {
                    subClassOf = true;
                    break;
                }
                
                // No match yet, so go to parent class
                QName parentClassName = classDef.getParentName();
                classDef = (parentClassName == null) ? null : getClass(parentClassName);
            }
        }        
        return subClassOf;
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.DictionaryService#getPropertyType(org.alfresco.repo.ref.QName)
     */
    public DataTypeDefinition getDataType(QName name)
    {
        return dictionaryDAO.getDataType(name);
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.dictionary.DictionaryService#getDataType(java.lang.Class)
     */
    public DataTypeDefinition getDataType(Class javaClass)
    {
        return dictionaryDAO.getDataType(javaClass);
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.DictionaryService#getType(org.alfresco.repo.ref.QName)
     */
    public TypeDefinition getType(QName name)
    {
        return dictionaryDAO.getType(name);
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.DictionaryService#getAspect(org.alfresco.repo.ref.QName)
     */
    public AspectDefinition getAspect(QName name)
    {
        return dictionaryDAO.getAspect(name);
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.DictionaryService#getClass(org.alfresco.repo.ref.QName)
     */
    public ClassDefinition getClass(QName name)
    {
        return dictionaryDAO.getClass(name);
    }
    
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.DictionaryService#getAnonymousType(org.alfresco.repo.ref.QName, java.util.Collection)
     */
    public TypeDefinition getAnonymousType(QName type, Collection<QName> aspects)
    {
        return dictionaryDAO.getAnonymousType(type, aspects);
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.DictionaryService#getProperty(org.alfresco.repo.ref.QName, org.alfresco.repo.ref.QName)
     */
    public PropertyDefinition getProperty(QName className, QName propertyName)
    {
        PropertyDefinition propDef = null;
        ClassDefinition classDef = dictionaryDAO.getClass(className);
        if (classDef != null)
        {
            Map<QName,PropertyDefinition> propDefs = classDef.getProperties();
            propDef = propDefs.get(propertyName);
        }
        return propDef;
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.DictionaryService#getProperty(org.alfresco.repo.ref.QName)
     */
    public PropertyDefinition getProperty(QName propertyName)
    {
        return dictionaryDAO.getProperty(propertyName);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.DictionaryService#getAssociation(org.alfresco.repo.ref.QName)
     */
    public AssociationDefinition getAssociation(QName associationName)
    {
        return dictionaryDAO.getAssociation(associationName);
    }
    
}
