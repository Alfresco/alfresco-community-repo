<#macro resultsJSON item>
<#escape x as jsonUtils.encodeJSONString(x)>
{
    "name": "${item.name}",
    "title": "${item.title!""}",
    "mimetype": "${item.mimetype!""}",
    "size": ${item.size?c},
    "modifiedOn": "${xmldate(item.modified)}",
    "modifier":
    {
        "firstName": "${item.modifierFirstName!""}",
        "lastName": "${item.modifierLastName!""}"
    },
    "thumbnailDefinitions":
    [
    <#if item.thumbnailDefinitions??>
    <#list item.thumbnailDefinitions as thumbnailDefinition>
       "${thumbnailDefinition}"
       <#if thumbnailDefinition_has_next>,</#if>
    </#list>
    </#if>
    ],
    "thumbnails":
    [
    <#if item.thumbnailNames??>
    <#list item.thumbnailNames as thumbnailName>
       "${thumbnailName}"
       <#if thumbnailName_has_next>,</#if>
    </#list>
    </#if>
    ],
    "lastThumbnailModificationData":
    [
    <#if item.lastThumbnailModificationData??>
    <#list item.lastThumbnailModificationData as lastThumbnailMod>
       "${lastThumbnailMod}"
       <#if lastThumbnailMod_has_next>,</#if>
    </#list>
    </#if>
    ]
}
</#escape>
</#macro>