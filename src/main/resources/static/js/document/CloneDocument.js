function cloneDocument() {
    $(document).on("click", ".btn-copy", function () {
        let docId = $(this).attr("docId");
        let docName = mvDocuments[docId].name;
        $("#btnConfirmCloneDoc").attr("docId", docId);
        $("#docCloneNameField").val(docName);
        $("#modalCloneDoc").modal();
    })

    $("#btnConfirmCloneDoc").on("click", function () {
        let docId = $(this).attr("docId");
        let newName = $("#docCloneNameField").val();
        if (newName === "") {
            alert("Vui lòng nhập tên tài liệu!")
            return;
        }
        let apiURL = mvHostURLCallApi + "/stg/doc/copy/" + docId;
        $.post(apiURL, {nameCopy : newName}, function (response) {
            if (response.status === "OK") {
                alert("Sao chép thành công!")
                window.location.reload();
            }
        }).fail(function () {
            showErrorModal("Could not connect to the server");
        });
    })
}