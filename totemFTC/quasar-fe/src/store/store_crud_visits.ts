import {defineStore} from 'pinia'
import {api} from 'boot/axios';
import {dateFormat, emptyTraining, EntityCrudTraining} from 'src/store/store_crud_training';
import {date} from 'quasar';
import {emptyUser, EntityUser} from "src/store/store_crud_user";

export interface EntityCrudVisit {
  trainingId: number,
  user: EntityUser,
  comment: string,
  ticketId: number,
  markSchedule: boolean,
  markSelf: boolean,
  markMaster: boolean,
}

const emptyVisit: EntityCrudVisit = {
  trainingId: -1,
  user: emptyUser,
  comment: '',
  ticketId: -1,
  markSchedule: false,
  markSelf: false,
  markMaster: false,
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
    async setDate(date: string) {
      this.date = date;
      await this.reloadTrainings();
    },

    async reloadTrainings() {
      this.trainings = (await api.get<EntityCrudTraining[]>(`/api/training/byDate/${this.date}`)).data;
    },

    async seTraining(training: EntityCrudTraining) {
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
        if (index > 0) {
          this.visits[index] = visit;
        }
    },

    async update(visit: EntityCrudVisit, type: 'Schedule' | 'Self' | 'Master', value: boolean) {
      await api.put(`/api/mark${type}/${String(value)}`, visit);
      switch (type) {
        case "Schedule": visit.markMaster = value; break;
        case "Self": visit.markSelf = value; break;
        case "Master": visit.markMaster = value; break;
      }
    },
  },
});
