package org.alfresco.repo.forms;

/**
 * Interface definition for an object used to represent any restrictions 
 * a data type may enforce.
 *
 * @author Gavin Cornwell
 */
public interface DataTypeParameters
{
    /**
     * Returns the parameters in a Java friendly manner i.e. as an Object.
     * The Object can be as complex as a multiple nested Map of Maps or as
     * simple as a String.
     * 
     * @return An Object representing the data type parameters
     */
    public Object getAsObject();
    
    /**
     * Returns the parameters represented as JSON.
     * <p>
     * Implementations can use whatever JSON libraries they
     * desire, the only rule is that the object returned must
     * toString() to either a JSON array or JSON object i.e.
     * [...] or {...}
     * </p>
     * 
     * @return JSON Object representing the parameters
     */
    public Object getAsJSON();
}
