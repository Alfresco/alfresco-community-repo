package org.alfresco.repo.jscript;

import java.io.Serializable;

import org.mozilla.javascript.Scriptable;

/**
 * Response for page of ScriptNode results
 * 
 * @author janv
 * @version 4.0
 */
public class ScriptPagingNodes implements Serializable
{
    private static final long serialVersionUID = -3252996649397737176L;
    
    private Scriptable results;           // array of script nodes
    private Boolean hasMoreItems;         // true if has more items (past this page) - note: could also indicate cutoff/trimmed page
    private int totalResultCountLower;    // possible total count lower estimate (-1 => unknown)
    private int totalResultCountUpper;    // possible total count upper estimate (-1 => unknown)
    
    public ScriptPagingNodes(Scriptable results, Boolean hasMoreItems, int totalResultCountLower, int totalResultCountUpper)
    {
        this.results = results;
        this.hasMoreItems = hasMoreItems;
        this.totalResultCountLower = totalResultCountLower;
        this.totalResultCountUpper = totalResultCountUpper;
    }
    
    public Scriptable getPage()
    {
        return results;
    }
    
    public Boolean hasMoreItems()
    {
        return hasMoreItems;
    }
    
    public int getTotalResultCountLower()
    {
        return totalResultCountLower;
    }
    
    public int getTotalResultCountUpper()
    {
        return totalResultCountUpper;
    }
}
