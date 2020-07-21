function notifyIntranetGroup(content) {
var mail = actions.create("mail");
mail.parameters.to = "wasa@alfresco.com";
mail.parameters.subject = "[INFO][NEW][NEWS] " + content.properties["cm:name"];
mail.parameters.text = "Een nieuw nieuwsbericht werd toegevoegd.";
var notificationTemplate = search.luceneSearch("@cm\\:name:'notify_new_document_email.ftl'");
if(notificationTemplate.length >= 1) { 
if(notificationTemplate[0].name == "notify_new_document_email.ftl")
{ mail.parameters.template = notificationTemplate[0]; }
}
mail.execute(content);
}

var content = search.luceneSearch("@cm\\:name:'cronjob_test.txt'")[0];
notifyIntranetGroup(content);