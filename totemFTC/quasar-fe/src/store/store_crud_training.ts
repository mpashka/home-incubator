import {emptyUser, EntityUser} from "src/store/store_crud_user";
import {defineStore} from "pinia";
import {api} from "boot/axios";
import {dateFormat, DateInterval, DateValue, weekStart} from 'src/store/store_utils';
import {date} from 'quasar';

export interface EntityCrudTrainingType {
  trainingType: string,
  trainingName: string,
}

export const emptyTrainingType: EntityCrudTrainingType = {
  trainingType: '',
  trainingName: '',
}

export interface EntityCrudTraining {
  id: number,
  time: string,
  trainer: EntityUser,
  trainingType: EntityCrudTrainingType,
  comment: string,
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

const emptyDateTraining: DateTraining = {date: '', dateTrainings: [], trainings: []};

async function loadTrainingsByDate(interval: DateInterval): Promise<EntityCrudTraining[]> {
  return (await api.get<EntityCrudTraining[]>(`/api/userTraining/byDateInterval?from=${date.formatDate(interval.from, dateFormat)}+00:00&to=${date.formatDate(interval.to, dateFormat)}+00:00`)).data;
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
      const trainingsByWeek = [];
      let weekTrainings: DateTraining = emptyDateTraining;
      let dayTrainings: DateTraining = emptyDateTraining;
      state.trainings.forEach(t => {
        const dayDate = date.formatDate(date.startOfDate(t.time, 'day'), dateFormat);
        if (dayTrainings.date != dayDate) {
          if (dayTrainings.date === emptyDateTraining.date) {
            // First day, first week
            weekTrainings = {date: dayDate, dateTrainings: [], trainings: []};

          } else {
            // New day
            weekTrainings.dateTrainings.push(dayTrainings);

            const weekDate = date.formatDate(weekStart(t.time), dateFormat);
            if (weekTrainings.date !== weekDate) {
              trainingsByWeek.push(weekTrainings);
              weekTrainings = {date: weekDate, dateTrainings: [], trainings: []};
            }
          }
          dayTrainings = {date: dayDate, dateTrainings: [], trainings: []};
        }
        dayTrainings.trainings.push(t);
      });
      if (dayTrainings.trainings.length > 0) {
        weekTrainings.dateTrainings.push(dayTrainings);
        if (weekTrainings.dateTrainings.length > 0) {
          trainingsByWeek.push(weekTrainings);
        }
      }
      console.log('Weeks: ', trainingsByWeek);
      return trainingsByWeek;
    },
  },

  actions: {
    async loadTrainers() {
      this.trainers = (await api.get<EntityUser[]>('/api/trainers/list')).data;
      console.log('Trainers received', this.trainers);
    },

    async loadTrainingTypes() {
      this.trainingTypes = (await api.get<EntityCrudTrainingType[]>('/api/training/types')).data;
      console.log('TrainingTypes received', this.trainingTypes);
    },

    async loadTrainings(interval: DateInterval) {
      console.log('Loading trainings', interval);
      this.trainings = await loadTrainingsByDate(interval);
      this.trainingsInterval = interval;
      console.log('Trainings received', this.trainings);
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
