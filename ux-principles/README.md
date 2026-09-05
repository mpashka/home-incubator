# ux-principles

Interaction rules in three layers, with a test that keeps each rule in its own
layer.

They were extracted from one product's UX page, where twelve of its fifteen rules
turned out to be general, four needed a pointer or a window to be stated, and
only the worked examples were really about that product. That ratio is the reason
this repository exists: most of what a team learns about interaction is portable,
and it stays trapped in one repository because nobody separated it from the
evidence.

## The layers

| Layer | What lives there |
| --- | --- |
| [`rules/core.md`](rules/core.md) | principles for any interface: count the steps and weight them by frequency, the action lives on the object, no confirmation for a reversible action, truncate only what stays reachable, a warning is shown only while there is something to do about it |
| [`rules/desktop.md`](rules/desktop.md) | pointer, keyboard, windows, shell: a way back one layer at a time, the layout may not move under the pointer, the three input routes, replacing part of the shell means inheriting its duties |
| the product's own page | this product's rules, and the cases with their step counts |

Plus [`rules/process.md`](rules/process.md) (how a case is written, how a step
count is taken, what a feature request must contain) and
[`rules/positions.md`](rules/positions.md) — stances that are defensible rather
than universal, kept apart on purpose.

## The two ideas worth stealing even if you take nothing else

**The promotion test.** A principle is general only if you can state it without
naming an input device, a platform affordance or your product's parts. Apply it
in the direction that hurts: a shared rule set fails because everything drifts
upward.

**Evidence never moves up.** The case that produced a rule stays with the
product. Step counts and screenshots rot, and they are what makes a rule set
unreadable to anyone who did not live through the case.

## Using it

See [`INSTRUCTIONS.md`](INSTRUCTIONS.md) — an agent copies `rules/*` into the
target repository as `.claude/rules/ux/` and adds one line to its `AGENTS.md`.

Same shape as [llm-wiki-tags](https://github.com/mpashka/llm-wiki-tags): the
canonical wording lives in `rules/`, the READMEs describe it, `INSTRUCTIONS*`
gives the procedure.

Russian: [`README.ru.md`](README.ru.md).
