/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.repo.webservice.classification;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.TransactionUtil;
import org.alfresco.repo.webservice.AbstractWebService;
import org.alfresco.repo.webservice.Utils;
import org.alfresco.repo.webservice.types.Category;
import org.alfresco.repo.webservice.types.ClassDefinition;
import org.alfresco.repo.webservice.types.Classification;
import org.alfresco.repo.webservice.types.Predicate;
import org.alfresco.repo.webservice.types.Reference;
import org.alfresco.repo.webservice.types.Store;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Web service implementation of the ClassificationService. The WSDL for this
 * service can be accessed from
 * http://localhost:8080/alfresco/wsdl/classification-service.wsdl
 * 
 * @author gavinc
 */
public class ClassificationWebService extends AbstractWebService implements
        ClassificationServiceSoapPort
{
    private static Log logger = LogFactory.getLog(ClassificationWebService.class);

    /**
     * The category service
     */
    private CategoryService categoryService;
    
    /**
     * The dictionary service
     */
    private DictionaryService dictionaryService;
    
    /**
     * The transaction service
     */
    private TransactionService transactionService;

    /**
     * Set the category service
     * 
     * @param categoryService   the category service
     */
    public void setCategoryService(CategoryService categoryService)
    {
        this.categoryService = categoryService;
    }
    
    /**
     * Set the transaction service
     * 
     * @param transactionService    the transaction service
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService  = transactionService;
    }
    
    /**
     * Set the dictionary service
     * 
     * @param dictionaryService     the dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    /**
     * @see org.alfresco.repo.webservice.classification.ClassificationServiceSoapPort#getClassifications()
     */
    public Classification[] getClassifications(final Store store) throws RemoteException,
            ClassificationFault
    {
        try
        {
            return TransactionUtil.executeInUserTransaction(
                    this.transactionService,
                    new TransactionUtil.TransactionWork<Classification[]>()
                    {
                        public Classification[] doWork()
                        {
                            List<Classification> classifications = new ArrayList<Classification>();
                            
                            Collection<QName> categoryAspects = ClassificationWebService.this.categoryService.getClassificationAspects();
                            for (QName aspect : categoryAspects)
                            {
                                // Get the title of the cateogry
                                String title = null;
                                org.alfresco.service.cmr.dictionary.ClassDefinition aspectDefinition = ClassificationWebService.this.dictionaryService.getClass(aspect);
                                if (aspectDefinition != null)
                                {
                                    title = aspectDefinition.getTitle();
                                }
                                
                                if (logger.isDebugEnabled())
                                {
                                    logger.debug("Category aspect found: " + title + " (" + aspect.toString() + ")");
                                }
                                
                                Collection<ChildAssociationRef> assocs = ClassificationWebService.this.categoryService.getCategories(
                                                                                Utils.convertToStoreRef(store),
                                                                                aspect,
                                                                                CategoryService.Depth.IMMEDIATE);
                                for (ChildAssociationRef assoc : assocs)
                                {
                                    NodeRef categoryNodeRef = assoc.getChildRef();
                                    
                                    Classification classification = new Classification();
                                    classification.setClassification(aspect.toString());
                                    classification.setTitle(title);
                                    // TODO set the description
                                    classification.setRootCategory(convertToCategory(categoryNodeRef));
                                    
                                    classifications.add(classification);
                                }
                            }
                            
                            return classifications.toArray(new Classification[classifications.size()]);
                        }
                    });
        }
        catch (Throwable e)
        {
           if (logger.isDebugEnabled())
           {
              logger.error("Unexpected error occurred", e);
           }
         
           throw new ClassificationFault(0, e.getMessage());
        }
    }
    
    private Category convertToCategory(NodeRef nodeRef)
    {
        String title = (String)this.nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Category: " + title + "(" + nodeRef.toString() + ")");
        }
        
        Category category = new Category();
        category.setId(Utils.convertToReference(this.nodeService, this.namespaceService, nodeRef));
        category.setTitle(title);
        // TODO need to set the description
        return category;
    }

    /**
     * @see org.alfresco.repo.webservice.classification.ClassificationServiceSoapPort#getChildCategories(org.alfresco.repo.webservice.types.Reference)
     */
    public Category[] getChildCategories(final Reference parentCategory)
            throws RemoteException, ClassificationFault
    {
        try
        {
            return TransactionUtil.executeInUserTransaction(
                    this.transactionService,
                    new TransactionUtil.TransactionWork<Category[]>()
                    {
                        public Category[] doWork()
                        {
                            NodeRef parentNodeRef = Utils.convertToNodeRef(
                                    parentCategory, 
                                    ClassificationWebService.this.nodeService,
                                    ClassificationWebService.this.searchService,
                                    ClassificationWebService.this.namespaceService);
                            
                            Collection<ChildAssociationRef> assocs = ClassificationWebService.this.categoryService.getChildren(
                                    parentNodeRef, 
                                    CategoryService.Mode.SUB_CATEGORIES,
                                    CategoryService.Depth.IMMEDIATE);
                            
                            List<Category> categories = new ArrayList<Category>(assocs.size());
                            
                            for (ChildAssociationRef assoc : assocs)
                            {
                                NodeRef categoryNodeRef = assoc.getChildRef();
                                categories.add(convertToCategory(categoryNodeRef));
                            }
                            
                            return categories.toArray(new Category[categories.size()]);
                        }
                    });
        }
        catch (Throwable e)
        {
           if (logger.isDebugEnabled())
           {
              logger.error("Unexpected error occurred", e);
           }
         
           throw new ClassificationFault(0, e.getMessage());
        }
    }

    /**
     * @see org.alfresco.repo.webservice.classification.ClassificationServiceSoapPort#getCategories(org.alfresco.repo.webservice.types.Predicate)
     */
    public CategoriesResult[] getCategories(final Predicate items)
            throws RemoteException, ClassificationFault
    {
        try
        {
            return TransactionUtil.executeInUserTransaction(
                    this.transactionService,
                    new TransactionUtil.TransactionWork<CategoriesResult[]>()
                    {
                        public CategoriesResult[] doWork()
                        {
                            List<CategoriesResult> result = new ArrayList<CategoriesResult>();
                            
                            List<NodeRef> nodeRefs = Utils.resolvePredicate(
                                    items,
                                    ClassificationWebService.this.nodeService,
                                    ClassificationWebService.this.searchService,
                                    ClassificationWebService.this.namespaceService);
                            
                            for (NodeRef nodeRef : nodeRefs)
                            {
                                List<AppliedCategory> appliedCategories = new ArrayList<AppliedCategory>();
                                
                                Set<QName> apsects = ClassificationWebService.this.nodeService.getAspects(nodeRef);
                                for (QName aspect : apsects)
                                {
                                    if (ClassificationWebService.this.dictionaryService.isSubClass(aspect, ContentModel.ASPECT_CLASSIFIABLE) == true)
                                    {
                                        QName categoryPropertyName = getPropertyName(aspect);
                                        
                                        if (categoryPropertyName != null)
                                        {
                                            // Get the category value
                                            Collection<NodeRef> categoryNodeRefs = DefaultTypeConverter.INSTANCE.getCollection(
                                                                                NodeRef.class, 
                                                                                ClassificationWebService.this.nodeService.getProperty(nodeRef, categoryPropertyName));
                                            
                                            Reference[] categoryReferences = new Reference[categoryNodeRefs.size()];
                                            int iIndex = 0;
                                            for (NodeRef categoryNodeRef : categoryNodeRefs)
                                            {
                                                categoryReferences[iIndex] = Utils.convertToReference(ClassificationWebService.this.nodeService, ClassificationWebService.this.namespaceService, categoryNodeRef);
                                                iIndex ++;
                                            }
                                                                                
                                                                                
                                            // Create the applied category object
                                            AppliedCategory appliedCategory = new AppliedCategory();
                                            appliedCategory.setClassification(aspect.toString());
                                            appliedCategory.setCategories(categoryReferences);
                                            
                                            appliedCategories.add(appliedCategory);
                                        }
                                    }
                                }
                                
                                // Create the category result object
                                CategoriesResult categoryResult = new CategoriesResult();
                                categoryResult.setNode(Utils.convertToReference(ClassificationWebService.this.nodeService, ClassificationWebService.this.namespaceService, nodeRef));
                                categoryResult.setCategories(appliedCategories.toArray(new AppliedCategory[appliedCategories.size()]));
                                
                                result.add(categoryResult);
                            }
                            
                            return result.toArray(new CategoriesResult[result.size()]);
                        }
                    });
        }
        catch (Throwable e)
        {
           if (logger.isDebugEnabled())
           {
              logger.error("Unexpected error occurred", e);
           }
         
           throw new ClassificationFault(0, e.getMessage());
        }
    }
    
    /**
     * Get the category property qname for a classifiable apsect
     * 
     * @param aspect        the aspect qname
     * @return              the property qname, null if none found
     */
    private QName getPropertyName(QName aspect)
    {
        QName categoryPropertyName = null;
        
        // Need to get category property
        org.alfresco.service.cmr.dictionary.ClassDefinition classDefinition = ClassificationWebService.this.dictionaryService.getClass(aspect);
        for (PropertyDefinition propertyDefintion : classDefinition.getProperties().values())
        {
            if (DataTypeDefinition.CATEGORY.equals(propertyDefintion.getDataType().getName()) == true)
            {
                // We have found the category property (assume there is only one)
                categoryPropertyName = propertyDefintion.getName();
                break;
            }
        }
        
        return categoryPropertyName;
    }

    /**
     * @see org.alfresco.repo.webservice.classification.ClassificationServiceSoapPort#setCategories(org.alfresco.repo.webservice.types.Predicate,
     *      org.alfresco.repo.webservice.classification.AppliedCategory[])
     */
    public CategoriesResult[] setCategories(final Predicate items, final AppliedCategory[] categories) 
             throws RemoteException, ClassificationFault
    {
        try
        {
            return TransactionUtil.executeInUserTransaction(
                    this.transactionService,
                    new TransactionUtil.TransactionWork<CategoriesResult[]>()
                    {
                        public CategoriesResult[] doWork()
                        {
                            List<CategoriesResult> result = new ArrayList<CategoriesResult>();
                            
                            List<NodeRef> nodeRefs = Utils.resolvePredicate(
                                    items,
                                    ClassificationWebService.this.nodeService,
                                    ClassificationWebService.this.searchService,
                                    ClassificationWebService.this.namespaceService);
                            
                            for (NodeRef nodeRef : nodeRefs)
                            {
                                List<AppliedCategory> appliedCategories = new ArrayList<AppliedCategory>();
                                
                                for (AppliedCategory category : categories)
                                {
                                    QName aspect = QName.createQName(category.getClassification());
                                    QName propertyName = getPropertyName(aspect);
                                    if (propertyName != null)
                                    {
                                        // First check that the aspect has been applied to the node
                                        if (ClassificationWebService.this.nodeService.hasAspect(nodeRef, aspect) == false)
                                        {
                                            ClassificationWebService.this.nodeService.addAspect(nodeRef, aspect, null);
                                        }
                                        
                                        ArrayList<NodeRef> categoryNodeRefs = new ArrayList<NodeRef>(category.getCategories().length);
                                        for (Reference categoryReference : category.getCategories())
                                        {
                                            categoryNodeRefs.add(Utils.convertToNodeRef(
                                                                        categoryReference,
                                                                        ClassificationWebService.this.nodeService,
                                                                        ClassificationWebService.this.searchService,
                                                                        ClassificationWebService.this.namespaceService));
                                        }
                                        
                                        ClassificationWebService.this.nodeService.setProperty(nodeRef, propertyName, categoryNodeRefs);
                                        
                                        // Create the applied category object
                                        AppliedCategory appliedCategory = new AppliedCategory();
                                        appliedCategory.setClassification(category.getClassification());
                                        appliedCategory.setCategories(category.getCategories());
                                        
                                        appliedCategories.add(appliedCategory);
                                    }
                                }
                                
                                
                                // Create the category result object
                                CategoriesResult categoryResult = new CategoriesResult();
                                categoryResult.setNode(Utils.convertToReference(ClassificationWebService.this.nodeService, ClassificationWebService.this.namespaceService, nodeRef));
                                categoryResult.setCategories(appliedCategories.toArray(new AppliedCategory[appliedCategories.size()]));
                                
                                result.add(categoryResult);
                            }                            
                            
                            return result.toArray(new CategoriesResult[result.size()]);
                        }
                    });
        }
        catch (Throwable e)
        {
           if (logger.isDebugEnabled())
           {
              logger.error("Unexpected error occurred", e);
           }
         
           throw new ClassificationFault(0, e.getMessage());
        }
    }

    /**
     * @see org.alfresco.repo.webservice.classification.ClassificationServiceSoapPort#describeClassification(org.alfresco.repo.webservice.types.Reference)
     */
    public ClassDefinition describeClassification(final String classification)
            throws RemoteException, ClassificationFault
    {
        try
        {
            return TransactionUtil.executeInUserTransaction(
                    this.transactionService,
                    new TransactionUtil.TransactionWork<ClassDefinition>()
                    {
                        public ClassDefinition doWork()
                        {
                            org.alfresco.service.cmr.dictionary.ClassDefinition classDefinition = ClassificationWebService.this.dictionaryService.getClass(QName.createQName(classification));
                            return Utils.setupClassDefObject(classDefinition);
                        }
                    });
        }
        catch (Throwable e)
        {
           if (logger.isDebugEnabled())
           {
              logger.error("Unexpected error occurred", e);
           }
         
           throw new ClassificationFault(0, e.getMessage());
        }
    }
}
