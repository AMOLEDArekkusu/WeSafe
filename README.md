# WeSafe

WeSafe is an Android application focused on personal safety and emergency response. It provides users with tools to report incidents, manage emergency contacts, and access safety features through an intuitive interface.

## Features

- **Incident Reporting**: Users can report safety incidents with details and media attachments
- **Emergency Contacts**: Manage and quickly access emergency contacts
- **Interactive Map**: View and report incidents on an integrated Google Maps interface
- **Media Support**: Capture and attach photos using camera or gallery
- **Location Services**: Access precise location data for accurate incident reporting

## Technical Details

- **Minimum SDK**: Android 24 (Android 7.0)
- **Target SDK**: Android 36
- **Architecture**: Material Design UI components
- **Dependencies**:
  - Google Maps API for location services
  - AndroidX components
  - Material Design components

## Permissions

The app requires the following permissions:
- Fine location access (GPS)
- Coarse location access
- Internet access
- Camera access
- Storage access for media
- Network state access

## Installation

1. Clone the repository
2. Open the project in Android Studio
3. Configure your Google Maps API key in the manifest
4. Build and run the application

Huawei AppGallary and Google Play Store Will Comming Soon.......

<img width="400" height="120" alt="Huawei_AppGallery_white_badge_EN" src="https://github.com/user-attachments/assets/9a41845f-db73-40f0-8e99-a4dbf3d7c5ce" href="#"/>  <img width="400" height="120" alt="get-it-on-google-play-badge-seeklogo" src="https://github.com/user-attachments/assets/ba93b018-0b7d-496c-8587-aec569c2e023" />


## Activities

- MainActivity: Main dashboard and navigation
- MapActivity: Interactive incident map
- IncidentReportActivity: Report new incidents
- EmergencyContactsActivity: Manage emergency contacts
- SettingsActivity: App configuration
- AboutActivity: App information

## Build and Development

This project uses Gradle as the build system. To build the project:

```sh
./gradlew build
