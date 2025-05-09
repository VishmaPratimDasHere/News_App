## 📰 News App

A modern Android application built with Kotlin that fetches and displays the latest news using the [News API](https://newsapi.org/). The app features a clean architecture and intuitive UI for a seamless news browsing experience.

### 📱 Features

* Browse real-time news articles from News API
* Search functionality
* Clean and responsive UI
* Save your favourite news piece as a bookmark
* MVVM architecture
* Kotlin + Jetpack libraries

### 🛠 Tech Stack

* **Language**: Kotlin
* **Architecture**: MVVM (Model-View-ViewModel)
* **Build System**: Gradle with Kotlin DSL
* **Jetpack Components**: LiveData, ViewModel, Navigation
* **Networking**: Retrofit
* **Async**: Coroutines
* **Image Loading**: Glide

### 🔐 API Integration

This app uses [NewsAPI.org](https://newsapi.org/) to fetch news data.

### 🚀 Getting Started

#### Prerequisites

* Android Studio Hedgehog or later
* JDK 17+
* Internet connection

#### Setup

1. Clone the repository:

   ```bash
   git clone https://github.com/yourusername/News_App.git
   cd News_App
   ```

2. Add your News API key.

3. Open the project in Android Studio.

4. Sync Gradle and run the app on an emulator or physical device.

### 📂 Project Structure

```
News_App/
├── app/                 # Main app module
├── build.gradle.kts     # Project-level build script
├── settings.gradle.kts  # Project settings
├── gradle.properties    # Gradle configuration
```

### 📄 License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
