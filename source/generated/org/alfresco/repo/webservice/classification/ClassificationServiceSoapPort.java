/**
 * ClassificationServiceSoapPort.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.alfresco.repo.webservice.classification;

public interface ClassificationServiceSoapPort extends java.rmi.Remote {

    /**
     * Gets available classifications.
     */
    public org.alfresco.repo.webservice.types.Classification[] getClassifications(org.alfresco.repo.webservice.types.Store store) throws java.rmi.RemoteException, org.alfresco.repo.webservice.classification.ClassificationFault;

    /**
     * Gets child categories for the specified parent category.
     */
    public org.alfresco.repo.webservice.types.Category[] getChildCategories(org.alfresco.repo.webservice.types.Reference parentCategory) throws java.rmi.RemoteException, org.alfresco.repo.webservice.classification.ClassificationFault;

    /**
     * Gets the categories applied to the specified content resource.
     */
    public org.alfresco.repo.webservice.classification.CategoriesResult[] getCategories(org.alfresco.repo.webservice.types.Predicate items) throws java.rmi.RemoteException, org.alfresco.repo.webservice.classification.ClassificationFault;

    /**
     * Modifies the categories applied to the specified content resource.
     */
    public org.alfresco.repo.webservice.classification.CategoriesResult[] setCategories(org.alfresco.repo.webservice.types.Predicate items, org.alfresco.repo.webservice.classification.AppliedCategory[] categories) throws java.rmi.RemoteException, org.alfresco.repo.webservice.classification.ClassificationFault;

    /**
     * Describe a classification in terms of how it is represented
     * in the meta model i.e. is it an aspect, which property holds the applied
     * category.
     */
    public org.alfresco.repo.webservice.types.ClassDefinition describeClassification(java.lang.String classification) throws java.rmi.RemoteException, org.alfresco.repo.webservice.classification.ClassificationFault;
}
