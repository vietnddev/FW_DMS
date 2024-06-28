<!DOCTYPE html>
<html lang="en" xmlns:th="https://www.thymeleaf.org">

<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Kho tài liệu</title>
    <th:block th:replace="header :: stylesheets"></th:block>
    <link rel="stylesheet" type="text/css" th:href="@{/plugins/pdf-js/web/viewer.css}">
</head>

<body class="hold-transition sidebar-mini layout-fixed">
    <div class="wrapper">
        <!-- Navbar (header) -->
        <div th:replace="header :: header"></div>
        <!-- /.navbar (header)-->

        <!-- Folder tree -->
        <div th:replace="fragments :: folderTree"></div>

        <!-- Content Wrapper. Contains page content -->
        <div class="content-wrapper" style="padding-top: 10px; padding-bottom: 1px;">
            <!-- Main content -->
            <div class="content" style="padding-left: 20px; padding-right: 20px">
                <!--Breadcrumb-->
                <div th:replace="fragments :: breadcrumb"></div>

                <!-- Small boxes (Stat box) -->
                <div class="row">
                    <div class="card col-12" style="font-size: 14px">
                        <div class="card-body pl-0 pr-0" style="padding-top: 8px">
                            <div class="row">
                                <div class="col-sm-8">
                                    <iframe class="w-100" th:src="@{'/' + ${docDetail.file.src}}" style="min-height: 583px"></iframe>
                                </div>
                                <div class="col-sm-4">
                                    <div class="card">
                                        <div class="card-header p-2">
                                            <ul class="nav nav-pills" style="font-size: 13px">
                                                <li class="nav-item">
                                                    <a class="nav-link active" href="#docData" data-toggle="tab">THÔNG TIN CHUNG</a>
                                                </li>
                                                <li class="nav-item">
                                                    <a class="nav-link" href="#docDetail" data-toggle="tab">CHI TIẾT</a>
                                                </li>
                                                <li class="nav-item">
                                                    <a class="nav-link" href="#version" data-toggle="tab" id="fileVersionTab">PHIÊN BẢN</a>
                                                </li>
                                                <li class="nav-item">
                                                    <div class="btn-group">
                                                        <button type="button" class="btn btn-sm btn-default" disabled>Action</button>
                                                        <button type="button" class="btn btn-sm btn-default dropdown-toggle dropdown-icon" data-toggle="dropdown" aria-expanded="false">
                                                            <span class="sr-only">Toggle Dropdown</span>
                                                        </button>
                                                        <div class="dropdown-menu" role="menu" style="">
                                                            <a class="dropdown-item" href="#" data-toggle="modal" data-target="#modalChangeFile">
                                                                <i class="fa-solid fa-arrows-rotate mr-1"></i>Thay file
                                                            </a>
                                                        </div>
                                                    </div>
                                                </li>
                                            </ul>
                                        </div><!-- /.card-header -->
                                        <div class="card-body">
                                            <div class="tab-content">
                                                <!--Tab metadata-->
                                                <div class="active tab-pane" id="docData">
                                                    <form class="form-horizontal" method="GET" th:action="@{/stg/doc/update-metadata/{id}(id=${docDetail.id})}">
                                                        <div class="form-group row">
                                                            <label class="col-sm-4 col-form-label">Document type</label>
                                                            <div class="col-sm-8"><input class="form-control" type="text" disabled th:value="${docDetail.docTypeName}"/></div>
                                                        </div>
                                                        <div class="form-group row" th:each="list : ${docMeta}">
                                                            <label class="col-sm-4 col-form-label" th:text="${list.fieldName}"></label>
                                                            <div class="col-sm-8">
                                                                <input type="hidden" name="fieldId" th:value="${list.fieldId}">
                                                                <input type="hidden" name="dataId" th:value="${list.dataId}">
                                                                <input class="form-control"
                                                                       name="dataValue"
                                                                       th:type="${list.fieldType}" th:placeholder="${list.fieldName}" th:value="${list.dataValue}"
                                                                       th:if="${list.fieldRequired}" required>
                                                                <input class="form-control"
                                                                       name="dataValue"
                                                                       th:type="${list.fieldType}" th:placeholder="${list.fieldName}" th:value="${list.dataValue}"
                                                                       th:if="!${list.fieldRequired}">
                                                            </div>
                                                        </div>
                                                        <div class="form-group row">
                                                            <div class="offset-sm-4 col-sm-9">
                                                                <button class="btn btn-sm btn-primary" style="font-weight: bold;">Lưu</button>
                                                            </div>
                                                        </div>
                                                    </form>
                                                </div>
                                                <!--End Tab metadata-->

                                                <!-- Tab tài liệu detail-->
                                                <div class="tab-pane" id="docDetail" style="font-size: 15px">
                                                    <div class="row mb-2">
                                                        <div class="col-sm-6 mb-3">Upload time:</div>     <div class="col-sm-6" th:text="${docDetail.createdAt}"></div>
                                                        <div class="col-sm-6 mb-3">Last update time:</div><div class="col-sm-6" th:text="${docDetail.lastUpdatedAt}"></div>
                                                        <div class="col-sm-6 mb-3">Last update at:</div>  <div class="col-sm-6" th:text="${docDetail.lastUpdatedBy}"></div>
                                                        <div class="col-sm-6">File size:</div>       <div class="col-sm-6" th:text="${docDetail.file.size} + ' KB'"></div>
                                                    </div>
                                                    <hr style="margin: 0">
                                                </div>
                                                <!-- End Tab tài liệu liên quan -->

                                                <!-- Tab version -->
                                                <div class="tab-pane" id="version" style="font-size: 15px;">
                                                    <table class="table table-hover table-responsive p-0">
                                                        <tbody class="align-self-center" id="tableVersion"></tbody>
                                                    </table>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!--CHANGE FILE-->
            <div class="modal fade" id="modalChangeFile">
                <div class="modal-dialog">
                    <div class="modal-content text-left">
                        <form th:action="@{/stg/doc/change-file/{id}(id=${docDetail.id})}"
                              enctype="multipart/form-data" method="post">
                            <div class="modal-header">
                                <strong class="modal-title">Thay file đính kèm</strong>
                                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                                    <span aria-hidden="true">&times;</span>
                                </button>
                            </div>
                            <div class="modal-body">
                                <div class="row">
                                    <div class="form-group w-100">
                                        <label>Chọn file mới</label>
                                        <input class="form-control" type="file" name="file" required>
                                    </div>
                                </div>
                            </div>
                            <div class="modal-footer justify-content-end">
                                <button type="button" class="btn btn-default" data-dismiss="modal">Hủy</button>
                                <button type="submit" class="btn btn-primary">Lưu</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
            <!--END CHANGE FILE-->

        </div>

        <aside class="control-sidebar control-sidebar-dark"></aside>

        <div th:replace="header :: scripts"></div>
    </div>

    <script type="text/javascript">
        let mvDocId = [[${documentId}]];

        $(document).ready(function () {
            loadFolderTreeOnSideBar();
            loadFileVersion();
        });

        function loadFileVersion() {
            $("#fileVersionTab").on("click", function () {
                $.get(mvHostURLCallApi + '/stg/doc/files/' + mvDocId, function (response) {
                    if (response.status === "OK") {
                        $("#tableVersion").empty();
                        $.each(response.data, function (index, d) {
                            $("#tableVersion").append(`
                                <tr class="align-self-center">
                                    <td>${index + 1}</td>
                                    <td>${d.uploadAt}</td>
                                    <td>${d.originalName}</td>
                                    <td>${d.isActive}</td>
                                    <td>
                                        <button type="submit" style="border: none; background: none">
                                            <img src="/dist/icon/restore.png">
                                        </button>
                                    </td>
                                </tr>
                            `);
                        });
                    }
                }).fail(function () {
                    showErrorModal("Could not connect to the server");
                });
            })
        }
    </script>
</body>
</html>