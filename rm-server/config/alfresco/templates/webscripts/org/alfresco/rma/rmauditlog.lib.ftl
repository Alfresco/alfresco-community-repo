<#macro auditStatusJSON auditstatus>
<#escape x as jsonUtils.encodeJSONString(x)>
{
    "data":
    {
        "enabled": ${auditstatus.enabled?string},
        "started": "${auditstatus.started}",
        "stopped": "${auditstatus.stopped}"
    }
}
</#escape>
</#macro>
