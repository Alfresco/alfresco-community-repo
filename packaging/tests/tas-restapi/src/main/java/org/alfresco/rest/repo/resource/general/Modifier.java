package org.alfresco.rest.repo.resource.general;

import org.alfresco.rest.model.RestCategoryModel;
import org.alfresco.utility.model.ContentModel;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.RepoTestModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestModel;
import org.alfresco.utility.model.UserModel;

/**
 * Declares operations, which can be performed on repository resource.
 *
 * @param <RESOURCE> repository resource, e.g. folder, category, etc.
 * @param <SELF> return type - this interface extension or implementation
 */
public interface Modifier<RESOURCE extends TestModel, SELF extends Modifier<RESOURCE, ?>>
{
    RESOURCE get(String id);

    <USER extends UserModel> SELF asUser(USER user);

    /** Allows to specify fields, which should be included in response */
    SELF include(String... includes);

    void delete();

    interface ContentModifier<CONTENT extends ContentModel, SELF extends Modifier<CONTENT, ?>>
        extends Modifier<CONTENT, SELF>
    {
        <FOLDER extends FolderModel> void moveTo(FOLDER target);

        <FOLDER extends FolderModel> CONTENT copyTo(FOLDER target);

        <FOLDER extends FolderModel> void linkTo(FOLDER secondaryParent);

        <FOLDER extends FolderModel> void linkTo(FOLDER... secondaryParents);

        <FOLDER extends FolderModel> void unlinkFrom(FOLDER secondaryParent);

        <CATEGORY extends RestCategoryModel> void linkTo(CATEGORY category);

        <CATEGORY extends RestCategoryModel> void linkTo(CATEGORY... categories);

        <CATEGORY extends RestCategoryModel> void unlinkFrom(CATEGORY category);

        <SITE extends SiteModel> SELF withinSite(SITE site);
    }

    interface FolderModifier extends ContentModifier<FolderModel, FolderModifier>,
        ResourceIntroducer<Specifier.MultiContentSpecifier>, ResourceRemover<Specifier.AssociationSpecifier>
    {}

    interface FileModifier extends ContentModifier<FileModel, FileModifier>
    {}

    interface CategoryModifier extends Modifier<RestCategoryModel, CategoryModifier>,
        ResourceIntroducer<Specifier.CategoriesSpecifier>
    {}
}
