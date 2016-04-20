package org.alfresco.service.cmr.admin;

import java.util.List;

import org.apache.commons.logging.Log;

/**
 * Bean to carry Red/Amber/Green status messages
 * 
 * @author Derek Hulley
 * @since V3.4 Team
 */
public class RepoUsageStatus
{
    /**
     * Enumeration of usage levels
     * 
     * @author Derek Hulley
     * @since V3.4 Team
     */
    public enum RepoUsageLevel
    {
        OK, WARN_ADMIN, WARN_ALL, LOCKED_DOWN
    }
    
    private RepoUsage restrictions;
    private RepoUsage usage;
    private final RepoUsageLevel level;
    private final List<String> warnings;
    private final List<String> errors;
    
    public RepoUsageStatus(
            RepoUsage restrictions, RepoUsage usage,
            RepoUsageLevel level, List<String> warnings, List<String> errors)
    {
        this.restrictions = restrictions;
        this.usage = usage;
        this.level = level;
        this.warnings = warnings;
        this.errors = errors;
    }

    @Override
    public String toString()
    {
        return "UsageStatus [level=" + level + ", warnings=" + warnings + ", errors=" + errors + "]";
    }

    /**
     * Log warnings and errors to the given logger
     */
    public void logMessages(Log logger)
    {
        for (String msg : warnings)
        {
            logger.warn(msg);
        }
        for (String msg : errors)
        {
            logger.error(msg);
        }
    }

    public RepoUsage getRestrictions()
    {
        return restrictions;
    }

    public RepoUsage getUsage()
    {
        return usage;
    }

    /**
     * @return              Returns the current warning level
     */
    public RepoUsageLevel getLevel()
    {
        return level;
    }

    /**
     * @return              Returns any warnings generated
     */
    public List<String> getWarnings()
    {
        return warnings;
    }

    /**
     * @return              Returns any errors generated
     */
    public List<String> getErrors()
    {
        return errors;
    }
}
