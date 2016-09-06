package org.alfresco.rest.api.search.model;

/**
 * POJO class representing the extra information that comes back from Search.
 **/
public class SearchEntry
{
    Float score;

    public SearchEntry(Float score)
    {
        this.score = score;
    }

    public Float getScore()
    {
        return score;
    }

    //In future highlighting.
}
