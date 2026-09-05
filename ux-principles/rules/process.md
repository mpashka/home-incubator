# UX principles — process

How the recurring pieces of work are done. Principles are in
[`core.md`](core.md) and [`desktop.md`](desktop.md); these are procedures.

## Write the case down before designing

One page per goal, in the person's words, with the state they are already in and
the steps the main path costs today. Keep them with the product, and **add or
update the case in the same change as the design**.

A use case nobody wrote down is a feature list waiting to happen. And a recorded
step count is what makes a later regression visible: without it, "this got worse"
is an opinion.

## How a step count is taken

- **Start from where the person already is**, not from a cold desktop. "Open the
  application, find the menu, …" measures the wrong thing if they had the menu
  open already.
- **Count what the person spends**: pointer travels, clicks, keystrokes, and
  every surface they must read before acting. A dialog is at least two steps —
  read it, answer it — even when it has one button.
- **Record the frequency and your confidence in it.** An unmeasured "a few times
  a day" is fine as long as it says so; a fake number is not.
- **Count the same path before and after.** A change that removes a step from a
  path nobody walks has not saved anything.

## What a feature request must contain

Not a solution — a case. Three things, and a request without them cannot be
prioritised against anything:

1. **the use case**, in the person's words;
2. **its frequency**, with the confidence above;
3. **what it costs now, and what it would cost after** — in steps, on the same
   path.

Then the proposal, and — this is the part usually missing — **the alternatives
that were rejected and why**. A request that lists no alternatives has not been
thought about; a request whose alternatives are strawmen is worse.

## Before filing, search what is already filed

Read the existing requests — **open and closed** — and search their *bodies*, not
their titles. A request is a duplicate when it shares the **need**, however
differently it is worded.

When one matches, **comment on it with what your version adds**: the new case,
the wider model, the constraint the original missed. Say that it arrived
separately and was merged there. One issue that grew is worth more than four that
agree — it is the only way to judge demand for something.

File separately only when the *need* differs, and then **link the neighbours both
ways** ("complementary to #N, not a substitute"), so the next reader sees the
cluster instead of one arbitrary member of it.

## When a rule is broken, record it where it was broken

A product that must depart from a principle writes the departure down **in the
product**, with its reason, next to the code that departs. It does not edit the
principle.

A shared principle with a list of exceptions is a principle nobody can rely on. A
product with a documented departure is a product whose author thought about it —
and the next reader can tell the difference between a decision and an oversight.

## Related

- [`index.md`](index.md) — the layers, the promotion test, and why evidence stays
  with the product.

Russian mirror: [`process.ru.md`](process.ru.md).
