// User types
export interface User {
  uid: string;
  phoneNumber?: string;
  email?: string;
  displayName?: string;
  photoURL?: string;
  createdAt: Date;
  preferences: UserPreferences;
}

export interface UserPreferences {
  language: Language;
  voiceGender: 'male' | 'female';
  guidanceLevel: 'basic' | 'detailed' | 'expert';
  enabledApps: string[];
  isServiceEnabled: boolean;
  showFloatingWidget: boolean;
}

export type Language = 'hindi' | 'english' | 'hinglish';

// App types
export interface InstalledApp {
  packageName: string;
  appName: string;
  icon?: string;
  category: AppCategory;
  isEnabled: boolean;
  isSupported: boolean;
  requiredPermissions: Permission[];
  description?: string;
  descriptionHindi?: string;
}

export type AppCategory = 
  | 'payment' 
  | 'communication' 
  | 'social' 
  | 'utility' 
  | 'government' 
  | 'entertainment'
  | 'other';

export interface Permission {
  name: string;
  displayName: string;
  displayNameHindi: string;
  description: string;
  descriptionHindi: string;
  isGranted: boolean;
  isRequired: boolean;
  icon: string;
}

// Supported app guidance
export interface SupportedApp {
  packageName: string;
  displayName: string;
  displayNameHindi: string;
  category: AppCategory;
  guidanceScripts: GuidanceScript[];
  difficulty: 'easy' | 'medium' | 'hard';
  popularityScore: number;
}

export interface GuidanceScript {
  id: string;
  actionName: string;
  englishText: string;
  hindiText: string;
  hinglishText: string;
  priority: number;
}

// Progress tracking
export interface UserProgress {
  totalSessions: number;
  appsLearned: string[];
  dailyUsage: Record<string, number>;
  currentStreak: number;
  achievements: Achievement[];
}

export interface Achievement {
  id: string;
  title: string;
  titleHindi: string;
  description: string;
  descriptionHindi: string;
  icon: string;
  unlockedAt?: Date;
}

// Navigation types
export type RootStackParamList = {
  Splash: undefined;
  Onboarding: undefined;
  Login: undefined;
  PhoneAuth: undefined;
  OTPVerification: { phoneNumber: string; verificationId: string };
  Main: undefined;
  AppDetails: { app: InstalledApp };
  PermissionGuide: { permission: Permission };
};

export type MainTabParamList = {
  AppLibrary: undefined;
  Dashboard: undefined;
  Settings: undefined;
  Progress: undefined;
};

// Auth types
export interface AuthState {
  user: User | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  error: string | null;
}

export interface PhoneAuthCredentials {
  phoneNumber: string;
  verificationId: string;
  verificationCode: string;
}

// App state
export interface AppState {
  installedApps: InstalledApp[];
  supportedApps: SupportedApp[];
  isLoading: boolean;
  error: string | null;
  selectedCategory: AppCategory | 'all';
}
