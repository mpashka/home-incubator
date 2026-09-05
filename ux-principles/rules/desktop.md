# UX principles — desktop

Rules that need a pointer, a keyboard, a window or a desktop shell to be stated
at all. Everything device-independent is in [`core.md`](core.md).

## 1. There is a way back, and it goes one layer at a time

Each layer — a menu, a typed query, the popup itself — is dismissed in that
order, by repeated `Escape`, never more than the layer the person is in. A key
that closes everything at once is a key people stop pressing, because the cost of
a mistake is the whole context.

## 2. Inside an open surface the keyboard works; getting there is the pointer's job

Opening a menu puts the keyboard where typing is useful: `Enter` takes the
obvious action, arrows move, `Escape` backs out. That much costs nothing to
learn — the focus is already there and the keys are the same in every
application.

Reaching a control in the first place is a different question, answered by the
three routes below.

## 3. The layout may not move under the pointer

A surface that resizes with its own content pushes its rows out from under the
pointer and shakes. Give it one size, chosen for the widest content it will hold.

This is not a licence to cut content down to a size (that is
[`core.md` rule 11](core.md)); it is a rule about *stability*: the target must
still be where the person aimed when their hand arrives.

The same rule forbids the tempting trick of **moving a message away from the
pointer that approaches it**. Once is defensible for a transient hint — it frees
the screen underneath and then stays put. Repeatedly is not, and the failure is
subtle: the move itself takes the surface out from under the pointer, so the
"pointer left" event fires immediately. Anything that re-arms on leave re-arms at
once, every approach becomes a first approach, and the thing can never be
clicked at all.

## 4. The three input routes, and what each is for

| Route | What it costs the person | What it is good for |
| --- | --- | --- |
| **Pointer** | nothing to remember — the control is visible, its meaning is on it, and the only skill needed is aiming | **the default for everything.** Discoverable by looking, which no other route is |
| **Keyboard shortcut** | must be memorised, and kept from colliding with the shell's and every application's | **very frequent actions only** — there it is the fastest route by a wide margin, because there is no aiming at all |
| **Full keyboard navigation** | nothing to memorise in theory; in practice slow, fiddly and expensive to build correctly | see [`positions.md`](positions.md) — this is a stance, not a law |

Two consequences:

- **A shortcut is earned by frequency, not by importance.** A combination spent
  on something done twice a month is worse than no combination at all: it takes a
  scarce, globally shared resource and adds a thing to remember for an action the
  pointer already handles. Open a menu, run a command, switch a window — yes.
  Open a settings page — no.
- **A shortcut must say its own name.** A person's memory cannot be the only
  place a binding is recorded, so it appears where the pointer already is when
  the question comes up.

## 5. Replacing part of the shell means inheriting its duties

Software that hides or replaces part of its host environment — a shell
extension, a browser extension, a kiosk, a full-screen mode — takes on everything
that part provided. Anything reachable *only* from what you hid stops being
reachable the moment someone accepts the offer.

Taking the screen space is the easy half; taking the responsibility is the part
that makes the trade honest. So whenever a change hides part of the host, ask
**what stopped being reachable** and cover it: for a state, by showing it; for an
action, by offering it. Mirroring the host's own indicator is usually cheaper
than reimplementing the feature.

## Related

- [`core.md`](core.md) — the device-independent principles these sharpen.
- [`positions.md`](positions.md) — where this file deliberately departs from
  platform guidelines.

Russian mirror: [`desktop.ru.md`](desktop.ru.md).
