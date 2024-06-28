<!DOCTYPE html>
<html lang="en" xmlns:th="https://www.thymeleaf.org">

<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Kho tài liệu</title>
    <th:block th:replace="header :: stylesheets"></th:block>
    <style rel="stylesheet">
        .table td, th {
            vertical-align: middle;
        }
        /* Remove default bullets */
        ul, #myUL {
            list-style-type: none;
        }

        /* Remove margins and padding from the parent ul */
        #myUL {
            margin: 0;
            padding: 0;
        }

        /* Style the caret/arrow */
        .caret {
            cursor: pointer;
            user-select: none; /* Prevent text selection */
        }

        /* Create the caret/arrow with a unicode, and style it */
        .caret::before {
            content: "\25B6";
            color: black;
            display: inline-block;
            margin-right: 6px;
        }

        /* Rotate the caret/arrow icon when clicked on (using JavaScript) */
        .caret-down::before {
            transform: rotate(90deg);
        }

        /* Hide the nested list */
        .nested {
            display: none;
        }

        /* Show the nested list when the user clicks on the caret/arrow (with JavaScript) */
        .active {
            display: block;
        }

        .highlight-txt {
            color: blue;
            font-weight: bold;
        }
        .highlight-bg {
            background: aliceblue;
        }
        .caret-name {
            cursor: pointer;
        }
    </style>
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
            <section class="content">
                <div class="container-fluid">
                    <!--Breadcrumb-->
                    <div th:replace="fragments :: breadcrumb"></div>

                    <div class="row">
                        <div class="col-12">
                            <!--Search tool-->
                            <div th:replace="fragments :: searchTool('Y')" id="searchTool"></div>

                            <div class="card">
                                <div class="card-header">
                                    <div class="row justify-content-between">
                                        <div class="col-8" style="display: flex; align-items: center">
                                            <h3 class="card-title"><strong th:text="${documentParentName}"></strong></h3>
                                        </div>
                                        <div class="col-4 text-right">
                                            <button type="button" class="btn btn-primary" id="btnInsertFile">Thêm mới tài liệu</button>
                                            <button type="button" class="btn btn-warning" id="btnInsertFolder">Thêm mới thư mục</button>
                                            <button type="button" class="btn btn-info"    data-target="#modalExportData" data-toggle="modal">Xuất dữ liệu</button>
                                        </div>
                                    </div>
                                </div>
                                <div class="card-body align-items-center p-0">
                                    <table class="table table-bordered table-striped align-items-center">
                                        <thead class="align-self-center">
                                            <tr class="align-self-center">
                                                <th>STT</th>
                                                <th></th>
                                                <th>Thời gian</th>
                                                <th>Tên</th>
                                                <th>Loại tài liệu</th>
                                                <th>Mô tả</th>
                                                <th>Thao tác</th>
                                            </tr>
                                        </thead>
                                        <tbody id="contentTable"></tbody>
                                    </table>
                                </div>
                                <div class="card-footer">
                                    <div th:replace="fragments :: pagination"></div>
                                </div>

                                <!-- Modal insert and update -->
                                <div th:replace="pages/storage/fragments/document-fragments :: modalInsertAndUpdateFragment"></div>

                                <!-- Modal share role -->
                                <div th:replace="pages/storage/fragments/document-fragments :: modalShareFragment"></div>

                                <!-- Modal confirm export data -->
                                <div th:replace="pages/storage/fragments/document-fragments :: modalExportDataFragment"></div>

                                <!-- Modal confirm clone document -->
                                <div th:replace="pages/storage/fragments/document-fragments :: modalCloneDocFragment"></div>

                                <!-- Modal confirm clone document -->
                                <div th:replace="pages/storage/fragments/document-fragments :: modalMoveDocFragment"></div>
                            </div>
                        </div>
                    </div>
                </div>
            </section>
        </div>

        <div th:replace="modal_fragments :: confirm_modal"></div>

        <aside class="control-sidebar control-sidebar-dark"></aside>

        <div th:replace="header :: scripts"></div>

        <script type="text/javascript" th:src="@{/js/document/LoadDocument.js}"></script>
        <script type="text/javascript" th:src="@{/js/document/CreateUpdateDocument.js}"></script>
        <script type="text/javascript" th:src="@{/js/document/DeleteDocument.js}"></script>
        <script type="text/javascript" th:src="@{/js/document/ShareDocument.js}"></script>
        <script type="text/javascript" th:src="@{/js/document/CloneDocument.js}"></script>
        <script type="text/javascript" th:src="@{/js/document/MoveDocument.js}"></script>
    </div>

    <script type="text/javascript">
        let mvParentId = [[${parentId}]];
        let mvSearchTool = ["DOCUMENT_TYPE"];
        let mvDocuments = [];
        let mvDocType = $("#docTypeField");
        let mvName = $("#nameField");
        let mvDes = $("#desField");
        let mvAccountShares = [];
        let mvPageSize = mvPageSizeDefault;
        let mvPageNum = 1;
        let mvTotalPage = 1;
        let mvTotalElements = 1;
        let mvPagination;

        $(document).ready(function () {
            loadDocuments(mvPageSizeDefault, 1);
            setupSearchTool(mvSearchTool);
            loadFolderTreeOnSideBar();
            loadFolderTreeOnMoveModal();
            beforeSubmitShareRights();
            shareDoc();
            createDocument();
            updateDocument();
            submitInsertOrUpdate();
            deleteDocument();
            cloneDocument();
            moveDocument();
            search();
            //updateTableContentWhenOnClickPagination(loadDocuments, mvPageSize, mvPageNum, mvTotalPage, mvTotalElements);
            updateTableContentWhenOnClickPagination();
            exportData();
            downloadDocument();
        });

        function updateTableContentWhenOnClickPagination() {
            $('#selectPageSize').on('click', function() {
                if (mvPageSize === parseInt($(this).val())) {
                    return;
                }
                mvPageSize = $(this).val();
                loadDocuments($(this).val(), 1);
            });

            $('#firstPage').on('click', function() {
                if (mvPageNum === 1) {
                    return;
                }
                loadDocuments(mvPageSize, 1);
            });

            $('#previousPage').on('click', function() {
                if (mvPageNum === 1) {
                    return;
                }
                loadDocuments(mvPageSize, mvPageNum - 1);
            });

            $('#nextPage').on('click', function() {
                if (mvPageNum === mvTotalPage) {
                    return;
                }
                if (mvTotalElements <= mvPageSize) {
                    return;
                }
                loadDocuments(mvPageSize, mvPageNum + 1);
            });

            $('#lastPage').on('click', function() {
                if (mvPageNum === mvTotalPage) {
                    return;
                }
                if (mvTotalElements <= mvPageSize) {
                    return;
                }
                loadDocuments(mvPageSize, mvTotalPage);
            });
        }

        function search() {
            $("#btnSearch").on("click", function () {
                loadDocuments(mvPageSize, mvPageNum);
            })
        }

        function loadDocTypeCategory(defaultValue, idNotIn) {
            let apiURL = mvHostURLCallApi + "/category/document-type";
            if (idNotIn != null) {
                apiURL = apiURL + "?idNotIn=" + idNotIn;
            }
            $.get(apiURL, function (response) {
                if (defaultValue) {
                    mvDocType.append('<option>Chọn loại tài liệu</option>');
                }
                if (response.status === "OK") {
                    $.each(response.data, function (index, d) {
                        mvDocType.append('<option value=' + d.id + '>' + d.name + '</option>');
                    });
                }
            }).fail(function () {
                showErrorModal("Could not connect to the server");
            });
        }

        function exportData() {
            $("#btnConfirmExportData").on("click", function () {
                let apiURL = mvHostURLCallApi + "/stg/doc/export/excel";
                callApiExportData(apiURL);
                $("#modalExportData").modal('hide');
            })
        }

        function downloadDocument() {
            $(document).on("click", ".btn-download", function () {
                let apiURL = mvHostURLCallApi + "/stg/doc/download/" + $(this).attr("docId");
                callApiExportData(apiURL);
            })
        }
    </script>
</body>
</html>