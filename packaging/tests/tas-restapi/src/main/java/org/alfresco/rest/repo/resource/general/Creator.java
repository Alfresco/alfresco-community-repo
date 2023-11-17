package org.alfresco.rest.repo.resource.general;

import java.util.random.RandomGenerator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.alfresco.rest.model.RestCategoryModel;
import org.alfresco.utility.model.ContentModel;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.RepoTestModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestModel;
import org.alfresco.utility.model.UserModel;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * Declares operations, which can be performed to create repository resource.
 *
 * @param <RESOURCE> repository resource, e.g. folder, category, etc.
 * @param <SELF> return type - this interface extension or implementation
 */
@SuppressWarnings({"PMD.GenericsNaming"})
public interface Creator<RESOURCE extends TestModel, SELF extends Creator<RESOURCE, ?>>
{
    SELF withName(String name);

    <USER extends UserModel> SELF asUser(USER user);

    /** Allows to specify fields, which should be included in response */
    SELF include(String... includes);

    RESOURCE create();

    interface ContentCreator<CONTENT extends ContentModel, SELF extends ContentCreator<CONTENT, ?>>
        extends Creator<CONTENT, SELF>
    {
        SELF withTitle(String title);

        default SELF withRandomTitle()
        {
            return withTitle(RandomStringUtils.randomAlphanumeric(10));
        }

        SELF withDescription(String description);

        default SELF withRandomDescription()
        {
            return withDescription(RandomStringUtils.randomAlphanumeric(20));
        }

        <FOLDER extends FolderModel> SELF underFolder(FOLDER parent);

        <SITE extends SiteModel> SELF withinSite(SITE site);
    }

    interface FolderCreator extends ContentCreator<FolderModel, FolderCreator>
    {}

    interface FileCreator extends ContentCreator<FileModel, FileCreator>
    {
        FileCreator withContent(String fileContent);

        default FileCreator withRandomContent()
        {
            return withRandomContent(RandomGenerator.getDefault().nextInt(30, 70), 10);
        }

        default FileCreator withRandomContent(int wordsNumber, int wordsMaxLength)
        {
            return withContent(IntStream.range(0, wordsNumber)
                .mapToObj(i -> RandomStringUtils.randomAlphanumeric(1, wordsMaxLength))
                .collect(Collectors.joining(" ")));
        }
    }

    interface CategoryCreator extends Creator<RestCategoryModel, CategoryCreator>
    {
        <CATEGORY extends RestCategoryModel> CategoryCreator underCategory(CATEGORY parent);
    }
}
