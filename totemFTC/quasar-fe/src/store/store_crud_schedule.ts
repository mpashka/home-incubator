import {defineStore} from 'pinia'
import {api} from 'boot/axios';
import { date } from 'quasar';
import {EntityUser} from "src/store/store_login";

export interface EntityCrudTrainingType {
  trainingType: string,
  trainingName: string,
}

export interface EntityCrudTrainer extends EntityUser {
  trainingTypes: string[],
}

export interface EntityCrudSchedule {
  id: number,
  time: string,
  day: number,
  trainer: EntityCrudTrainer,
  trainingType: EntityCrudTrainingType,
}

const emptyTrainer: EntityCrudTrainer = {
  userId: 0,
  firstName: '',
  lastName: '',
  nickName: '',
  phones: [],
  emails: [],
  images: [],
  trainingTypes: [],
}

const emptyTrainingType: EntityCrudTrainingType = {
  trainingType: '',
  trainingName: '',
}

export const dateFormat = 'HH:mm';

const emptySchedule: EntityCrudSchedule = {
  id: -1,
  time: date.formatDate(new Date(), dateFormat),
  day: 0,
  trainer: emptyTrainer,
  trainingType: emptyTrainingType,
};

export {emptySchedule};

export const useStoreCrudSchedule = defineStore('crudSchedule', {
  state: () => ({
    schedule: [] as EntityCrudSchedule[],
    trainers: [] as EntityCrudTrainer[],
    trainingTypes: [] as EntityCrudTrainingType[],
  }),

  actions: {
    async load() {
      this.schedule = (await api.get<EntityCrudSchedule[]>('/api/schedule/list')).data;
      console.log('Rows received', this.schedule);
    },

    async loadTrainers() {
      this.trainers = (await api.get<EntityCrudTrainer[]>('/api/trainers/list')).data;
      console.log('Trainers received', this.trainers);
    },

    async loadTrainingTypes() {
      this.trainingTypes = (await api.get<EntityCrudTrainingType[]>('/api/trainers/trainingTypes')).data;
      console.log('TrainingTypes received', this.trainingTypes);
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
