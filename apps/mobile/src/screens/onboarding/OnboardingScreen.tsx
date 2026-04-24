import React, { useState } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, ActivityIndicator, Alert } from 'react-native';
import { useAuthStore } from '../../store/authStore';
import { api, ApiError } from '../../services/api';

const FARM_TYPES = ['vegetable', 'grain', 'orchard', 'greenhouse', 'mixed', 'other'];

const COMMON_CROPS = [
  'Tomatoes', 'Corn', 'Soybeans', 'Potatoes', 'Carrots',
  'Peppers', 'Cucumbers', 'Lettuce', 'Strawberries', 'Apples',
];

type Step = 'farmType' | 'crops' | 'done';

export default function OnboardingScreen() {
  const [step, setStep] = useState<Step>('farmType');
  const [farmType, setFarmType] = useState('');
  const [selectedCrops, setSelectedCrops] = useState<string[]>([]);
  const [loading, setLoading] = useState(false);
  const { setOnboarded } = useAuthStore();

  const toggleCrop = (crop: string) => {
    setSelectedCrops((prev) =>
      prev.includes(crop) ? prev.filter((c) => c !== crop) : [...prev, crop]
    );
  };

  const handleFinish = async () => {
    setLoading(true);
    try {
      await api.post('/api/v1/farms', {
        name: 'My Farm',
        farmType,
        crops: selectedCrops.map((c) => ({ cropName: c })),
      });
      setOnboarded();
    } catch (err) {
      const msg = err instanceof ApiError ? err.message : 'Setup failed. Please try again.';
      Alert.alert('Error', msg);
    } finally {
      setLoading(false);
    }
  };

  if (step === 'farmType') {
    return (
      <View style={styles.container}>
        <Text style={styles.title}>What type of farm?</Text>
        <View style={styles.grid}>
          {FARM_TYPES.map((type) => (
            <TouchableOpacity
              key={type}
              testID={`farm-type-${type}`}
              style={[styles.option, farmType === type && styles.optionActive]}
              onPress={() => setFarmType(type)}
            >
              <Text style={[styles.optionText, farmType === type && styles.optionTextActive]}>
                {type.charAt(0).toUpperCase() + type.slice(1)}
              </Text>
            </TouchableOpacity>
          ))}
        </View>
        <TouchableOpacity
          testID="continue-button"
          style={[styles.button, !farmType && styles.buttonDisabled]}
          onPress={() => setStep('crops')}
          disabled={!farmType}
        >
          <Text style={styles.buttonText}>Continue</Text>
        </TouchableOpacity>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <Text style={styles.title}>What do you grow?</Text>
      <View style={styles.grid}>
        {COMMON_CROPS.map((crop) => (
          <TouchableOpacity
            key={crop}
            testID={`crop-${crop.toLowerCase()}`}
            style={[styles.option, selectedCrops.includes(crop) && styles.optionActive]}
            onPress={() => toggleCrop(crop)}
          >
            <Text style={[styles.optionText, selectedCrops.includes(crop) && styles.optionTextActive]}>
              {crop}
            </Text>
          </TouchableOpacity>
        ))}
      </View>
      <TouchableOpacity
        testID="finish-button"
        style={[styles.button, loading && styles.buttonDisabled]}
        onPress={handleFinish}
        disabled={loading}
      >
        {loading ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Get Started</Text>}
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 24, backgroundColor: '#fff', justifyContent: 'center' },
  title: { fontSize: 24, fontWeight: 'bold', color: '#2d6a4f', marginBottom: 24, textAlign: 'center' },
  grid: { flexDirection: 'row', flexWrap: 'wrap', gap: 10, marginBottom: 32, justifyContent: 'center' },
  option: { paddingHorizontal: 16, paddingVertical: 10, borderRadius: 8, borderWidth: 1, borderColor: '#d1d5db' },
  optionActive: { borderColor: '#2d6a4f', backgroundColor: '#f0fdf4' },
  optionText: { color: '#374151' },
  optionTextActive: { color: '#2d6a4f', fontWeight: '600' },
  button: { backgroundColor: '#2d6a4f', padding: 16, borderRadius: 8, alignItems: 'center' },
  buttonDisabled: { opacity: 0.6 },
  buttonText: { color: '#fff', fontSize: 16, fontWeight: '600' },
});
