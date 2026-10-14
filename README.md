# CS2114-Project2-Group94

PokePull: a deck builder and card collector for Gen 1 Pokémon, built for CS 2114.
The user starts with 10 cards and 2 or 3 packs, opens packs for more cards,
builds and saves 60-card decks (at most 4 copies of any card), and draws an
opening hand of 7.

## Layout

- `src/pokepull/` application code
  - `LinkedChain.java` the provided generic chain (storage only)
  - `Card`, `Rarity`, `Deck`, `Pack`, `CardCatalog`, `PokePull`
- `test/pokepull/` JUnit 5 tests

## Setup (Eclipse)

1. File > Import > General > Existing Projects into Workspace, and pick this folder.
2. If Eclipse reports a JRE or JUnit problem, open Project > Properties > Java Build Path and fix the JRE System Library and JUnit 5 library entries.
3. Run `pokepull.PokePull` to play. Run the `test` folder as JUnit tests.

## Reflection

### What changed from our specification?

- **LinkedChain API.** Our spec guessed at a LinkedChain API before we had the
  real one, and three methods we planned do not exist (`removeAt`, `copy` and
  `iterator`). We wrote the code against the real API and dropped those three
  from the plan.
- **`Deck.openingHand` became `Deck.drawCards(int, Random)`.** The spec had
  `openingHand` drawing from a copy. Drawing the hand from a working copy of
  the deck in `PokePull.startHand` and then using the rest as the draw pile
  was simpler, because the hand and later draws must come from the same pile.
- **`PokePull` methods return values or throw instead of printing.** The spec
  said `openPack` and `startHand` print messages. We made them return the
  cards (or throw), and only `run()` prints. This keeps console I/O out of the
  game rules and lets JUnit test them without a Scanner. `drawCard` and
  `startHand` throw instead of printing "deck is empty" or "no such deck".
- **Where validation lives** is mostly as specified: `Deck`, `Card` and `Pack`
  throw, and `PokePull` explains the problem to the user. One difference: the
  menu catches exceptions in one place (`run()`) instead of at every call.
  `deleteDeck` still returns false, and the menu turns that into "no such deck".
- **Deck ownership rule.** The spec said a deck may not hold more copies of a
  card than the user owns. A new player owns about 30 cards, so a 60-card deck
  could never be built. We built a looser rule: a card can be added if the user
  owns at least one copy; the 4-copy rule still applies. In 2,000 seeded games
  a player who opens all starting packs always owns at least 16 different
  cards, and 15 are enough for a 60-card deck.
- **New class `CardCatalog`.** It holds the 49-card Gen 1 catalog the packs draw
  from, so `Pack` and `PokePull` stay free of card data.
- **Added methods.** `Deck.toArray()` and a package-level `Deck.copyOf`
  (copy a chain of cards), plus `PokePull.findOwned` and a few getters
  (`getPacksOwned`, `getCollectionSize`, `getDeckCount`) that tests use.
- **Pack details.** The rarity roll is 0..999 with five tiers (60/25/10/4/1 %).
  The rare guarantee is built in by picking one slot in the pack up front and
  making it at least RARE, rather than replacing a card afterward. If a tier
  has no cards, `pickCard` tries lower tiers, then higher tiers.
- **Tests.** The spec listed tests for every LinkedChain method; since that
  class was given to us, we dropped those and added `LinkedChainReuseTest`,
  which shows one class storing three element types. There are 62 tests.

### What changed in the LinkedChain?

Nothing, apart from adding the line `package pokepull;` at the top so it
lives in our package. We use it through its public API only: `add`,
`addToEnd`, `remove(T)`, `get`, `contains`, `count`, `size`, `isEmpty`,
`clear` and `toArray`. Anything we needed beyond that is built in the app
classes: for example, `Deck.copy` reads the chain with `toArray` and rebuilds
it, and `Deck.drawRandom` uses `get` then `remove(T)` (safe, because cards
with the same name are interchangeable).

The same LinkedChain class stores `Card` (collection, deck, pack, hand),
`Deck` (saved decks) and `String` (a seen-names list used when showing the
collection).

### What happened to our unknowns?

- **Weighted draws: figured out.** A roll of 0 to 999 is compared to
  cumulative cutoffs, one per rarity tier, in `Pack.rollRarity`. The weights
  are constants and are a first guess; how good they feel in play is open.
- **Testing randomness: mostly figured out.** `Random` is passed in, so tests
  use fixed seeds. The weights are checked statistically (1,000 packs, within
  3 percentage points). The rare guarantee is checked by never allowing five
  packs in a row without a RARE-or-better card.
- **Data relationships: figured out.** `Card` is immutable, so the collection,
  decks, packs and hands all hold references to the same `Card` objects and
  nothing is copied. Ownership is checked with `collection.count` and
  `contains`.

### What doesn't work yet?

- **Card-limit swap.** At the 4-copy limit the builder refuses the card and
  tells the user to use `remove <name>`, but it does not ask "do you want to
  swap?" as an interactive question.
- **Deck builder tests.** The full interactive builder (adding 60 cards by
  typing) has no end-to-end test; we test the menu's blank-name, cancel and
  fifth-copy paths, and the `Deck` and `PokePull` methods behind it.
- **Duplicate deck names in the menu** are rejected by `saveDeck` (tested) but
  the menu itself can't be tested for it, because `run()` always starts a new
  game with no saved decks.
- **No saving to disk.** The game resets when the program quits.
- **Catalog size.** 49 cards, not all 151.
- **Trading** lets the user give up any five cards, including ones they own
  only one of.

## Next Steps

**To finish the MVP, in order:**
1. Add the interactive "swap a card out" prompt at the card limit.
2. Test the deck builder end to end, with scripted input that builds a full deck.
3. Let the user see a saved deck's contents, not just its size.
4. Tune the rarity weights and playtest the pack and trade economy.
5. Fill out the catalog beyond 49 cards.

**Stretch goals.** We are committing to one: the weighted randomization
(rarity), which is already in as five tiers. We are dropping random deck,
duplicate trading and a separate pack-inventory element type.

**New tests needed:** the swap prompt (accepts a card to remove, refuses one
not in the deck), a full scripted deck build, deck contents display, and any
catalog rows we add (bad rows rejected, all five tiers present).

**Who does what:**

| Item | Owner |
|------|-------|
| Swap prompt at the card limit | TBD |
| End-to-end deck builder test | TBD |
| Deck contents display | TBD |
| Weight tuning and playtesting | TBD |
| Catalog expansion | TBD |

## Attribution

| File | Who | How |
|------|-----|-----|
| `LinkedChain.java` | CS 2114 staff | Provided; we only added the package line |
| `Rarity.java` | TBD | GenAI-assisted (drafted by Claude) |
| `Card.java` | TBD | GenAI-assisted (drafted by Claude) |
| `Deck.java` | TBD | GenAI-assisted (drafted by Claude) |
| `Pack.java` | TBD | GenAI-assisted (drafted by Claude) |
| `CardCatalog.java` | TBD | GenAI-assisted (drafted by Claude) |
| `PokePull.java` | TBD | GenAI-assisted (drafted by Claude) |
| `CardTest.java` | TBD | GenAI-assisted (drafted by Claude) |
| `DeckTest.java` | TBD | GenAI-assisted (drafted by Claude) |
| `PackTest.java` | TBD | GenAI-assisted (drafted by Claude) |
| `PokePullTest.java` | TBD | GenAI-assisted (drafted by Claude) |
| `LinkedChainReuseTest.java` | TBD | GenAI-assisted (drafted by Claude) |

**GenAI use.** We used Claude (Claude Code) to help turn our scope and spec
into this prototype. It drafted the specification from our scope, wrote the
first version of the classes and tests above, compiled them and ran the 62
tests, and wrote this README. The design choices that came from the team were
the scope itself, the decision to have a single reach goal (weighted
randomization), and the five rarity tiers. Teammates should replace each
"TBD" above with who reviewed, revised or tested each file.
