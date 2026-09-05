# ux-principles — the rules

Three layers, and the test that keeps a rule in its own one.

## The layers

| File | What lives there | Test |
| --- | --- | --- |
| [`core.md`](core.md) | principles that hold for any interface | statable without naming an input device or a platform affordance |
| [`desktop.md`](desktop.md) | pointer, keyboard, windows, the desktop shell | needs a device or a window to be stated at all |
| the project's own page | this product's rules, and the cases that produced them | names this product's parts |
| [`process.md`](process.md) | procedures: how a case is written, how a step count is taken, how a request is argued | — |
| [`positions.md`](positions.md) | opinionated stances, marked as such | someone reasonable could hold the opposite |

## The promotion test

**A principle is general only if you can state it without naming an input device**
(pointer, key, finger), **a platform affordance** (window, popover, menu bar,
notification) **or this product's parts.** If it needs a device, it is a desktop
rule. If it needs the product, it is the product's rule.

Apply it in the direction that hurts: the failure mode of a shared rule set is
that everything drifts *upward*, because everything feels general to the person
who just learned it. A rule that cannot pass the test is not thereby less true —
it is just less portable, and it belongs a layer down where its terms mean
something.

## Evidence never moves up

The case that produced a rule stays where it happened. Step counts, widget names,
screenshots and issue numbers live with the product; a general rule may say *that*
there was a case and link to it, never quote its numbers.

Two reasons, and the second is the one people miss:

- evidence rots — the widget is renamed, the count changes, the issue is closed —
  and a rule set full of stale examples stops being read;
- evidence is what makes a rule set unreadable to anyone else. A stranger can use
  "the action lives on the object". They can do nothing with "the Favorites
  category updates in the open menu".

## A layer sharpens the one above it, never contradicts it

If a product must break a general rule, that is recorded **in the product** as an
exception with its reason — not by editing the general rule. A general rule with
an exception list is a rule nobody can rely on; a product with a documented
departure is a product whose author thought about it.

## The four genres

A rule set that mixes genres becomes a wall of text nobody navigates. Keep them
apart:

- **Principles** — what to do, and why. `core.md`, `desktop.md`.
- **Procedures** — how to do a recurring piece of work. `process.md`.
- **Positions** — decisions that are defensible, not universal. `positions.md`.
- **Cases** — worked examples with real numbers. The product's own pages.

## Installing

See [`../INSTRUCTIONS.md`](../INSTRUCTIONS.md). In short: copy `core.md`,
`desktop.md`, `process.md` and `positions.md` into the target repository as
`.claude/rules/ux/`, and add one line to its `AGENTS.md` so agents that do not
load `.claude/rules/` automatically know where to read them.

Russian mirror: [`index.ru.md`](index.ru.md).
