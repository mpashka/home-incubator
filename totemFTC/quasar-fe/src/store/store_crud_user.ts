import {defineStore} from "pinia";
import {api} from "boot/axios";
import {contains} from "src/store/store_utils";

export const emptyUser: EntityUser = {
  userId: -1,
  firstName: '',
  lastName: '',
  nickName: '',
  type: 'guest',
  trainingTypes: [],
  phones: [],
  emails: [],
  images: [],
  socialNetworks: [],
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

export interface EntityUserSocialNetwork {
  networkId: string,
  id: string,
  link: string,
}

export type EntityUserType = 'guest' | 'user' | 'trainer' | 'admin';

export interface EntityUser {
  userId: number,
  firstName: string,
  lastName: string,
  nickName: string,
  primaryImage?: EntityUserImage,
  type: EntityUserType,
  trainingTypes: string[],
  phones: EntityUserPhone[],
  emails: EntityUserEmail[],
  images: EntityUserImage[],
  socialNetworks: EntityUserSocialNetwork[],
}

export const useStoreCrudUser = defineStore('crudUser', {
  state: () => ({
    rows: [] as EntityUser[],
    filterVal: '',
    filteredRows: [] as EntityUser[],
  }),

  actions: {
    async load() {
      this.rows = (await api.get<EntityUser[]>('/api/user/list')).data;
      this.filter();
      console.log('Rows received', this.rows);
    },

    async delete(user: EntityUser) {
      await api.delete(`/api/user/${String(user.userId)}`)
      this.rows = this.rows.filter(r => r.userId !== user.userId);
      this.filter();
    },

    async create(user: EntityUser) {
      user.userId = (await api.post<number>('/api/user', user)).data;
      this.rows.push(user);
      this.filter();
    },

    async update(user: EntityUser) {
      await api.put('/api/user', user);
      const index = this.rows.findIndex(r => r.userId === user.userId);
      if (index >= 0) {
        this.rows[index] = user;
      }
      this.filter();
    },

    disableFilter() {
      this.filterVal = '';
    },

    setFilter(filterStr: string) {
      this.filterVal = filterStr;
      this.filter();
    },

    filter() {
      if (!this.filterVal) {
        this.filteredRows = this.rows;
      } else {
        const filters = this.filterVal.toLowerCase().split(/ +/);
        this.filteredRows = this.rows.filter(u => contains([u.firstName.toLowerCase(), u.lastName.toLowerCase(), u.nickName.toLowerCase()], filters));
      }
    },

    userNameString(user: EntityUser) {
      let result = '';
      const name = user.firstName || user.lastName;

      function addName() {
        if (user.firstName) {
          result += user.firstName;
          if (user.lastName) {
            result += ' ';
          }
        }
        if (user.lastName) {
          result += user.lastName;
        }
      }

      if (user.nickName) {
        result = user.nickName
        if (name) {
          result += ' ('
          addName();
          result += ')';
        }
      } else {
        addName();
      }
      return result;
    },
  }
});
