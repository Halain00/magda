# web-server

![Version: 0.0.58-alpha.0](https://img.shields.io/badge/Version-0.0.58-alpha.0-informational?style=flat-square)

A Helm chart for Kubernetes

## Requirements

Kubernetes: `>= 1.14.0-0`

## Values

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| autoscaler.enabled | bool | `false` |  |
| autoscaler.maxReplicas | int | `3` |  |
| autoscaler.minReplicas | int | `1` |  |
| autoscaler.targetCPUUtilizationPercentage | int | `80` |  |
| contentApiBaseUrlInternal | string | `"http://content-api/v0/"` |  |
| csvLoaderChunkSize | int | `2097152` |  |
| custodianOrgLevel | int | `2` |  |
| datasetThemes | list | `[]` |  |
| dateConfig.dateFormats[0] | string | `"YYYY"` |  |
| dateConfig.dateFormats[1] | string | `"YYYY-MM"` |  |
| dateConfig.dateFormats[2] | string | `"DD-MM-YYYY"` |  |
| dateConfig.dateFormats[3] | string | `"MM-DD-YYYY"` |  |
| dateConfig.dateFormats[4] | string | `"YYYY-MM-DD"` |  |
| dateConfig.dateFormats[5] | string | `"YYYY-MM-DDThh:mmTZD"` |  |
| dateConfig.dateFormats[6] | string | `"YYYY-MM-DDThh:mm:ssTZD"` |  |
| dateConfig.dateFormats[7] | string | `"YYYY-MM-DDThh:mm:ss.sTZD"` |  |
| dateConfig.dateFormats[8] | string | `"DD-MMM-YYYY"` |  |
| dateConfig.dateFormats[9] | string | `"MMM-DD-YYYY"` |  |
| dateConfig.dateRegexes.dateRegex | string | `"(date|dt|year|decade)"` |  |
| dateConfig.dateRegexes.endDateRegex | string | `"(end).*(date|dt|year|decade)"` |  |
| dateConfig.dateRegexes.startDateRegex | string | `"(start|st).*(date|dt|year|decade)"` |  |
| defaultContactEmail | string | `"mail@example.com"` |  |
| disableAuthenticationFeatures | bool | `false` |  |
| featureFlags.cataloguing | bool | `false` | turn on / off metadata creation tool.  If this option is `false`, user won't be able to access the dataset add / edit UI  |
| featureFlags.datasetApprovalWorkflowOn | bool | `true` | turn on / off dataset approval note step |
| featureFlags.placeholderWorkflowsOn | bool | `false` | turn on / off some metadata creation tool questions that are still under development |
| featureFlags.previewAddDataset | bool | `false` | turn on / off the preview mode of metadata creation tool. Under preview mode, user can play with the metadata creation tool without requiring admin permission. No data will be saved under this mode. |
| featureFlags.publishToDga | bool | `false` | turn on / off the UI switch that allow user to select whether to auto push dataset data to a CKAN instance |
| featureFlags.useStorageApi | bool | `true` | turn on / off the UI option to use Magda internal storage for file storage. |
| image | object | `{}` |  |
| keywordsBlackList[0] | string | `"Mr"` |  |
| keywordsBlackList[10] | string | `"Mr."` |  |
| keywordsBlackList[11] | string | `"Ms."` |  |
| keywordsBlackList[12] | string | `"Mrs."` |  |
| keywordsBlackList[13] | string | `"Miss."` |  |
| keywordsBlackList[14] | string | `"Dr."` |  |
| keywordsBlackList[15] | string | `"Hon."` |  |
| keywordsBlackList[16] | string | `"Jr."` |  |
| keywordsBlackList[17] | string | `"Prof."` |  |
| keywordsBlackList[18] | string | `"Sr."` |  |
| keywordsBlackList[19] | string | `"St."` |  |
| keywordsBlackList[1] | string | `"Ms"` |  |
| keywordsBlackList[2] | string | `"Mrs"` |  |
| keywordsBlackList[3] | string | `"Miss"` |  |
| keywordsBlackList[4] | string | `"Dr"` |  |
| keywordsBlackList[5] | string | `"Hon"` |  |
| keywordsBlackList[6] | string | `"Jr"` |  |
| keywordsBlackList[7] | string | `"Prof"` |  |
| keywordsBlackList[8] | string | `"Sr"` |  |
| keywordsBlackList[9] | string | `"St"` |  |
| mandatoryFields[0] | string | `"dataset.title"` |  |
| mandatoryFields[10] | string | `"informationSecurity.disseminationLimits"` |  |
| mandatoryFields[11] | string | `"publishToDga"` |  |
| mandatoryFields[1] | string | `"dataset.description"` |  |
| mandatoryFields[2] | string | `"dataset.defaultLicense"` |  |
| mandatoryFields[3] | string | `"distributions.title"` |  |
| mandatoryFields[4] | string | `"distributions.format"` |  |
| mandatoryFields[5] | string | `"distributions.license"` |  |
| mandatoryFields[6] | string | `"dataset.publisher"` |  |
| mandatoryFields[7] | string | `"licenseLevel"` |  |
| mandatoryFields[8] | string | `"dataset.defaultLicense"` |  |
| mandatoryFields[9] | string | `"informationSecurity.classification"` |  |
| maxChartProcessingRows | int | `20000` |  |
| maxTableProcessingRows | int | `200` |  |
| noManualKeywords | bool | `false` |  |
| noManualThemes | bool | `false` |  |
| resources.limits.cpu | string | `"100m"` |  |
| resources.requests.cpu | string | `"10m"` |  |
| resources.requests.memory | string | `"30Mi"` |  |
| service | object | `{}` |  |
| showNotificationBanner | bool | `false` |  |
| useLocalStyleSheet | bool | `false` |  |
| vocabularyApiEndpoints | list | `[]` |  |