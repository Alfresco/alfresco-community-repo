
package org.alfresco.repo.web.scripts.search;

/**
 * Basic POJO to represent a term suggestion.
 * 
 * @author Jamal Kaabi-Mofrad
 * @since 5.0
 */
public class SearchSuggestionData
{
    private final String term;
    private final int weight;

    public SearchSuggestionData(String term, int weight)
    {
        this.term = term;
        this.weight = weight;
    }

    public String getTerm()
    {
        return this.term;
    }

    public int getWeight()
    {
        return this.weight;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(100);
        builder.append("SearchSuggestionData [term=").append(this.term).append(", weight=").append(this.weight)
                    .append("]");
        return builder.toString();
    }
}
