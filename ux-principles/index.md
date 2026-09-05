# index: `ux-principles/`

Interaction rules in three layers, plus the test that keeps each rule in its own
layer. Documentation only — there is nothing to build or run.

Parent: [`../README.md`](../README.md) (home-incubator).

## Directories

- [`rules/`](rules/index.md) — the payload and the canonical wording: `core`,
  `desktop`, `process`, `positions`, each with a `.ru.md` twin.

## Files

- `README.md` / `README.ru.md` — what this is, for a human.
- `INSTRUCTIONS.md` / `INSTRUCTIONS.ru.md` — the procedure an agent follows to
  install the rules into another repository.
- `LICENSE` — The Unlicense (public domain).

## Boundaries

- **`rules/` owns the wording.** The READMEs describe the convention and
  `INSTRUCTIONS*` gives the procedure; neither grows its own paraphrase.
- Content files come in mirrored English/Russian pairs. **Any change to one must
  be mirrored in its twin** in the same edit.
- No product's evidence lives here: step counts, widget names and issue numbers
  stay with the product they came from.
