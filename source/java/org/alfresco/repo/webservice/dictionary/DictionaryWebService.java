/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.webservice.dictionary;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.webservice.AbstractWebService;
import org.alfresco.repo.webservice.Utils;
import org.alfresco.repo.webservice.dictionary.ClassPredicate;
import org.alfresco.repo.webservice.dictionary.DictionaryFault;
import org.alfresco.repo.webservice.dictionary.DictionaryServiceSoapPort;
import org.alfresco.repo.webservice.types.AssociationDefinition;
import org.alfresco.repo.webservice.types.ClassDefinition;
import org.alfresco.repo.webservice.types.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.InvalidClassException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Web service implementation of the DictionaryService. The WSDL for this
 * service can be accessed from
 * http://localhost:8080/alfresco/wsdl/dictionary-service.wsdl
 * 
 * @author davidc
 */
public class DictionaryWebService extends AbstractWebService implements DictionaryServiceSoapPort
{
    private static Log logger = LogFactory.getLog(DictionaryWebService.class);

    // dependencies
    private DictionaryService dictionaryService;
    private NamespaceService namespaceService;


    /**
     * Sets the instance of the DictionaryService to be used
     * 
     * @param dictionaryService   The DictionaryService
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Sets the instance of the NamespaceService to be used
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.webservice.dictionary.DictionaryServiceSoapPort#getClasses(org.alfresco.repo.webservice.dictionary.ClassPredicate[], org.alfresco.repo.webservice.dictionary.ClassPredicate[])
     */
    public ClassDefinition[] getClasses(ClassPredicate types, ClassPredicate aspects) throws RemoteException, DictionaryFault
    {
        try
        {
            Set<org.alfresco.service.cmr.dictionary.ClassDefinition> classDefs = new HashSet<org.alfresco.service.cmr.dictionary.ClassDefinition>();
            classDefs.addAll(getClassDefs(types, false));
            classDefs.addAll(getClassDefs(aspects, true));
            
            List<ClassDefinition> wsClassDefs = new ArrayList<ClassDefinition>(classDefs.size());
            for (org.alfresco.service.cmr.dictionary.ClassDefinition classDef : classDefs)
            {
                wsClassDefs.add(Utils.setupClassDefObject(classDef));
            }
            
            return wsClassDefs.toArray(new ClassDefinition[wsClassDefs.size()]);
        }
        catch (Throwable e)
        {
            if (logger.isDebugEnabled())
            {
                logger.error("Unexpected error occurred", e);
            }
            throw new DictionaryFault(0, e.getMessage());
        }
    }


    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.webservice.dictionary.DictionaryServiceSoapPort#getProperties(java.lang.String[])
     */
    public PropertyDefinition[] getProperties(String[] propertyNames) throws RemoteException, DictionaryFault
    {
        try
        {
            PropertyDefinition[] propDefs = new PropertyDefinition[propertyNames.length];
    
            int i = 0;
            for (String propertyName : propertyNames)
            {
                QName propertyQName = QName.createQName(propertyName, namespaceService);
                org.alfresco.service.cmr.dictionary.PropertyDefinition ddPropDef = dictionaryService.getProperty(propertyQName);
                if (ddPropDef == null)
                {
                    throw new AlfrescoRuntimeException("Property propertyName does not exist.");
                }
                propDefs[i++] = Utils.setupPropertyDefObject(ddPropDef);
            }
            
            return propDefs;
        }
        catch (Throwable e)
        {
            if (logger.isDebugEnabled())
            {
                logger.error("Unexpected error occurred", e);
            }
            throw new DictionaryFault(0, e.getMessage());
        }
    }

    
    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.webservice.dictionary.DictionaryServiceSoapPort#getAssociations(java.lang.String[])
     */
    public AssociationDefinition[] getAssociations(String[] associationNames) throws RemoteException, DictionaryFault
    {
        try
        {
            AssociationDefinition[] assocDefs = new AssociationDefinition[associationNames.length];
    
            int i = 0;
            for (String associationName : associationNames)
            {
                QName associationQName = QName.createQName(associationName, namespaceService);
                org.alfresco.service.cmr.dictionary.AssociationDefinition ddAssocDef = dictionaryService.getAssociation(associationQName);
                if (ddAssocDef == null)
                {
                    throw new AlfrescoRuntimeException("Property propertyName does not exist.");
                }
                assocDefs[i++] = Utils.setupAssociationDefObject(ddAssocDef);
            }
            
            return assocDefs;
        }
        catch (Throwable e)
        {
            if (logger.isDebugEnabled())
            {
                logger.error("Unexpected error occurred", e);
            }
            throw new DictionaryFault(0, e.getMessage());
        }
    }

    
    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.webservice.dictionary.DictionaryServiceSoapPort#isSubClass(java.lang.String, java.lang.String)
     */
    public boolean isSubClass(String className, String isSubClassOfName) throws RemoteException, DictionaryFault
    {
        try
        {
            QName classQName = QName.createQName(className, namespaceService);
            QName isSubClassOfQName = QName.createQName(isSubClassOfName, namespaceService);
            return dictionaryService.isSubClass(classQName, isSubClassOfQName);
        }
        catch (Throwable e)
        {
            if (logger.isDebugEnabled())
            {
                logger.error("Unexpected error occurred", e);
            }
            throw new DictionaryFault(0, e.getMessage());
        }
    }
    
    
    /**
     * Retrieve class definitions that match the provided class predicate
     * 
     * @param predicate  the class predicate to filter by
     * @param forAspects  futher filtering on type or aspect
     * @return  class definitions that match
     */
    private Set<org.alfresco.service.cmr.dictionary.ClassDefinition> getClassDefs(ClassPredicate predicate, boolean forAspects)
    {
        Set<org.alfresco.service.cmr.dictionary.ClassDefinition> classDefs = new HashSet<org.alfresco.service.cmr.dictionary.ClassDefinition>();
        if (predicate != null)
        {
            String[] predicateTypeNames = predicate.getNames();
            if (predicateTypeNames != null)
            {
                // predicate class names have been provided, therefore retrieve class definitions for those
                for (String predicateTypeName : predicateTypeNames)
                {
                    QName classQName = QName.createQName(predicateTypeName, namespaceService);
                    org.alfresco.service.cmr.dictionary.ClassDefinition classDef = dictionaryService.getClass(classQName);
                    if (classDef == null || classDef.isAspect() != forAspects)
                    {
                        throw new InvalidClassException(classQName);
                    }
                    classDefs.add(classDef);
                }
                
                // also retrieve sub-classes and super-classes as specified by predicate
                if (predicate.isFollowSuperClass() || predicate.isFollowSubClass())
                {
                    Set<org.alfresco.service.cmr.dictionary.ClassDefinition> touchedClassDefs = new HashSet<org.alfresco.service.cmr.dictionary.ClassDefinition>();
                    for (org.alfresco.service.cmr.dictionary.ClassDefinition classDef : classDefs)
                    {
                        if (predicate.isFollowSuperClass())
                        {
                            getSuperClasses(classDef, touchedClassDefs, true);
                        }
                        else if (predicate.isFollowSubClass())
                        {
                            getSubClasses(classDef, touchedClassDefs, true);
                        }
                    }
                    classDefs.addAll(touchedClassDefs);
                }
            }
        }
        else
        {
            // return all classes
            Collection<QName> classQNames = (forAspects) ? dictionaryService.getAllAspects() : dictionaryService.getAllTypes();
            for (QName classQName : classQNames)
            {
                classDefs.add(dictionaryService.getClass(classQName));
            }
        }
        
        return classDefs;
    }
    
    
    /**
     * Retrieve the super-class of the specified class
     * 
     * @param classDef   the class definition to retrieve super-classes for
     * @param superClasses  the collection to place super-classes into
     * @param recurse  true => recurse down the sub-class hierarchy
     */
    private void getSuperClasses(org.alfresco.service.cmr.dictionary.ClassDefinition classDef, Set<org.alfresco.service.cmr.dictionary.ClassDefinition> superClasses, boolean recurse)
    {
        QName superClass = classDef.getParentName();
        if (superClass != null)
        {
            org.alfresco.service.cmr.dictionary.ClassDefinition superClassDef = dictionaryService.getClass(superClass);
            superClasses.add(superClassDef);
            if (recurse)
            {
                getSuperClasses(superClassDef, superClasses, recurse);
            }
        }
    }
     

    /**
     * Retrieve the sub-class of the specified class
     * 
     * @param classDef   the class definition to retrieve sub-classes for
     * @param superClasses  the collection to place sub-classes into
     * @param recurse  true => recurse up the super-class hierarchy
     */
    private void getSubClasses(org.alfresco.service.cmr.dictionary.ClassDefinition classDef, Set<org.alfresco.service.cmr.dictionary.ClassDefinition> subClasses, boolean recurse)
    {
        QName superClass = classDef.getName();
        Collection<QName> candidates = (classDef.isAspect()) ? dictionaryService.getAllAspects() : dictionaryService.getAllTypes();

        // Note: this is the brute force way of finding sub-classes
        // TODO: Add support into Dictionary for retrieving sub-classes
        for (QName candidate : candidates)
        {
            if (dictionaryService.isSubClass(candidate, superClass) && !candidate.equals(superClass))
            {
                org.alfresco.service.cmr.dictionary.ClassDefinition subClassDef = dictionaryService.getClass(candidate);
                subClasses.add(subClassDef);
                if (recurse)
                {
                    getSubClasses(subClassDef, subClasses, recurse);
                }
            }
        }
    }

}
