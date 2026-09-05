# UX principles — core

Principles that hold for any interface, stated without naming an input device or
a platform affordance. Device-bound rules are in [`desktop.md`](desktop.md);
procedures in [`process.md`](process.md); arguable stances in
[`positions.md`](positions.md).

## 1. Design from the use case, not from the feature

Write the goal down first, in the person's own words and with the state they are
already in: *"I have the applications menu open and I want this one in my
favorites."* The design is then whatever serves that sentence in the fewest
steps.

A feature list ("we need favorites management") produces a settings page. A use
case produces an action on the thing the person is already looking at. This is
the meta-rule: every rule below is a way of serving a case that was written down.

## 2. Count the steps, and weight them by frequency

Every interaction has a step count **from the state the person is already in**,
plus a frequency. Saving one step from something done fifty times a day outranks
saving three from something done twice ever.

The smaller count wins unless it costs clarity. Two simple steps beat one step
that opens something the person then has to read and dismiss.

Frequency is an estimate, and saying so is part of the work: record the number
and how confident you are, so the next person argues with the estimate instead of
inventing their own silently.

## 3. The action lives on the object

Acting on a thing starts by pointing at that thing. The object is already in
front of the person; a settings page has to be found, opened and searched.

The test: if a person can see the thing, they should be able to act on it from
there. Every action that lives somewhere else is an action they must first learn
the location of.

## 4. No confirmation for a reversible action, no dialog for a cheap one

Adding a favorite is one step and is undone by one step; asking "are you sure"
doubles a free action, and after the third time nobody reads it anyway — a
confirmation that is always answered the same way has stopped being a safeguard.

If an immediate action drops state the person could want back, offer **Undo that
restores the exact state**, not a question in front of every deliberate action.
Confirmation is for what cannot be undone.

## 5. One control that toggles beats two that do not

The thing is either a favorite or it is not: show the one action that applies now
("Add to Favorites" / "Remove from Favorites"), not both with one greyed out. Two
controls where one belongs is one more thing to read, every time, forever.

## 6. Never dead-end on a precondition

If an action needs something prepared first — a file copied somewhere writable, a
directory created, an account linked — the action does the preparation itself.
Telling the person what to do first is one more step **and** a research task, and
it arrives at the moment they were about to get what they came for.

## 7. Never destroy what you touch

The same rule read from the other side: work on a copy, leave the original alone,
and never overwrite something the person edited. Rule 6 says "prepare it for
them"; this one says "and do it without taking anything away".

## 8. Show the result where the person is looking

State changed by an action is visible immediately, in the place that caused it.
Having to reopen something to find out whether it worked turns one action into
three — and teaches people to distrust the first two.

## 9. An update must not lose the person's place

When the world changes and a view is rebuilt, it keeps the selection, the typed
query, the scroll position and the focus. Losing them is worse than not updating
at all: the update was for the person's benefit, and it cost them the state they
had built by hand.

## 10. Failure is quiet, local and named

One broken item is skipped and reported; it never becomes an interruption, and it
never takes the rest down with it. The half that works stays usable.

**Quiet is not silent.** The failure is still named where it happened, with its
cause and what to do about it — in a log, a status, a marked row. What this rule
forbids is *escalation*: turning one bad entry into a modal, a notification, or a
crash. What it does not permit is *concealment*: a silent fallback that reports
success is a lie the person will act on.

## 11. Truncate only what stays reachable

An ellipsis is a promise: the value is longer than the space, but it is still
there and there is a way to get at it. A spreadsheet cell shows `#####` and still
holds the number every formula reads; a title cut to the width is spelled out in
full on hover.

Where no such route exists, `…` is not a hint but a deletion, and the person has
no way of ever learning what was removed. So before truncating, name the route to
the full value. If there is none, the fix is not a narrower cut but:

- **a shorter value** — an identifier instead of a whole message, a name instead
  of a name plus its subtitle, a relative time instead of a timestamp. Most
  over-long strings are over-long because they carry something the reader was not
  going to use;
- **or letting the container take the width it needs.**

Truncating is what you do *after* both of those, and then only with the route in
place.

## 12. A warning is shown only while there is something to do about it

A control that reports a problem and the action that clears it is not a status
display. When the problem is gone it has nothing to say, and a permanent
"all fine" trains people to stop looking at exactly the spot the warning will
appear in.

This does not apply to something whose subject is the state itself — a clock, a
battery, a load graph — which is read *because* the value is normal. The test is
whether the healthy state carries information: "it is 14:30" does, "nothing is
wrong" does not.

## Related

- [`desktop.md`](desktop.md) — what these look like with a pointer, a keyboard
  and windows.
- [`process.md`](process.md) — how a case is written and a step count taken.
- [`index.md`](index.md) — the layers and the promotion test.

Russian mirror: [`core.ru.md`](core.ru.md).
