{{/*
Create a default fully qualified name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
*/}}
{{- define "content-services.fullname" -}}
{{- $name := default .Chart.Name .Values.nameOverride -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Get the Database hostname depending on the Database type
*/}}
{{- define "database.hostname" -}}
{{- if eq ( .Values.database.type | toString ) "postgresql" }}
{{- printf "%s-%s" .Release.Name .Values.postgresql.nameOverride -}}
{{- end }}
{{- end -}}

{{/*
Get the Database port depending on the Database type
*/}}
{{- define "database.port" -}}
{{- if eq ( .Values.database.type | toString ) "postgresql" }}
{{- print .Values.postgresql.service.port -}}
{{- end }}
{{- end -}}

{{/*
Create the Database driver depending on the Database type
*/}}
{{- define "database.driver" -}}
{{- if eq ( .Values.database.type | toString ) "postgresql" }}
{{- print .Values.postgresql.driver -}}
{{- end }}
{{- end -}}

{{/*
Get the Database user depending on the Database type
*/}}
{{- define "database.user" -}}
{{- if eq ( .Values.database.type | toString ) "postgresql" }}
{{- print .Values.postgresql.postgresUser -}}
{{- end }}
{{- end -}}

{{/*
Get the Database password depending on the Database type
*/}}
{{- define "database.password" }}
{{- if eq ( .Values.database.type | toString ) "postgresql" -}}
{{- print .Values.postgresql.postgresPassword -}}
{{- end }}
{{- end -}}
