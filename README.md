# Photos for Android 📸

A feature-rich Android application designed for personal photo management. This project is a mobile port of a desktop JavaFX application, reimagined with Android-native UI components and lifecycle management. It allows users to organize photos into albums, apply specific tags, and perform complex global searches.

## ✨ Key Features

### Album Management
* **Home Dashboard:** View all created albums in a clean, scrollable list.
* **Full Control:** Create new albums, rename existing ones, or delete them entirely via intuitive long-click dialogues.
* **Duplicate Prevention:** Built-in validation prevents the creation of multiple albums with the same name.

### Photo Gallery & Slideshow
* **Device Integration:** Securely import photos directly from the Android device gallery using Scoped Storage (`Intent.ACTION_OPEN_DOCUMENT`).
* **Grid View:** Browse an album's contents via memory-efficient, auto-scaling thumbnails.
* **Slideshow Mode:** Tap any photo to enter a full-screen display with manual "Next" and "Previous" navigation controls.
* **Organization:** Move photos seamlessly between different albums or remove them from an album entirely.

### Tagging System
* **Contextual Metadata:** Add descriptive tags to any photo while in the slideshow view.
* **Strict Categorization:** Tags are strictly enforced via dropdown menus to either **Person** or **Location** types.
* **Tag Visibility:** Applied tags are clearly displayed beneath the image in the slideshow view.

### Global Search & Autocomplete
* **Cross-Album Querying:** Search for photos across *all* albums simultaneously.
* **Smart Autocomplete:** As you type, the search bar dynamically suggests tag values based on the existing tags currently saved in the app (e.g., typing "New" suggests "New York").
* **Complex Logic:** Perform searches using a single tag, or combine two tags using **AND** (Conjunction) or **OR** (Disjunction) logic.

### Persistent Data
* **State Saving:** All albums, photo URI permissions, and tags are serialized and saved to the device's internal storage (`Context.MODE_PRIVATE`), ensuring data persists across app restarts.

---

## 🛠 Tech Stack & Architecture

* **Language:** 100% Pure Java
* **UI:** Android XML (ConstraintLayout, LinearLayout, GridView, ListView)
* **SDK:** Android API 36 / 37
* **Architecture:** Model-View-Controller (MVC) adaptation for Android Activities.
* **Image Rendering:** Custom `BitmapFactory` implementations for memory-safe thumbnail scaling (No external libraries like Picasso or Glide).

---

## 🚀 Installation & Setup

1. **Clone the repository** to your local machine using GitHub.
2. **Open the project** in Android Studio.
3. Ensure your build configuration uses the Kotlin DSL (`build.gradle.kts`) as per standard Android Studio defaults.
4. **Sync Project with Gradle Files** to ensure all dependencies are resolved.
5. **Run the application** on an emulator. 
   > **Note:** The app is optimized and tested for a device emulator with the specifications: **1080 x 2400 420 dpi** (e.g., Pixel 6 or Medium Phone) running API 36 or 37.

---

## 👨‍💻 Author

**Yashvin Jasani**
*Rutgers University - Bachelor of Science in Computer Science*

---

## 🤖 GenAI Usage Documentation

*This section outlines the use of Generative AI (Google Gemini) in porting functional components from the JavaFX project to Android.*

### Component 1: Model Adaptation & Storage
* **Prompt Issued:** "start by adapting your existing Java model classes for Android storage"
* **AI Contributions:** Adapted `Album`, `Photo`, and `Tag` classes. Implemented `java.io.Serializable`. Modified `Photo.java` to store the Android image path as a `String` (`uriString`). Generated `StorageManager.java` for internal file I/O.
* **My Contributions:** Reviewed and integrated model classes, verifying explicit captions and date logic were removed per assignment specs.

### Component 2: Home Screen (MainActivity)
* **Prompt Issued:** "begin outlining the XML layout and Java code for the Home Screen"
* **AI Contributions:** Generated `activity_main.xml` and `MainActivity.java`. Implemented `ListView` with an `ArrayAdapter`. Created programmatic `AlertDialog`s for creating, renaming, and deleting albums. Added intent routing.
* **My Contributions:** Wired the Activity in the Manifest and tested the short/long-click dialog flows on the emulator to ensure data serialization worked.

### Component 3: Album View & Gallery Integration
* **Prompt Issued:** "move on to build that AlbumActivity, set up a GridView for thumbnails, and implement the logic to pick images from the device gallery"
* **AI Contributions:** Designed `activity_album.xml` and `grid_item_photo.xml`. Generated `AlbumActivity.java`. Implemented the Gallery Picker (`ActivityResultLauncher`). Wrote a custom `BaseAdapter` that utilizes `BitmapFactory.Options` to scale images and prevent OutOfMemory crashes. Added Scoped Storage persistable URI logic.
* **My Contributions:** Added Activity to Manifest, tested memory usage in Android Studio Profiler to ensure smooth thumbnail loading.

### Component 4: Slideshow, Tagging, and Moving (PhotoDisplayActivity)
* **Prompt Issued:** "Phase 3/Phase 4: The Slideshow and Tagging, lets go into the implementing 3 and 4"
* **AI Contributions:** Generated `activity_photo_display.xml` with image, tag text, and control layouts. Implemented slideshow logic with bounds-checking. Implemented Tag addition via a constrained `Spinner` and Tag deletion via an `AlertDialog`. Developed the cross-album move photo logic.
* **My Contributions:** Added Activity to Manifest, validated tag duplication constraints, and ensured tags persisted properly after app restarts.

### Component 5: Global Search & Autocomplete
* **Prompt Issued:** "Phase 5 (Global Search & Autocomplete)"
* **AI Contributions:** Created `activity_search.xml` and `SearchActivity.java`. Extracted unique tag values from all albums to feed the `AutoCompleteTextView`. Implemented case-insensitive substring search logic for Single, AND, and OR queries. Utilized a `HashSet` to prevent duplicate photo results. 
* **My Contributions:** Registered the Activity, added navigation from the Home Screen, and rigorously tested the AND/OR logic and autocomplete threshold behaviors.
