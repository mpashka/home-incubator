2. Модель знаний пользователя (ключевая часть)
   2.1 Лексика

Для каждого слова:

{
"lemma": "račun",
"pos": "noun",
"known_forms": ["račun", "računa"],
"knowledge_level": 0..5,
"last_seen": "2026-01-24",
"last_success": true,
"contexts": [
"broj računa",
"račun za struju"
]
}


Важно: хранить лемму + формы, а не только слово.

2.2 Грамматика

Не «знаю/не знаю», а по темам:

Тема	Статус
Родительный падеж	частично
Предлоги u / na	плохо
Perfekat	хорошо
Aorist	не знаю
Глаголы движения	плохо
{
"topic": "prepositions_u_na",
"level": 0..5,
"error_rate": 0.42,
"examples_failed": [...]
}
----
