{
    <#if redirect??>"redirect": "${redirect}",</#if>
    <#if persistedObject??>"persistedObject": "${persistedObject.nodeRef?replace("\t", "")?string}",</#if>
    "message": "${msg(messageKey)}",
    "name": "${name}"
}