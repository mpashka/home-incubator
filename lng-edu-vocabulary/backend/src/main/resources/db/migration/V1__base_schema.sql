-- Схема словаря сербского языка.
-- Замысел и обоснования: docs/implementation/db-schema.md
-- @tag:source-db @tag:accent @tag:word-forms

---------------------------------------------------------------------------
-- Справочники
---------------------------------------------------------------------------

-- Откуда взято сведение. Нужно, чтобы отличать достоверное от предположенного
-- и чтобы можно было исправлять, не затирая надёжное ненадёжным.
create type data_source as enum (
    'SOURCE_DICTIONARY',  -- старый словарь: выписано прямо в статье, достоверно
    'RULES',              -- выведено нашими правилами, предположительно
    'WIKTIONARY',         -- викисловарь, достоверно (условия CC BY-SA)
    'LLM',                -- языковая модель, требует проверки
    'HUMAN'               -- проверено человеком, высшая степень доверия
);

-- Состояние обработки слова.
create type word_status as enum (
    'NO_TRANSLATION',   -- перевода нет, слово в очереди на разбор моделью
    'IMPORTED',         -- перенесено из старого словаря, о корнях сведений нет
    'AWAITING_REVIEW',  -- разобрано моделью либо внешним источником, ждёт проверки
    'COMPLETE'          -- обработано полностью, включая связь с корнями
);

create type part_of_speech as enum (
    'NOUN', 'VERB', 'ADJECTIVE', 'ADVERB', 'PRONOUN', 'NUMERAL',
    'INTERJECTION', 'CONJUNCTION', 'PREPOSITION', 'PARTICLE', 'UNKNOWN'
);

create type gender as enum ('MASCULINE', 'FEMININE', 'NEUTER');

---------------------------------------------------------------------------
-- Слово
---------------------------------------------------------------------------

create table word
(
    id                bigint generated always as identity primary key,

    -- Каноническая запись — кириллица со знаками ударения (см. docs/plan.md,
    -- «Решено по ходу этапа 1»). Латиница выводится преобразованием, обратное
    -- направление неоднозначно.
    headword          text           not null,
    -- То же без ударений: по нему ищут и по нему же слово опознаётся.
    headword_plain    text           not null,
    headword_latin    text           not null,

    part_of_speech    part_of_speech not null default 'UNKNOWN',
    part_of_speech_source data_source not null default 'RULES',
    gender            gender,

    status            word_status    not null default 'NO_TRANSLATION',

    -- Кто последним менял слово: правила, модель или человек. Отдельно от
    -- источника отдельных сведений — это про слово целиком.
    last_changed_by   data_source    not null default 'RULES',
    last_changed_at   timestamptz    not null default now(),

    -- Ключ статьи в исходной базе (words.name). Нужен, чтобы перенос можно было
    -- повторить, ничего не задвоив.
    source_key        text,

    -- Омонимы исходная база пакует в одну строку; при разделении у них
    -- совпадает headword_plain, поэтому номер омонима входит в ключ.
    homonym_index     int            not null default 1,

    created_at        timestamptz    not null default now(),

    constraint word_unique unique (headword_plain, part_of_speech, homonym_index)
);

comment on column word.headword is
    'Кириллица с надстрочными знаками ударения — канонический вид';
comment on column word.status is
    'Состояние обработки: от «перевода нет» до «обработано полностью»';

create index word_plain_idx on word (headword_plain text_pattern_ops);
create index word_latin_idx on word (headword_latin text_pattern_ops);
create index word_status_idx on word (status);

---------------------------------------------------------------------------
-- Значения, переводы, примеры
---------------------------------------------------------------------------

create table sense
(
    id           bigint generated always as identity primary key,
    word_id      bigint      not null references word (id) on delete cascade,

    -- Номер значения в исходной статье. Пусто, если нумерации не было —
    -- а её нет у 72 % статей, и разделить их разбором нельзя.
    number       int,
    -- Разделены ли значения достоверно. Для статей без нумерации здесь RULES:
    -- всё содержимое сложено в одно значение и ждёт разбора моделью.
    source       data_source not null default 'SOURCE_DICTIONARY',

    ordinal      int         not null,

    unique (word_id, ordinal)
);

create table translation
(
    id       bigint generated always as identity primary key,
    sense_id bigint      not null references sense (id) on delete cascade,
    -- Русский текст с надстрочным знаком ударения.
    text     text        not null,
    source   data_source not null default 'SOURCE_DICTIONARY',
    ordinal  int         not null,

    unique (sense_id, ordinal)
);

create index translation_text_idx on translation (text text_pattern_ops);

create table example
(
    id        bigint generated always as identity primary key,
    -- Пример принадлежит либо значению, либо слову целиком: устойчивые обороты
    -- из блока после «◊» к отдельному значению не привязаны.
    sense_id  bigint      references sense (id) on delete cascade,
    word_id   bigint      not null references word (id) on delete cascade,

    serbian   text        not null,
    russian   text        not null,
    -- Устойчивый оборот или обычный пример употребления.
    is_idiom  boolean     not null default false,
    source    data_source not null default 'SOURCE_DICTIONARY',
    ordinal   int         not null
);

create index example_word_idx on example (word_id);
create index example_sense_idx on example (sense_id);

---------------------------------------------------------------------------
-- Словоформы
---------------------------------------------------------------------------

-- Главная таблица для поиска по любой форме слова.
--
-- Ударение хранится у КАЖДОЙ формы отдельно и не выводится из заглавного слова:
-- измерено, что оно совпадает лишь в 16,4 % случаев (docs/implementation/word-forms.md).
create table word_form
(
    id           bigint generated always as identity primary key,
    word_id      bigint      not null references word (id) on delete cascade,

    -- Форма с ударением. Пусто, если форму породили правила: буквы они дают,
    -- а ударение — нет.
    form         text,
    -- Форма без ударения. Именно по ней идёт поиск, поэтому not null.
    form_plain   text        not null,

    -- Грамматическое описание: 'gen.sg', 'nom.pl', 'praes.1sg' и подобное.
    grammar      text,

    -- Откуда взята сама форма (буквы).
    source       data_source not null,
    -- Откуда взято ударение. Отдельно от источника формы: буквы могут быть
    -- от правил, а ударение — из викисловаря.
    accent_source data_source,

    -- Правила дают несколько правдоподобных вариантов формы, и какой из них
    -- верен, по буквам не видно. Для поиска годятся все, для показа — один.
    is_preferred boolean     not null default false
);

comment on table word_form is
    'Словоформы для поиска. Ударение у каждой формы своё, правилами не выводится';
comment on column word_form.accent_source is
    'Пусто — ударения нет; SOURCE_DICTIONARY/WIKTIONARY — достоверно; LLM — требует проверки';

-- Поиск по словоформе — основная задача указателя.
create index word_form_plain_idx on word_form (form_plain);
create index word_form_word_idx on word_form (word_id);

---------------------------------------------------------------------------
-- Связь с корнями
---------------------------------------------------------------------------

create type root_kind as enum ('PROTO_SLAVIC', 'PROTO_INDO_EUROPEAN', 'RUSSIAN', 'SERBIAN', 'OTHER');

create table word_root
(
    id      bigint generated always as identity primary key,
    word_id bigint      not null references word (id) on delete cascade,
    kind    root_kind   not null,
    -- Сам корень либо родственное слово: '*voda', '*wódr̥', 'вода'.
    value   text        not null,
    note    text,
    source  data_source not null,

    -- Ссылка на источник. Для викисловаря обязательна: он под CC BY-SA.
    source_url text,

    unique (word_id, kind, value)
);

comment on table word_root is
    'Связь с русским, праславянским и сербским корнями. Источник обязателен: '
        'сведения из викисловаря требуют ссылки по условиям CC BY-SA';

---------------------------------------------------------------------------
-- Расхождения между источниками
---------------------------------------------------------------------------

-- Когда внешний источник спорит с тем, что уже есть, запись не затирается:
-- расхождение откладывается сюда и разбирается отдельно.
create table discrepancy
(
    id          bigint generated always as identity primary key,
    word_id     bigint      not null references word (id) on delete cascade,

    -- Что именно разошлось: 'part_of_speech', 'accent', 'translation'.
    field       text        not null,
    current_value text,
    proposed_value text,

    current_source  data_source not null,
    proposed_source data_source not null,
    proposed_url    text,

    resolved    boolean     not null default false,
    resolution  text,
    created_at  timestamptz not null default now()
);

create index discrepancy_unresolved_idx on discrepancy (word_id) where not resolved;
