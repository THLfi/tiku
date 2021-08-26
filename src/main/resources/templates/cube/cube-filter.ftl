[#ftl]
[#if pivot.columns?size > 0 && pivot.rows?size > 0]
<div class="filter-control">

    [#if filters?size > 0 && (!filters[0].dimension.isMeasure() || multipleMeasuresShown)]
    <h4>${message("cube.filter.selected")}</h4>
    [/#if]
    <dl>
        [#list dimensions as d]
             [#assign idx = 0 /]
             [#list filters as f]
                    [#if f.dimension.isMeasure() && !multipleMeasuresShown]

                    [#elseif f.dimension.id == d.id]
                        <dt>[@label d /]</dt>
                        <dd><span class="label label-default" data-ref="${d.id}" data-index="${idx}">[@label f /] [#if RequestParameters.sc?? && f.code??](${f.code})[/#if]<span class="fas fa-times"></span></span></dd>
                        [#assign idx = idx + 1 /]
                    [/#if]
            [/#list]
        [/#list]

    </dl>

    <div class="btn-group filter-target" role="group" aria-label="...">
        <span>${message("cube.filter")}</span>
    </div>

    <p />
</div>
[/#if]
