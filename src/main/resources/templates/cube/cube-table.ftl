[#ftl]

        <table id="to-main-content" class="cube table table-hover table-condensed table-bordered">
[#list pivot.columns as ch]
   <tr class="sticky-header">
        [#if ch_index == 0]
        <th colspan="${pivot.rows?size}" rowspan="${pivot.columns?size}">
            [#if !multipleMeasuresShown]
                  [#list filters as f]
                    [#if f.dimension.isMeasure()]
                        [@label f /]
                        <a id="measure-meta" href="${rc.contextPath}/${env}/${lang}/${subject}/${hydra}/${cubeId}/${f.surrogateId}"> <span class="sr-only">title="${message('cube.dimension.info.sr')}"</span><span class="fas fa-info-circle fa-info-circle-table" title="${message('cube.dimension.info')}"></span></a>
                    [/#if]
                  [/#list]
            [/#if]
        </th>
        [/#if]

        <th>[@dimDropdown "column" ch.selectedNode ch_index pivot.columns?size message("cube.dimension.columntools") /]</th>
        [#assign span = 0 /]
        [#list 0..<pivot.columnCount as c]
            [#if span == 0]
                [#assign span = tableHelper.getColumnSpanAt(ch_index, c) /]
                [#assign hn = pivot.getColumnAt(ch_index, c) /]
                <th colspan="${span}" class="column-target [#if ch_index = pivot.columns?size -1]leaf[/#if]" data-ref="${pivot.getColumnNumber(c)}" data-level="${ch_index}">
                        <a href="#" class="colhdr" id="${hn.dimension.id}${dimensionSeparator}${hn.surrogateId}-${pivot.getColumnNumber(c)}" data-ref="${hn.dimension.id}${dimensionSeparator}${hn.surrogateId}">
                        [@label hn /] [#if RequestParameters.sc?? && hn.code??](${hn.code!})[/#if]
                    </a>
                </th>
            [/#if]
            [#assign span = span - 1 /]
        [/#list]
    </tr>
[/#list]

<tr>
    [#list pivot.rows as rh]
        <th>
            [#if rh.lastNode??]
            [@dimDropdown "row" rh.selectedNode rh_index pivot.rows?size message("cube.dimension.rowtools") /]
            [/#if]
        </th>
    [/#list]
    <th class="empty"></th>
    <th class="column-target accept-all" colspan="${pivot.columnCount}">
        <span class="fas fa-plus glyphicon glyphicon-plus"></span>
    </th>
</tr>
[#assign rowNum = -1]
[#list pivot.iterator() as cell]
    [#if cell.columnNumber = 0]
        [#assign rowNum = rowNum + 1 /]
        [#if cell_index > 0]</tr>[/#if]
        <tr>
            [#list pivot.rows as r]
                [#if rowspan.next(r_index)]
                    [#assign rh = pivot.getRowAt(r_index, rowNum)]
                    [#assign span = rowspan.assign(r_index, tableHelper.getRowSpanAt(r_index, rowNum)) /]
                    <th class="row-target sticky-cell [#if r_index = pivot.rows?size - 1]leaf[/#if]" [#if span > 1]rowspan="${span}"[/#if] data-level="${r_index}" data-ref="${cell.actualRowNumber}">
                        <a href="#" class="rowhdr" id="${rh.dimension.id}${dimensionSeparator}${rh.surrogateId}-${cell.actualRowNumber}" data-ref="${rh.dimension.id}${dimensionSeparator}${rh.surrogateId}">
                            [@label rh /] [#if RequestParameters.sc?? && rh.code??](${rh.code!})[/#if]
                        </a>
                    </th>
                [/#if]
            [/#list]
            [#if cell_index == 0]
            <th class="row-target accept-all" rowspan="${pivot.rowCount}">
                <span class="fas fa-plus glyphicon glyphicon-plus"></span>
            </th>
            [/#if]
    [/#if]
    <td[#if (sortByColumn && sortIndex == cell.columnNumber) || (sortByRow && sortIndex == cell.rowNumber)] class="highlight"[/#if]>${value(cell.i18nValue!)}</td>
[/#list]
</tr>

</table>
