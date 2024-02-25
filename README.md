# Background Location Tracking App
## Overview
This Android app allows users to track their location in the background and store location data for later use.
## Setup and Usage
1. **Clone the Repository:**
   - Clone this repository to your local machine using `git clone https://github.com/tridevdeka/Run-Track.git`.
2. **Open in Android Studio:**
   - Open the project in Android Studio.
3. **Build and Run:**
   - Build and run the project on an Android device or emulator.
## Libraries Used
- **AndroidX Libraries:**
  - Purpose: AndroidX libraries provide backward compatibility and additional features.
  - Integration: Added AndroidX dependencies in the `build.gradle` file.
- **Jetpack Navigation:**
  - Purpose: Jetpack Navigation simplifies navigation between different screens in the app.
  - Integration: Integrated Jetpack Navigation by adding the Navigation component in the `build.gradle` file and setting up navigation graphs.
- **Google Play Services Location API:**
  - Purpose: Used for accessing the device's location services for background location tracking.
  - Integration: Added dependency in the `build.gradle` file and implemented necessary permissions and code to request and receive location updates.
- **Google Maps API / Android MapView:**
  - Purpose: Used to visualize the user's location on a map.
  - Integration: Integrated either the Google Maps API or Android's MapView by adding the necessary dependencies in the `build.gradle` file and implementing the map view in the layout XML files.

- **SQLite Database:**
  - Purpose: Used for storing location data locally.
  - Integration: Utilized SQLiteOpenHelper or Room Persistence Library to create and manage the local database. Implemented necessary data models and DAOs for interacting with the database.

- **MVVM Architecture:**
  - Purpose: MVVM (Model-View-ViewModel) architecture separates the UI logic from the business logic, making the app more modular and easier to maintain.
  - Integration: Implemented MVVM architecture by structuring the app into models, views, and view models. Used data binding to connect views with view models.

- **Hilt Dependency Injection:**
  - Purpose: Hilt simplifies dependency injection in Android apps, making it easier to manage dependencies and improve code organization.
  - Integration: Integrated Hilt by adding the necessary dependencies in the build.gradle file and setting up Hilt modules and components for dependency injection.

- **Firebase Firestore:**
- Purpose: Utilizes Firebase Firestore for cloud-based storage of location data, enabling synchronization across devices.
  
- **Coroutines:**
- Purpose: Uses Kotlin Coroutines for asynchronous and non-blocking programming, enhancing app responsiveness and performance.
- 
- **Glide:**
- Purpose: Integrates Glide for efficient loading and caching of images, particularly useful for displaying map markers.
