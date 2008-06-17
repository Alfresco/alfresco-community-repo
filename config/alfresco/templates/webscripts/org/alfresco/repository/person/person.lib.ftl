<#macro personJSON person>
{
   "url" : "${url.serviceContext}/api/person/${person.userName}",
   "userName" : "${person.userName}",
   "title" : "${person.title}",
   "firstName" : "${person.firstName}",
   "lastName" : "${person.lastName}",
   "organisation" : "${person.organisation}",
   "jobTitle" : "${person.jobTitle}",
   "email" : "${person.email}",
   "bio" : "${person.bio}",
   "avatarUrl" : "${person.avatarUrl}",
}
</#macro>

<#macro personSummaryJSON person>
{
   "url" : "${url.serviceContext}/api/person/${person.userName}",
   "userName" : "${person.userName}",
   "firstName" : "${person.firstName}",
   "lastName" : "${person.lastName}"
}
</#macro>