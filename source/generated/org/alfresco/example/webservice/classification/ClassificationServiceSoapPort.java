/**
 * ClassificationServiceSoapPort.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package org.alfresco.example.webservice.classification;

public interface ClassificationServiceSoapPort extends java.rmi.Remote {

    /**
     * Gets available classifications.
     */
    public org.alfresco.example.webservice.types.Classification[] getClassifications(org.alfresco.example.webservice.types.Store store) throws java.rmi.RemoteException, org.alfresco.example.webservice.classification.ClassificationFault;

    /**
     * Gets child categories for the specified parent category.
     */
    public org.alfresco.example.webservice.types.Category[] getChildCategories(org.alfresco.example.webservice.types.Reference parentCategory) throws java.rmi.RemoteException, org.alfresco.example.webservice.classification.ClassificationFault;

    /**
     * Gets the categories applied to the specified content resource.
     */
    public org.alfresco.example.webservice.classification.CategoriesResult[] getCategories(org.alfresco.example.webservice.types.Predicate items) throws java.rmi.RemoteException, org.alfresco.example.webservice.classification.ClassificationFault;

    /**
     * Modifies the categories applied to the specified content resource.
     */
    public org.alfresco.example.webservice.classification.CategoriesResult[] setCategories(org.alfresco.example.webservice.types.Predicate items, org.alfresco.example.webservice.classification.AppliedCategory[] categories) throws java.rmi.RemoteException, org.alfresco.example.webservice.classification.ClassificationFault;

    /**
     * Describe a classification in terms of how it is represented
     * in the meta model i.e. is it an aspect, which property holds the applied
     * category.
     */
    public org.alfresco.example.webservice.types.ClassDefinition describeClassification(java.lang.String classification) throws java.rmi.RemoteException, org.alfresco.example.webservice.classification.ClassificationFault;
}
