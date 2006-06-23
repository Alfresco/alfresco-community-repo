/**
 * DictionaryServiceSoapPort.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.3 Oct 05, 2005 (05:23:37 EDT) WSDL2Java emitter.
 */

package org.alfresco.repo.webservice.dictionary;

public interface DictionaryServiceSoapPort extends java.rmi.Remote {

    /**
     * Retrieves the class definitions of types and aspects.
     */
    public org.alfresco.repo.webservice.types.ClassDefinition[] getClasses(org.alfresco.repo.webservice.dictionary.ClassPredicate types, org.alfresco.repo.webservice.dictionary.ClassPredicate aspects) throws java.rmi.RemoteException, org.alfresco.repo.webservice.dictionary.DictionaryFault;

    /**
     * Retrieves property definitions.
     */
    public org.alfresco.repo.webservice.types.PropertyDefinition[] getProperties(java.lang.String[] propertyNames) throws java.rmi.RemoteException, org.alfresco.repo.webservice.dictionary.DictionaryFault;

    /**
     * Retrieves association definitions.
     */
    public org.alfresco.repo.webservice.types.AssociationDefinition[] getAssociations(java.lang.String[] associationNames) throws java.rmi.RemoteException, org.alfresco.repo.webservice.dictionary.DictionaryFault;

    /**
     * Determines whether a type (or aspect) is a sub class of another
     * type (or aspect).
     */
    public boolean isSubClass(java.lang.String className, java.lang.String isSubClassOfName) throws java.rmi.RemoteException, org.alfresco.repo.webservice.dictionary.DictionaryFault;
}
