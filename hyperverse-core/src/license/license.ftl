<#function licenseFormat licenses>
    <#assign result = ""/>
    <#list licenses as license>
        <#assign result = result + "- (" + license + ")"/>
    </#list>
    <#return result>
</#function>
<#function artifactFormat p>
    <#if p.name?index_of('Unnamed') &gt; -1>
        <#return "[" + p.artifactId + " (" + p.groupId + ":" + p.artifactId + ":" + p.version +")](" + (p.url!"") + ")">
    <#else>
        <#return "[" + p.name + " (" + p.groupId + ":" + p.artifactId + ":" + p.version +")](" + (p.url!"") + ")">
    </#if>
</#function>
<#if dependencyMap?size == 0>
    The project has no dependencies.
<#else>
    Lists of ${dependencyMap?size} third-party dependencies.
    <#list dependencyMap as e>
        <#assign project = e.getKey()/>
        <#assign licenses = e.getValue()/>
        ${licenseFormat(licenses)} ${artifactFormat(project)}
    </#list>
</#if>
