import {emptyUser, EntityUser} from "src/store/store_crud_user";
import {defineStore} from "pinia";
import {api} from "boot/axios";
import {dateFormat, DateInterval, DateValue, EditType, sameDate, weekStart} from 'src/store/store_utils';
import {date} from 'quasar';

export interface EntityCrudTrainingType {
  trainingType: string,
  trainingName: string,
  defaultCost: number,

  localPropertyEdit?: EditType,
}

export const emptyTrainingType: EntityCrudTrainingType = {
  trainingType: '',
  trainingName: '',
  defaultCost: 0,
}

export interface EntityCrudTraining {
  id: number,
  time: string,
  trainer: EntityUser,
  trainingType: EntityCrudTrainingType,
  comment: string,

  // to show time in dialog
  localPropertyTime?: string,
}

export const emptyTraining: EntityCrudTraining = {
  id: -1,
  time: '',
  trainer: emptyUser,
  trainingType: emptyTrainingType,
  comment: '',
}

export interface DateTraining {
  date: string,
  dateTrainings: DateTraining[],
  trainings: EntityCrudTraining[],
}

async function loadTrainingsByDate(interval: DateInterval): Promise<EntityCrudTraining[]> {
  // console.log('loadTrainingsByDate. From', interval.from, 'to', interval.to);
  return (await api.get<EntityCrudTraining[]>(`/api/userTraining/byDateInterval?from=${date.formatDate(interval.from, dateFormat)}+00:00&to=${date.formatDate(interval.to, dateFormat)}+24:00`)).data;
}

export const useStoreCrudTraining = defineStore('crudTraining', {
  state: () => ({
    trainers: [] as EntityUser[],
    trainingTypes: [] as EntityCrudTrainingType[],
    trainingsInterval: {from: '', to: ''} as DateInterval,
    trainings: [] as EntityCrudTraining[],
  }),

  getters: {
    trainingsByWeek(state) {
      const result = [];
      // const
      const weeksBegin = weekStart(state.trainingsInterval.from);
      const weeksEnd = weekStart(state.trainingsInterval.to);
      for (let weekDate = weeksBegin; weekDate <= weeksEnd; weekDate = date.addToDate(weekDate, {days: 7})) {
        const weekTrainings: DateTraining = {date: date.formatDate(weekDate, dateFormat), dateTrainings: [], trainings: []};
        result.push(weekTrainings);
        for (let day = 0; day < 7; day++) {
          const dayDate = date.addToDate(weekDate, {days: day});
          const dayTrainings: DateTraining = {date: date.formatDate(dayDate, dateFormat), dateTrainings: [], trainings: []};
          weekTrainings.dateTrainings.push(dayTrainings);
          state.trainings.forEach(t => {
            if (sameDate(t.time, dayDate)) {
              dayTrainings.trainings.push(t);
            }
          });
        }
      }
      return result;
    },
  },

  actions: {
    async loadTrainers() {
      this.trainers = (await api.get<EntityUser[]>('/api/trainers/list')).data;
      console.log('Trainers received', this.trainers);
    },

    async loadTrainingTypes() {
      this.trainingTypes = (await api.get<EntityCrudTrainingType[]>('/api/trainingType/list')).data;
      console.log('TrainingTypes received', this.trainingTypes);
    },

    async createTrainingType(trainingType: EntityCrudTrainingType) {
      await api.post('/api/trainingType', trainingType);
      delete trainingType.localPropertyEdit;
      this.trainingTypes.push(trainingType);
      this.trainingTypes.sort((a, b) => a.trainingType.localeCompare(b.trainingType));
    },

    async updateTrainingType(trainingType: EntityCrudTrainingType) {
      await api.put('/api/trainingType', trainingType);
      delete trainingType.localPropertyEdit;
      const oldTrainingTypeIndex = this.trainingTypes.findIndex(t => t.trainingType === trainingType.trainingType);
      if (oldTrainingTypeIndex >= 0) {
        this.trainingTypes[oldTrainingTypeIndex] = trainingType;
      }
    },

    async deleteTrainingType(trainingType: EntityCrudTrainingType) {
      await api.delete(`/api/trainingType/${trainingType.trainingType}`);
      this.trainingTypes = this.trainingTypes.filter(t => t.trainingType !== trainingType.trainingType);
    },

    async loadTrainings(interval: DateInterval) {
      console.log('Loading trainings', interval);
      this.trainings = await loadTrainingsByDate(interval);
      this.trainingsInterval = interval;
      console.log('Trainings received', this.trainings);
    },

    async loadTrainingById(trainingId: number): Promise<EntityCrudTraining> {
      return (await api.get<EntityCrudTraining>(`/api/training/byId/${String(trainingId)}`)).data;
    },

    async loadTrainingsPrev(from: DateValue) {
      const prevTrainings = await loadTrainingsByDate({from: from, to: this.trainingsInterval.from});
      this.trainingsInterval.from = from;
      this.trainings = prevTrainings.concat(this.trainings);
    },

    async loadTrainingsNext(to: DateValue) {
      const nextTrainings = await loadTrainingsByDate({from: this.trainingsInterval.to, to: to});
      this.trainingsInterval.to = to;
      this.trainings = this.trainings.concat(nextTrainings);
      // this.trainings.sort((a,b) => a.time.localeCompare(b.time));
    },

    async schedulePropagate(d: DateValue) {
      (await api.get<EntityCrudTraining[]>(`/api/utils/schedulePropagate?weekStart=${date.formatDate(d, dateFormat)}`));
    },

    sortTrainings() {
      this.trainings.sort((a, b) => a.time.localeCompare(b.time));
    },

    async createTraining(training: EntityCrudTraining) {
      training.id = (await api.post<number>('/api/training', training)).data;
      this.trainings.push(training);
      this.sortTrainings();
    },

    async updateTraining(training: EntityCrudTraining) {
      await api.put('/api/training', training);
      const index = this.trainings.findIndex(r => r.id === training.id);
      if (index >= 0) {
        this.trainings[index] = training;
      }
      this.sortTrainings();
    },

    async deleteTraining(id: number) {
      await api.delete(`/api/training/${id}`)
      this.trainings = this.trainings.filter(r => r.id !== id);
    },


  }
});
