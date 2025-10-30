import React from 'react';
import {
  View,
  StyleSheet,
  Dimensions,
  Image,
} from 'react-native';
import { Button, Text } from 'react-native-paper';
import LinearGradient from 'react-native-linear-gradient';
import { useNavigation } from '@react-navigation/native';
import { StackNavigationProp } from '@react-navigation/stack';
import { RootStackParamList } from '../../types';
import { colors, spacing, typography, borderRadius } from '../../theme/theme';
import Icon from 'react-native-vector-icons/MaterialCommunityIcons';

const { height } = Dimensions.get('window');

type LoginScreenNavigationProp = StackNavigationProp<RootStackParamList, 'Login'>;

export const LoginScreen: React.FC = () => {
  const navigation = useNavigation<LoginScreenNavigationProp>();

  const handlePhoneLogin = () => {
    navigation.navigate('PhoneAuth');
  };

  const handleGoogleLogin = async () => {
    // Google Sign-In will be implemented in AuthContext
    console.log('Google login initiated');
  };

  return (
    <LinearGradient
      colors={[colors.primary, colors.primaryDark]}
      style={styles.container}
    >
      <View style={styles.content}>
        {/* Logo and Title Section */}
        <View style={styles.headerSection}>
          <View style={styles.logoContainer}>
            <Icon name="account-voice" size={80} color={colors.white} />
          </View>
          
          <Text style={styles.title}>Bharatvoice</Text>
          <Text style={styles.titleHindi}>भारतवॉयस</Text>
          
          <Text style={styles.subtitle}>
            Your Personal Assistant
          </Text>
          <Text style={styles.subtitleHindi}>
            आपका व्यक्तिगत सहायक
          </Text>
        </View>

        {/* Login Options */}
        <View style={styles.loginSection}>
          <Text style={styles.loginPrompt}>Sign in to continue</Text>
          <Text style={styles.loginPromptHindi}>जारी रखने के लिए साइन इन करें</Text>

          {/* Phone Login Button */}
          <Button
            mode="contained"
            onPress={handlePhoneLogin}
            style={styles.phoneButton}
            contentStyle={styles.buttonContent}
            labelStyle={styles.buttonLabel}
            icon={({ size, color }) => (
              <Icon name="phone" size={size} color={color} />
            )}
          >
            Continue with Phone
          </Button>

          <Text style={styles.hindiButtonText}>फोन से जारी रखें</Text>

          {/* Divider */}
          <View style={styles.divider}>
            <View style={styles.dividerLine} />
            <Text style={styles.dividerText}>OR</Text>
            <View style={styles.dividerLine} />
          </View>

          {/* Google Login Button */}
          <Button
            mode="outlined"
            onPress={handleGoogleLogin}
            style={styles.googleButton}
            contentStyle={styles.buttonContent}
            labelStyle={styles.googleButtonLabel}
            icon={({ size }) => (
              <Icon name="google" size={size} color={colors.error} />
            )}
          >
            Continue with Google
          </Button>

          <Text style={styles.hindiButtonText}>Google से जारी रखें</Text>
        </View>

        {/* Terms and Privacy */}
        <View style={styles.footer}>
          <Text style={styles.footerText}>
            By continuing, you agree to our
          </Text>
          <View style={styles.footerLinks}>
            <Text style={styles.footerLink}>Terms</Text>
            <Text style={styles.footerText}> & </Text>
            <Text style={styles.footerLink}>Privacy Policy</Text>
          </View>
        </View>
      </View>
    </LinearGradient>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  content: {
    flex: 1,
    padding: spacing.lg,
    justifyContent: 'space-between',
  },
  headerSection: {
    alignItems: 'center',
    marginTop: height * 0.1,
  },
  logoContainer: {
    width: 120,
    height: 120,
    borderRadius: 60,
    backgroundColor: 'rgba(255, 255, 255, 0.2)',
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: spacing.lg,
  },
  title: {
    ...typography.displayLarge,
    color: colors.white,
    marginBottom: spacing.xs,
  },
  titleHindi: {
    ...typography.headlineMedium,
    color: colors.primaryContainer,
    marginBottom: spacing.lg,
  },
  subtitle: {
    ...typography.bodyLarge,
    color: colors.white,
    opacity: 0.9,
    textAlign: 'center',
  },
  subtitleHindi: {
    ...typography.bodyMedium,
    color: colors.primaryContainer,
    textAlign: 'center',
    marginTop: spacing.xs,
  },
  loginSection: {
    width: '100%',
  },
  loginPrompt: {
    ...typography.headlineSmall,
    color: colors.white,
    textAlign: 'center',
    marginBottom: spacing.xs,
  },
  loginPromptHindi: {
    ...typography.bodyMedium,
    color: colors.primaryContainer,
    textAlign: 'center',
    marginBottom: spacing.lg,
  },
  phoneButton: {
    backgroundColor: colors.secondary,
    borderRadius: borderRadius.md,
    marginBottom: spacing.xs,
  },
  buttonContent: {
    height: 56,
  },
  buttonLabel: {
    ...typography.titleLarge,
    color: colors.gray900,
  },
  hindiButtonText: {
    ...typography.bodySmall,
    color: colors.primaryContainer,
    textAlign: 'center',
    marginBottom: spacing.md,
  },
  divider: {
    flexDirection: 'row',
    alignItems: 'center',
    marginVertical: spacing.lg,
  },
  dividerLine: {
    flex: 1,
    height: 1,
    backgroundColor: 'rgba(255, 255, 255, 0.3)',
  },
  dividerText: {
    ...typography.labelMedium,
    color: colors.white,
    marginHorizontal: spacing.md,
  },
  googleButton: {
    borderColor: colors.white,
    borderWidth: 2,
    borderRadius: borderRadius.md,
    backgroundColor: colors.white,
    marginBottom: spacing.xs,
  },
  googleButtonLabel: {
    ...typography.titleLarge,
    color: colors.gray900,
  },
  footer: {
    alignItems: 'center',
    marginBottom: spacing.lg,
  },
  footerText: {
    ...typography.bodySmall,
    color: colors.white,
    opacity: 0.8,
  },
  footerLinks: {
    flexDirection: 'row',
    marginTop: spacing.xs,
  },
  footerLink: {
    ...typography.bodySmall,
    color: colors.secondary,
    fontWeight: '600',
  },
});
