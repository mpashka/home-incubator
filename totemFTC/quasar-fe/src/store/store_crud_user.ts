import {defineStore} from "pinia";
import {api} from "boot/axios";

export const emptyUser: EntityUser = {
  userId: -1,
  firstName: '',
  lastName: '',
  nickName: '',
  phones: [],
  emails: [],
  images: [],
  type: 'guest',
};

export interface EntityUserEmail {
  email: string,
  confirmed: boolean,
}

export interface EntityUserPhone {
  phone: string,
  confirmed: boolean,
}

export interface EntityUserImage {
  id: number,
  contentType: string|null,
}

export type EntityUserType = 'guest' | 'user' | 'trainer' | 'admin';

export interface EntityUser {
  userId: number,
  firstName: string,
  lastName: string,
  nickName: string,
  primaryImage?: EntityUserImage,
  phones: Array<EntityUserPhone>,
  emails: Array<EntityUserEmail>,
  images: Array<EntityUserImage>,
  type: EntityUserType,
}

export const useStoreCrudUser = defineStore('crudUser', {
  state: () => ({
    rows: [] as EntityUser[],
  }),

  actions: {
    async load() {
      this.rows = (await api.get<EntityUser[]>('/api/user/list')).data;
      console.log('Rows received', this.rows);
    },

    async delete(user: EntityUser) {
      await api.delete(`/api/user/${String(user.userId)}`)
      this.rows = this.rows.filter(r => r.userId !== user.userId);
    },

    async create(user: EntityUser) {
      user.userId = (await api.post<number>('/api/user', user)).data;
      this.rows.push(user);
    },

    async update(user: EntityUser) {
      await api.put('/api/user', user);
      const index = this.rows.findIndex(r => r.userId === user.userId);
      if (index > 0) {
        this.rows[index] = user;
      }
    },
  }
});
