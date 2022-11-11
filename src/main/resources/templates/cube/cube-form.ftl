[#ftl]
<form class="hidden" id="pivot">
    [#list rowParams as r]
    <div class="form-group">
        <label for="row_${r_index}">Rivi</label>
        <input type="text" id="row_${r_index}" name="row" class="row-selection form-control" value="${r}" readonly>
    </div>
    [/#list]
    [#list colParams as c]
    <div class="form-group">
        <label for="column_${c_index}">Sarake</label>
        <input type="text" id="column_${c_index}" name="column" class="column-selection form-control"  value="${c}" readonly>
    </div>
    [/#list]

    [#list filters as d]
        [#if isDefaultMeasureUsed && d.dimension.id == "measure"]
            [#--
                We do not want to use the default dimension as a filter
                parameter as then user is more likely to make an error while
                modifying the cube.
            --]
        [#else]
        <div class="form-group">
            <label for="filter-${d.dimension.id}-${d_index}">[@label d.dimension /]</label>
            <input type="text" name="filter" id="filter-${d.dimension.id}-${d_index}" class="form-control filter-${d.dimension.id}" value="${d.dimension.id}${dimensionSeparator}${d.surrogateId}" readonly>
        </div>
        [/#if]
     [/#list]

     <input type="hidden" name="fo" value="1" [#if RequestParameters.fo??][#else]disabled[/#if]>
     <input type="hidden" name="fz" value="1" [#if RequestParameters.fz??][#else]disabled[/#if]>
     <input type="hidden" name="sc" value="1" [#if RequestParameters.sc??][#else]disabled[/#if]>

     <input type="hidden" name="sort" disabled>
     <input type="hidden" name="mode" disabled>

     <button type="submit" class="btn btn-primary">P&auml;ivit&auml;</button>
</form>
