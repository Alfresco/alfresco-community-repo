{{/*
Create a default fully qualified name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
*/}}
{{- define "content-services.fullname" -}}
{{- $name := default .Chart.Name .Values.nameOverride -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "database.hostname" -}}
{{- $dbtype := ( .Values.database.type | toString ) -}}
{{- if eq $dbtype "postgresql" }}
{{- printf "%s-%s" .Release.Name .Values.postgresql.nameOverride | trim -}}
{{- end }}
{{- end -}}

{{- define "database.port" -}}
{{- $dbtype := ( .Values.database.type | toString ) -}}
{{- if eq $dbtype "postgresql" }}
{{- print .Values.postgresql.service.port | trim -}}
{{- end }}
{{- end -}}

{{- define "database.driver" -}}
{{- $dbtype := ( .Values.database.type | toString ) -}}
{{- if eq $dbtype "postgresql" }}
{{- print .Values.postgresql.driver | trim -}}
{{- end }}
{{- end -}}

{{- define "database.user" -}}
{{- $dbtype := ( .Values.database.type | toString ) -}}
{{- if eq $dbtype "postgresql" }}
{{- print .Values.postgresql.postgresUser | trim -}}
{{- end }}
{{- end -}}

{{- define "database.password" }}
{{- $dbtype := ( .Values.database.type | toString ) -}}
{{- if eq $dbtype "postgresql" -}}
{{- print .Values.postgresql.postgresPassword | trim -}}
{{- end }}
{{- end -}}
