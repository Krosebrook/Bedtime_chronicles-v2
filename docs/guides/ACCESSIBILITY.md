# Accessibility Best Practices — Bedtime Chronicles

**Standard:** WCAG 2.1 AA (Mobile compliance)  
**Target Audience:** Children ages 3–9 and parents.  
**Programming Environment:** Native Android (Kotlin & Jetpack Compose)  

---

## 🧭 1. General Mobile Accessibility Requirements

When crafting layout parts or screen overlays inside the Jetpack Compose environment, you must adhere strictly to these accessibility guidelines to ensure an inclusive, delightful experience for young children and parents.

| Rule | Applies To | Compose Mechanism |
|------|------------|-------------------|
| **Content Descriptions** | Images, Icons | `contentDescription: String?` |
| **Ergonomic Touch Targets** | Tappable areas, Buttons | Minimum target of `48.dp` x `48.dp` |
| **Semantic Roles & Actions** | Custom widgets, Row cards | `Modifier.clearAndSetSemantics { ... }` or `role = Role.Button` |
| **System Typography Scaling** | Screen headings, body text | Strictly utilize type scale in `sp` (never hardcoded `dp`) |
| **High Contrast UI Modes** | Cosmic background, cards | Midnight Velvet Color scheme contrast compliant (>= 4.5:1) |
| **Motion Respect** | Enter/Exit transitions, fades | Honor `LocalConfiguration.current` or Preferences for reduced motion |

---

## 🗣️ 2. Content Descriptions for Non-Text Media

Every interactive or visual asset (such as profile avatar picker choices, back arrows, or cover art illustrations) MUST define a clean description, unless declared purely decorative.

### ✅ Good Pattern (Jetpack Compose)
```kotlin
// Decorative spacer backgrounds can pass null
Image(
    painter = painterResource(id = R.drawable.ic_cosmic_space_wind),
    contentDescription = null
)

// Interactive navigation actions require clear explanations
IconButton(
    onClick = { onBackClick() },
    modifier = Modifier.testTag("back_button")
) {
    Icon(
        imageVector = Icons.Default.ArrowBack,
        contentDescription = "Go back to Home dashboard"
    )
}
```

---

## 🔘 3. Touch Target Densities

Young readers have developing motor coordination skills, requiring larger, highly accessible hitboxes.
- All tappable cards, checkbox controls, seek slots, and navigation icons **MUST** extend their interactive boundary to at least **`48.dp` x `48.dp`**.
- Compose inherently enforces this with standard interactive components. For custom icons or visual widgets, declare padding or size values explicitly:

```kotlin
// Ensure custom widgets preserve accessibility limits
Box(
    modifier = Modifier
        .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
        .clickable { /* action */ }
        .testTag("custom_card_activator"),
    contentAlignment = Alignment.Center
) {
    Text("Select", style = MaterialTheme.typography.labelLarge)
}
```

---

## 🎨 4. Typographic & Color Access

- **Flexible Spacing**: Never employ hardcoded container heights (like a fixed `height(40.dp)`) for elements wrapping text. Dynamic scaling fonts can overflow rigid bounds. Use standard multipliers or margins.
- **Midnight Velvet Contrast Color Scheme**: Maintain high visual legibility on cosmic dark canvases:
  - Primary text utilizes high-intensity colors (e.g. `Cream White` / `Soft Silver`) on Midnight background tokens.
  - Secondary attributes use high-contrast text tags (e.g., gold or coral highlight accents).
- **Font Scaling Units**: Headings and paragraphs must declare dimensions in `sp` to ensure the operational OS accessibility font-sizer matches successfully.

---

## 🎬 5. Reduced Motion Compliance

For children sensitive to rapid animation sweeps or parallax layers, respect reduced-motion properties:
- Configure entry/exit animations on screens utilizing comfortable, simple transitions.
- Check user preference variables to switch off particle effects or active physics canvases dynamically in screens.
- Keep duration periods smooth and moderate.
