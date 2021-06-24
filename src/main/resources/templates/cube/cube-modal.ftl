[#ftl]

<div class="modal fade" id="subset-selector">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h4 class="modal-title">${message("cube.dimension.customize.title")}: <span class="dimension"></span></h4>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"><span aria-hidden="true"></span></button>
      </div>
      <div class="modal-body">

        <div class="container">

        <div class="col-xs-6 selectable">

            <h5>${message("cube.dimension.customize.selectable")}</h5>
            <div class="form-group search">
                <span class="glyphicon glyphicon-search"></span>
                <input class="form-control"></input>

            </div>

             <div class="form-group">
                <div class="form-control options">

                </div>
            </div>
            <button type="button" class="btn btn-secondary">${message("cube.dimension.customize.select-all")}</button>
        </div>


        <div class="col-xs-6 selected">
            <h5>${message("cube.dimension.customize.selected")}</h5>

           <div class="form-group search">
                <span class="glyphicon glyphicon-search"></span>
                <input class="form-control"></input>

            </div>

             <div class="form-group">
                <div class="form-control options">

                </div>
            </div>
            <button type="button" class="btn btn-secondary">${message("cube.dimension.customize.remove-all")}</button>

        </div>
      </div>

      </div>

      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">${message("site.modal.close")}</button>
        <button type="button" class="btn btn-primary save">${message("site.modal.apply")}</button>
      </div>
    </div><!-- /.modal-content -->
  </div><!-- /.modal-dialog -->
</div><!-- /.modal -->
