---
description: "Куда ставить токены @tag: в Java-коде."
paths: ["**/*.java"]
---

# llm-wiki-tags — тэги в Java

Токен `@tag:<slug>` ставится в комментарии прямо над маркируемым элементом.
Подходит любой стиль — `//`, `/* */` или Javadoc; там, где у элемента уже есть
Javadoc, ставь тэг в него, чтобы тэг шёл вместе с документацией API. Несколько
тэгов — через пробел. Формат и реестр: [`tags.md`](tags.md).

- **Пакет** — в `package-info.java`, в Javadoc над объявлением `package`:
  ```java
  /**
   * Payments domain.
   * @tag:payments
   */
  package com.example.payments;
  ```
- **Файл** — комментарий в самом верху `.java`-файла, над строкой `package`:
  ```java
  // @tag:payments
  package com.example.payments;
  ```
- **Класс** (а также интерфейс, enum, record) — прямо над объявлением:
  ```java
  /** Charges a card. @tag:payments @tag:payments/retry */
  public class ChargeService { ... }
  ```
- **Метод** — прямо над методом:
  ```java
  // @tag:payments/retry
  public void charge(Card card) { ... }
  ```
