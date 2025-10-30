import React from 'react';
import {
  View,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  Linking,
} from 'react-native';
import { Text, Button, Switch, Divider } from 'react-native-paper';
import { SafeAreaView } from 'react-native-safe-area-context';
import { RouteProp, useRoute, useNavigation } from '@react-navigation/native';
import { StackNavigationProp } from '@react-navigation/stack';
import Icon from 'react-native-vector-icons/MaterialCommunityIcons';
import {
  colors,
  spacing,
  typography,
  borderRadius,
  shadows,
  getCategoryColor,
} from '../../theme/theme';
import { RootStackParamList, Permission } from '../../types';

type AppDetailsRouteProp = RouteProp<RootStackParamList, 'AppDetails'>;
type AppDetailsNavigationProp = StackNavigationProp<RootStackParamList, 'AppDetails'>;

export const AppDetailsScreen: React.FC = () => {
  const route = useRoute<AppDetailsRouteProp>();
  const navigation = useNavigation<AppDetailsNavigationProp>();
  const { app } = route.params;

  const openAppSettings = () => {
    // In real app, open app settings
    Linking.openSettings();
  };

  const renderPermissionCard = (permission: Permission) => (
    <View key={permission.name} style={styles.permissionCard}>
      <View style={styles.permissionHeader}>
        <View
          style={[
            styles.permissionIcon,
            {
              backgroundColor: permission.isGranted
                ? colors.success + '20'
                : colors.warning + '20',
            },
          ]}
        >
          <Icon
            name={permission.icon as any}
            size={24}
            color={permission.isGranted ? colors.success : colors.warning}
          />
        </View>

        <View style={styles.permissionInfo}>
          <Text style={styles.permissionName}>{permission.displayName}</Text>
          <Text style={styles.permissionNameHindi}>{permission.displayNameHindi}</Text>
        </View>

        <View
          style={[
            styles.permissionStatus,
            {
              backgroundColor: permission.isGranted
                ? colors.success + '20'
                : colors.error + '20',
            },
          ]}
        >
          <Text
            style={[
              styles.permissionStatusText,
              {
                color: permission.isGranted ? colors.success : colors.error,
              },
            ]}
          >
            {permission.isGranted ? 'Granted' : 'Denied'}
          </Text>
        </View>
      </View>

      <View style={styles.permissionBody}>
        <Text style={styles.permissionDescription}>{permission.description}</Text>
        <Text style={styles.permissionDescriptionHindi}>
          {permission.descriptionHindi}
        </Text>

        {permission.isRequired && (
          <View style={styles.requiredBadge}>
            <Icon name="alert-circle" size={14} color={colors.error} />
            <Text style={styles.requiredText}>Required / आवश्यक</Text>
          </View>
        )}

        {!permission.isGranted && (
          <Button
            mode="outlined"
            onPress={openAppSettings}
            style={styles.grantButton}
            labelStyle={styles.grantButtonLabel}
            icon="cog"
          >
            Grant Permission
          </Button>
        )}
      </View>
    </View>
  );

  return (
    <SafeAreaView style={styles.container} edges={['top']}>
      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity
          style={styles.backButton}
          onPress={() => navigation.goBack()}
        >
          <Icon name="arrow-left" size={24} color={colors.white} />
        </TouchableOpacity>
        <Text style={styles.headerTitle}>App Details</Text>
        <View style={{ width: 40 }} />
      </View>

      <ScrollView style={styles.scrollView} showsVerticalScrollIndicator={false}>
        {/* App Info Card */}
        <View style={styles.appInfoCard}>
          <View
            style={[
              styles.appIconLarge,
              { backgroundColor: getCategoryColor(app.category) },
            ]}
          >
            <Text style={styles.appIconTextLarge}>{app.appName.charAt(0)}</Text>
          </View>

          <Text style={styles.appName}>{app.appName}</Text>
          <Text style={styles.appDescription}>{app.description}</Text>
          <Text style={styles.appDescriptionHindi}>{app.descriptionHindi}</Text>

          <View style={styles.categoryBadge}>
            <Icon name="tag" size={16} color={getCategoryColor(app.category)} />
            <Text
              style={[
                styles.categoryText,
                { color: getCategoryColor(app.category) },
              ]}
            >
              {app.category.toUpperCase()}
            </Text>
          </View>
        </View>

        {/* Permissions Section */}
        <View style={styles.section}>
          <View style={styles.sectionHeader}>
            <Icon name="shield-lock" size={24} color={colors.primary} />
            <View style={styles.sectionTitleContainer}>
              <Text style={styles.sectionTitle}>Permissions</Text>
              <Text style={styles.sectionTitleHindi}>अनुमतियाँ</Text>
            </View>
          </View>

          <View style={styles.permissionsSummary}>
            <View style={styles.summaryItem}>
              <Text style={styles.summaryNumber}>{app.requiredPermissions.length}</Text>
              <Text style={styles.summaryLabel}>Total</Text>
            </View>
            <View style={styles.summaryDivider} />
            <View style={styles.summaryItem}>
              <Text style={[styles.summaryNumber, { color: colors.success }]}>
                {app.requiredPermissions.filter(p => p.isGranted).length}
              </Text>
              <Text style={styles.summaryLabel}>Granted</Text>
            </View>
            <View style={styles.summaryDivider} />
            <View style={styles.summaryItem}>
              <Text style={[styles.summaryNumber, { color: colors.error }]}>
                {app.requiredPermissions.filter(p => !p.isGranted).length}
              </Text>
              <Text style={styles.summaryLabel}>Denied</Text>
            </View>
          </View>

          {app.requiredPermissions.length > 0 ? (
            <View style={styles.permissionsList}>
              {app.requiredPermissions.map(renderPermissionCard)}
            </View>
          ) : (
            <View style={styles.noPermissions}>
              <Icon name="check-circle" size={48} color={colors.success} />
              <Text style={styles.noPermissionsText}>
                No permissions required
              </Text>
              <Text style={styles.noPermissionsTextHindi}>
                कोई अनुमति आवश्यक नहीं
              </Text>
            </View>
          )}
        </View>

        {/* Info Box */}
        <View style={styles.infoBox}>
          <Icon name="information" size={20} color={colors.info} />
          <Text style={styles.infoText}>
            Permissions help the app function properly. Grant only necessary permissions
            for your privacy.
          </Text>
        </View>

        <View style={styles.infoBoxHindi}>
          <Text style={styles.infoTextHindi}>
            अनुमतियां ऐप को ठीक से काम करने में मदद करती हैं। अपनी गोपनीयता के लिए केवल आवश्यक अनुमतियां दें।
          </Text>
        </View>

        {/* Action Buttons */}
        <View style={styles.actionButtons}>
          <Button
            mode="contained"
            onPress={openAppSettings}
            style={styles.settingsButton}
            contentStyle={styles.buttonContent}
            labelStyle={styles.buttonLabel}
            icon="cog"
          >
            Open App Settings
          </Button>

          <Text style={styles.buttonHint}>ऐप सेटिंग्स खोलें</Text>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.gray50,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: spacing.lg,
    paddingVertical: spacing.md,
    backgroundColor: colors.primary,
  },
  backButton: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: 'rgba(255, 255, 255, 0.2)',
    justifyContent: 'center',
    alignItems: 'center',
  },
  headerTitle: {
    ...typography.headlineSmall,
    color: colors.white,
    fontWeight: '600',
  },
  scrollView: {
    flex: 1,
  },
  appInfoCard: {
    alignItems: 'center',
    backgroundColor: colors.white,
    padding: spacing.xl,
    marginBottom: spacing.md,
  },
  appIconLarge: {
    width: 96,
    height: 96,
    borderRadius: borderRadius.lg,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: spacing.md,
    ...shadows.lg,
  },
  appIconTextLarge: {
    ...typography.displayLarge,
    color: colors.white,
    fontWeight: '700',
  },
  appName: {
    ...typography.headlineLarge,
    color: colors.gray900,
    marginBottom: spacing.xs,
  },
  appDescription: {
    ...typography.bodyLarge,
    color: colors.gray600,
    textAlign: 'center',
  },
  appDescriptionHindi: {
    ...typography.bodyMedium,
    color: colors.gray500,
    textAlign: 'center',
    marginTop: spacing.xs,
  },
  categoryBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.xs,
    marginTop: spacing.md,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
    backgroundColor: colors.gray50,
    borderRadius: borderRadius.lg,
  },
  categoryText: {
    ...typography.labelMedium,
    fontWeight: '600',
  },
  section: {
    backgroundColor: colors.white,
    padding: spacing.lg,
    marginBottom: spacing.md,
  },
  sectionHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.md,
    marginBottom: spacing.lg,
  },
  sectionTitleContainer: {
    flex: 1,
  },
  sectionTitle: {
    ...typography.headlineSmall,
    color: colors.gray900,
  },
  sectionTitleHindi: {
    ...typography.bodyMedium,
    color: colors.gray600,
  },
  permissionsSummary: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-around',
    backgroundColor: colors.gray50,
    padding: spacing.lg,
    borderRadius: borderRadius.md,
    marginBottom: spacing.lg,
  },
  summaryItem: {
    alignItems: 'center',
  },
  summaryNumber: {
    ...typography.headlineLarge,
    color: colors.primary,
    fontWeight: '700',
  },
  summaryLabel: {
    ...typography.labelMedium,
    color: colors.gray600,
    marginTop: spacing.xs,
  },
  summaryDivider: {
    width: 1,
    height: 40,
    backgroundColor: colors.gray300,
  },
  permissionsList: {
    gap: spacing.md,
  },
  permissionCard: {
    backgroundColor: colors.gray50,
    borderRadius: borderRadius.md,
    padding: spacing.md,
    borderWidth: 1,
    borderColor: colors.gray200,
  },
  permissionHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.md,
    marginBottom: spacing.md,
  },
  permissionIcon: {
    width: 48,
    height: 48,
    borderRadius: borderRadius.md,
    justifyContent: 'center',
    alignItems: 'center',
  },
  permissionInfo: {
    flex: 1,
  },
  permissionName: {
    ...typography.titleMedium,
    color: colors.gray900,
    fontWeight: '600',
  },
  permissionNameHindi: {
    ...typography.bodySmall,
    color: colors.gray600,
  },
  permissionStatus: {
    paddingHorizontal: spacing.sm,
    paddingVertical: spacing.xs,
    borderRadius: borderRadius.sm,
  },
  permissionStatusText: {
    ...typography.labelSmall,
    fontWeight: '600',
  },
  permissionBody: {
    gap: spacing.sm,
  },
  permissionDescription: {
    ...typography.bodyMedium,
    color: colors.gray700,
  },
  permissionDescriptionHindi: {
    ...typography.bodySmall,
    color: colors.gray600,
  },
  requiredBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.xs,
    marginTop: spacing.xs,
  },
  requiredText: {
    ...typography.labelSmall,
    color: colors.error,
    fontWeight: '600',
  },
  grantButton: {
    marginTop: spacing.sm,
    borderColor: colors.primary,
  },
  grantButtonLabel: {
    ...typography.labelMedium,
  },
  noPermissions: {
    alignItems: 'center',
    paddingVertical: spacing.xxl,
    gap: spacing.md,
  },
  noPermissionsText: {
    ...typography.titleLarge,
    color: colors.success,
  },
  noPermissionsTextHindi: {
    ...typography.bodyMedium,
    color: colors.gray600,
  },
  infoBox: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    gap: spacing.sm,
    backgroundColor: colors.info + '15',
    padding: spacing.md,
    marginHorizontal: spacing.lg,
    borderRadius: borderRadius.md,
  },
  infoText: {
    ...typography.bodySmall,
    color: colors.info,
    flex: 1,
  },
  infoBoxHindi: {
    backgroundColor: colors.gray100,
    padding: spacing.md,
    marginHorizontal: spacing.lg,
    marginTop: spacing.sm,
    borderRadius: borderRadius.md,
  },
  infoTextHindi: {
    ...typography.bodySmall,
    color: colors.gray700,
    textAlign: 'center',
  },
  actionButtons: {
    padding: spacing.lg,
    gap: spacing.sm,
  },
  settingsButton: {
    backgroundColor: colors.primary,
    borderRadius: borderRadius.md,
  },
  buttonContent: {
    height: 56,
  },
  buttonLabel: {
    ...typography.titleMedium,
  },
  buttonHint: {
    ...typography.bodySmall,
    color: colors.gray600,
    textAlign: 'center',
  },
});
