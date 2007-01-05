package org.alfresco.web.api;

import javax.servlet.ServletContext;

public interface APIContextAware
{

    public void setAPIContext(ServletContext context);
}
