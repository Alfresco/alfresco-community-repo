package org.alfresco.rest.repo.resource.general;

import java.util.List;
import java.util.random.RandomGenerator;

import org.alfresco.rest.model.RestCategoryModel;
import org.alfresco.utility.model.ContentModel;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.RepoTestModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestModel;
import org.alfresco.utility.model.UserModel;

/**
 * Declares actions, which can be performed to create multiple repository resources.
 *
 * @param <RESOURCE> repository resource, e.g. folder, category, etc.
 * @param <SELF> return type - this interface extension or implementation
 */
@SuppressWarnings({"PMD.GenericsNaming"})
public interface MultiCreator<RESOURCE extends TestModel, SELF extends MultiCreator<RESOURCE, ?>>
{
    SELF withNames(String... names);

    <USER extends UserModel> SELF asUser(USER user);

    /** Allows to specify fields, which should be included in response */
    SELF include(String... includes);

    List<RESOURCE> create();

    interface ContentsCreator<MODEL extends ContentModel, SELF extends ContentsCreator<MODEL, ?>> extends MultiCreator<MODEL, SELF>
    {
        SELF withTitles(String... titles);

        SELF withRandomTitles();

        SELF withDescriptions(String... descriptions);

        SELF withRandomDescriptions();

        <FOLDER extends FolderModel> SELF underFolder(FOLDER parent);

        <SITE extends SiteModel> SELF withinSite(SITE site);
    }

    interface FoldersCreator extends ContentsCreator<FolderModel, FoldersCreator>
    {}

    interface FilesCreator extends ContentsCreator<FileModel, FilesCreator>
    {
        FilesCreator withContents(List<String> filesContents);

        default FilesCreator withRandomContents()
        {
            return withRandomContents(RandomGenerator.getDefault().nextInt(30, 70), 10);
        }

        FilesCreator withRandomContents(int wordsCount, int wordsMaxLength);
    }

    interface CategoriesCreator extends MultiCreator<RestCategoryModel, CategoriesCreator>
    {
        <CATEGORY extends RestCategoryModel> CategoriesCreator underCategory(CATEGORY parent);
    }
}
