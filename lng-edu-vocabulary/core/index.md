---
tags: "@tag:markup @tag:accent @tag:part-of-speech @tag:word-forms"
---

# Ядро словаря

Родительский индекс: [../docs/index.md](../docs/index.md) ·
Устройство: [../docs/implementation/index.md](../docs/implementation/index.md)

Модель словаря, разбор исходной разметки и языковые правила. Зависимостей от Spring,
базы и сети здесь нет: `core` собирается и проверяется сам по себе, а пользуются им
`importer` и `backend`.

Код: `src/main/java/org/mpashka/vocabulary/core/`, тесты: `src/test/java/…` —
77 проверок, [../docs/testing/unit-tests.md](../docs/testing/unit-tests.md).

## Разбор исходной разметки

Формат описан в [../docs/implementation/source-db.md](../docs/implementation/source-db.md).

- `MarkupParser.java` — разбирает поле `words.xml` (вопреки названию не XML, а поток
  `текст$ПОМЕТА#`) на фрагменты
- `Chunk.java` — фрагмент разметки: кусок текста со своей пометой класса
- `EntryParser.java` — собирает из строки исходной базы разобранную статью
- `Entry.java` — словарная статья: заглавное слово, пометы, значения, примеры, обороты

## Письмо и ударение

- `Accent.java` — четыре сербских тона и заударная долгота; знаки исходной базы →
  комбинируемые знаки Unicode
- `Serbian.java` — кириллица → латиница и показ ударений; кириллица со знаками —
  канонический вид слова
- `Russian.java` — русский текст исходной базы: ударение записано U+2019 после гласной

## Языковые правила

- `PartOfSpeechRules.java` — определение части речи по помете и по строению слова
  ([part-of-speech.md](../docs/implementation/part-of-speech.md))
- `NounDeclension.java` — склонение существительных, опорная форма — родительный падеж
- `VerbConjugation.java` — спряжение глаголов, опорная форма — первое лицо настоящего времени
- `AdjectiveDeclension.java` — формы прилагательного по родам
- `Alternations.java` — чередования на стыке основы и окончания: беглое «а», оглушение
  ([word-forms.md](../docs/implementation/word-forms.md))

## Словоформы

- `Form.java` — словоформа с грамматической пометой (`gen.sg`, `praes.1sg`, `adj`);
  общий тип для правил, указателя поиска и карточки

## Перечисления

- `PartOfSpeech.java` — часть речи
- `Gender.java` — род существительного
- `WordStatus.java` — состояние обработки слова, по возрастанию готовности
