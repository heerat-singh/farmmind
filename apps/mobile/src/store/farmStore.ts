import { create } from 'zustand';

interface Crop {
  id: string;
  cropName: string;
  variety?: string;
  plantingDate?: string;
  expectedHarvestDate?: string;
  acreagePlanted?: number;
  active: boolean;
}

interface Farm {
  id: string;
  name: string;
  latitude: number;
  longitude: number;
  province?: string;
  climateZone?: string;
  acreage?: number;
  farmType?: string;
  crops: Crop[];
}

interface FarmState {
  farms: Farm[];
  activeFarmId: string | null;
  setFarms: (farms: Farm[]) => void;
  setActiveFarm: (id: string) => void;
  getActiveFarm: () => Farm | undefined;
  updateFarm: (id: string, updates: Partial<Farm>) => void;
}

export const useFarmStore = create<FarmState>((set, get) => ({
  farms: [],
  activeFarmId: null,

  setFarms: (farms) =>
    set({ farms, activeFarmId: farms.length > 0 ? farms[0].id : null }),

  setActiveFarm: (activeFarmId) => set({ activeFarmId }),

  getActiveFarm: () => {
    const { farms, activeFarmId } = get();
    return farms.find((f) => f.id === activeFarmId);
  },

  updateFarm: (id, updates) =>
    set((state) => ({
      farms: state.farms.map((f) => (f.id === id ? { ...f, ...updates } : f)),
    })),
}));
