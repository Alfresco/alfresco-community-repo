<#macro transactionJSON txn>
{
    "id": "${txn.id?c}",
    "commitTimeMs": "${txn.commitTimeMs?c}",
    "updates": "${txn.updates?c}",
    "deletes": "${txn.deletes?c}"
}
</#macro>

<#macro nodeJSON node>
{
    "nodeID": "${node.id?c}",
    "txnID": "${node.transaction.id?c}",
    "deleted": "${node.deleted?string}"
}
</#macro>