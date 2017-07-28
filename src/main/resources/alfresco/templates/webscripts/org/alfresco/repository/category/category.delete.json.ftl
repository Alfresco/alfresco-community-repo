{
    <#if redirect??>"redirect": "${redirect}",</#if>
    <#if persistedObject??>"persistedObject": "${persistedObject?replace("\t", "")?string}",</#if>
    <#if name??>"name": "${name}",</#if>
    "message": "${msg(messageKey)}"
}