import {defineStore} from 'pinia'
import {api} from 'boot/axios';
import { date } from 'quasar';
import {
  emptyTrainer,
  emptyTrainingType,
  EntityCrudTrainer,
  EntityCrudTrainingType, timeFormat
} from "src/store/store_crud_training";

export interface EntityCrudSchedule {
  id: number,
  time: string,
  day: number,
  trainer: EntityCrudTrainer,
  trainingType: EntityCrudTrainingType,
}

const emptySchedule: EntityCrudSchedule = {
  id: -1,
  time: date.formatDate(new Date(), timeFormat),
  day: 0,
  trainer: emptyTrainer,
  trainingType: emptyTrainingType,
};

export {emptySchedule};

export const useStoreCrudSchedule = defineStore('crudSchedule', {
  state: () => ({
    schedule: [] as EntityCrudSchedule[],
  }),

  actions: {
    async load() {
      this.schedule = (await api.get<EntityCrudSchedule[]>('/api/schedule/list')).data;
      console.log('Rows received', this.schedule);
    },

    async delete(id: number) {
      await api.delete(`/api/schedule/${id}`)
      this.schedule = this.schedule.filter(r => r.id !== id);
    },

    async create(schedule: EntityCrudSchedule) {
      schedule.id = (await api.post<number>('/api/schedule', schedule)).data;
      this.schedule.push(schedule);
      this.sort();
    },

    async update(schedule: EntityCrudSchedule) {
      await api.put('/api/schedule', schedule);
      const index = this.schedule.findIndex(r => r.id === schedule.id);
      if (index > 0) {
        this.schedule[index] = schedule;
      }
    },

    sort() {
      this.schedule.sort((a,b) => a.time.localeCompare(b.time));
    }
  },
});
