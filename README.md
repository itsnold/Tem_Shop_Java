# Tem Shop — Undertale store screen in Java Swing

A hand-coded Java Swing recreation of the **Tem Shop** (`room_shop5`) from
*Undertale*. One single-page store screen: buy/sell/talk/exit without leaving
the shop, with a simulated gold balance and inventory.

Visual reference: the original Tem Shop (`room_shop5`) as documented at
https://undertale.wiki/w/Tem_Shop — layout, wares, prices, and dialogue
mapped 1:1 in this recreation.
Recreation screenshots: `build/preview_idle.png`, `build/preview_buy.png`,
`build/preview_joy.png`, `build/preview_nervous.png`,
`build/preview_graduated.png`.
Artwork, audio, and typeface assets are bundled under `assets/`.

## Run

Requires Java 17 or newer. On Windows, double-click `run.bat` or run it from a
terminal. It compiles `src/main/java` into `build/` and launches the app:

```bat
.\run.bat
```

No connection to the real game is needed. If music cannot load, the app stays
silent and reports the error in the console.

## Play

- **Up / Down** or **W / S** — move the heart selector
- **Enter** or **Space** — open Buy/Sell/Talk/Exit, confirm a purchase or sale
- **Esc** — back to the main menu, or leave from the main menu
- **M** — toggle music
- **G** — demo helper: +500G (for testing the 1000G college purchase)
- **Mouse** — click a main-menu option; in Buy/Sell, click once to select and
  again to confirm; the last row is `Exit`

The player starts with **145G** and **4/8** inventory slots filled.

## Store behavior (same page, no checkout)

- **Buy**: 10 wares — `tem flake` 3G, `tem flake (SALE)` 1G,
  `tem cupcake` 8G, `tem juice` 12G, `tem flake (EXP)` 20G,
  `tem donut` 30G, `tem egg` 50G, `tem scarf` 150G, `tem hat` 300G,
  `tem pay 4 colleg` 1000G. Success checks funds and space, deducts gold,
  adds the item, shows `thanks PURCHASE!` with Temmie's joy face, and updates
  `G` and `n/8` on the same screen. Consumables are repeatable.
  The list scrolls (4 visible rows + `Exit`, arrow markers, Up/Down wraps)
  so all 10 fit the single page without overlapping the G / n/8 status.
- **College (one-time)**: buying `tem pay 4 colleg` with 1000G graduates
  Temmie — black cap appears, the tenth ware becomes `temy ARMOR`, and the
  college entry cannot be bought again.
- **Failed purchase**: without enough gold, the sale is blocked, nothing is
  deducted, and Temmie shows her nervous sweat face with
  `you don hav da muns,`. A full 8/8 bag shows `WAO!! bag FULLS!`.
  The shop stays usable after either message.
- **Sell**: only items you own (college excluded). Success adds
  half price (min 1G), removes one copy, and shows Temmie's joy face.
  Empty inventory shows `no more item...`.
- **Talk**: cycles authentic lines (`HOI!! im temmie`, deep history,
  `go to TEM SHOP!!!`, plus armor/degree lines after graduation).
- **Exit**: `bOI!!`, then the window closes. No login, browser, or real-money
  flow.

## Animated Temmie (original parts)

`src/main/java/com/temshop/Temmie.java` composes the shopkeeper each frame
from `assets/tem/` (body, brows, 7 eye sets, 3 mouth sets, 2 sell-faces,
sweat, steaming cup, graduation cap, authentic `TEM SHOP` counter box),
mirroring `obj_shop5_body`:

- Gentle bob, random blink (open/closed eyes), mouth alternation while the
  typewriter reveals text, animated cup steam and sell-face frames.
- Emotions follow events: idle browse, side-eye in Buy/Sell, joy on
  purchase/sale, nervous+sweat on broke, happy on full bag/talk, cap after
  college. If layered assets are missing, the app falls back to
  `assets/temmie.png` and then to drawn shapes.

## Lab requirements mapping

- **Single page**: Buy/Sell lists, item details (`TemShopPanel.java:438`),
  balance, and feedback all render on the one `TemShopPanel`; purchases never
  open another window. Exit closes the shop, matching the original.
- **A. Visual recreation**: original `bg_temshop` interior, Temmie assembled
  from 23 original sprites, real heart cursor (`assets/heart.png`), real UI
  sounds (`assets/sfx/`), 8bitoperator JVE typeface, authentic wares, prices,
  dialogue, and `mus_temshop` loop (original reference:
  https://undertale.wiki/w/Tem_Shop).
- **B. Balance**: starts at 145G, painted every frame
  (`TemShopPanel.java:216`), updated on every buy/sell.
- **C. Same-page purchase**: `buy()` (`TemShopPanel.java:327`) checks funds
  and 8-slot space, deducts gold, adds the item, updates `G` and `n/8`, and
  shows `thanks PURCHASE!` on the same screen. Consumables are repeatable.
  All 10 wares share one scrollable Buy list (4 visible rows + `Exit`).
- **D. Failed purchase**: broke shows `you don hav da muns,` + nervous face,
  full bag shows `WAO!! bag FULLS!`; nothing is deducted or granted and the
  shop stays usable.
- **E. One-time behavior**: `tem pay 4 colleg` graduates Temmie (cap appears,
  tenth ware becomes `temy ARMOR`, found by name so it survives the 10-item
  list) and cannot be bought again; every visible control (Buy/Sell/Talk/Exit) responds.
- **Hand-coded Swing**: no GUI builders; `JFrame` + hand-painted `JPanel`
  with `KeyStroke` bindings, `MouseAdapter`, and `Timer`s. No JavaFX/web.
- **Self-contained**: relative `assets/` + `lib/` paths only (verified: no
  absolute paths in `src/`); no network; missing assets fall back or stay
  silent instead of crashing.

## Code layout

- `src/main/java/com/temshop/TemShopApp.java` — `JFrame` bootstrap.
- `src/main/java/com/temshop/TemShopPanel.java` — hand-coded `JPanel`,
  `KeyStroke` bindings, `MouseAdapter`, two `Timer`s (typewriter + animator),
  balance/inventory/purchase rules, Undertale-style custom painting.
- `src/main/java/com/temshop/Temmie.java` — layered sprite loader/animator.
- `src/main/java/com/temshop/ShopItem.java` — item record.
- `src/main/java/com/temshop/MusicPlayer.java` — Ogg/WAV looper (`M` toggles).
- `src/main/java/com/temshop/SfxPlayer.java` — authentic UI sounds:
  `snd_squeak` on cursor move, `snd_select` on confirm, `snd_buyitem` on a
  completed purchase. Missing/unplayable files stay silent with a console note.
- No GUI builders or generated forms; Swing only (no JavaFX/web).

## Assets

- `assets/shop-background.png` — shop interior background
- `assets/temmie.png` — fallback composite sprite
- `assets/tem/*.png` — animated Temmie layers (see `assets/SOURCES.md`)
- `assets/mus_temshop.ogg` — original Tem Shop music
- `assets/sfx/*.wav` — original UI sounds (cursor, confirm, purchase)
- `assets/heart.png` — real SOUL heart cursor sprite (`spr_heartsmall`, 2x)
- `assets/fonts/8bitoperator_jvec.ttf` — the actual Undertale dialogue
  typeface, 8bitoperator JVE (see `assets/SOURCES.md`)
- `assets/tem-shop-loop.wav` — optional WAV music override
- `lib/jorbis-0.0.17.jar` — Ogg decoding (LGPL-2.1, see `lib/COPYING.LESSER`)
