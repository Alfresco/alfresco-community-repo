
package org.alfresco.service.cmr.search;

/**
 * This class defines suggester parameters
 * 
 * @author Jamal Kaabi-Mofrad
 * @since 5.0
 */
public class SuggesterParameters
{
    private final String term;
    private final int limit;
    private final boolean termIsCaseSensitive;

    public SuggesterParameters(String term)
    {
        this(term, -1, false);
    }

    public SuggesterParameters(String term, int limit, boolean termIsCaseSensitive)
    {
        this.term = term;
        this.limit = limit;
        this.termIsCaseSensitive = termIsCaseSensitive;
    }

    /**
     * @return the term
     */
    public String getTerm()
    {
        return this.term;
    }

    /**
     * @return the limit
     */
    public int getLimit()
    {
        return this.limit;
    }

    /**
     * @return the termIsCaseSensitive
     */
    public boolean isTermIsCaseSensitive()
    {
        return this.termIsCaseSensitive;
    }

    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(200);
        builder.append("SuggesterParameters [term=").append(this.term).append(", limit=").append(this.limit)
                    .append(", termIsCaseSensitive=").append(this.termIsCaseSensitive).append("]");
        return builder.toString();
    }
}
