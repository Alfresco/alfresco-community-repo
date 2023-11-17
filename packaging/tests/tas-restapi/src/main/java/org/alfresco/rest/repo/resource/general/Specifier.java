package org.alfresco.rest.repo.resource.general;

import org.alfresco.rest.model.RestTagModel;
import org.alfresco.utility.model.ContentModel;

/**
 * Specifies repository resource to perform an operation on like: add, modify, remove.
 */
public interface Specifier
{
    interface FolderSpecifier extends Specifier
    {
        Creator.FolderCreator folder(String name);

        Creator.FolderCreator randomFolder();

        Creator.FolderCreator randomFolder(String nameSuffix);

        MultiCreator.FoldersCreator folders(String... names);

        MultiCreator.FoldersCreator randomFolders(String... nameSuffixes);

        MultiCreator.FoldersCreator randomFolders(int quantity);

        MultiCreator.FoldersCreator nestedFolders(String... names);

        MultiCreator.FoldersCreator nestedRandomFolders(String... nameSuffixes);

        MultiCreator.FoldersCreator nestedRandomFolders(int depth);
    }

    interface FileSpecifier extends Specifier
    {
        Creator.FileCreator file(String name);

        Creator.FileCreator randomFile();

        Creator.FileCreator randomFile(String nameSuffix);

        MultiCreator.FilesCreator files(String... names);

        MultiCreator.FilesCreator randomFiles(String... nameSuffixes);

        MultiCreator.FilesCreator randomFiles(int quantity);
    }

    interface AssociationSpecifier extends Specifier
    {
        <CONTENT extends ContentModel> void secondaryContent(CONTENT content);

        <TAG extends RestTagModel> void tag(TAG tag);
    }

    interface MultiContentSpecifier extends FolderSpecifier, FileSpecifier, AssociationSpecifier
    {
        <CONTENT extends ContentModel> void secondaryContent(CONTENT... contents);

        <TAG extends RestTagModel> void tags(TAG... tags);
    }

    interface CategoriesSpecifier extends Specifier
    {
        Creator.CategoryCreator category(String name);

        Creator.CategoryCreator randomCategory();

        Creator.CategoryCreator randomCategory(String nameSuffix);

        MultiCreator.CategoriesCreator categories(String... names);

        MultiCreator.CategoriesCreator randomCategories(String... nameSuffixes);

        MultiCreator.CategoriesCreator randomCategories(int quantity);

        MultiCreator.CategoriesCreator nestedCategories(String... names);

        MultiCreator.CategoriesCreator nestedRandomCategories(String... nameSuffixes);

        MultiCreator.CategoriesCreator nestedRandomCategories(int depth);
    }
}
