# LABS-MOBILE-3LBC-BIS

EVERYTHING YOU NEED IN LABS IN ESPRIT SCHOOL OF BUSINESS

Solutions to the Mobile Development labs (3 LBC-BIS, 2026-2027): Android, **Java**, **XML Views**, Groovy Gradle DSL.

## Labs

| Module | Labs | App | What it covers |
| --- | --- | --- | --- |
| [`lab1-2-esb`](lab1-2-esb) | LAB 1, LAB 2 | ESB | First project, string resources, translations (en / fr / ar), `LinearLayout` form, `android:onClick`, Toast and dialog pop-ups, `TableLayout` and `ConstraintLayout` homework |
| [`lab3-4-esb2025`](lab3-4-esb2025) | LAB 3, LAB 4 | ESB_2025 | Figma prototype to XML (colors, shape drawables, density-specific images), input validation, `OnClickListener`, second Activity, `Intent` extras, `finish()` |
| [`lab5-tpdm`](lab5-tpdm) | LAB 5 | TPDM | Employee management: sign-in, registration, admin home, employee list with full SQLite CRUD, plus the six bonus exercises |

Written answers to the LAB 5 comprehension questions: [`docs/LAB5_ANSWERS.md`](docs/LAB5_ANSWERS.md).

## Open and run

1. Android Studio > **Open** > select this folder (the repository root, not a module).
2. Wait for the Gradle sync.
3. Pick the module in the run configuration list (`lab1-2-esb`, `lab3-4-esb2025` or `lab5-tpdm`) and press **Run**.

From a terminal:

```bash
./gradlew assembleDebug        # build the three APKs
./gradlew lintDebug            # static analysis
./gradlew :lab5-tpdm:installDebug   # install one app on a running emulator/device
```

Requirements: Android Studio with JDK 17+, Android SDK Platform 36. `minSdk` is 24.

### Test accounts

| App | Username | Password |
| --- | --- | --- |
| ESB_2025 (LAB 4) | `Admin` | `Admin` |
| TPDM (LAB 5) | `admin` | `1234` |

## Project structure

```
├── build.gradle, settings.gradle     root build, one Gradle project for all labs
├── gradle/libs.versions.toml         version catalog: every SDK and library version in one place
├── lab1-2-esb/                       com.example.esb
├── lab3-4-esb2025/                   com.example.esb_2025
├── lab5-tpdm/                        com.example.tpdm
│   └── src/main/
│       ├── java/com/example/tpdm/    User (model), SetupSqliteDB + UserDB (data), *Activity (screens)
│       └── res/                      layout, values (strings, colors, styles, arrays), drawable
└── docs/                             written answers
```

## Conventions followed

- **No hard-coded UI text**: every label and message lives in `res/values/strings.xml`, with translations in `values-fr` (and `values-ar` for LAB 2 to 4).
- **No hard-coded colors or repeated attributes**: colors in `colors.xml`, shared sizes in `dimens.xml`, reusable widget looks in `styles.xml` and shape drawables.
- **Constants instead of magic strings**: Intent extra keys (`Dashboard.EXTRA_USERNAME`, `HomeActivity.EXTRA_USER`), roles (`User.ROLE_ADMIN`), and table/column names defined once in `SetupSqliteDB`.
- **Safe SQL**: user input only goes through `?` placeholders; every `Cursor` is closed (try-with-resources); the database is opened in `onCreate` and closed in `onDestroy`.
- **Layouts**: `start`/`end` instead of `left`/`right` (RTL-ready for Arabic), `ScrollView` roots so forms stay usable with the keyboard open, `contentDescription` on images, `autofillHints` on inputs.
- **Back stack**: `finish()` after login and logout so Back never returns to a screen the user should not see.
- **Naming**: Java classes in `PascalCase`, methods and fields in `camelCase`, resources in `snake_case`. Identifiers imposed by the lab handouts (`btnconnect1`, `txtformal`, `mot_de_pass`, `afficherEmployees`, ...) are kept as written so the code matches the instructions.
- **Git**: build outputs, `.gradle`, `.idea` and `local.properties` are ignored; commits follow `type(scope): message`.

## Differences from the handouts

- The three projects are modules of one Gradle build instead of three separate projects, so the repository opens once in Android Studio.
- `compileSdk`/`targetSdk` are 36 (the handout mentions API 37); change the two values in `gradle/libs.versions.toml` if API 37 is installed.
- LAB 2 `constraint.xml` and the LAB 3 screen are rebuilt from the written description of the handout screenshots.
- LAB 5 additions beyond the handout: registration checks the username alone (the handout compared username + password), editing an employee refuses a username already taken, and the admin home has a "Create a user" button (bonus 5).

## Known limitation

LAB 5 stores passwords in clear text in SQLite, as the handout requires (the edit form displays them). A real application must store a salted hash instead.
