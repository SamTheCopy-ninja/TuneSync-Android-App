# TuneSync

TuneSync is an offline music player app built for Android using Android Studio.  
The app allows users to listen to music from their local media storage (e.g. SD Card), and use this music to create playlists which also provide a `genre breakdown` in the form of a pie chart to show users what types of music preferences they have.  

Users can also access individual albums, listen to songs from a specific artist (which can also be filtered based on an `album release timeline`). Users can also access lyrics for the currently playing song, by leveraging the integrated API to search for lyrics. The app also allows users to search for concert events, view the the list of results, and click on any event link that will take them to the event organizer's webpage, where they can view more information about the event, or buy tickets if they are interested in the event.  

The app allows allows music playback to continue in the background even with the screen turned off, or if the user switches tabs to a different app on their device.

### View a demo video of the app and its features here: [Walkthrough Demo](https://youtu.be/LrQR7Y7G_lQ)  

![playstore](https://github.com/user-attachments/assets/aac289ad-2d8b-45e1-9609-60dd2fdb6fb0)

## Tune Sync is now on the Google Play Store! Download the app for your Android Device here: [Google Play Download](https://play.google.com/store/apps/details?id=com.tunesync.app)  

View some of our Play Store image assets below:  

![1](https://github.com/user-attachments/assets/239c685a-1faf-4e44-8df3-fe079f4a3ee8)

## Project Maintainer

TuneSync has been built by the `13th Coffin Software Development` company.  
Founder and Android Dev: `Samkelo Tshabalala - In the 13th Coffin`  
For any enquiries regarding this project, please find my contact information here: [Contact](https://www.linkedin.com/in/samkelo-tshabalala-tsts/)

## New Features and Updates

- **Enhanced Layouts**:  
  Redesigned layouts across the application for improved user experience and consistency.

- **Artist and Album Filtering**:  
  - Users can now filter both artists and albums using the Search View feature.
  - Artists are now displayed with the number of songs and albums associated with each artist.
  - Timeline feature now includes a reset option to clear filters.

- **Album and Artist View Options**:  
  - Users can switch between grid and list views for viewing albums and artists.

- **Offline Support**:  
  - Users attempting to retrieve lyrics without an internet connection are now notified of their offline status.
  - For email/password login, users without an internet connection are prompted to use fingerprint authentication instead.
  - **Offline Mode for Concert Searches**: Concert searches made offline are cached, allowing users to sync and perform these searches when back online.

- **Fingerprint Authentication**:  
  Fingerprint-based sign-in option added, providing a secure and convenient login experience.

- **Real-Time Notifications**:  
  Push notifications alert users to any pending concert searches.

- **Media and Language Enhancements**:  
  - Icon indicator added to show the currently playing song.
  - Language support expanded to include both English and Afrikaans.

- **Alphabetical Song Sorting**:  
  Songs are now consistently sorted in alphabetical order for easier navigation.


## Table of Contents
- [Features](#features)
- [Technical Stack](#technical-stack)
- [Screens and Navigation](#screens-and-navigation)
- [Music Playback](#music-playback)
- [User Account Management](#user-account-management)
- [Data Management](#data-management)
- [Additional Features](#additional-features)
- [Design Considerations](#design-considerations)
- [GitHub and CI/CD](#github-and-cicd)
- [Installation](#installation)
- [Usage](#usage)
- [Libraries Used and Code Attribution](#libraries-used-and-code-attribution)

## Features

- User account creation and authentication
- Local music playback from device storage
- Album and artist browsing
- Playlist creation and management
- Lyric fetching for currently playing songs
- Concert discovery based on user's search query
- Equalizer settings access
- Background audio playback with the screen turned off
- Music genre analysis for playlists

## Technical Stack

- **Development Environment**: Android Studio
- **Authentication**: Firebase OR Android Fingerprint Biometrics
- **Local Database**: Room
- **Music Playback**: Media3 Exo Player
- **UI Components**: 
  - Fragment Navigation
  - ViewPager 2
  - RecyclerView
  - CardView
- **External Libraries**:
  - timeline-view (for artist album timelines)
  - Chart library (for playlist genre distribution)
  - Swipe refresh layout library (for refresh list of artists)

## Screens and Navigation

TuneSync uses fragment navigation to move between different screens:

1. **Landing Page**: Main hub with navigation buttons to other screens
2. **Songs Tab**: List of all songs with search functionality, and the ability to listen to all the songs found on the device
3. **Artists Tab**: List of specific artists and songs, which can be filtered by using the album timelines
4. **Albums Tab**: Card views of all albums, which the user can click on to access the album tracks
5. **Playlist Management**: Create, edit, and delete playlists
6. **Concert Discovery**: Search and view nearby concerts

## Music Playback

- Utilizes Media3 Exo Player for high-quality audio playback
- Supports background playback and playback with the screen off
- Integrates with Android OS equalizer settings, to allow users to adjust their audio settings

## User Account Management

- User registration and login powered by Firebase authentication, and Android Fingerprint Biometrics

## Data Management

- **Local Music**: Retrieved using MediaStore.Audio
- **Caching**: Room database for quick loading of songs each time a user opens the app
- **Search**: Implemented for songs, artists, albums and concert events

## Additional Features

- **Lyric Fetching**: API integration to display lyrics for the current song
- **Concert Discovery**: API-powered search for nearby concerts with external links for ticket purchase, or viewing event details
- **Playlist Analysis**: Pie chart visualization of genre distribution in playlists
- **Album Timelines**: Filter artist songs based on the album

## Design Considerations

TuneSync was designed with the following principles in mind:

- **User Experience**: Intuitive navigation using fragment-based architecture and ViewPager 2 for tab switching.
- **Performance**: 
  - Efficient data loading and caching using Room database.
  - Background audio playback for uninterrupted listening experience.
- **Modularity**: Clear separation of concerns between different app components (e.g., music playback, data management, UI).
- **Scalability**: Designed to easily accommodate future features and improvements.
- **Offline Functionality**: Core music playback features work without an internet connection.
- **API Integration**: Seamless integration with external services for enhanced features like lyrics and concert discovery.
- **Material Design**: Adherence to Android's Material Design guidelines for a consistent and modern UI.

## GitHub and CI/CD

TuneSync leverages GitHub for version control and collaborative development:

- **Repository Structure**: 
  - `master` branch for stable builds of the current prototype

### GitHub Actions

We utilize GitHub Actions for continuous integration and deployment in the `TuneSync` project:

1. **Build Automation**: 
   - Triggered on every push and pull request to the `master` branch.
   - Compiles the app and ensures the build is functional on the latest Android SDK (API level 34).
   - Uses Gradle to automate building and testing the project.

2. **Code Quality Checks**:
   - Runs Android's built-in Lint tool to perform static code analysis.
   - Ensures code quality by identifying potential bugs and enforcing best practices.
   - Lint reports are generated and archived as artifacts for easy review.

3. **Automated Testing**:
   - Executes unit tests using **Robolectric** to simulate Android environments and verify code functionality.
   - Runs tests across Android API levels to ensure compatibility.
   - Generates test reports, which are archived for review after each run.

4. **Test Reporting**:
   - Archives both Lint and unit test reports as downloadable artifacts, ensuring thorough visibility of code quality and test outcomes.
   - Ensures tests are run and reported even when errors occur, using the `always()` directive.

5. **Release Management**:
   - Automates the creation of debug builds after every push and pull request.
   - Can be extended to produce release builds in the future by adjusting the workflow.

GitHub Actions ensure automated testing, help maintain high code quality, and allow for continuous integration, providing a smooth development workflow for the `TuneSync` app.
  

## Installation

Follow these steps to install and set up the `TuneSync` app on your local machine:

### Prerequisites

Before you begin, ensure you have the following installed:

- **Android Studio**: Download and install [Android Studio](https://developer.android.com/studio).
- **Java Development Kit (JDK)**: Install JDK 17. You can download it from [Adoptium](https://adoptium.net/temurin/releases/) or use [OpenJDK](https://jdk.java.net/).

### Clone the Repository

1. Open your terminal or command prompt.
2. Clone the repository using the following command:

   ```bash
   git clone https://github.com/VCDN-2024/opsc7312-part-poe-ST10082747.git
3. Navigate to the project directory:
   ```bash
   cd tunesync

### Open the Project

1. Open Android Studio.
2. Select **"Open an existing Android Studio project."**
3. Navigate to the cloned `TuneSync` directory and select it.

### Sync Project

- Android Studio will prompt you to sync the project with Gradle files. Click on **"Sync Now."**

### Run the App

1. Ensure you have an emulator set up or connect a physical Android device.
2. Select the device from the device dropdown menu.
3. Click the **Run** button (green triangle) or press **Shift + F10** to build and run the app.

## Usage

Here are some basic instructions on how to use the `TuneSync` app:

### Navigation

- The app utilizes **fragment navigation** to transition between different screens.
- After logging in, users will land on the main page with navigation buttons to access various features of the app.

#### User can also swipe the screen to navigate between the tabs

### Music Playback

- The app uses **Media3 ExoPlayer** for seamless music playback, allowing users to play audio even with the screen off or while navigating through other screens.
- In the **Songs Tab**, users can listen to all songs stored on their device. 
  - Music is cached in the Room database for quick loading after opening the app.
  - A **search view** is available for finding specific songs, artists, or albums.
  - Users can fetch lyrics for the currently playing song via an integrated API, by clicking on the `Show lyrics` button.

### Albums and Artists

- In the **Albums Tab**, users can view a list of all albums found on their device.
  - Clicking on an album card provides access to all songs in that particular album.
- The **Artists Tab** displays individual artists based on the music available on the user's device.
  - Users can click on an artist's name to access all songs associated with that artist.
  - A **timeline view** shows all albums released by the artist, allowing users to filter songs by album release dates.

### Playlists

- Users can create and manage playlists:
  - Add songs to playlists or delete existing playlists.
  - The playlist screen features a pie chart displaying genre distribution based on the songs added.  

### Concerts

- The **Concerts Screen** allows users to search for concerts near them using an external API.
  - Concert events are displayed in a **RecyclerView** with card views containing details for each concert.
  - Users can click on a concert result to open their browser and visit the event organizer's website to purchase tickets.

### Equalizer Settings

- Access the **equalizer settings** by clicking on the `Audio Settings` button which will navigate you to your device's Sound Settings page where you can access the equalizer.


## Libraries Used and Code Attribution

The following libraries and code snippets have been utilized in the development of the `TuneSync` app:  

- **Check if User Has Pending Searches and Set Notification if Confirmed**:  
  Adapted from [Learn How to Display and Manage Notifications in Android](https://dev.to/charfaouiyounes/learn-how-to-display-and-manage-notifications-in-android-4k8)  
  Author: Younes Charfaoui

- **Allow User to Refresh Layout to Repopulate Data if Artist Data is Still Loading**:  
  Adapted from [SwipeRefreshLayout for Pull/Swipe to Refresh](https://www.digitalocean.com/community/tutorials/android-swiperefreshlayout-pull-swipe-refresh)  
  Author: Anupam Chugh

- **Check if Device Has Internet Access**:  
  Adapted from [Simple Ways to Monitor Internet Connectivity in Android](https://medium.com/@manuchekhrdev/simple-ways-to-monitor-internet-connectivity-in-android-a3bef75bd3d9)  
  Author: Manuchekhr Tursunov

- **Biometric Login Using Fingerprint Authentication**:  
  Adapted from [Biometric Authentication with Fingerprint in Android](https://developer.android.com/identity/sign-in/biometric-auth)  
  Author: Developer.Android  

- **MPAndroidChart Library**:  
  Adapted from [MPAndroidChart GitHub Repository](https://github.com/PhilJay/MPAndroidChart)  
  Creator/Author: PhilJay

- **Timeline View Library**:  
  Adapted from [Timeline-View GitHub Repository](https://github.com/vipulasri/Timeline-View)  
  Creator/Author: Vipul Asri ([GitHub Profile](https://github.com/vipulasri))  

- **Fragments**:  
  Adapted from [Android Developer Guide on Fragments](https://developer.android.com/guide/fragments)  
  Author: Android

- **Check User Permissions**:  
  Adapted from [How to Check and Request Permissions in Android](https://towardsdev.com/how-to-check-and-request-permissions-in-android-1c3c4a66f285)  
  Author: Anggara Dwi Kuntoro

- **SearchView for Finding Songs**:  
  Adapted from [SearchView in Android with ListView](https://www.geeksforgeeks.org/searchview-in-android-with-listview/)  
  Author: GeeksForGeeks

- **ROOM Database Integration**:  
  Adapted from [Android Training on Data Storage with Room](https://developer.android.com/training/data-storage/room)  
  Author: Developer.Android

- **Accessing Local Storage with MediaStore.Audio**:  
  Adapted from [Accessing Shared Media](https://developer.android.com/training/data-storage/shared/media)  
  Author: Developer.Android

- **Fetch Lyrics for Current Song Using API**:  
  API Source: RapidAPI  
  Endpoint Documentation: [Musixmatch Lyrics Songs](https://rapidapi.com/Paxsenix0/api/musixmatch-lyrics-songs)

- **Search for Concerts Using API**:  
  API Source: RapidAPI  
  Endpoint Documentation: [Real-Time Events Search](https://rapidapi.com/letscrape-6bRBa3QguO5/api/real-time-events-search)

- **Tab Layout with ViewPager2**:  
  Adapted from [Android ViewPager in Kotlin](https://www.geeksforgeeks.org/android-viewpager-in-kotlin/)  
  Author: GeeksForGeeks

- **Display Pie Chart with Genre Information**:  
  Adapted from [How to Add a Pie Chart into an Android Application](https://www.geeksforgeeks.org/how-to-add-a-pie-chart-into-an-android-application/)  
  Author: GeeksForGeeks

- **Register Player.Listener for Playback Events**:  
  Adapted from [ExoPlayer Hello World](https://developer.android.com/media/media3/exoplayer/hello-world)  
  Author: Developer.Android

- **Adapters**:  
  Based on tutorial from Scaler (YouTube Channel)  
  Video Title: [Building a Music App using Kotlin - Part #2](https://www.youtube.com/watch?v=odiIBC9jl_4)

- **Abstract Class for ROOM Database**:  
  Adapted from [Android Room Using Kotlin](https://medium.com/android-beginners/android-room-using-kotlin-798ae83b3bf0)  
  Author: Velmurugan Murugesan

- **Coroutines for Asynchronous Database Operations**:  
  Adapted from [Using Suspend Functions in Android DAOs](https://dnmtechs.com/using-suspend-functions-in-android-daos/)  
  Author: Draper Oscar

- **ViewModelProvider Interface for Playlists**:  
  Adapted from [Understanding the ViewModelProviderFactory in Android with Kotlin](https://dev.to/theplebdev/understanding-the-viewmodelproviderfactory-in-android-with-kotlin-11dp)  
  Author: Tristan Elliott

- **Paginated RecyclerView for Selecting Songs**:  
  Adapted from [Paging3 RecyclerView Pagination Made Easy](https://medium.com/swlh/paging3-recyclerview-pagination-made-easy-333c7dfa8797)  
  Author: Vikus Kuma

## Artificial Intelligence Use Cases

The GitHub Actions Workflow used in this repo to run the automated tests and perform the build tests was put together with assistance and guidance from [ChatGPT](https://openai.com/chatgpt) and [ClaudeAI](https://www.anthropic.com/), based on the functions and use case of our app, as well as the prexisting `unit tests` we wrote and used to perform local tests in `Android Studio`.
