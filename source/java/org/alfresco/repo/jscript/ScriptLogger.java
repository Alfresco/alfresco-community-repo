package org.alfresco.repo.jscript;

import org.alfresco.repo.processor.BaseProcessorExtension;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Kevin Roast
 */
public final class ScriptLogger extends BaseProcessorExtension
{
    private static final Log logger = LogFactory.getLog(ScriptLogger.class);
    private static final SystemOut systemOut = new SystemOut();
    
    public boolean isLoggingEnabled()
    {
        return isDebugLoggingEnabled();
    }
    
    public void log(String str)
    {
        debug(str);
    }
    
    public boolean isDebugLoggingEnabled()
    {
        return logger.isDebugEnabled();
    }
    
    public void debug(String str)
    {
        logger.debug(str);
    }
    
    public boolean isInfoLoggingEnabled()
    {
        return logger.isInfoEnabled();
    }
    
    public void info(String str)
    {
        logger.info(str);
    }
    
    public boolean isWarnLoggingEnabled()
    {
        return logger.isWarnEnabled();
    }
    
    public void warn(String str)
    {
        logger.warn(str);
    }
    
    public boolean isErrorLoggingEnabled()
    {
        return logger.isErrorEnabled();
    }
    
    public void error(String str)
    {
        logger.error(str);
    }

    public SystemOut getSystem()
    {
        return systemOut;
    }
    
    public static class SystemOut
    {
        public void out(Object str)
        {
            System.out.println(str);
        }
    }
}
