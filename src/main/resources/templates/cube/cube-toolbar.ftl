[#ftl]
<div id="toolbar-left">
<div class="btn-group" role="group">
    <div class="dropdown options-dropdown" title="${message("cube.dimension.column-selections")}">
        <button class="btn btn-secondary hide-btn-focus" type="button" id="dropdownMenuButton1" data-bs-toggle="dropdown" aria-expanded="false" aria-label="..." title="${message('cube.dimension.column-selections')}">${message("cube.options")}<span class="caret"></span></button>
        <ul title="${message("cube.dimension.column-selections")}" class="dropdown-menu" aria-labelledby="dropdownMenuButton1">
            <li class="reset-action" role="presentation"><a href="#" class="dropdown-item" role="menuitem"><span class="fas fa-sync"></span> ${message("cube.reset")}</a></li>
            <li class="transpose-action" role="presentation"><a href="#" class="dropdown-item" role="menuitem"><span class="fas fa-retweet"></span> ${message("cube.transpose")}</a></li>
            <li class="hide-zero-action" role="presentation"><a href="#" class="dropdown-item" role="menuitem">[#if RequestParameters.fz??]${message("cube.filter-zero.off")}[#else]${message("cube.filter-zero.on")}[/#if]</a></li>
            <li class="hide-empty-action" [#if RequestParameters.fz??]disabled[/#if] role="presentation"><a href="#" class="dropdown-item" role="menuitem">[#if RequestParameters.fo??]${message("cube.filter-empty.off")}[#else]${message("cube.filter-empty.on")}[/#if]</a></li>
            <li class="show-codes-action" role="presentation"><a href="#" class="dropdown-item" role="menuitem">[#if RequestParameters.sc??]${message("cube.codes.off")}[#else]${message("cube.codes.on")}[/#if]</a></li>

            [#if views?size > 0]
                <li><hr class="dropdown-divider"></li>
                [#list views as view]
                    <li role="presentation"><a class="dropdown-item" role="menuitem" href="?${view.url}">${view.label.getValue(lang)!"n/a"}</a></li>
                [/#list]
            [/#if]
        </ul>
    </div>

    <div class="dropdown export">
        <button class="btn btn-secondary hide-btn-focus" type="button" id="dropdownMenuButton2" data-bs-toggle="dropdown" aria-expanded="false" aria-label="...">
            <i class="fa fa-download"></i>
            <span class="hide-xs">${message("cube.export")}</span>
        </button>
        <ul class="dropdown-menu" aria-labelledby="dropdownMenuButton2" role="menu">
            [#assign reqParameters][#compress]
                [#list rowParams as r]row=${r}&amp;[/#list]
                [#list colParams as r]column=${r}&amp;[/#list]
                [#list filters as r]filter=${r.dimension.id}-${r.surrogateId}&amp;[/#list]
                [#if RequestParameters.fo??]&amp;fo=1[/#if]
                [#if RequestParameters.fz??]&amp;fz=1[/#if]
                [#if RequestParameters.sc??]&amp;sc=1[/#if]
                [#if RequestParameters.sort??]&amp;sort=${RequestParameters.sort}[/#if]
                [#if RequestParameters.mode??]&amp;mode=${RequestParameters.mode}[/#if]

            [/#compress][/#assign]
            [#assign reqParameters=reqParameters?replace("\\n|\\r", "", "rm") /]

            <li class="csv-action" role="presentation">
                <a class="dropdown-item" href="${target}.csv?${reqParameters}">
                    <span class="fas fa-file"></span>
                    ${message("cube.export.csv")}
                </a>
            </li>

            <li class="xlsx-action" role="presentation">
                <a class="dropdown-item" href="${target}.xlsx?${reqParameters}">
                    <span class="fas fa-file"></span>
                    ${message("cube.export.xlsx")}
                </a>
            </li>

            <li class="pdf-action" role="presentation">
                <a class="dropdown-item" href="${target}.pdf?${reqParameters}">
                    <span class="fas fa-file"></span>
                    ${message("cube.export.pdf")}
                </a>
            </li>
        </ul>
    </div>

    [#if backUrl??]
    <a class="lnk-btn" href="${rc.contextPath}/${backUrl}">
        <i class="fas fa-th"></i>
        <span class="hide-xs">${message("site.changeMaterial")}</span>
    </a>
    [/#if]

    [#if metaLink??]
      <a class="lnk-btn" target="_blank" href="${metaLink.getValue(lang)}">
        <span class="fas fa-info-circle"></span>
        <span class="hide-xs">${message("summary.more")}</span>
      </a>
    [/#if]

    <a class="lnk-btn" target="_blank" href="${message('site.help.url')}">
      <span class="fas fa-question-circle"></span>
      <span class="hide-xs">${message("site.help")}</span>
    </a>

    [#if requireLogin]
    <div class="dropdown" role="group" aria-label="...">
        <form class="form" method="POST" action="${target}/logout">
            <button type="submit" class="btn btn-secondary">
                <span class="fas fa-sign-out-alt"></span>
                <span class="hide-xs">${message("site.logout")}</span>
            </button>
        </form>
    </div>
    [/#if]

</div>
</div>