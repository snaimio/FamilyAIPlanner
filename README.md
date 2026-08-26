# 🏡 Family AI Planner — Smart All-In-One Family Organizer

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.20-7F52FF.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Android Gradle Plugin](https://img.shields.io/badge/AGP-8.7.3-3DDC84.svg?style=flat&logo=android)](https://developer.android.com/build)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-24%20(Nougat)-blue.svg)](https://developer.android.com/about/dashboards)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-34%20(UpsideDownCake)-darkblue.svg)](https://developer.android.com/about/versions/14)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**Family AI Planner** is a native Android family organizer app built to match the modern soft teal/mint UI design system. It brings together shared family calendars, weekly dinner meal planning, real-time AI suggestions, interactive grocery checklists, a slide-out hamburger navigation drawer, and **One-Tap Firebase Authentication (Google, Apple, and Email/Password)**.

---

## 🌟 Key Features

### 1. 🔐 One-Tap Social Authentication (`WelcomeActivity`)
* **Google One-Tap Sign In**: Authenticate in one tap with Google Play Services and Firebase Auth.
* **Apple Sign In**: Firebase `OAuthProvider("apple.com")` integration for cross-platform Apple accounts.
* **Email & Password**: Modal bottom sheets for registration and login with input validation and loading spinners.
* **Auto-Login**: Detects persistent auth sessions on startup and automatically routes to the dashboard.

### 2. 🍔 Slide-Out Navigation Drawer (`DrawerLayout`)
* Functional hamburger menu (≡) opening the side drawer with direct shortcuts to all family modules, family profile switcher, and Firebase Sign Out.

### 3. 🏠 Family Dashboard (`DashboardFragment`)
* Personalized greeting (*"Good morning, Sarah."*).
* 4-Tile Navigation Grid:
  * 📅 **Calendar**
  * 🔘 **Tasks**
  * 🍴 **Meal Planner**
  * 🛒 **Grocery List**
* Polished 4-tab bottom navigation with custom active/inactive state selectors.

### 4. 📅 Interactive Monthly Calendar (`CalendarFragment` & `CalendarMonthView`)
* Custom Canvas Month Grid (**April 2023**) with active day highlight (**11**).
* Daily schedule event list:
  * `9:00` Pediatrician appointment
  * `1:02` Pick up Emma
  * `4:00` Soccer practice
* Floating Action Button (+) to add new events.

### 5. 🍴 Meal Planner & Smart AI Suggestions (`MealPlannerFragment`)
* Weekly dinner schedule with weekday headers (*Spaghetti, Chicken Stir Fry, Vegetable Soup*).
* **Suggestions**: AI dinner recommendation cards (*Tacos, Meatloaf, Grilled Cheese*) with one-tap addition to the weekly plan.

### 6. 🛒 Smart Grocery List (`GroceryListFragment`)
* Interactive checklist with automatic strikethrough animation (*Bread, Apples, Chicken, Pasta, Carrots*).
* Instant item adding bar with emoji category tagger.

### 7. 🤖 Conversational AI Assistant (`AIAssistantFragment` & `AIAssistantEngine`)
* Quick-prompt suggestion chips (*"Remind me about Emma's school trip"*, *"Add grocery item"*, *"How can I assist you today?"*).
* Context-aware conversational AI engine that automatically adds calendar events and updates grocery checklists based on chat requests.

### 8. 👨‍👩‍👧‍👦 Family Members & Settings (`SettingsFragment`)
* Manage household members (*Spouse, Emma, Jacob, Grandparent*) and add custom family profiles.

---

## 🏗️ Project Architecture

```
FamilyAIPlanner/
├── app/
│   ├── src/main/
│   │   ├── java/com/yourname/familyaiplanner/
│   │   │   ├── WelcomeActivity.kt            # Onboarding & One-Tap Auth (Google, Apple, Email)
│   │   │   ├── MainActivity.kt               # DrawerLayout & BottomNavigation Host
│   │   │   ├── data/
│   │   │   │   ├── EventItem.kt              # Calendar event data model
│   │   │   │   ├── MealItem.kt               # Meal planner data model
│   │   │   │   ├── GroceryItem.kt            # Grocery list data model
│   │   │   │   ├── ChatMessage.kt            # AI Chat message model
│   │   │   │   ├── FamilyMember.kt           # Family member profile model
│   │   │   │   ├── PlannerRepository.kt      # Mockup seed repository & persistence
│   │   │   │   └── AIAssistantEngine.kt      # Natural language parser & task dispatcher
│   │   │   └── ui/
│   │   │       ├── dashboard/DashboardFragment.kt
│   │   │       ├── calendar/CalendarFragment.kt
│   │   │       ├── meals/MealPlannerFragment.kt
│   │   │       ├── grocery/GroceryListFragment.kt
│   │   │       ├── chat/AIAssistantFragment.kt
│   │   │       ├── settings/SettingsFragment.kt
│   │   │       ├── adapters/                 # List adapters for all modules
│   │   │       └── views/CalendarMonthView.kt# Custom Canvas monthly grid view
│   │   ├── res/
│   │   │   ├── layout/                       # All screen layouts matching mockup
│   │   │   ├── drawable/                     # Custom vector icons & social logos
│   │   │   ├── menu/                         # Drawer & bottom navigation menus
│   │   │   └── values/                       # Soft teal / mint palette & styles
│   │   ├── google-services.json              # Firebase configuration
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── LICENSE                                   # MIT License (c) 2026 Sheikh Naim
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites
* **Android Studio Ladybug (2024.2+)** or newer
* **JDK 17 or 21**
* Android Device or Emulator with **API 24+ (Nougat)**

### Building & Running
1. Clone the repository:
   ```bash
   git clone https://github.com/snaimio/FamilyAIPlanner.git
   cd FamilyAIPlanner
   ```
2. Open in **Android Studio**.
3. Allow Gradle sync to complete.
4. Click **Run (`app`)** ▶️.

---

## 📄 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
