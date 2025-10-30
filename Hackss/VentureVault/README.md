<<<<<<< HEAD
# Smart Guide - React Native Frontend 📱

> AI-powered accessibility assistant for semi-literate and elderly users

Beautiful cross-platform mobile app with phone/Gmail authentication, app selection, and permission
management in Hindi/English.

## ✨ Features

- 📱 **Phone + Gmail Authentication** - Secure OTP-based login
- 📋 **App Library** - View all installed apps with beautiful cards
- 🔒 **Permission Management** - See and manage app permissions
- 🎯 **Category Filters** - Payment, Communication, Social, Utility, etc.
- 🔍 **Search** - Find apps quickly
- 🌐 **Bilingual** - English + Hindi throughout
- 🎨 **Beautiful UI** - Material Design 3 with professional colors
- ⚡ **Fast** - Optimized performance

## 🎯 Demo Screens

### 1. Login Screen

- Phone number with OTP
- Google Sign-In button
- Gradient background with app logo
- Terms & privacy links

### 2. Phone Auth Screen

- Country code selector (+91)
- 10-digit phone input
- Security indicators
- Hindi helper text

### 3. App Library Screen

- **Header** - Title + active apps counter
- **Search Bar** - Find apps by name
- **Category Chips** - Filter by category
- **App Cards** showing:
    - Colorful icon (category-based)
    - App name (English + Hindi)
    - Description (bilingual)
    - Permission count
    - Toggle switch
    - Category badge

### 4. App Details Screen

- Large app icon
- Full description
- **Permission Summary**: Total, Granted, Denied
- **Permission Cards** with:
    - Icon and status
    - Name (English + Hindi)
    - Description (bilingual)
    - Required badge
    - Grant button if denied
- Quick link to app settings

## 🚀 Quick Start

### Prerequisites

- Node.js 16+
- React Native CLI or Expo
- Android Studio (for Android)
- Xcode 14+ (for iOS, macOS only)

### Installation

```bash
# Clone the repository
cd smart-guide-rn

# Install dependencies
npm install

# iOS specific (macOS only)
cd ios && pod install && cd ..

# Start Metro bundler
npm start
```

### Run on Device

```bash
# Android
npm run android

# iOS
npm run ios
```

## 📦 Dependencies

### Core

- `react-native` - 0.73.2
- `react` - 18.2.0
- `typescript` - 5.3.3

### Navigation

- `@react-navigation/native` - 6.1.9
- `@react-navigation/stack` - 6.3.20
- `@react-navigation/bottom-tabs` - 6.5.11

### UI Components

- `react-native-paper` - 5.11.3 (Material Design 3)
- `react-native-vector-icons` - 10.0.3
- `lottie-react-native` - 6.4.1
- `react-native-linear-gradient` - 2.8.3

### Authentication

- `@react-native-firebase/app` - 19.0.1
- `@react-native-firebase/auth` - 19.0.1
- `@react-native-google-signin/google-signin` - 11.0.0

### Device Integration

- `react-native-device-info` - 10.12.0
- `react-native-permissions` - 4.0.3
- `@react-native-async-storage/async-storage` - 1.21.0

## 🎨 Theme

### Colors

```typescript
Primary: #2563EB (Blue 600)
Secondary: #F59E0B (Amber 500)
Success: #10B981 (Green 500)
Error: #EF4444 (Red 500)
```

### Category Colors

```typescript
Payment: #10B981 (Green)
Communication: #3B82F6 (Blue)
Social: #8B5CF6 (Purple)
Utility: #F59E0B (Amber)
Government: #EF4444 (Red)
Entertainment: #EC4899 (Pink)
```

## 📱 Mock Data

The app includes mock data for 5 popular Indian apps:

1. **Google Pay** (Payment)
    - 3 permissions: Camera, Contacts, SMS
    - Enabled by default

2. **WhatsApp** (Communication)
    - 2 permissions: Camera, Microphone
    - Enabled by default

3. **PhonePe** (Payment)
    - 1 permission: Camera
    - Disabled by default

4. **YouTube** (Entertainment)
    - 0 permissions
    - Enabled by default

5. **Amazon** (Utility)
    - 1 permission: Location
    - Disabled by default

## 🔧 Firebase Setup (for Production)

1. Create Firebase project at console.firebase.google.com
2. Enable Phone Authentication
3. Enable Google Sign-In
4. Download config files:
    - Android: `google-services.json` → `android/app/`
    - iOS: `GoogleService-Info.plist` → `ios/`

5. Create `.env` file:

```env
FIREBASE_API_KEY=your_api_key
FIREBASE_AUTH_DOMAIN=your_auth_domain
FIREBASE_PROJECT_ID=your_project_id
FIREBASE_APP_ID=your_app_id
```

## 📁 Project Structure

```
src/
├── types/
│   └── index.ts              # TypeScript type definitions
├── theme/
│   └── theme.ts              # Colors, typography, spacing
├── screens/
│   ├── auth/
│   │   ├── LoginScreen.tsx
│   │   └── PhoneAuthScreen.tsx
│   └── main/
│       ├── AppLibraryScreen.tsx
│       └── AppDetailsScreen.tsx
├── components/               # Reusable components (future)
├── services/                 # API services (future)
└── contexts/                 # React contexts (future)
```

## 🎯 Key Components

### LoginScreen

```typescript
- Beautiful gradient background
- Phone authentication button
- Google Sign-In button
- Bilingual text (English + Hindi)
```

### PhoneAuthScreen

```typescript
- Country code picker
- Phone number input (10 digits)
- OTP sending
- Security indicators
```

### AppLibraryScreen

```typescript
- Search bar
- Category filters (chips)
- App cards with toggle switches
- Active apps counter
- Empty state
```

### AppDetailsScreen

```typescript
- App information
- Permission summary (total, granted, denied)
- Detailed permission cards
- Grant permission buttons
- Link to app settings
```

## 🎨 Design Patterns

### Color Usage

```typescript
import { colors, getCategoryColor } from './theme/theme';

// Get category-specific color
const color = getCategoryColor('payment'); // Returns green
```

### Typography

```typescript
import { typography } from './theme/theme';

// Use consistent text styles
<Text style={typography.headlineLarge}>Title</Text>
<Text style={typography.bodyMedium}>Body text</Text>
```

### Spacing

```typescript
import { spacing } from './theme/theme';

// Consistent spacing
padding: spacing.md  // 16px
gap: spacing.lg      // 24px
```

## 🔍 Search & Filter

```typescript
// Search by app name
const filteredApps = apps.filter(app =>
  app.appName.toLowerCase().includes(searchQuery.toLowerCase())
);

// Filter by category
const categoryApps = apps.filter(app =>
  selectedCategory === 'all' || app.category === selectedCategory
);
```

## 🎯 Permission Management

### Permission Status

- **Granted** (Green) - Permission is allowed
- **Denied** (Red) - Permission needs to be granted
- **Required** - Essential for app functionality
- **Optional** - Nice to have

### Grant Permission Flow

1. User taps "Grant Permission" button
2. App opens system settings
3. User enables permission manually
4. Return to app to see updated status

## 📊 State Management

Currently using local state with React hooks. Ready to integrate:

- Context API for global state
- Firebase Firestore for sync
- AsyncStorage for local persistence

## 🚀 Performance

- Optimized FlatLists with proper keys
- Memoized components where needed
- Lazy loading ready
- Image caching with Coil
- Efficient re-renders

## 🌐 Internationalization

### Current Languages

- English (primary)
- Hindi (हिंदी)

### Adding More Languages

1. Add language to `Language` type
2. Add translations to UI strings
3. Update language selector
4. Test with native speakers

## 🎯 Next Steps

- [ ] Connect to real Firebase
- [ ] Implement Google Sign-In flow
- [ ] Add OTP verification screen
- [ ] Fetch real installed apps
- [ ] Request real permissions
- [ ] Add voice guidance integration
- [ ] Add progress tracking
- [ ] Add achievements
- [ ] Analytics integration

## 🐛 Troubleshooting

### Common Issues

**Metro bundler not starting**

```bash
npx react-native start --reset-cache
```

**Build fails on Android**

```bash
cd android && ./gradlew clean && cd ..
```

**Pods not installing (iOS)**

```bash
cd ios && pod deintegrate && pod install && cd ..
```

## 📄 License

MIT License - See LICENSE file for details

## 🤝 Contributing

Contributions welcome! Please read CONTRIBUTING.md for guidelines.

## 📞 Support

For questions or support:

- Email: support@smartguide.app
- Issues: GitHub Issues
- Docs: Full documentation

---

**Built with ❤️ for digital inclusion in India**

🇮🇳 Made in India, for India
=======
# VentureVault
>>>>>>> 5d9678437013ae99bd03b20b53088627b07d72ad
