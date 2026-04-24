import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { useFarmStore } from '../../store/farmStore';

export default function FarmScreen() {
  const { getActiveFarm } = useFarmStore();
  const farm = getActiveFarm();

  return (
    <View style={styles.container}>
      <Text style={styles.title}>{farm?.name ?? 'My Farm'}</Text>
      <Text style={styles.placeholder}>Farm profile & weather widget — coming in Week 4</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, justifyContent: 'center', alignItems: 'center', padding: 24, backgroundColor: '#fff' },
  title: { fontSize: 22, fontWeight: 'bold', color: '#2d6a4f', marginBottom: 12 },
  placeholder: { color: '#6b7280', textAlign: 'center' },
});
