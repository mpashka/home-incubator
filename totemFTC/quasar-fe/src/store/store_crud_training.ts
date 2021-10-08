import {EntityUser} from "src/store/store_crud_user";
import {defineStore} from "pinia";
import {api} from "boot/axios";

export interface EntityCrudTrainingType {
  trainingType: string,
  trainingName: string,
}

export interface EntityCrudTrainer extends EntityUser {
  trainingTypes: string[],
}

export const emptyTrainer: EntityCrudTrainer = {
  userId: 0,
  firstName: '',
  lastName: '',
  nickName: '',
  phones: [],
  emails: [],
  images: [],
  trainingTypes: [],
  type: 'trainer',
}

export const emptyTrainingType: EntityCrudTrainingType = {
  trainingType: '',
  trainingName: '',
}

export interface EntityCrudTraining {
  id: number,
  time: string,
  trainer: EntityCrudTrainer,
  trainingType: EntityCrudTrainingType,
  comment: string,
}

export const emptyTraining: EntityCrudTraining = {
  id: -1,
  time: '',
  trainer: emptyTrainer,
  trainingType: emptyTrainingType,
  comment: '',
}

export const timeFormat = 'HH:mm';
export const dateFormat = 'YYYY-MM-DD';
export const dateTimeFormat = 'YYYY-MM-DD HH:mm';

export const useStoreCrudTraining = defineStore('crudTraining', {
  state: () => ({
    trainers: [] as EntityCrudTrainer[],
    trainingTypes: [] as EntityCrudTrainingType[],
  }),

  actions: {
    async loadTrainers() {
      this.trainers = (await api.get<EntityCrudTrainer[]>('/api/trainers/list')).data;
      console.log('Trainers received', this.trainers);
    },

    async loadTrainingTypes() {
      this.trainingTypes = (await api.get<EntityCrudTrainingType[]>('/api/trainers/trainingTypes')).data;
      console.log('TrainingTypes received', this.trainingTypes);
    },

  }
});
