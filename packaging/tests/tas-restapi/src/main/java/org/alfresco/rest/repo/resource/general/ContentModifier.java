package org.alfresco.rest.repo.resource.general;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.RestCategoryLinkBodyModel;
import org.alfresco.rest.model.RestCategoryModel;
import org.alfresco.rest.model.RestNodeBodyMoveCopyModel;
import org.alfresco.rest.model.RestNodeModel;
import org.alfresco.utility.data.DataContent;
import org.alfresco.utility.model.ContentModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;

public abstract class ContentModifier<CONTENT extends ContentModel, SELF extends Modifier.ContentModifier<CONTENT, ?>>
    extends ResourceModifier<CONTENT, SELF>
    implements Modifier.ContentModifier<CONTENT, SELF>
{
    protected SiteModel site;
    private final DataContent dataContent;
    private final RestWrapper restClient;
    protected final CONTENT contentModel;

    protected ContentModifier(DataContent dataContent, RestWrapper restClient, CONTENT contentModel)
    {
        this.dataContent = dataContent;
        this.restClient = restClient;
        this.contentModel = contentModel;
    }

    public <SITE extends SiteModel> SELF withinSite(SITE site)
    {
        this.site = site;
        return self();
    }

    protected CONTENT get(String id, Supplier<CONTENT> contentSupplier)
    {
        ContentModel getNodeModel = new ContentModel();
        getNodeModel.setNodeRef(id);
        RestNodeModel node = buildNodeRestRequest(restClient, getNodeModel).getNode();

        CONTENT content = contentSupplier.get();
        content.setName(node.getName());
        content.setNodeRef(node.getId());
        content.setCmisLocation(getCmisLocation(node.getPath(), node.getName()));
        return content;
    }

    @Override
    public  <FOLDER extends FolderModel> void moveTo(FOLDER target)
    {
        RestNodeBodyMoveCopyModel moveModel = new RestNodeBodyMoveCopyModel();
        moveModel.setTargetParentId(target.getNodeRef());
        moveModel.setName(contentModel.getName());

        RestNodeModel movedNode = buildNodeRestRequest(restClient, contentModel).includePath().move(moveModel);
        contentModel.setCmisLocation(getCmisLocation(movedNode.getPath(), movedNode.getName()));
    }

    protected CONTENT copyTo(FolderModel target, Function<CONTENT, CONTENT> contentSupplier)
    {
        RestNodeBodyMoveCopyModel copyModel = new RestNodeBodyMoveCopyModel();
        copyModel.setTargetParentId(target.getNodeRef());
        copyModel.setName(contentModel.getName());

        RestNodeModel nodeCopy = buildNodeRestRequest(restClient, contentModel).includePath().copy(copyModel);
        CONTENT contentCopy = contentSupplier.apply(contentModel);
        contentCopy.setName(nodeCopy.getName());
        contentCopy.setNodeRef(nodeCopy.getId());
        contentCopy.setCmisLocation(getCmisLocation(nodeCopy.getPath(), nodeCopy.getName()));
        return contentCopy;
    }

    @Override
    public void delete()
    {
        dataContent.usingUser(user).usingResource(contentModel).deleteContent();
    }

    @Override
    public <FOLDER extends FolderModel> void linkTo(FOLDER secondaryParent)
    {
        buildNodeRestRequest(restClient, secondaryParent).addSecondaryChild(contentModel);
    }

    @Override
    @SafeVarargs
    public final <FOLDER extends FolderModel> void linkTo(FOLDER... secondaryParents)
    {
        Stream.of(secondaryParents).forEach(secondaryParent ->
            buildNodeRestRequest(restClient, secondaryParent).addSecondaryChildren(contentModel)
        );
    }

    @Override
    public <FOLDER extends FolderModel> void unlinkFrom(FOLDER secondaryParent)
    {
        buildNodeRestRequest(restClient, secondaryParent).removeSecondaryChild(contentModel);
    }

    @Override
    public <CATEGORY extends RestCategoryModel> void linkTo(CATEGORY category)
    {
        buildNodeRestRequest(restClient, contentModel).linkToCategory(
            RestCategoryLinkBodyModel.builder().categoryId(category.getId()).create()
        );
    }

    @Override
    @SafeVarargs
    public final <CATEGORY extends RestCategoryModel> void linkTo(CATEGORY... categories)
    {
        buildNodeRestRequest(restClient, contentModel).linkToCategories(
            Stream.of(categories)
                .map(category -> RestCategoryLinkBodyModel.builder().categoryId(category.getId()).create())
                .collect(Collectors.toList())
        );
    }

    @Override
    public <CATEGORY extends RestCategoryModel> void unlinkFrom(CATEGORY category)
    {
        buildNodeRestRequest(restClient, contentModel).unlinkFromCategory(category.getId());
    }

    private static String getCmisLocation(Object pathMap, String name)
    {
        return Stream.concat(
                Stream.of(pathMap)
                    .filter(Objects::nonNull)
                    .filter(path -> path instanceof Map)
                    .map(Map.class::cast)
                    .map(path -> path.get("elements"))
                    .filter(Objects::nonNull)
                    .filter(elements -> elements instanceof List)
                    .map(List.class::cast)
                    .flatMap(elements -> (Stream<?>) elements.stream())
                    .skip(1)
                    .filter(element -> element instanceof Map)
                    .map(Map.class::cast)
                    .map(element -> element.get("name"))
                    .filter(Objects::nonNull)
                    .filter(elementName -> elementName instanceof String)
                    .map(String.class::cast),
                Stream.of(name))
            .collect(Collectors.joining("/", "/", "/"));
    }
}
