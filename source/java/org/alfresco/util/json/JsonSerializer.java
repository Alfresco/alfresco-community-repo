
package org.alfresco.util.json;

/**
 * An interface for converting objects of a specified type (T) into a serialized
 * Json format (of type S).
 * 
 * @author Nick Smith
 */
public interface JsonSerializer<T, S>
{

    S serialize(T object);

    T deserialize(S object);
}
