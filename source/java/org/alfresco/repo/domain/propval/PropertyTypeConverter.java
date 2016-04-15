package org.alfresco.repo.domain.propval;

import java.io.Serializable;

import org.alfresco.repo.domain.propval.PropertyValueEntity.PersistedType;

/**
 * Interface for converters that to translate between persisted values and external values.
 * <p/>
 * Implementations must be able to convert between values being stored and Long, Double, String -
 * and back again.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public interface PropertyTypeConverter
{
    /**
     * When external to persisted type mappings are not obvious, the persistence framework,
     * before persisting as {@link PersistedType#SERIALIZABLE}, will give the converter
     * a chance to choose how the value must be persisted:
     * <ul>
     *   <li>{@link PersistedType#LONG}</li>
     *   <li>{@link PersistedType#DOUBLE}</li>
     *   <li>{@link PersistedType#STRING}</li>
     *   <li>{@link PersistedType#SERIALIZABLE}</li>
     *   <li>{@link PersistedType#CONSTRUCTABLE}</li>
     * </ul>
     * The converter should return {@link PersistedType#SERIALIZABLE} if no further conversions
     * are possible.  Implicit in the return value is the converter's ability to do the
     * conversion when required.
     * <p/>
     * If the converter can fully reconstruct an equal instance using just the name of the value's
     * class, then {@link PersistedType#CONSTRUCTABLE} can be used.
     * 
     * @param value             the value that does not have an obvious persistence slot
     * @return                  Returns the type of persistence to use
     */
    PersistedType getPersistentType(Serializable value);

    /**
     * Convert a value to a given type.
     * 
     * @param targetClass       the desired type to convert <i>to</i>
     * @param value             the value to convert
     * @return                  Returns the persisted type and value to persist
     */
    <T> T convert(Class<T> targetClass, Serializable value);
    
    /**
     * Construct an instance of an object that was deemed to be {@link PersistedType#CONSTRUCTABLE}.
     * 
     * @param clazzName         the name of the class
     * @return                  Returns the new instance
     */
    Serializable constructInstance(String clazzName);
}
