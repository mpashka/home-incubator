function startQuestionary() {
    for (var wordIndex in words) {
        var word = words[wordIndex];
        word.level = 0;
//        word.;
    }
}


function startNextExercise() {
    var level = selectNextExerciseLevel();

}


function selectNextExerciseLevel() {
    var minLevel = 100;
    var maxLevel = 0;
    var sum = 0;
    var count = 0;
    for (var wordIndex in words) {
        var word = words[wordIndex];
        var level = word.level;
        sum += level;
        count++;
        if (minLevel > level) minLevel = level;
        if (maxLevel < level) maxLevel = level;
    }
    level = Math.round((Math.random()+Math.random()-Math.random()-Math.random()) * (maxLevel-minLevel) / 4 + sum/count);
    if (level>maxLevel) level = maxLevel;
    if (level<minLevel) level = minLevel;
    return level;
}


const sameLevelProbability = 0.1;
function selectWords(level, count) {
    var wordsForSelection = [];
    for (var wordIndex in words) {
        var word = words[wordIndex];
        if (word.level == level) {
            wordsForSelection.push(word);
        } else {
            var levelDif = Math.abs(word.level - level);
            if (Math.random() / levelDif < sameLevelProbability) {
                wordsForSelection.push(word);
            }
        }
    }

    var selectedWords = [];
    for (var i= 0; i < count; i++) {
        var index = Math.random() * wordsForSelection.length;
        var word = wordsForSelection[index];
        selectedWords.push(word);
        wordsForSelection.splice(index, 1);
    }
    return selectedWords;
}
