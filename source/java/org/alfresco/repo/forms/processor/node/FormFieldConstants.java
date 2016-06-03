
package org.alfresco.repo.forms.processor.node;

/**
 * @since 3.4
 * @author Nick Smith
 */
public interface FormFieldConstants
{
    /** Public constants */
    public static final String ON = "on";

    public static final String PROP = "prop";

    public static final String ASSOC = "assoc";

    public static final String FIELD_NAME_SEPARATOR = ":";

    public static final String DATA_KEY_SEPARATOR = "_";

    public static final String PROP_DATA_PREFIX = PROP + DATA_KEY_SEPARATOR;

    public static final String ASSOC_DATA_PREFIX = ASSOC + DATA_KEY_SEPARATOR;

    public static final String ASSOC_DATA_ADDED_SUFFIX = DATA_KEY_SEPARATOR + "added";

    public static final String ASSOC_DATA_REMOVED_SUFFIX = DATA_KEY_SEPARATOR + "removed";

    public static final String ADDED = "added";

    public static final String REMOVED = "removed";
    
    public static final String DOT_CHARACTER = ".";

    public static final String DOT_CHARACTER_REPLACEMENT = "#dot#";
    
    /** Protected constants */
    public static final String DEFAULT_CONTENT_MIMETYPE = "text/plain";
    
}
