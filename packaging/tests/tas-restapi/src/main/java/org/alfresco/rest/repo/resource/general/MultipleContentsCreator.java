package org.alfresco.rest.repo.resource.general;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.alfresco.utility.model.ContentModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.UserModel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;

@SuppressWarnings({"PMD.GenericsNaming"})
public abstract class MultipleContentsCreator<CONTENT extends ContentModel, SELF extends MultiCreator.ContentsCreator<CONTENT, ?>>
    extends MultipleResourcesCreator<CONTENT, SELF>
    implements MultiCreator.ContentsCreator<CONTENT, SELF>
{

    protected FolderModel parent;
    protected SiteModel site;
    protected UserModel user;
    protected List<String> titles = new ArrayList<>();
    protected List<String> descriptions = new ArrayList<>();

    public SELF withTitles(String... titles)
    {
        this.titles = Stream.of(titles).collect(Collectors.toList());
        return self();
    }

    public SELF withRandomTitles()
    {
        this.titles = IntStream.range(0, names.size()).mapToObj(i -> RandomStringUtils.randomAlphanumeric(10)).collect(Collectors.toList());
        return self();
    }

    public SELF withDescriptions(String... descriptions)
    {
        this.descriptions = Stream.of(descriptions).collect(Collectors.toList());
        return self();
    }

    public SELF withRandomDescriptions()
    {
        this.descriptions = IntStream.range(0, names.size()).mapToObj(i -> RandomStringUtils.randomAlphanumeric(20)).collect(Collectors.toList());
        return self();
    }

    public <FOLDER extends FolderModel> SELF underFolder(FOLDER parent)
    {
        this.parent = parent;
        return self();
    }

    public <SITE extends SiteModel> SELF withinSite(SITE site)
    {
        this.site = site;
        return self();
    }

    protected void verifyDataConsistency()
    {
        if (CollectionUtils.isEmpty(names))
        {
            throw new IllegalArgumentException("Names of files/folders to create needs to be provided");
        }

        if (CollectionUtils.isNotEmpty(titles) && titles.size() < names.size())
        {
            throw new IllegalArgumentException("Provided titles size is different from created files/folders amount");
        }

        if (CollectionUtils.isNotEmpty(descriptions) && descriptions.size() < names.size())
        {
            throw new IllegalArgumentException("Provided descriptions size is different from created files/folders amount");
        }
    }
}
