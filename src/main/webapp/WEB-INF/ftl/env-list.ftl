[#ftl]
[#include "common.ftl"]

[#assign breadcrumbs]
    <li class="first active">Environments</li>
[/#assign]
[@amor_page]
<h1>Environments</h1>
<ul>
    <li><a href="${rc.contextPath}/deve">Development</a></li>
    <li><a href="${rc.contextPath}/test">Testing</a></li>
    <li><a href="${rc.contextPath}/beta">Beta</a></li>
    <li><a href="${rc.contextPath}/prod">Production</a></li>
</ul>
[/@amor_page]
