import {defineStore} from 'pinia'
import {api} from 'boot/axios';
import {randomString} from 'src/components/utils';
import {EntityCrudTrainer, emptyTrainer} from 'src/store/store_crud_trainers';
import {processLoadError} from "src/store/store_utils";
import { date } from 'quasar';

export interface EntityCrudVisit {
  visitId: number,
  trainer: EntityCrudTrainer,
  visitDate: string,
  visitComment: string,
}

export const dateFormat = 'YYYY-MM-DD HH:mm';

const emptyVisit: EntityCrudVisit = {
  visitId: -1,
  trainer: emptyTrainer,
  visitDate: date.formatDate(new Date(), dateFormat),
  visitComment: '',
};

export {emptyVisit};

export const useStoreCrudVisit = defineStore('crudVisit', {
  state: () => ({
    rows: [] as EntityCrudVisit[],
    loadingStack: [] as string[],
  }),

  getters: {
    isLoading(): boolean {
      return this.loadingStack.length > 0;
    }
  },

  actions: {
    startLoading(operation: string): string {
      const id = `${operation}_${randomString(10)}`;
      this.loadingStack.push(id);
      return id;
    },

    stopLoading(id: string) {
      this.loadingStack = this.loadingStack.filter(v => id !== v);
    },

    async load() {
      const loadId = this.startLoading('load');
      try {
        const axiosResponse = await api.get('/api/visit/list');
        console.log('Http response', axiosResponse);
        this.rows = axiosResponse.data as EntityCrudVisit[];
        console.log('Rows received', this.rows);
      } catch (e) {
        processLoadError('visits.load', e);
      } finally {
        this.stopLoading(loadId);
      }
    },

    async delete(id: number) {
      const loadId = this.startLoading('delete');
      try {
        await api.delete(`/api/visit/${id}`)
        this.rows = this.rows.filter(r => r.visitId !== id);
      } catch (e) {
        processLoadError('visits.delete', e);
      } finally {
        this.stopLoading(loadId);
      }
    },

    async create(visit: EntityCrudVisit) {
      const loadId = this.startLoading('create');
      try {
        visit.visitId = (await api.post('/api/visit', visit)).data as number;
        this.rows.push(visit);
      } catch (e) {
        processLoadError('visits.create', e);
      } finally {
        this.stopLoading(loadId);
      }
    },

    async update(visit: EntityCrudVisit) {
      const loadId = this.startLoading('update');
      try {
        await api.put('/api/visit', visit);
        const index = this.rows.findIndex(r => r.visitId === visit.visitId);
        if (index > 0) {
          this.rows[index] = visit;
        }
      } catch (e) {
        processLoadError('visits.update', e);
      } finally {
        this.stopLoading(loadId);
      }
    },
  },
});
