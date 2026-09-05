# Install ux-principles into a repository

Instructions for an AI coding agent. A person points their agent at this file
("install ux-principles from <path or url>") and the agent does the following.

## What is being installed

Three layers of interaction rules and the test that keeps each rule in its own
layer. The general layers are copied into the target repository; the product's
own layer is written there by whoever knows the product.

## Steps

1. **Copy the rule files verbatim** from `rules/` into the target repository as
   `.claude/rules/ux/`:

   - `core.md` — principles for any interface;
   - `desktop.md` — pointer, keyboard, windows, shell;
   - `process.md` — procedures;
   - `positions.md` — opinionated stances;
   - `index.md` — the layers and the promotion test.

   Copy the `.ru.md` twins as well **only if the target repository is
   bilingual**. A single-language repository takes the single language it uses.

   Do not paraphrase, summarise or "adapt" them while copying. The wording here
   is the convention; a paraphrase in a target repository is a second convention
   that will drift.

2. **Add one line to the target repository's `AGENTS.md`**, so agents that do not
   load `.claude/rules/` automatically know where to read:

   > **UX:** the interaction rules live in [`.claude/rules/ux/`](.claude/rules/ux/)
   > — Claude Code loads them automatically; other agents must read them there.
   > This project's own rules and the cases behind them are in
   > `docs/process/ux.md`, and they win over the general ones.

3. **Create the product's own layer** if it does not exist: a page for the rules
   that name this product's parts, the worked examples with their step counts,
   and any documented departure from a general rule. Link it to the copied rules
   and back.

4. **Do not copy** `README*`, `INSTRUCTIONS*`, `LICENSE` or this repository's own
   `index.md` into the target. They describe the convention; they are not part of
   it.

## Keeping it current

`rules/` here is the canonical wording. When a principle changes, it changes
here first, and installed copies are refreshed by repeating step 1. A target
repository that edited its copy has forked the convention — which is allowed, but
then say so in that repository, and stop calling it an installed copy.

Russian: [`INSTRUCTIONS.ru.md`](INSTRUCTIONS.ru.md).
