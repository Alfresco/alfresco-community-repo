
package org.alfresco.repo.virtual.ref;

public interface Parameter
{
    /**
     * Converts the value attribute into a string representation according to
     * {@link Encodings} definition, using provided {@link Stringifier}
     * parameter.
     * 
     * @param stringifier
     * @return the string representation of this parameter as provided by the
     *         given {@link Stringifier}
     * @throws ReferenceEncodingException
     */
    String stringify(Stringifier stringifier) throws ReferenceEncodingException;
}
