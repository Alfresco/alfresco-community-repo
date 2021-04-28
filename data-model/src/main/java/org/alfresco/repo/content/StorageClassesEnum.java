package org.alfresco.repo.content;

/**
 * @author Lucian Tuca
 */
public enum StorageClassesEnum
{
    STANDARD("standard"),
    ARCHIVE("archive"),
    WORM("worm");

    private final String label;

    StorageClassesEnum(String label)
    {
        this.label = label;
    }

    public String getLabel()
    {
        return label;
    }
}

