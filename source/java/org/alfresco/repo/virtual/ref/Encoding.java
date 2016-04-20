
package org.alfresco.repo.virtual.ref;

import java.io.Serializable;

/**
 * A {@link Reference} {@link String} encoding definition.<br>
 * 
 * @see ReferenceParser
 * @see Stringifier
 * @author Bogdan Horje
 */
public class Encoding implements Serializable
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * String encoding token - signals the beginning of a reference encoded with
     * this encoding in given String.
     */
    public final Character token;

    /**
     * <code>true</code> if {@link Reference}s encoded using this encoding 
     * can be part of URLs.
     */
    public final boolean urlNative;

    public final ReferenceParser parser;

    public final Stringifier stringifier;

    public Encoding(Character token, ReferenceParser parser, Stringifier stringifier, boolean urlNative)
    {
        super();
        this.token = token;
        this.parser = parser;
        this.stringifier = stringifier;
        this.urlNative = urlNative;
    }

}
