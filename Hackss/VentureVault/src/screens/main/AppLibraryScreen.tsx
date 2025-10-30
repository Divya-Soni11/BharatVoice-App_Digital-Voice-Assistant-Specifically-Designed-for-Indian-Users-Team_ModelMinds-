import React, { useState, useEffect } from 'react';
import {
  View,
  StyleSheet,
  FlatList,
  TouchableOpacity,
  Image,
  Dimensions,
} from 'react-native';
import { Text, Searchbar, Chip, Switch, Badge, ActivityIndicator } from 'react-native-paper';
import { SafeAreaView } from 'react-native-safe-area-context';
import Icon from 'react-native-vector-icons/MaterialCommunityIcons';
import {
  colors,
  spacing,
  typography,
  borderRadius,
  shadows,
  getCategoryColor,
} from '../../theme/theme';
import { InstalledApp, AppCategory } from '../../types';
import { useNavigation } from '@react-navigation/native';
import { StackNavigationProp } from '@react-navigation/stack';
import { RootStackParamList } from '../../types';

type AppLibraryNavigationProp = StackNavigationProp<RootStackParamList>;

const { width } = Dimensions.get('window');

// Mock data - In real app, this would come from device
const MOCK_APPS: InstalledApp[] = [
  {
    packageName: 'com.google.android.apps.nbu.paisa.user',
    appName: 'Google Pay',
    category: 'payment',
    isEnabled: true,
    isSupported: true,
    requiredPermissions: [
      {
        name: 'CAMERA',
        displayName: 'Camera',
        displayNameHindi: 'कैमरा',
        description: 'To scan QR codes for payments',
        descriptionHindi: 'भुगतान के लिए QR कोड स्कैन करने के लिए',
        isGranted: true,
        isRequired: true,
        icon: 'camera',
      },
      {
        name: 'CONTACTS',
        displayName: 'Contacts',
        displayNameHindi: 'संपर्क',
        description: 'To send money to your contacts',
        descriptionHindi: 'अपने संपर्कों को पैसे भेजने के लिए',
        isGranted: true,
        isRequired: true,
        icon: 'contacts',
      },
      {
        name: 'SMS',
        displayName: 'SMS',
        displayNameHindi: 'SMS',
        description: 'To read transaction messages',
        descriptionHindi: 'लेनदेन संदेश पढ़ने के लिए',
        isGranted: false,
        isRequired: false,
        icon: 'message-text',
      },
    ],
    description: 'Send money, pay bills, recharge',
    descriptionHindi: 'पैसे भेजें, बिल भरें, रिचार्ज करें',
  },
  {
    packageName: 'com.whatsapp',
    appName: 'WhatsApp',
    category: 'communication',
    isEnabled: true,
    isSupported: true,
    requiredPermissions: [
      {
        name: 'CAMERA',
        displayName: 'Camera',
        displayNameHindi: 'कैमरा',
        description: 'To take photos and videos',
        descriptionHindi: 'फोटो और वीडियो लेने के लिए',
        isGranted: true,
        isRequired: true,
        icon: 'camera',
      },
      {
        name: 'MICROPHONE',
        displayName: 'Microphone',
        displayNameHindi: 'माइक्रोफोन',
        description: 'To record voice messages',
        descriptionHindi: 'वॉइस मैसेज रिकॉर्ड करने के लिए',
        isGranted: true,
        isRequired: true,
        icon: 'microphone',
      },
    ],
    description: 'Send messages, make calls',
    descriptionHindi: 'मैसेज भेजें, कॉल करें',
  },
  {
    packageName: 'com.phonepe.app',
    appName: 'PhonePe',
    category: 'payment',
    isEnabled: false,
    isSupported: true,
    requiredPermissions: [
      {
        name: 'CAMERA',
        displayName: 'Camera',
        displayNameHindi: 'कैमरा',
        description: 'To scan QR codes',
        descriptionHindi: 'QR कोड स्कैन करने के लिए',
        isGranted: true,
        isRequired: true,
        icon: 'camera',
      },
    ],
    description: 'UPI payments and recharges',
    descriptionHindi: 'UPI भुगतान और रिचार्ज',
  },
  {
    packageName: 'com.google.android.youtube',
    appName: 'YouTube',
    category: 'entertainment',
    isEnabled: true,
    isSupported: true,
    requiredPermissions: [],
    description: 'Watch videos',
    descriptionHindi: 'वीडियो देखें',
  },
  {
    packageName: 'in.amazon.mShop.android.shopping',
    appName: 'Amazon',
    category: 'utility',
    isEnabled: false,
    isSupported: true,
    requiredPermissions: [
      {
        name: 'LOCATION',
        displayName: 'Location',
        displayNameHindi: 'स्थान',
        description: 'To show nearby products',
        descriptionHindi: 'पास के उत्पाद दिखाने के लिए',
        isGranted: true,
        isRequired: false,
        icon: 'map-marker',
      },
    ],
    description: 'Shop online',
    descriptionHindi: 'ऑनलाइन खरीदारी करें',
  },
];

const CATEGORIES: Array<{ id: AppCategory | 'all'; label: string; labelHindi: string }> = [
  { id: 'all', label: 'All Apps', labelHindi: 'सभी ऐप्स' },
  { id: 'payment', label: 'Payment', labelHindi: 'भुगतान' },
  { id: 'communication', label: 'Communication', labelHindi: 'संचार' },
  { id: 'social', label: 'Social', labelHindi: 'सोशल' },
  { id: 'utility', label: 'Utility', labelHindi: 'उपयोगिता' },
  { id: 'entertainment', label: 'Entertainment', labelHindi: 'मनोरंजन' },
];

export const AppLibraryScreen: React.FC = () => {
  const navigation = useNavigation<AppLibraryNavigationProp>();
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<AppCategory | 'all'>('all');
  const [apps, setApps] = useState<InstalledApp[]>(MOCK_APPS);
  const [isLoading, setIsLoading] = useState(false);

  const toggleAppEnabled = (packageName: string) => {
    setApps(prev =>
      prev.map(app =>
        app.packageName === packageName
          ? { ...app, isEnabled: !app.isEnabled }
          : app
      )
    );
  };

  const filteredApps = apps.filter(app => {
    const matchesSearch = app.appName.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesCategory = selectedCategory === 'all' || app.category === selectedCategory;
    return matchesSearch && matchesCategory;
  });

  const renderAppCard = ({ item }: { item: InstalledApp }) => (
    <TouchableOpacity
      style={styles.appCard}
      onPress={() => navigation.navigate('AppDetails', { app: item })}
      activeOpacity={0.7}
    >
      <View style={styles.appCardContent}>
        {/* App Icon */}
        <View
          style={[
            styles.appIcon,
            { backgroundColor: getCategoryColor(item.category) },
          ]}
        >
          <Text style={styles.appIconText}>{item.appName.charAt(0)}</Text>
        </View>

        {/* App Info */}
        <View style={styles.appInfo}>
          <View style={styles.appHeader}>
            <Text style={styles.appName}>{item.appName}</Text>
            {item.isSupported && (
              <Badge style={styles.supportedBadge} size={20}>
                ✓
              </Badge>
            )}
          </View>
          
          <Text style={styles.appDescription} numberOfLines={1}>
            {item.description}
          </Text>
          
          <Text style={styles.appDescriptionHindi} numberOfLines={1}>
            {item.descriptionHindi}
          </Text>

          {/* Permissions Count */}
          <View style={styles.permissionsRow}>
            <Icon name="shield-check" size={14} color={colors.info} />
            <Text style={styles.permissionsText}>
              {item.requiredPermissions.length} permission{item.requiredPermissions.length !== 1 ? 's' : ''}
            </Text>
            {item.requiredPermissions.some(p => !p.isGranted) && (
              <View style={styles.warningDot} />
            )}
          </View>
        </View>

        {/* Toggle Switch */}
        <Switch
          value={item.isEnabled}
          onValueChange={() => toggleAppEnabled(item.packageName)}
          color={colors.primary}
        />
      </View>

      {/* Category Badge */}
      <View
        style={[
          styles.categoryBadge,
          { backgroundColor: getCategoryColor(item.category) + '20' },
        ]}
      >
        <Text style={[styles.categoryText, { color: getCategoryColor(item.category) }]}>
          {item.category.toUpperCase()}
        </Text>
      </View>
    </TouchableOpacity>
  );

  return (
    <SafeAreaView style={styles.container} edges={['top']}>
      {/* Header */}
      <View style={styles.header}>
        <View>
          <Text style={styles.headerTitle}>App Library</Text>
          <Text style={styles.headerTitleHindi}>ऐप लाइब्रेरी</Text>
        </View>
        <View style={styles.statsContainer}>
          <Text style={styles.statsText}>
            {apps.filter(a => a.isEnabled).length}/{apps.length}
          </Text>
          <Text style={styles.statsLabel}>Active</Text>
        </View>
      </View>

      {/* Search Bar */}
      <View style={styles.searchContainer}>
        <Searchbar
          placeholder="Search apps / ऐप्स खोजें"
          onChangeText={setSearchQuery}
          value={searchQuery}
          style={styles.searchBar}
          iconColor={colors.primary}
          inputStyle={styles.searchInput}
        />
      </View>

      {/* Category Filters */}
      <View style={styles.categoryContainer}>
        <FlatList
          horizontal
          showsHorizontalScrollIndicator={false}
          data={CATEGORIES}
          keyExtractor={item => item.id}
          contentContainerStyle={styles.categoryList}
          renderItem={({ item }) => (
            <Chip
              selected={selectedCategory === item.id}
              onPress={() => setSelectedCategory(item.id)}
              style={[
                styles.categoryChip,
                selectedCategory === item.id && styles.categoryChipSelected,
              ]}
              textStyle={[
                styles.categoryChipText,
                selectedCategory === item.id && styles.categoryChipTextSelected,
              ]}
            >
              {item.label}
            </Chip>
          )}
        />
      </View>

      {/* Info Banner */}
      <View style={styles.infoBanner}>
        <Icon name="information" size={20} color={colors.info} />
        <Text style={styles.infoBannerText}>
          Enable apps to receive voice guidance / वॉयस गाइडेंस के लिए ऐप्स सक्षम करें
        </Text>
      </View>

      {/* App List */}
      {isLoading ? (
        <View style={styles.loadingContainer}>
          <ActivityIndicator size="large" color={colors.primary} />
          <Text style={styles.loadingText}>Loading apps...</Text>
        </View>
      ) : (
        <FlatList
          data={filteredApps}
          renderItem={renderAppCard}
          keyExtractor={item => item.packageName}
          contentContainerStyle={styles.appList}
          ItemSeparatorComponent={() => <View style={{ height: spacing.md }} />}
          ListEmptyComponent={() => (
            <View style={styles.emptyContainer}>
              <Icon name="application-outline" size={64} color={colors.gray400} />
              <Text style={styles.emptyText}>No apps found</Text>
              <Text style={styles.emptyTextHindi}>कोई ऐप नहीं मिला</Text>
            </View>
          )}
        />
      )}
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
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: spacing.lg,
    paddingVertical: spacing.md,
    backgroundColor: colors.white,
    borderBottomWidth: 1,
    borderBottomColor: colors.gray200,
  },
  headerTitle: {
    ...typography.headlineLarge,
    color: colors.gray900,
  },
  headerTitleHindi: {
    ...typography.bodyMedium,
    color: colors.gray600,
  },
  statsContainer: {
    alignItems: 'center',
    backgroundColor: colors.primaryContainer,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
    borderRadius: borderRadius.md,
  },
  statsText: {
    ...typography.titleLarge,
    color: colors.primary,
    fontWeight: '700',
  },
  statsLabel: {
    ...typography.labelSmall,
    color: colors.primary,
  },
  searchContainer: {
    paddingHorizontal: spacing.lg,
    paddingVertical: spacing.md,
    backgroundColor: colors.white,
  },
  searchBar: {
    backgroundColor: colors.gray50,
    elevation: 0,
  },
  searchInput: {
    ...typography.bodyMedium,
  },
  categoryContainer: {
    backgroundColor: colors.white,
    paddingBottom: spacing.md,
  },
  categoryList: {
    paddingHorizontal: spacing.lg,
    gap: spacing.sm,
  },
  categoryChip: {
    backgroundColor: colors.gray100,
    borderRadius: borderRadius.lg,
  },
  categoryChipSelected: {
    backgroundColor: colors.primary,
  },
  categoryChipText: {
    ...typography.labelMedium,
    color: colors.gray700,
  },
  categoryChipTextSelected: {
    color: colors.white,
  },
  infoBanner: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: colors.info + '15',
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
    marginHorizontal: spacing.lg,
    marginVertical: spacing.md,
    borderRadius: borderRadius.md,
    gap: spacing.sm,
  },
  infoBannerText: {
    ...typography.bodySmall,
    color: colors.info,
    flex: 1,
  },
  appList: {
    padding: spacing.lg,
  },
  appCard: {
    backgroundColor: colors.white,
    borderRadius: borderRadius.lg,
    ...shadows.md,
    overflow: 'hidden',
  },
  appCardContent: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: spacing.md,
    gap: spacing.md,
  },
  appIcon: {
    width: 56,
    height: 56,
    borderRadius: borderRadius.md,
    justifyContent: 'center',
    alignItems: 'center',
  },
  appIconText: {
    ...typography.headlineMedium,
    color: colors.white,
    fontWeight: '700',
  },
  appInfo: {
    flex: 1,
  },
  appHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.xs,
    marginBottom: spacing.xs,
  },
  appName: {
    ...typography.titleLarge,
    color: colors.gray900,
    fontWeight: '600',
  },
  supportedBadge: {
    backgroundColor: colors.success,
  },
  appDescription: {
    ...typography.bodyMedium,
    color: colors.gray600,
  },
  appDescriptionHindi: {
    ...typography.bodySmall,
    color: colors.gray500,
    marginTop: 2,
  },
  permissionsRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.xs,
    marginTop: spacing.sm,
  },
  permissionsText: {
    ...typography.labelSmall,
    color: colors.gray600,
  },
  warningDot: {
    width: 6,
    height: 6,
    borderRadius: 3,
    backgroundColor: colors.warning,
  },
  categoryBadge: {
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.xs,
    alignSelf: 'flex-start',
    marginLeft: spacing.md,
    marginBottom: spacing.sm,
    borderRadius: borderRadius.sm,
  },
  categoryText: {
    ...typography.labelSmall,
    fontWeight: '600',
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    gap: spacing.md,
  },
  loadingText: {
    ...typography.bodyMedium,
    color: colors.gray600,
  },
  emptyContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingVertical: spacing.xxl,
    gap: spacing.md,
  },
  emptyText: {
    ...typography.titleLarge,
    color: colors.gray500,
  },
  emptyTextHindi: {
    ...typography.bodyMedium,
    color: colors.gray400,
  },
});
