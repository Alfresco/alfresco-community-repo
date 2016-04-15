
package org.alfresco.repo.virtual.ref;

public interface ZeroEncoding
{

    static final String DELIMITER = ":";

    static final String REFERENCE_DELIMITER = "*";

    static final String STRING_PARAMETER = "s";

    /** depending on the resource type */
    static final String RESOURCE_PARAMETER[] = { "0", "1", "2" };

    static final String REFERENCE_PARAMETER = "r";

    public static final int VANILLA_PROTOCOL_CODE = 0;

    public static final int VIRTUAL_PROTOCOL_CODE = 3;

    public static final int NODE_PROTOCOL_CODE = 6;

    public static final int REPOSITORY_RESOURCE_CODE = 0;

    public static final int PATH_CODE = 0;

    public static final int NODE_CODE = 1;

    public static final int CLASSPATH_RESOURCE_CODE = 2;
}
