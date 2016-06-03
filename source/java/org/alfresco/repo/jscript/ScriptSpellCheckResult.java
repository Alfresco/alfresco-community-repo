
package org.alfresco.repo.jscript;

import java.io.Serializable;
import java.util.List;

/**
 * @author Jamal Kaabi-Mofrad
 * @since 5.0
 */
public class ScriptSpellCheckResult implements Serializable
{
    private static final long serialVersionUID = -5947320933438147267L;

    private final String originalSearchTerm;
    private final String resultName;
    private final List<String> results;
    private final boolean searchedFor;
    private final boolean spellCheckExist;

    public ScriptSpellCheckResult(String originalSearchTerm, String resultName, boolean searchedFor,
                List<String> results, boolean spellCheckExist)
    {
        this.originalSearchTerm = originalSearchTerm;
        this.resultName = resultName;
        this.results = results;
        this.searchedFor = searchedFor;
        this.spellCheckExist = spellCheckExist;
    }

    /**
     * @return the originalSearchTerm
     */
    public String getOriginalSearchTerm()
    {
        return this.originalSearchTerm;
    }

    /**
     * @return the resultName
     */
    public String getResultName()
    {
        return this.resultName;
    }

    /**
     * @return the results
     */
    public List<String> getResults()
    {
        return this.results;
    }

    /**
     * @return the searchedFor
     */
    public boolean isSearchedFor()
    {
        return this.searchedFor;
    }

    /**
     * @return the spellCheckExist
     */
    public boolean isSpellCheckExist()
    {
        return this.spellCheckExist;
    }
}
