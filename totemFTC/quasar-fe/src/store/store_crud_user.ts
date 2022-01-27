import {defineStore} from 'pinia';
import {api} from 'boot/axios';
import {contains} from 'src/store/store_utils';

export const emptyUser: EntityUser = {
  userId: -1,
  firstName: '',
  lastName: '',
  nickName: '',
  types: [],
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
  /** Network name */
  networkName: string,
  id: string,
  link: string,
}

export type EntityUserType = 'user' | 'trainer' | 'admin';
export type EntityUserTypeFilter = EntityUserType | 'all' | 'guest';

export interface EntityUser {
  userId: number,
  firstName: string,
  lastName: string,
  nickName: string,
  primaryImage?: EntityUserImage,
  types: EntityUserType[],
  trainingTypes: string[],
  phones: EntityUserPhone[],
  emails: EntityUserEmail[],
  images: EntityUserImage[],
  socialNetworks: EntityUserSocialNetwork[],
}

export interface EntityUserFilter {
  name: string,
  type: EntityUserTypeFilter,
  trainingType?: string,
  excludeIds: number[],
}

export const emptyUserFilter: EntityUserFilter = {
  name: '',
  type: 'all',
  excludeIds: [],
};

export const useStoreCrudUser = defineStore('crudUser', {
  state: () => ({
    user: {...emptyUser} as EntityUser,
    users: [] as EntityUser[],
    trainers: [] as EntityUser[],
    filterVal: {...emptyUserFilter},
    filteredRows: [] as EntityUser[],
  }),

  getters: {
    isGuest(): (user: EntityUser) => boolean {return (user: EntityUser) => user.types.length === 0},
    isUser(): (user: EntityUser) => boolean {return (user: EntityUser) => user.types.includes('user')},
    isTrainer(): (user: EntityUser) => boolean {return (user: EntityUser) => user.types.includes('trainer')},
    isAdmin(): (user: EntityUser) => boolean {return (user: EntityUser) => user.types.includes('admin')},

    userNameString(): (user: EntityUser) => string { return (user: EntityUser) => {
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

      if (user.nickName && user.nickName !== user.firstName && user.nickName !== user.lastName) {
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
    }},

    fullName(): string {
      return this.userNameString(this.user);
    },
  },

  actions: {
    async loadUser() {
      this.user = (await api.get<EntityUser>('/api/user/current')).data;
      console.log('User received', this.user);
    },

    clear() {
      this.user = {...emptyUser};
    },

    async load() {
      this.users = (await api.get<EntityUser[]>('/api/user/list')).data;
      this.filter();
      console.log('Users received', this.users);
    },

    async loadTrainers() {
      this.trainers = (await api.get<EntityUser[]>('/api/user/listTrainers')).data;
      console.log('Trainers received', this.users);
    },

    async delete(user: EntityUser) {
      await api.delete(`/api/user/delete/${String(user.userId)}`)
      this.users = this.users.filter(r => r.userId !== user.userId);
      this.filter();
    },

    async create(user: EntityUser) {
      user.userId = (await api.post<number>('/api/user', user)).data;
      this.users.push(user);
      this.filter();
    },

    async update(user: EntityUser) {
      await api.put('/api/user', user);
      const index = this.users.findIndex(r => r.userId === user.userId);
      if (index >= 0) {
        this.users[index] = user;
      }
      this.filter();
    },

    async deleteSocialNetwork(name: string) {
      await api.delete(`/api/user/current/network/${name}`);
      this.user.socialNetworks = this.user.socialNetworks.filter(s => s.networkName !== name);
    },

    disableFilter() {
      this.filterVal = {...emptyUserFilter};
      this.filter();
    },

    setFilterByName(filterName: string) {
      this.filterVal.name = filterName;
      this.filter();
    },

    setFilterByType(filterType: EntityUserTypeFilter) {
      this.filterVal.type = filterType;
      this.filter();
    },

    setFilterExclude(excludeIds: number[]) {
      this.filterVal.excludeIds = excludeIds;
      this.filter();
    },

    setFilterByTrainingType(filterTrainingType?: string) {
      this.filterVal.trainingType = filterTrainingType;
      this.filter();
    },

    isFiltered(): boolean {
      return this.filterVal.name.length > 0 || this.filterVal.type !== 'all' || this.filterVal.trainingType !== undefined || this.filterVal.excludeIds.length > 0;
    },

    filter() {
      if (!this.filterVal.name) {
        this.filteredRows = this.users;
      } else {
        const filters = this.filterVal.name.toLowerCase().split(/ +/);
        this.filteredRows = this.users.filter(u => contains([u.firstName.toLowerCase(), u.lastName.toLowerCase(), u.nickName.toLowerCase()], filters));
      }
      if (this.filterVal.type === 'guest') {
        this.filteredRows = this.filteredRows.filter(user => user.types.length == 0);
      } else if (this.filterVal.type !== 'all') {
        this.filteredRows = this.filteredRows.filter(user => user.types.includes(this.filterVal.type as EntityUserType));
      }
      if (this.filterVal.trainingType !== undefined) {
        this.filteredRows = this.filteredRows.filter(user => user.trainingTypes !== null && user.trainingTypes.includes(this.filterVal.trainingType as string));
      }
      if (this.filterVal.excludeIds.length > 0) {
        this.filteredRows = this.filteredRows.filter(user => !this.filterVal.excludeIds.includes(user.userId));
      }
    },
  }
});
