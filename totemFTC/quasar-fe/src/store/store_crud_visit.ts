import {defineStore} from 'pinia'
import {api} from 'boot/axios';
import {emptyTraining, EntityCrudTraining} from 'src/store/store_crud_training';
import {dateFormat, DateInterval, dateTimeFormat, DateValue, EditType} from 'src/store/store_utils';
import {date} from 'quasar';
import {emptyUser, EntityUser, useStoreCrudUser} from 'src/store/store_crud_user';
import {emptyTicket, EntityCrudTicket, useStoreCrudTicket} from 'src/store/store_crud_ticket';

export type EntityVisitMark = 'on' | 'off' | 'unmark';
export const nextMark: Map<EntityVisitMark, EntityVisitMark> = new Map<EntityVisitMark, EntityVisitMark>([
  ['on', 'off'],
  ['off', 'unmark'],
  ['unmark', 'on'],
]);

export interface EntityCrudVisit {
  trainingId: number,
  training: EntityCrudTraining,
  user: EntityUser,
  comment: string,
  ticketId: number,
  ticket: EntityCrudTicket,
  markSchedule: boolean,
  markSelf: EntityVisitMark,
  markMaster: EntityVisitMark,

  //
  localPropertyEdit?: EditType,
}

const emptyVisit: EntityCrudVisit = {
  trainingId: -1,
  training: emptyTraining,
  user: emptyUser,
  comment: '',
  ticketId: -1,
  ticket: emptyTicket,
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

    // Show user visits
    userVisitsInterval: {from: '', to: ''} as DateInterval,
    // todo Move to storeTicket
    userSelectedTicket: null as EntityCrudTicket | null,
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
      const ticket = (await api.put<EntityCrudTicket>('/api/visit/delete', visit)).data;
      this.visits = this.visits.filter(r => r.trainingId !== visit.trainingId && r.user.userId !== visit.user.userId);
      this.updateTicket(ticket);
    },

    async createVisit(visit: EntityCrudVisit) {
      const presentUserVisits = this.visits.filter(v => v.user.userId == visit.user.userId);
      delete visit.localPropertyEdit;
      if (presentUserVisits.length === 0) {
        // New user visit
        await api.post('/api/visit', visit);
        this.visits.push(visit);

      } else {
        // User is already present in this training visit list, just update comment and set master mark
        const presentVisit = presentUserVisits[0];
        await this.updateMaster(presentVisit, 'on');
        presentVisit.comment = visit.comment;
        await this.updateComment(presentVisit);
      }
    },

    async updateComment(visit: EntityCrudVisit) {
        await api.put('/api/visit', visit);
        const index = this.visits.findIndex(r => r.trainingId === visit.trainingId && r.user.userId === visit.user.userId);
        if (index >= 0) {
          this.visits[index].comment = visit.comment;
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
      const ticket = (await api.put<EntityCrudTicket>(`/api/visit/markMaster/${value}`, visit)).data;
      this.updateTicket(ticket);
    },

    updateTicket(ticket: EntityCrudTicket | null) {
      if (ticket) {
        console.log('Ticket received. Updating...', ticket);
        const storeUser = useStoreCrudUser();
        ticket.user = storeUser.user;
        const storeTicket = useStoreCrudTicket();
        storeTicket.updateTicket(ticket);
      }
    },

    //
    // User visits
    //

    async loadUserVisits() {
      const user = useStoreCrudUser().user;
      this.visits = [];
      if (this.userSelectedTicket === null) {
        this.visits = (await api.get<EntityCrudVisit[]>(`/api/visit/byUser/${user.userId}?from=${date.formatDate(this.userVisitsInterval.from, dateTimeFormat)}`)).data;
      } else {
        this.visits = (await api.get<EntityCrudVisit[]>(`/api/visit/byTicket/${this.userSelectedTicket.id}`)).data;
      }
      this.visits.forEach(v => v.user = user);
    }
  },
});
