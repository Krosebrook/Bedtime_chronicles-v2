---
name: Cosmic
colors:
  surface: '#0d0d2b'
  surface-dim: '#10112a'
  surface-bright: '#363752'
  surface-container-lowest: '#0b0b25'
  surface-container-low: '#191933'
  surface-container: '#1d1d37'
  surface-container-high: '#272742'
  surface-container-highest: '#32324d'
  on-surface: '#e1dfff'
  on-surface-variant: '#c7c4d7'
  inverse-surface: '#e1dfff'
  inverse-on-surface: '#2e2e49'
  outline: '#908fa0'
  outline-variant: '#464554'
  surface-tint: '#c0c1ff'
  primary: '#c0c1ff'
  on-primary: '#1000a9'
  primary-container: '#8083ff'
  on-primary-container: '#0d0096'
  inverse-primary: '#494bd6'
  secondary: '#ffb95f'
  on-secondary: '#472a00'
  secondary-container: '#ee9800'
  on-secondary-container: '#5b3800'
  tertiary: '#c8c5d0'
  on-tertiary: '#302f38'
  tertiary-container: '#928f9a'
  on-tertiary-container: '#2a2931'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#e1e0ff'
  primary-fixed-dim: '#c0c1ff'
  on-primary-fixed: '#07006c'
  on-primary-fixed-variant: '#2f2ebe'
  secondary-fixed: '#ffddb8'
  secondary-fixed-dim: '#ffb95f'
  on-secondary-fixed: '#2a1700'
  on-secondary-fixed-variant: '#653e00'
  tertiary-fixed: '#e5e1ed'
  tertiary-fixed-dim: '#c8c5d0'
  on-tertiary-fixed: '#1b1b23'
  on-tertiary-fixed-variant: '#47464f'
  background: '#10112a'
  on-background: '#e1dfff'
  surface-variant: '#32324d'
  elevated: '#141430'
  glass: rgba(255, 255, 255, 0.03)
  glassBorder: rgba(255, 255, 255, 0.1)
  accentDim: rgba(99, 102, 241, 0.2)
  textMuted: '#8899aa'
typography:
  display:
    fontFamily: Bangers
    fontSize: 48px
    fontWeight: '400'
    lineHeight: 56px
  headline-xl:
    fontFamily: Bangers
    fontSize: 30px
    fontWeight: '400'
    lineHeight: 38px
  headline-lg:
    fontFamily: Bangers
    fontSize: 24px
    fontWeight: '400'
    lineHeight: 32px
  body-lg:
    fontFamily: Nunito Sans
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  body-base:
    fontFamily: Nunito Sans
    fontSize: 17px
    fontWeight: '400'
    lineHeight: 24px
  label-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 15px
    fontWeight: '600'
    lineHeight: 20px
  label-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 13px
    fontWeight: '500'
    lineHeight: 18px
  caption:
    fontFamily: Plus Jakarta Sans
    fontSize: 11px
    fontWeight: '400'
    lineHeight: 14px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  touch-target-min: 48px
  touch-target-large: 56px
  margin-screen: 24px
  gutter: 16px
  stack-sm: 8px
  stack-md: 16px
  stack-lg: 32px
---

## Brand & Style

The design system is a deep-space, immersive aesthetic tailored for children ages 3-9, specifically designed to facilitate a gentle and friendly storytelling experience. It balances a high-tech "Cosmic" look with the warmth required for a bedtime companion.

The style is a sophisticated **Glassmorphism** execution set against a **Minimalist** deep-space backdrop. It relies on translucent layers, soft background blurs, and vibrant "starlight" accents to create a sense of infinite depth and wonder. Despite the technical theme, the interface remains approachable through the use of ultra-rounded corners and oversized, child-friendly touch targets.

**Design Principles:**
- **Cosmic Depth:** Use of star-field backgrounds and glowing orbs to create a sense of space.
- **Gentle Immersion:** Always dark mode to preserve "sleep hygiene" and create a calming bedtime environment.
- **Tactile Softness:** Elements should feel "squishy" and safe, avoiding any sharp edges or aggressive transitions.
- **High Clarity:** Information is sparse and clear, ensuring the UI never overwhelms the primary storytelling content.

## Colors

The palette is anchored in the deep void of space. The **Primary** color (Accent Indigo) drives interaction, while the **Secondary** (Gold) is reserved for celebration, badges, and achievements.

**Color Usage Guidelines:**
- **Backgrounds:** Use the pure dark neutral (`#05051e`) for all primary screen backgrounds.
- **Surfaces:** Use `surface` for bottom sheets and `elevated` for modals to create depth.
- **Glassmorphism:** Apply `glass` as the fill for cards and `glassBorder` for the 1px stroke.
- **Accessibility:** Ensure all text achieves a minimum contrast ratio of 4.5:1. Avoid using `glassBorder` or low-opacity whites for critical text; use `textMuted` or `starlight` instead.
- **Voice Chat:** Use the Primary color for the active "Pulsing Orb" to signify the companion is listening or speaking.

## Typography

The typography system uses three distinct fonts to separate "flavor," "content," and "utility."

- **Bangers (Display/Headlines):** Used for story titles, hero names, and celebratory milestones. It provides a playful, comic-book energy.
- **Nunito Sans (Body):** The primary storytelling font. Its rounded terminals make it warm and highly legible for early readers.
- **Plus Jakarta Sans (UI/Labels):** Used for navigation, settings, and system chrome. It provides a clean, modern structure to the application.

**Accessibility & Scaling:**
- **Large Text Mode:** The design system supports a "Large" setting which multiplies all base sizes by 1.15x.
- **Reading Comfort:** Line heights are generous (1.4x+) to assist children with tracking text during bedtime reading.

## Layout & Spacing

The layout philosophy follows a **Fixed Grid** on larger devices and a **Fluid Content Model** on mobile, optimized strictly for portrait orientation.

**Key Layout Rules:**
- **Child-Centric Targets:** Every interactive element must meet a minimum 48px touch target. For toddlers (ages 3-5), primary actions should be 56px.
- **Safe Areas:** All layouts must respect `useSafeAreaInsets` to ensure content is not obscured by "notches" or the home indicator.
- **The 8pt Rhythm:** All margins, padding, and gaps should be multiples of 8px to maintain a consistent visual cadence.
- **Vertical Padding:** The bottom of every screen should include a 60px offset plus the safe area inset to accommodate the persistent tab bar.

## Elevation & Depth

This system rejects traditional shadows in favor of **Tonal Layers** and **Backdrop Blurs** to simulate cosmic depth.

- **Level 0 (Background):** Pure `#05051e` with an animated StarField layer.
- **Level 1 (Glass Surface):** Semi-transparent white (`0.03` opacity) with a `10px` to `20px` backdrop blur. This is the standard for cards and content containers.
- **Level 2 (Elevated):** Solid `#141430` for modals and secondary sheets that need to feel physically closer to the user.
- **Glow Accents:** Use subtle outer glows (`shadowColor: colors.accent, shadowOpacity: 0.3, shadowRadius: 15`) instead of drop shadows to indicate active or "magical" states.

## Shapes

The shape language is consistently **Rounded**, ensuring the UI feels safe and friendly for children.

- **Standard Cards:** `16px` radius (rounded-lg).
- **Interactive Buttons:** `12px` radius.
- **Profile Avatars & Action Orbs:** Always use fully circular/pill shapes (50% or higher).
- **Glass Borders:** All glass containers must feature a 1px border with `colors.glassBorder` to define edges against the starfield background.

## Components

### Buttons
- **Primary CTA:** Background `accent`, white text, 12px radius, min-height 48px.
- **Secondary:** Background `accentDim`, text `accent`, 12px radius.
- **Voice Orb:** A 56px+ circular button that pulses when listening, utilizing a gradient or glow.

### Cards
- All cards use the **Glassmorphism Card Pattern**. They must have `overflow: 'hidden'` to ensure no child elements break the rounded corners.

### Lists & Inputs
- **Inputs:** Use `surface` as the background with 12px rounded corners and `starlight` text. 
- **Checkboxes/Radio:** Must be oversized (minimum 32px visual size within a 48px hit area) for easier interaction.

### Voice Chat UI
- The voice interface should feature a **Pulsing Orb** at the center of the screen, creating a rhythmic visual heartbeat that matches the "gentle companion" narrative.

### Feedback & Celebration
- **Badges:** Displayed as circular cards with a high-contrast emoji and a gold (`colors.gold`) border upon achievement.
- **Confetti:** Full-screen particle animation using primary and secondary colors upon story completion.