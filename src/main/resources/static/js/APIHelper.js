function callApiDelete(apiURL, redirectTo) {
    $.ajax({
        url: apiURL,
        type: 'DELETE',
        success: function(result) {
            // Xử lý kết quả nếu cần thiết
            alert(result.data)
            if (redirectTo != null) {
                window.location = mvHostURL + redirectTo;
            } else {
                window.location.reload();
            }
        },
        error: function(xhr, status, error) {
            // Xử lý lỗi nếu có
            alert(status + ': ' + JSON.stringify(xhr.responseJSON));
        }
    });
}

function callApiExportData(apiURL) {
    $.ajax({
        url: apiURL,
        method: 'GET',
        xhrFields: {
            responseType: 'blob'
        },
        success: function(data, status, xhr) {
            // Get the filename from the Content-Disposition header
            var filename = "";
            var disposition = xhr.getResponseHeader('Content-Disposition');
            if (disposition && disposition.indexOf('attachment') !== -1) {
                var filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
                var matches = filenameRegex.exec(disposition);
                if (matches != null && matches[1]) {
                    filename = matches[1].replace(/['"]/g, '');
                }
            }

            // Create a URL for the blob
            var url = window.URL.createObjectURL(data);

            // Create a link element, set the URL and trigger a click to download the file
            var a = document.createElement('a');
            a.href = url;
            a.download = filename;
            document.body.appendChild(a);
            a.click();

            // Clean up
            setTimeout(function() {
                window.URL.revokeObjectURL(url);
                document.body.removeChild(a);
            }, 0);
        },
        error: function(xhr, status, error) {
            alert("Error: " + $.parseJSON(xhr.responseText).message);
        }
    });
}