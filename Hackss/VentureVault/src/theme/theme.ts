import { MD3LightTheme, MD3DarkTheme } from 'react-native-paper';

// Professional Blue and Amber color scheme
export const colors = {
  // Primary - Professional Blue
  primary: '#2563EB',
  primaryDark: '#1E40AF',
  primaryLight: '#3B82F6',
  primaryContainer: '#EFF6FF',
  onPrimary: '#FFFFFF',
  
  // Secondary - Warm Amber
  secondary: '#F59E0B',
  secondaryDark: '#D97706',
  secondaryLight: '#FBBF24',
  secondaryContainer: '#FFFBEB',
  onSecondary: '#1F2937',
  
  // Accent colors for categories
  payment: '#10B981',      // Green
  communication: '#3B82F6', // Blue
  social: '#8B5CF6',       // Purple
  utility: '#F59E0B',      // Amber
  government: '#EF4444',   // Red
  entertainment: '#EC4899', // Pink
  
  // Semantic colors
  success: '#10B981',
  warning: '#F59E0B',
  error: '#EF4444',
  info: '#3B82F6',
  
  // Neutral colors
  white: '#FFFFFF',
  black: '#000000',
  gray50: '#F9FAFB',
  gray100: '#F3F4F6',
  gray200: '#E5E7EB',
  gray300: '#D1D5DB',
  gray400: '#9CA3AF',
  gray500: '#6B7280',
  gray600: '#4B5563',
  gray700: '#374151',
  gray800: '#1F2937',
  gray900: '#111827',
  
  // Dark theme specific
  darkBackground: '#0F172A',
  darkSurface: '#1E293B',
  darkSurfaceVariant: '#334155',
};

export const lightTheme = {
  ...MD3LightTheme,
  colors: {
    ...MD3LightTheme.colors,
    primary: colors.primary,
    onPrimary: colors.onPrimary,
    primaryContainer: colors.primaryContainer,
    onPrimaryContainer: colors.primaryDark,
    
    secondary: colors.secondary,
    onSecondary: colors.onSecondary,
    secondaryContainer: colors.secondaryContainer,
    onSecondaryContainer: colors.secondaryDark,
    
    tertiary: colors.social,
    onTertiary: colors.white,
    
    background: colors.gray50,
    onBackground: colors.gray900,
    
    surface: colors.white,
    onSurface: colors.gray900,
    surfaceVariant: colors.gray100,
    onSurfaceVariant: colors.gray700,
    
    error: colors.error,
    onError: colors.white,
    
    outline: colors.gray300,
    outlineVariant: colors.gray200,
  },
};

export const darkTheme = {
  ...MD3DarkTheme,
  colors: {
    ...MD3DarkTheme.colors,
    primary: colors.primary,
    onPrimary: colors.onPrimary,
    primaryContainer: colors.primaryDark,
    onPrimaryContainer: colors.primaryLight,
    
    secondary: colors.secondary,
    onSecondary: colors.gray900,
    secondaryContainer: colors.secondaryDark,
    onSecondaryContainer: colors.secondaryLight,
    
    tertiary: colors.social,
    onTertiary: colors.white,
    
    background: colors.darkBackground,
    onBackground: colors.gray100,
    
    surface: colors.darkSurface,
    onSurface: colors.gray100,
    surfaceVariant: colors.darkSurfaceVariant,
    onSurfaceVariant: colors.gray300,
    
    error: colors.error,
    onError: colors.white,
    
    outline: colors.gray600,
    outlineVariant: colors.gray700,
  },
};

export const spacing = {
  xs: 4,
  sm: 8,
  md: 16,
  lg: 24,
  xl: 32,
  xxl: 48,
};

export const borderRadius = {
  sm: 8,
  md: 12,
  lg: 16,
  xl: 24,
  full: 9999,
};

export const typography = {
  displayLarge: {
    fontSize: 32,
    fontWeight: '700' as const,
    lineHeight: 40,
  },
  displayMedium: {
    fontSize: 28,
    fontWeight: '700' as const,
    lineHeight: 36,
  },
  displaySmall: {
    fontSize: 24,
    fontWeight: '700' as const,
    lineHeight: 32,
  },
  headlineLarge: {
    fontSize: 22,
    fontWeight: '600' as const,
    lineHeight: 28,
  },
  headlineMedium: {
    fontSize: 20,
    fontWeight: '600' as const,
    lineHeight: 26,
  },
  headlineSmall: {
    fontSize: 18,
    fontWeight: '600' as const,
    lineHeight: 24,
  },
  titleLarge: {
    fontSize: 16,
    fontWeight: '600' as const,
    lineHeight: 24,
  },
  titleMedium: {
    fontSize: 14,
    fontWeight: '600' as const,
    lineHeight: 20,
  },
  bodyLarge: {
    fontSize: 16,
    fontWeight: '400' as const,
    lineHeight: 24,
  },
  bodyMedium: {
    fontSize: 14,
    fontWeight: '400' as const,
    lineHeight: 20,
  },
  bodySmall: {
    fontSize: 12,
    fontWeight: '400' as const,
    lineHeight: 16,
  },
  labelLarge: {
    fontSize: 14,
    fontWeight: '500' as const,
    lineHeight: 20,
  },
  labelMedium: {
    fontSize: 12,
    fontWeight: '500' as const,
    lineHeight: 16,
  },
  labelSmall: {
    fontSize: 10,
    fontWeight: '500' as const,
    lineHeight: 14,
  },
};

export const shadows = {
  sm: {
    shadowColor: colors.black,
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 2,
    elevation: 1,
  },
  md: {
    shadowColor: colors.black,
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 2,
  },
  lg: {
    shadowColor: colors.black,
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.15,
    shadowRadius: 8,
    elevation: 4,
  },
  xl: {
    shadowColor: colors.black,
    shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.2,
    shadowRadius: 16,
    elevation: 8,
  },
};

export const getCategoryColor = (category: string): string => {
  switch (category) {
    case 'payment':
      return colors.payment;
    case 'communication':
      return colors.communication;
    case 'social':
      return colors.social;
    case 'utility':
      return colors.utility;
    case 'government':
      return colors.government;
    case 'entertainment':
      return colors.entertainment;
    default:
      return colors.gray500;
  }
};
