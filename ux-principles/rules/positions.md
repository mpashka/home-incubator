# UX principles — positions

Decisions that are **defensible, not universal**. Someone reasonable holds the
opposite of each of these, and platform guidelines say the opposite of at least
one.

They are kept apart from [`core.md`](core.md) and [`desktop.md`](desktop.md) on
purpose. A stance published as a law discredits the laws around it: the first
person who disagrees with one has been given a reason to stop trusting all of
them. Adopt these, argue with them, or drop the file — the principles stand
without it.

## Full keyboard navigation is not a goal

Tab/arrow reachability for every control is expensive to build correctly and
unpleasant to use even when done. The stance: **the pointer is the default route,
shortcuts are earned by frequency, and full keyboard navigation is an acceptable
thing to not have** — especially for configuration, which is pointer-designed and
visited rarely.

This departs from a "keyboard parity everywhere" reading of most platform
guidelines. The practical consequence: a review finding of the form "X has no
keyboard route" is **not automatically a defect** — the question is always how
often X is done.

Where it does not apply: software whose users cannot use a pointer. This stance
is about effort allocation for a general-purpose tool, not an argument that
keyboard access does not matter.

## A light, not an interrupter

When software has something to say that is not urgent, it changes something the
person can glance at, and does not take the foreground: no notification, no
focus steal, no modal. The person comes back when they choose to.

The opposing view is honest — a notification is *seen*, a light can be missed —
and for genuinely urgent things it wins. The stance is that "urgent" is claimed
far more often than it is true, and that software which interrupts wrongly once
is muted forever, after which it can no longer say anything at all.

## Aggregate to the loudest, detail on demand

When several things each have a state, show **one** indicator carrying the
most urgent of them, and put the per-item breakdown one step away. Not one
indicator per item.

The reasoning: the person acts on one thing at a time, the aggregate answers
"is there anything for me" for free, and N indicators split attention while
growing the surface. The opposing view — that per-item indicators are more
informative — is true and is exactly the cost being paid.

## Cheap dismissal is not always a feature

For software whose whole purpose is to interrupt a pattern the person asked to
have interrupted — a rest reminder, a spending limit — making the interruption
cheaper to wave away can defeat the point. Sometimes the correct answer to "this
takes too many steps" is "yes, and the alternative costs none".

Obviously abusable. It applies only where the person **asked** for the friction,
and never to anything they did not choose.

Russian mirror: [`positions.ru.md`](positions.ru.md).
