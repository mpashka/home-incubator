import {defineStore} from 'pinia';
import {api} from 'boot/axios';
import {EntityCrudTrainingType} from 'src/store/store_crud_training';
import {emptyUser, EntityUser, useStoreCrudUser} from 'src/store/store_crud_user';

export interface EntityCrudTicketType {
  id: number,
  trainingTypes: EntityCrudTrainingType[],
  name: string,
  cost: number,
  visits: number,
  days: number,
}

export interface EntityCrudTicket {
  id: number,
  ticketType: EntityCrudTicketType,
  user: EntityUser,
  buy: string,
  start: string,
  end: string,
  visited: number,
}

export const emptyTicketType: EntityCrudTicketType = {
  id: -1,
  trainingTypes: [],
  name: '',
  cost: 0,
  visits: 0,
  days: 0,
}

export const emptyTicket: EntityCrudTicket = {
  id: -1,
  ticketType: emptyTicketType,
  user: emptyUser,
  buy: '',
  start: '',
  end: '',
  visited: 0,
}

export const useStoreCrudTicket = defineStore('crudTicket', {
  state: () => ({
    ticketTypes: [] as EntityCrudTicketType[],
    userTickets: [] as EntityCrudTicket[],
  }),

  actions: {

    // User page
    async loadTicketTypes() {
      this.ticketTypes = (await api.get<EntityCrudTicketType[]>('/api/ticketTypes/list')).data;
    },

    async createTicketType(ticketType: EntityCrudTicketType) {
      ticketType.id = (await api.post<number>('/api/ticketType', ticketType)).data;
      this.ticketTypes.push(ticketType);
      this.sortTicketTypes();
    },

    async updateTicketType(ticketType: EntityCrudTicketType) {
      await api.put('/api/ticketType', ticketType);
      const oldTicketTypeIndex = this.ticketTypes.findIndex(t => t.id === ticketType.id);
      if (oldTicketTypeIndex >= 0) {
        this.ticketTypes[oldTicketTypeIndex] = ticketType;
      }
      this.sortTicketTypes();
    },

    async deleteTicketType(ticketType: EntityCrudTicketType) {
      await api.delete(`/api/ticketType/${ticketType.id}`);
      this.ticketTypes = this.ticketTypes.filter(t => t.id !== ticketType.id);
    },

    sortTicketTypes() {
      this.ticketTypes.sort((a, b) => a.name.localeCompare(b.name));
    },

    async loadUserTickets() {
      this.userTickets = [];
      const storeUser = useStoreCrudUser();
      this.userTickets = (await api.get<EntityCrudTicket[]>(`/api/tickets/byUser/${storeUser.user.userId}`)).data;
    },

    async deleteTicket(id: number) {
      (await api.delete(`/api/ticket/${id}`));
      this.userTickets = this.userTickets.filter(t => t.id !== id);
    },

    async createTicket(ticket: EntityCrudTicket) {
      ticket.id = (await api.post<number>('/api/ticket', ticket)).data;
      this.userTickets.push(ticket);
      this.userTickets.sort((a, b) => a.buy.localeCompare(b.buy));
    },

    /**  */
    updateTicket(ticket: EntityCrudTicket) {
      const oldTicketIndex = this.userTickets.findIndex(t => t.id === ticket.id);
      if (oldTicketIndex >= 0) {
        this.userTickets[oldTicketIndex] = ticket;
      }
    }
  }
});
