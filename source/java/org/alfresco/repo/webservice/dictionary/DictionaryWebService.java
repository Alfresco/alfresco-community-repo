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
package org.alfresco.repo.webservice.dictionary;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.repo.webservice.AbstractWebService;
import org.alfresco.repo.webservice.Utils;
import org.alfresco.repo.webservice.dictionary.ClassPredicate;
import org.alfresco.repo.webservice.dictionary.DictionaryFault;
import org.alfresco.repo.webservice.dictionary.DictionaryServiceSoapPort;
import org.alfresco.repo.webservice.types.ClassDefinition;
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
