function startSelectWords() {
    var textHtml = $('#PageSelectWords #Text p');
    var selectedWordsHtml = $('#PageSelectWords #SelectedWords ul');
    var wordRegexp = /\w+/g;
    var wordMatcher;
    var lastIndex = 0;
    while ((wordMatcher = wordRegexp.exec(text)) != null) {
        var wordText = wordMatcher[0];
/*
        console.log("TextWordMatcher: " + wordMatcher + ", lastIndex: " + wordRegexp.lastIndex
            + ", word index: " + wordMatcher.index + ", word: " + wordText);
*/
        if (wordMatcher.index > lastIndex) {
            var textGap = text.substring(lastIndex, wordMatcher.index);
            textHtml.append(textGap);
        }
        lastIndex = wordRegexp.lastIndex;
        var wordTextHtml = $("<span>" + wordText +"</span>");
        textHtml.append(wordTextHtml);
        var wordNorm = wordText.toLowerCase();
        var word = words[wordNorm];
        if (!word) {
            var selectedWordHtml = $('<li class="unselected">' + wordNorm + '</li>');
            word = {word: wordNorm, selected: false, selectedWordHtml: selectedWordHtml};
            words[wordNorm] = word;
            selectedWordHtml[0].word = word;
            selectedWordsHtml.append(selectedWordHtml);
        }
        wordTextHtml[0].word = word;
    }
    if (text.length > lastIndex) {
        textGap = text.substring(lastIndex, text.length);
        textHtml.append(textGap);
    }


    $('#PageSelectWords #Text p span').click(function (e) {selectWordInText(this.word);});
    $('#PageSelectWords #SelectedWords ul li').click(function (e) {selectWordInText(this.word, false);});

}



function selectWordInText(word, selected) {
    if (typeof selected === 'undefined') {
        selected = !word.selected;
    }
    console.log("Select word [" + word.word + "] : " + selected);
    var className = selected ? "selected" : "unselected";
    word.selectedWordHtml.attr("class", className);
    word.selected = selected;


    $('#PageSelectWords #Text p span').each(function(i, e) {
        if ($(this).text().toLowerCase() == word.word) {
            $(this).attr('class', className);
        }
    });
}



function finishSelectWords() {
    for (var wordIndex in words) {
        var word = words[wordIndex];
        if (!word.selected) {
            delete words[word];
            continue;
        }
        delete word.selected;
        delete word.selectedWordHtml;
    }
    loadPage('prepare/prepareWords');
}
