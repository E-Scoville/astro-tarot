# astro-tarot

An Android tarot app where nothing is random: the positions of the planets at
the moment of the reading determine which cards appear.

## How it works

An offline ephemeris calculator computes planet positions, houses, and aspects
for any place and time (GPS, searched location, or manual coordinates;
present, historical, or future dates). Each card in the Rider–Waite–Smith deck
is weighted by its astrological correspondences — ruling planet or sign for
the Major Arcana, decans for the numbered minors, degree ranges for the
courts, elements for the aces — plus bonuses from active aspects. Cards are
then drawn by weighted sample; a card drawn despite low planetary weight
may arrive reversed — the odds rise the further below average the sky
weighted it — tagged with the retrograde or tension aspect that best
explains the resistance.

## Structure

- **`engine/`** — pure Kotlin module: ephemeris calculator, aspect
  calculation, card weighting, and spread logic. No Android dependencies;
  fully unit-tested.
- **`app/`** — Jetpack Compose UI in an Art Nouveau style: spread picker
  (single card, Three Angles, Seven Planets, Twelve Houses), tap-to-reveal
  card lore, planetary influence display, and persistent reading history.

## Building

Standard Gradle Android build: `./gradlew :app:assembleDebug`. Tests:
`./gradlew :engine:test :app:testDebugUnitTest`.
