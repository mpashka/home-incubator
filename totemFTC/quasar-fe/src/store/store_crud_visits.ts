import {defineStore} from 'pinia'
import {api} from 'boot/axios';
import {emptyTraining, EntityCrudTraining} from 'src/store/store_crud_training';
import {dateFormat, DateValue} from 'src/store/store_utils';
import {date} from 'quasar';
import {emptyUser, EntityUser} from "src/store/store_crud_user";

export type EntityVisitMark = 'on' | 'off' | 'unmark';
export const nextMark: Map<EntityVisitMark, EntityVisitMark> = new Map<EntityVisitMark, EntityVisitMark>([
  ['on', 'off'],
  ['off', 'unmark'],
  ['unmark', 'on'],
]);

export interface EntityCrudVisit {
  trainingId: number,
  user: EntityUser,
  comment: string,
  ticketId: number,
  markSchedule: boolean,
  markSelf: EntityVisitMark,
  markMaster: EntityVisitMark,
}

const emptyVisit: EntityCrudVisit = {
  trainingId: -1,
  user: emptyUser,
  comment: '',
  ticketId: -1,
  markSchedule: false,
  markSelf: 'unmark',
  markMaster: 'unmark',
};

export {emptyVisit};

export const useStoreCrudVisit = defineStore('crudVisit', {
  state: () => ({
    date: date.formatDate(new Date(), dateFormat),
    trainings: [] as EntityCrudTraining[],
    training: emptyTraining,
    visits: [] as EntityCrudVisit[],
  }),

  actions: {
    async setDate(newDate: DateValue) {
      await this.setDateStr(date.formatDate(newDate, dateFormat));
    },

    async setDateStr(date: string) {
      this.date = date;
      this.training = emptyTraining;
      this.visits = [];
      await this.reloadTrainings();
    },

    async addDate(days: number) {
      await this.setDate(date.addToDate(this.date, {days: days}));
    },

    async reloadTrainings() {
      this.trainings = (await api.get<EntityCrudTraining[]>(`/api/training/byDate/${this.date}`)).data;
    },

    async setTraining(training: EntityCrudTraining) {
      this.training = training;
      await this.reloadVisits();
    },

    async reloadVisits() {
      this.visits = (await api.get<EntityCrudVisit[]>(`/api/visit/byTraining/${String(this.training.id)}`)).data;
    },

    async deleteVisit(visit: EntityCrudVisit) {
      await api.put('/api/visit/delete', visit)
      this.visits = this.visits.filter(r => r.trainingId !== visit.trainingId && r.user.userId !== visit.user.userId);
    },

    async createVisit(visit: EntityCrudVisit) {
        await api.post('/api/visit', visit);
        this.visits.push(visit);
    },

    async updateComment(visit: EntityCrudVisit) {
        await api.put('/api/visit', visit);
        const index = this.visits.findIndex(r => r.trainingId === visit.trainingId && r.user.userId === visit.user.userId);
        if (index >= 0) {
          this.visits[index] = visit;
        }
    },

    async updateSchedule(visit: EntityCrudVisit, value: boolean) {
      visit.markSchedule = value;
      await api.put(`/api/visit/markSchedule/${String(value)}`, visit);
    },

    async updateSelf(visit: EntityCrudVisit, value: EntityVisitMark) {
      visit.markSelf = value;
      await api.put(`/api/visit/markSchedule/${value}`, visit);
    },

    async updateMaster(visit: EntityCrudVisit, value: EntityVisitMark | undefined) {
      if (!value) {
        value = 'unmark';
      }
      visit.markMaster = value;
      await api.put(`/api/visit/markMaster/${value}`, visit);
    },
  },
});
