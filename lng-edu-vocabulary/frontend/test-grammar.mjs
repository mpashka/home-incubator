// Проверки расшифровки помет и разбора формы по правилу.
// Запуск: npm test (вместе с test-serbian.mjs).
import assert from 'node:assert/strict'
import { formName, formNameSerbian, caseKey } from './src/labels.js'
import { ruleFor, breakdown } from './src/grammar-rules.js'

// --- пометы ---------------------------------------------------------------

assert.equal(formName('gen.sg'), 'родительный падеж, единственное число')
assert.equal(formName('dat.pl'), 'дательный падеж, множественное число')
assert.equal(formName('praes.1sg'), 'настоящее время, 1-е лицо единственного числа')
assert.equal(formName('adj'), 'форма прилагательного')
assert.equal(formName(null), '')
assert.equal(formNameSerbian('ins.sg'), 'Instrumental jednine')
assert.equal(formNameSerbian('nom.pl'), 'Nominativ množine')
assert.equal(caseKey('loc.pl'), 'loc')
assert.equal(caseKey('praes.1sg'), '')

// --- правило --------------------------------------------------------------

const feminine = ruleFor('noun-declension', 'FEMININE_A')
assert.equal(feminine.kind, 'known')
assert.equal(feminine.endings.dat[1], '-ама')

assert.equal(ruleFor('noun-declension', 'FEMININE_CONSONANT').kind, 'absent')
assert.equal(ruleFor(null, 'FEMININE_A'), null)

// --- разбор формы ---------------------------------------------------------
// Ударение стоит комбинируемым знаком: окончание надо отрезать по буквам,
// иначе знак уедет в основу вместе с лишним символом.
const GRAVE = '̀'

assert.deepEqual(
  breakdown(feminine, { grammar: 'dat.pl', form: `во${GRAVE}дама` }),
  { stem: `во${GRAVE}д`, ending: 'ама' })

assert.deepEqual(
  breakdown(feminine, { grammar: 'nom.sg', form: `во${GRAVE}да` }),
  { stem: `во${GRAVE}д`, ending: 'а' })

// Форма, которой окончание правила не отвечает, разбору не поддаётся:
// выдавать словарную форму за выведенную нельзя.
assert.equal(breakdown(feminine, { grammar: 'gen.sg', form: 'ру̏ци' }), null)

// Нулевое окончание мужского рода: основа — всё слово.
const masculine = ruleFor('noun-declension', 'MASCULINE_CONSONANT')
assert.deepEqual(
  breakdown(masculine, { grammar: 'nom.sg', form: 'зи̑д' }),
  { stem: 'зи̑д', ending: '—' })

console.log('grammar: все проверки прошли')
