# MovieFlix

MovieFlix is an Android app for browsing popular movies from the TMDB API.

Users can explore popular movies, load additional pages, save favorites, 
view detailed informations, and access the first page while offline.

## Features

### Home
- Popular movies with title, image, released date, rating and favorite status.
- Unfinite scrolling
- Pull-to-refresh
- Loading, empty and error state
- Retry support for initial loading, refresh and pagination
- Persistent favorites

### Movie details
- Title, image, genres, release date, rating, runtime and overview
- Cast information
- Up to 3 reviews
- Up to 6 similar movies
- Favorite support
- Navigation to similar movie
- Sharing functionality - Hideable if homepage is not available

### Offline support
- Page 1 movie data is stored in Room
- Page 1 images are prefetched in Coil cache
- Favorite IDs are stored with DataStore
- Cached movies and images are available after re-opening the app without internet connection

> Only first page is available offline
> Movie details required internet connection


## Architecture

### Home: MVI

The home screen uses MVI because it manages several states at the same time,
including loading, refresh, pagination, offline and error states.

User Action
↓
HomeIntent
↓
HomeViewModel
↓
HomeState
↓
HomeScreen

Navigation is emitted as a one-time action.

### Details: MVVM

The details screen uses MVVM. The ViewModel loads the movie data and combines it 
with the current favorite IDs.

Reviews and similar movies are optional. If one of these requests fails, the main
movie details can still be displayed.

Reviews and similar movies are optional. If one of these requests fails, the
main movie details can still be displayed.


## Offline Implementation

The offline implementation uses different tools for different types of data:

- **Room** stores page 1 movie metadata
- **Coil** stores the downloaded image data
- **DataStore** stores favorite movie IDs

Room also stores the image URL because Coil uses the same URL as the disk-cache
key when loading the image offline.

The original API order is preserved by storing the position of every movie in
the Room entity.

TMDB accepts pages from 1 to 500, so remote pagination is capped at 500. Cached
results report only one available page because only page 1 is stored locally.

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- MVI and MVVM
- Kotlin Coroutines, Flow, and StateFlow
- Hilt
- Retrofit and OkHttp
- Kotlinx Serialization
- Room
- Preferences DataStore
- Coil 3
- Navigation Compose with typed routes
- JUnit, MockK, and Coroutines Test

### API Key
I used the Api key cause the token was not working
