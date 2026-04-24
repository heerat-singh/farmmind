import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import { useAuthStore } from '../../store/authStore';

export default function SettingsScreen() {
  const { user, clearAuth } = useAuthStore();

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Settings</Text>
      <Text style={styles.email}>{user?.email}</Text>
      <Text style={styles.tier}>{user?.subscriptionTier ?? 'free'} plan</Text>
      <TouchableOpacity style={styles.signOutButton} onPress={clearAuth}>
        <Text style={styles.signOutText}>Sign Out</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, justifyContent: 'center', alignItems: 'center', padding: 24, backgroundColor: '#fff' },
  title: { fontSize: 22, fontWeight: 'bold', color: '#2d6a4f', marginBottom: 8 },
  email: { color: '#6b7280', marginBottom: 4 },
  tier: { color: '#374151', marginBottom: 32, textTransform: 'capitalize' },
  signOutButton: { borderWidth: 1, borderColor: '#ef4444', paddingHorizontal: 24, paddingVertical: 12, borderRadius: 8 },
  signOutText: { color: '#ef4444', fontWeight: '600' },
});
