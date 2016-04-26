package org.alfresco.repo.web.scripts.rule.ruleset;

import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.rule.Rule;

/**
 * Rule object for REST API
 * 
 * @author unknown
 *
 */
public class RuleRef
{

    /** Serial version UID */
    private static final long serialVersionUID = -923276130307938661L;

    private FileInfo owningFileInfo;

    private Rule rule;

    public RuleRef(Rule rule, FileInfo owningFileInfo)
    {
        this.rule = rule;
        this.owningFileInfo = owningFileInfo;
    }

    /**
     * Set the rule
     * 
     * @param rule the rule to set
     */
    public void setRule(Rule rule)
    {
        this.rule = rule;
    }

    /**
     * Return the rule
     * 
     * @return rule
     */
    public Rule getRule()
    {
        return rule;
    }

    /**
     * Set the owning file info reference for rule
     * 
     * @param owningFileInfo the owning file info reference to set
     */
    public void setOwningFileInfo(FileInfo owningFileInfo)
    {
        this.owningFileInfo = owningFileInfo;
    }

    /**
     * Returns the owning file info reference for a rule.
     * 
     * @return the owning file info reference
     */
    public FileInfo getOwningFileInfo()
    {
        return owningFileInfo;
    }
}
