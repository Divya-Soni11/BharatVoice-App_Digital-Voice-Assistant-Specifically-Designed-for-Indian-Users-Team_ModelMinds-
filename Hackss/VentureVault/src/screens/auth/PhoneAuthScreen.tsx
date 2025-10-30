import React, { useState } from 'react';
import {
  View,
  StyleSheet,
  KeyboardAvoidingView,
  Platform,
  ScrollView,
  TouchableOpacity,
} from 'react-native';
import { TextInput, Button, Text, ActivityIndicator } from 'react-native-paper';
import { useNavigation } from '@react-navigation/native';
import { StackNavigationProp } from '@react-navigation/stack';
import { RootStackParamList } from '../../types';
import { colors, spacing, typography, borderRadius, shadows } from '../../theme/theme';
import Icon from 'react-native-vector-icons/MaterialCommunityIcons';

type PhoneAuthNavigationProp = StackNavigationProp<RootStackParamList, 'PhoneAuth'>;

export const PhoneAuthScreen: React.FC = () => {
  const navigation = useNavigation<PhoneAuthNavigationProp>();
  const [phoneNumber, setPhoneNumber] = useState('');
  const [countryCode, setCountryCode] = useState('+91');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSendOTP = async () => {
    setError('');
    
    // Validate phone number
    if (phoneNumber.length !== 10) {
      setError('Please enter a valid 10-digit phone number');
      return;
    }

    setIsLoading(true);

    try {
      // Firebase phone authentication
      // In real implementation, this would call Firebase auth
      const fullPhoneNumber = `${countryCode}${phoneNumber}`;
      
      // Simulate OTP sending
      await new Promise(resolve => setTimeout(resolve, 1500));
      
      // Navigate to OTP verification
      navigation.navigate('OTPVerification', {
        phoneNumber: fullPhoneNumber,
        verificationId: 'mock-verification-id',
      });
    } catch (err) {
      setError('Failed to send OTP. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <KeyboardAvoidingView
      style={styles.container}
      behavior={Platform.OS === 'ios' ? 'padding' : undefined}
    >
      <ScrollView
        contentContainerStyle={styles.scrollContent}
        keyboardShouldPersistTaps="handled"
      >
        {/* Header */}
        <TouchableOpacity
          style={styles.backButton}
          onPress={() => navigation.goBack()}
        >
          <Icon name="arrow-left" size={24} color={colors.gray900} />
        </TouchableOpacity>

        <View style={styles.header}>
          <View style={styles.iconContainer}>
            <Icon name="cellphone-message" size={48} color={colors.primary} />
          </View>
          <Text style={styles.title}>Enter Phone Number</Text>
          <Text style={styles.titleHindi}>अपना फोन नंबर दर्ज करें</Text>
          <Text style={styles.subtitle}>
            We'll send you an OTP to verify your number
          </Text>
          <Text style={styles.subtitleHindi}>
            हम आपका नंबर सत्यापित करने के लिए OTP भेजेंगे
          </Text>
        </View>

        {/* Phone Input */}
        <View style={styles.inputSection}>
          <View style={styles.phoneContainer}>
            {/* Country Code Selector */}
            <TouchableOpacity style={styles.countryCode}>
              <Icon name="flag" size={20} color={colors.primary} />
              <Text style={styles.countryCodeText}>{countryCode}</Text>
              <Icon name="chevron-down" size={20} color={colors.gray500} />
            </TouchableOpacity>

            {/* Phone Number Input */}
            <TextInput
              mode="outlined"
              label="Phone Number"
              value={phoneNumber}
              onChangeText={setPhoneNumber}
              keyboardType="phone-pad"
              maxLength={10}
              style={styles.phoneInput}
              outlineColor={colors.gray300}
              activeOutlineColor={colors.primary}
              error={!!error}
              left={<TextInput.Icon icon="phone" />}
            />
          </View>

          {error ? (
            <Text style={styles.errorText}>{error}</Text>
          ) : null}

          <View style={styles.hindiHelper}>
            <Text style={styles.helperText}>
              अपना 10 अंकों का मोबाइल नंबर दर्ज करें
            </Text>
          </View>

          {/* Send OTP Button */}
          <Button
            mode="contained"
            onPress={handleSendOTP}
            style={styles.sendButton}
            contentStyle={styles.buttonContent}
            labelStyle={styles.buttonLabel}
            disabled={isLoading || phoneNumber.length !== 10}
            loading={isLoading}
          >
            {isLoading ? 'Sending OTP...' : 'Send OTP'}
          </Button>

          <Text style={styles.hindiButtonText}>OTP भेजें</Text>
        </View>

        {/* Info Section */}
        <View style={styles.infoSection}>
          <View style={styles.infoRow}>
            <Icon name="shield-check" size={20} color={colors.success} />
            <Text style={styles.infoText}>
              Your number is safe and secure
            </Text>
          </View>
          <View style={styles.infoRow}>
            <Icon name="message-processing" size={20} color={colors.info} />
            <Text style={styles.infoText}>
              OTP will arrive within 30 seconds
            </Text>
          </View>
          <View style={styles.infoRow}>
            <Icon name="lock" size={20} color={colors.warning} />
            <Text style={styles.infoText}>
              We respect your privacy
            </Text>
          </View>
        </View>

        {/* Alternative Login */}
        <TouchableOpacity
          style={styles.alternativeLogin}
          onPress={() => navigation.goBack()}
        >
          <Text style={styles.alternativeText}>
            Use Google Sign-In instead
          </Text>
        </TouchableOpacity>
      </ScrollView>
    </KeyboardAvoidingView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.white,
  },
  scrollContent: {
    flexGrow: 1,
    padding: spacing.lg,
  },
  backButton: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: colors.gray100,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: spacing.lg,
  },
  header: {
    alignItems: 'center',
    marginBottom: spacing.xl,
  },
  iconContainer: {
    width: 80,
    height: 80,
    borderRadius: 40,
    backgroundColor: colors.primaryContainer,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: spacing.md,
  },
  title: {
    ...typography.headlineLarge,
    color: colors.gray900,
    marginBottom: spacing.xs,
  },
  titleHindi: {
    ...typography.bodyLarge,
    color: colors.gray600,
    marginBottom: spacing.md,
  },
  subtitle: {
    ...typography.bodyMedium,
    color: colors.gray600,
    textAlign: 'center',
  },
  subtitleHindi: {
    ...typography.bodySmall,
    color: colors.gray500,
    textAlign: 'center',
    marginTop: spacing.xs,
  },
  inputSection: {
    marginBottom: spacing.xl,
  },
  phoneContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.sm,
  },
  countryCode: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: colors.gray100,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
    borderRadius: borderRadius.md,
    height: 56,
    gap: spacing.xs,
    ...shadows.sm,
  },
  countryCodeText: {
    ...typography.titleMedium,
    color: colors.gray900,
  },
  phoneInput: {
    flex: 1,
    backgroundColor: colors.white,
  },
  errorText: {
    ...typography.bodySmall,
    color: colors.error,
    marginTop: spacing.sm,
  },
  hindiHelper: {
    marginTop: spacing.sm,
    marginBottom: spacing.lg,
  },
  helperText: {
    ...typography.bodySmall,
    color: colors.gray600,
    textAlign: 'center',
  },
  sendButton: {
    backgroundColor: colors.primary,
    borderRadius: borderRadius.md,
    marginBottom: spacing.xs,
  },
  buttonContent: {
    height: 56,
  },
  buttonLabel: {
    ...typography.titleLarge,
  },
  hindiButtonText: {
    ...typography.bodySmall,
    color: colors.gray600,
    textAlign: 'center',
  },
  infoSection: {
    backgroundColor: colors.gray50,
    padding: spacing.md,
    borderRadius: borderRadius.md,
    gap: spacing.md,
    marginBottom: spacing.lg,
  },
  infoRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.sm,
  },
  infoText: {
    ...typography.bodyMedium,
    color: colors.gray700,
    flex: 1,
  },
  alternativeLogin: {
    alignItems: 'center',
    paddingVertical: spacing.md,
  },
  alternativeText: {
    ...typography.bodyMedium,
    color: colors.primary,
    fontWeight: '600',
  },
});
