<!DOCTYPE html>
<html lang="en" xmlns:th="https://www.thymeleaf.org">

<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Flowiee | System category</title>
    <div th:replace="header :: stylesheets"></div>
    <style rel="stylesheet">
        .table td, th {
            vertical-align: middle;
        }
    </style>
</head>

<body class="hold-transition sidebar-mini layout-fixed">
    <div class="wrapper">
        <div th:replace="header :: header"></div>

        <div th:replace="sidebar :: sidebar"></div>

        <div class="content-wrapper" style="padding-top: 10px; padding-bottom: 1px;">
            <section class="content">
                <div class="container-fluid">
                    <div class="row">
                        <div class="col-12">
                            <!--Search tool-->
                            <div th:replace="fragments :: searchTool('Y')" id="searchTool"></div>

                            <div class="card">
                                <div class="card-header">
                                    <div class="row justify-content-between">
                                        <div class="col-4" style="display: flex; align-items: center">
                                            <h3 class="card-title text-uppercase font-weight-bold" th:text="${ctgRootName}"></h3>
                                        </div>
                                        <div class="col-6 text-right">
                                            <button type="button" class="btn btn-primary" data-toggle="modal" data-target="#import"><i class="fa-solid fa-cloud-arrow-up"></i>Import</button>
                                            <a th:href="@{${url_export}}" class="btn btn-info"><i class="fa-solid fa-cloud-arrow-down"></i>Export</a>
                                            <button type="button" class="btn btn-success btn-insert"><i class="fa-solid fa-circle-plus"></i>Thêm mới</button>
                                        </div>
                                    </div>
                                </div>
                                <!-- /.card-header -->
                                <div class="card-body align-items-center p-0">
                                    <table class="table table-bordered table-striped align-items-center">
                                        <thead class="align-self-center">
                                            <tr class="align-self-center">
                                                <th>#</th>
                                                <th>Code</th>
                                                <th>Name</th>
                                                <th>Note</th>
                                                <th>In use</th>
                                                <th>Status</th>
                                                <th>Default</th>
                                                <th>Sort</th>
                                                <th></th>
                                            </tr>
                                        </thead>
                                        <tbody id="contentTable"></tbody>
                                        <tfoot>
                                            <tr class="align-self-center">
                                                <th>#</th>
                                                <th>Code</th>
                                                <th>Name</th>
                                                <th>Note</th>
                                                <th>In use</th>
                                                <th>Status</th>
                                                <th>Default</th>
                                                <th>Sort</th>
                                                <th></th>
                                            </tr>
                                        </tfoot>
                                    </table>
                                </div>
                                <!-- /.card-body -->
                                <div class="card-footer">
                                    <div th:replace="fragments :: pagination"></div>
                                </div>

                                <!--Modal insert/update-->
                                <div class="modal fade" id="modal_insert_update">
                                    <div class="modal-dialog modal-lg">
                                        <div class="modal-content">
                                            <div class="modal-header">
                                                <strong class="modal-title" id="modal_insert_update_title"></strong>
                                                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                                            </div>
                                            <div class="modal-body">
                                                <div class="row">
                                                    <div class="col-12">
                                                        <div class="form-group">
                                                            <label for="codeField">Mã danh mục</label>
                                                            <input type="text" class="form-control" placeholder="Mã danh mục" required id="codeField"/>
                                                        </div>
                                                        <div class="form-group">
                                                            <label for="nameField">Tên loại</label>
                                                            <input type="text" class="form-control" placeholder="Tên danh mục" required id="nameField"/>
                                                        </div>
                                                        <div class="form-group">
                                                            <label for="nameField">Sắp xếp</label>
                                                            <input type="text" class="form-control" placeholder="Sắp xếp" required id="sortField"/>
                                                        </div>
                                                        <div class="form-group">
                                                            <label for="noteField">Ghi chú</label>
                                                            <textarea class="form-control" rows="5" placeholder="Ghi chú" id="noteField"></textarea>
                                                        </div>
                                                        <div class="form-group">
                                                            <label for="statusField">Trạng thái</label>
                                                            <select class="custom-select" id="statusField"></select>
                                                        </div>
                                                    </div>
                                                </div>
                                                <div class="modal-footer justify-content-end" style="margin-bottom: -15px;">
                                                    <button type="button" class="btn btn-default" data-dismiss="modal">Hủy</button>
                                                    <button type="button" class="btn btn-primary" id="btn-insert-update-submit">Lưu</button>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <!--Modal insert/update-->

                                <!-- modal import -->
                                <div class="modal fade" id="import">
                                    <div class="modal-dialog">
                                        <div class="modal-content">
                                            <form th:action="@{${url_import}}" enctype="multipart/form-data" method="POST">
                                                <div class="modal-header">
                                                    <strong class="modal-title">Import data</strong>
                                                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                                                        <span aria-hidden="true">&times;</span>
                                                    </button>
                                                </div>
                                                <div class="modal-body">
                                                    <div class="row">
                                                        <div class="col-12">
                                                            <div class="form-group">
                                                                <label>Chọn file import</label>
                                                                <input type="file" class="form-control" name="file">
                                                            </div>
                                                            <div class="form-group">
                                                                <label>Template</label>
                                                                <a th:href="@{${url_template}}" class="form-control link">
                                                                    <i class="fa-solid fa-cloud-arrow-down"></i>[[${templateImportName}]]
                                                                </a>
                                                            </div>
                                                        </div>
                                                    </div>
                                                    <div class="modal-footer justify-content-end" style="margin-bottom: -15px;">
                                                        <button type="button" class="btn btn-default" data-dismiss="modal">Hủy</button>
                                                        <button type="submit" class="btn btn-primary">Lưu</button>
                                                    </div>
                                                </div>
                                            </form>
                                        </div>
                                    </div>
                                </div>
                                <!-- modal import -->
                            </div>
                        </div>
                    </div>
                </div>
            </section>
        </div>

        <div th:replace="modal_fragments :: confirm_modal"></div>

        <div th:replace="footer :: footer"></div>

        <aside class="control-sidebar control-sidebar-dark"></aside>

        <div th:replace="header :: scripts"></div>
    </div>
    <script>
        let mvCategories = {};
        let mvId = 0;
        let mvType = "[[${categoryType}]]";
        let mvCode = $("#codeField");
        let mvName = $("#nameField");
        let mvNote = $("#noteField");
        let mvSort = $("#sortField");
        let mvStatus = $("#statusField");

        $(document).ready(function () {
            loadCategories(mvPageSizeDefault, 1);
            updateTableContentWhenOnClickPagination(loadCategories);

            preCreateCategory();
            preUpdateCategory();
            submitInsertOrUpdate();
            deleteCategory();
        });

        function preCreateCategory() {
            $(document).on("click", ".btn-insert", function () {
                $("#modal_insert_update_title").text("Thêm mới danh mục hệ thống");
                mvCode.val("");
                mvName.val("");
                mvNote.val("");
                mvSort.val("");
                mvStatus.append('<option value="true">Sử dụng</option><option value="false">Không sử dụng</option>');
                $("#btn-insert-update-submit").attr("actionType", "insert");
                $("#modal_insert_update").modal();
            });
        }

        function preUpdateCategory() {
            $(document).on("click", ".btn-update", function () {
                let category = mvCategories[$(this).attr("id")];
                $("#modal_insert_update_title").text("Cập nhật danh mục hệ thống");
                mvId = category.id;
                mvCode.val(category.code);
                mvName.val(category.name);
                mvNote.val(category.note);
                mvSort.val(category.sort);
                if (category.status === true) {
                    mvStatus.append('<option value="true">Sử dụng</option><option value="false">Không sử dụng</option>');
                } else {
                    mvStatus.append('<option value="false">Không sử dụng</option><option value="true">Sử dụng</option>');
                }
                $("#btn-insert-update-submit").attr("actionType", "update");
                $("#modal_insert_update").modal();
            });
        }

        function submitInsertOrUpdate() {
            $("#btn-insert-update-submit").on("click", function () {
                let actionType = $(this).attr("actionType");
                let category = {id : mvId, type : mvType, code : mvCode.val(), name : mvName.val(), sort : mvSort.val(), isDefault : 0, note : mvNote.val(), status: mvStatus.val()};
                console.log(category)
                if (actionType === "insert") {
                    $.ajax({
                        url: mvHostURLCallApi + "/category/create",
                        type: "POST",
                        contentType: "application/json",
                        data: JSON.stringify(category),
                        success: function(response, a, b) {
                            if (response.status === "OK") {
                                alert("Create successfully!");
                                window.location.reload();
                            }
                        },
                        error: function (xhr) {
                            alert("Error: " + $.parseJSON(xhr.responseText).message);
                        }
                    });
                }
                if (actionType === "update") {
                    $.ajax({
                        url: mvHostURLCallApi + "/category/update/" + mvId,
                        type: "PUT",
                        contentType: "application/json",
                        data: JSON.stringify(category),
                        success: function(response) {
                            if (response.status === "OK") {
                                alert("Update successfully!");
                                window.location.reload();
                            }
                        },
                        error: function (xhr) {
                            alert("Error: " + $.parseJSON(xhr.responseText).message);
                        }
                    });
                }
            });
        }

        function deleteCategory() {
            $(document).on("click", ".btn-delete", function () {
                let category = mvCategories[$(this).attr("id")];
                mvId = category.id;
                $(this).attr("actionType", "delete");
                $(this).attr("entityName", category.name);
                showConfirmModal($(this), "Xóa danh mục hệ thống", "Bạn chắc chắn muốn xóa danh mục: " + category.name);
            });

            $('#yesButton').on("click", function () {
                let apiURL = mvHostURLCallApi + "/category/delete/" + mvId;
                callApiDelete(apiURL);
            });
        }

        function loadCategories(pageSize, pageNum) {
            let apiURL = mvHostURLCallApi + "/category/" + "[[${categoryType}]]";
            let params = {pageSize: pageSize, pageNum: pageNum}
            $.get(apiURL, params, function (response) {
                if (response.status === "OK") {
                    let data = response.data;
                    let pagination = response.pagination;

                    updatePaginationUI(pagination.pageNum, pagination.pageSize, pagination.totalPage, pagination.totalElements);

                    let contentTable = $('#contentTable');
                    contentTable.empty();
                    $.each(data, function (index, d) {
                        mvCategories[d.id] = d;
                        let iconConfig = "";
                        if (d.type === "DOCUMENT_TYPE") {
                            iconConfig = `<a class="btn btn-secondary btn-sm" href="/stg/doc/doc-type/${d.id}"><i class="fa-solid fa-gear"></i></a>`;
                        }
                        contentTable.append(`
                            <tr>
                                <td>${(((pageNum - 1) * pageSize + 1) + index)}</td>
                                <td>${d.code}</td>
                                <td>${d.name}</td>
                                <td>${d.note}</td>
                                <td></td>
                                <td></td>
                                <td>${d.isDefault}</td>
                                <td>${d.sort}</td>
                                <td>
                                    <button class="btn btn-info   btn-sm btn-update mr-1" id="${d.id}"><i class="fa-solid fa-pencil"></i></button>
                                    <button class="btn btn-danger btn-sm btn-delete"      id="${d.id}"><i class="fa-solid fa-trash"></i></button>
                                    ${iconConfig}
                                </td>
                            </tr>
                        `);
                    });
                }
            }).fail(function () {
                showErrorModal("Could not connect to the server");//nếu ko gọi xuống được controller thì báo lỗi
            });
        }
    </script>
</body>
</html>