package org.alfresco.repo.management.subsystems;

/**
 * Interface that describes an "early" checker for subsystem properties.
 * Useful when we want to check that a property value is valid before stopping / starting the subsystem.
 * 
 * @author abalmus
 */
public interface SubsystemEarlyPropertyChecker
{
    /**
     * Get the (optional) paired property name (e.g., if we want to check a port
     * number we might want to do that together with a specific local address).
     * 
     * @return The paired property name.
     */
    String getPairedPropertyName();
    
    /**
     * Check if a subsystem property is valid.
     * @param propertyName
     * @param propertyValue
     * @param pairedPropertyValue
     * @throws InvalidPropertyValueException 
     */
    void checkPropertyValue(String propertyName, String propertyValue, String pairedPropertyValue) throws InvalidPropertyValueException;
}
