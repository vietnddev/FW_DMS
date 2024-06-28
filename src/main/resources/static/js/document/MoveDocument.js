let currentFolderSelected;
let documentToMoveId;
function moveDocument() {
    $(document).on("click", ".btn-move", function () {
        documentToMoveId = $(this).attr("docId");
        let docName = mvDocuments[documentToMoveId].name;
        $("#btnConfirmMoveDoc").attr("docId", documentToMoveId);
        $("#modalMoveDoc").modal();

        var toggler = document.getElementsByClassName("caret");
        var i;
        for (i = 0; i < toggler.length; i++) {
            toggler[i].addEventListener("click", function() {
                this.parentElement.querySelector(".nested").classList.toggle("active");
                this.classList.toggle("caret-down");
            });
        }
    })

    $("#btnConfirmMoveDoc").on("click", function () {
        if (currentFolderSelected == null) {
            alert("Vui lòng chọn thư mục cần di chuyến đến!");
            return;
        }
        let docSelectedId = currentFolderSelected.attr("docId");
        let apiURL = mvHostURLCallApi + "/stg/doc/move/" + documentToMoveId;
        let body = {destinationId : docSelectedId};
        $.ajax({
            url: apiURL,
            type: 'PUT',
            data: body,
            success: function(response) {
                if (response.status === "OK") {
                    alert("Di chuyển thành công!");
                    window.location.reload();
                }
            },
            error: function(xhr, status, error) {
                alert("Error: " + $.parseJSON(xhr.responseText).message);
            }
        })
    })
}

function loadFolderTreeOnMoveModal() {
    $(document).on('click', 'span[class^="caret docId-"]', function() {
        if ($(this).attr("hasSubFolder") === "N") {
            return;
        }
        let folderId = $(this).attr("docId");
        let subFolders = $('ul[class^="nested docId-' + folderId + '"]');

        if ($(this).attr("collapse") === "Y") {
            $(this).attr("collapse", "N");
            subFolders.empty();
            $(this).removeClass("caret-down");
            return;
        }
        $.get(mvHostURLCallApi + '/stg/doc/folders', {parentId: folderId}, function (response) {
            let subFoldersData = response.data;
            $.each(subFoldersData, function (index, d) {
                if (d.hasSubFolder === "N") {
                    subFolders.append(`<li class="caret-name block-caret" docId="${d.id}">${d.name}</li>`);
                }
                if (d.hasSubFolder === "Y") {
                    subFolders.append(`
                        <li>
                            <span class="caret docId-${d.id} caret-down" docId="${d.id}" collapse="N"></span> <span class="caret-name block-caret" docId="${d.id}">${d.name}</span>
                            <ul class="nested docId-${d.id} active"></ul>
                        </li>                        
                    `);
                }
                console.log("docId = ${d.id}, docName = " + d.name);
            })
        }).fail(function () {
            showErrorModal("Could not connect to the server");
        });
        $(this).addClass("caret-down");
        $(this).attr("collapse", "Y");
    });

    //Thay đổi màu nền của folder khi rê chuột vào
    $(document).on('mouseenter', '.block-caret', function() {
        $(this).addClass('highlight-bg');
    }).on('mouseleave', '.block-caret', function() {
        $(this).removeClass('highlight-bg');
    });

    $(document).on('click', '.caret-name', function() {
        $(this).addClass('highlight-txt');
        if (currentFolderSelected != null) {
            currentFolderSelected.removeClass('highlight-txt');
        }
        currentFolderSelected = $(this);
    });
}