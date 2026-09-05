const CYRILLIC_TO_LATIN = {
  а: 'a', б: 'b', в: 'v', г: 'g', д: 'd', ђ: 'đ', е: 'e', ж: 'ž', з: 'z', и: 'i', ј: 'j',
  к: 'k', л: 'l', љ: 'lj', м: 'm', н: 'n', њ: 'nj', о: 'o', п: 'p', р: 'r', с: 's', т: 't',
  ћ: 'ć', у: 'u', ф: 'f', х: 'h', ц: 'c', ч: 'č', џ: 'dž', ш: 'š'
}

/** Переводит сербскую кириллицу в латиницу, сохраняя ударения и прочие знаки. */
export function toLatin (text) {
  return [...(text ?? '')].map(letter => {
    const latin = CYRILLIC_TO_LATIN[letter.toLowerCase()]
    return latin ? (letter === letter.toUpperCase() ? latin[0].toUpperCase() + latin.slice(1) : latin) : letter
  }).join('')
}
