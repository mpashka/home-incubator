function startPrepareWords() {
    var table = $('#PagePrepareWords div.content table');
    for (var wordText in words) {
        var word = words[wordText];
        if (!word.selected) {
            delete words[word];
            continue;
        }
//        var wordText = word.word;
        var tr = $('<tr><td>' + wordText + '</td></tr>');
        var imageInputHtml = $('<input type="text"/>').val("[IMG:" + wordText + "]");
        var translationInputHtml = $('<input type="text"/>').val("[TR:" + wordText + "]");
        table.append(tr.append($('<td />').append(imageInputHtml)).append($('<td />').append(translationInputHtml)));
        word.imageInputHtml = translationInputHtml;
        word.translationInputHtml = translationInputHtml;
    }

}


function finishPrepareWords() {
    for (var wordIndex in words) {
        var word = words[wordIndex];
        word.imageUrl = word.imageInputHtml.val();
        word.translation = word.translationInputHtml.val();

        delete word.imageInputHtml;
        delete word.translationInputHtml;
    }

    loadPage('questionary/questionary.html');
//    alert('Ok');
}