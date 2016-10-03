[#ftl]
<div>

    <div class="btn-group dropdown" role="group" aria-label="...">

      <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-expanded="false">${message("cube.options")} <span class="caret"></span></button>

      <ul class="dropdown-menu" role="menu">

      <li class="reset-action" role="presentation"><a role="menuitem"><span class="glyphicon glyphicon-refresh"></span> ${message("cube.reset")}</a></li>
      <li class="transpose-action" role="presentation"><a role="menuitem"><span class="glyphicon glyphicon-resize-full"></span> ${message("cube.transpose")}</a></li>
      <li class="hide-zero-action" role="presentation"><a role="menuitem"><span class="glyphicon"></span>  [#if RequestParameters.fz??]${message("cube.filter-zero.off")}[#else]${message("cube.filter-zero.on")}[/#if]</a></li>
      <li class="hide-empty-action" [#if RequestParameters.fz??]disabled[/#if] role="presentation"><a role="menuitem"><span class="glyphicon"></span> [#if RequestParameters.fo??]${message("cube.filter-empty.off")}[#else]${message("cube.filter-empty.on")}[/#if]</a></li>
      <li class="show-codes-action" role="presentation"><a role="menuitem"><span class="glyphicon"></span> [#if RequestParameters.sc??]${message("cube.codes.off")}[#else]${message("cube.codes.on")}[/#if]</a></li>

      </ul>

    </div>
    <div class="btn-group dropdown" role="group" aria-label="...">

      <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-expanded="false">${message("cube.export")} <span class="caret"></span></button>

      <ul class="dropdown-menu" role="menu">
      [#assign reqParameters][#compress]
        [#list rowParams as r]row=${r}&[/#list]
        [#list colParams as r]column=${r}&[/#list]
        [#list filters as r]filter=${r.dimension.id}-${r.surrogateId}&[/#list]
        [#if RequestParameters.fo??]&fo=1[/#if]
        [#if RequestParameters.fz??]&fz=1[/#if]
        [#if RequestParameters.sc??]&sc=1[/#if]
        [#if RequestParameters.sort??]&sort=${RequestParameters.sort}[/#if]
        [#if RequestParameters.mode??]&mode=${RequestParameters.mode}[/#if]

        [/#compress][/#assign]
        [#assign reqParameters=reqParameters?replace("\\n|\\r", "", "rm") /]

        <li class="csv-action" role="presentation">
          <a role="menuitem" href="${target}.csv?${reqParameters}">
            <span class="glyphicon glyphicon-file"></span>
            ${message("cube.export.csv")}
          </a>
        </li>
        <li class="xlsx-action" role="presentation">
          <a role="menuitem" href="${target}.xlsx?${reqParameters}">
            <span class="glyphicon glyphicon-file"></span>
            ${message("cube.export.xlsx")}
          </a>
        </li>
        <li class="pdf-action" role="presentation">
          <a role="menuitem" href="${target}.pdf?${reqParameters}">
          <span class="glyphicon glyphicon-file"></span>
          ${message("cube.export.pdf")}
          </a>
        </li>

      </ul>

    </div>
    [#if metaLink??]
    <div class="btn-group" role="group" aria-label="...">
      <a class="btn btn-default" href="${metaLink.getValue(lang)}">${message("summary.more")}</a>
    </div>
    [/#if]

    <div class="btn-group" role="group" aria-label="...">
        <a href="#" type="submit" class="btn btn-default">${message("site.help")}</a>
    </div>
    [#if requireLogin]
     <div class="btn-group dropdown" role="group" aria-label="...">
        <form class="form" method="POST" action="${target}/logout">
            <button type="submit" class="btn btn-default">${message("site.logout")}</button>
        </form>
    </div>
    [/#if]
</div>
</div>
