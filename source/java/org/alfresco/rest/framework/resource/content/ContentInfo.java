package org.alfresco.rest.framework.resource.content;

import java.util.Locale;

/**
 * Basic information about content.  Typically used with HTTPServletResponse
 */
public interface ContentInfo extends BasicContentInfo{
    public long getLength();
    public Locale getLocale();
}
