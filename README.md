# MAD test project

This test project aims at using Jetpack libraries and a multi-module structure as outlined in the
Modern Android Development docs, but with a twist. It's basically MVVM+ if you will. 

The app is a simple Pastebin app. It is connected to the omg.lol Pastebin API and is able to fetch
pastes from there and stores them in a database.

The code contains extensive comments for further thinking and debates.

## Main Goals

1. Find out how well MAD generally works nowadays.
2. How good can UI state be reflected and kept separately in the view model? No `rememberSaveable`
unless already baked into UI components.
3. How well can we strip UI of logic?
4. To what extend can we facilitate separation of concerns using a multi-module multi-layered approach? And how to split up the models in a meaningful way?
5. How can one-shot events (like the error Snackbar) be handled?

## Libraries used

Check `libs.version.toml`. Generally I tried to stay as close to MAD suggested libraries as I could.

## Gradle Modules

The project is set up as a multi-module project. The main upside is separation of concerns and the
need to define clean APIs and dependencies. A nice side-effect is faster build times (after the
initial sync and build).

Here is a quick overview over the modules and what they do:

| Module        | Notes                                                                                                              |
|---------------|--------------------------------------------------------------------------------------------------------------------|
| app           | Entrypoints and entrypoint definitions to the app as well as main navigation                                       |
| core-model    | Common models shared between all layers of the app (layer/component specific models are in the respective modules) |
| core-util     | Common util classes and functions                                                                                  |
| core-l10n     | Common localizable resources                                                                                       |
| core-testing  | Common test utils                                                                                                  |
| core-ui       | Common UI classes and theming                                                                                      |
| core-platform | Common platform classes and system service (usually mostly used by middle and lower layers)                        |
| core-data     | Data layer with repositories                                                                                       |
| core-database | Local data sources and model definitions (should probably rather be called core-local-storage)                     |
| core-network  | Remote data sources and model defintions (should probably rather be called core-remote)                            |
| feature-login | Login Screen UI and VM                                                                                             |
| feature-paste | Pastes Screen UI and VM                                                                                            |
| test-app      | Instrumentation tests (doesn't work yet)                                                                           |

Most modules also contain some tests to show how testing might look like. They are by no means complete
and in many cases not even very meaningful. 

## What works

- Fetching pastes using correct the API key and user name.
- Storing new pastes in the local DB.
- Falling back to local DB if data cannot be fetched from the API.
- Tapping on a Paste item copies it to the clipboard.
- Error handling and presentation from the bottom all the way to the top on all layers up to the view layer.
- Force refreshing via an error snackbar to attempt recover from API issues.
- Full support for orientation change state keeping and state restoration after system induced process 
death and restoration (input fields, cursor state, scroll state, navigation state)
- Dynamic start destination for navigation based on whether the user is logged in or not.
- Dynamic color theme FWIW.

## What doesn't work or isn't fledged out (mostly because it was out of scope for this project)

- Log in comes with no form of authentication verification - we store whatever you type in as user
name and API key and try to use that for loading pastes from the omg.lol API.
- You cannot get change your login details after having "logged in" once.
- When adding a paste you it will only be stored in the database, but never actually be uploaded.
- There is no clever caching except for simply storing everything in the local database. The app will
always attempt to load data freshly from the API when it is started.
- Error handling is there and works, but there are tons of possible errors (e.g. around logging in)
which for aren't covered.
- No localization except for EN.
- UI is barely serviceable, no animations and fancy layouts and stuff. The focus for this project was
distinctly on the architectural side of things.
- Only rudimentary tests (with no guarantee that all work (especially the instrumentation ones)).
They are rather here to show how testing could look like in such a project and how well testable the
code is.
- There is no dedicated logging setup, just plain old Logcat.
- No Use Cases/interactors (the app was simply too small for such reusable architecture elements)
- KAPT - yes, it's not an issue with the code per se, but it obviously makes compilation times slower.
Unfortunately Hilt/Dagger only have very rudimentary early KSP support for now.
- There is duplicated code in the module build scripts - I might add a convention plugin later on.
- Check the TODOs in code for more stuff and details.

## Known bugs (I haven't have time to fix up until now)

- When closing the app via BACK you may see `Client error while fetching and/or storing pastes: Job was cancelled` in the log
after previously having entered a paste. Everything will still be saved properly.
- After having logged in for the first time and then pressing back on the Pastes Screen, the screen
will open once again and it will load its content once more. There likely is a super stupid issue
in the dynamic start destination handling which is causing this.
