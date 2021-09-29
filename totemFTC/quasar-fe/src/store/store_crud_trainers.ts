import {defineStore} from 'pinia'
import {api} from 'boot/axios';
import {randomString} from 'src/components/utils';

export interface EntityCrudTrainer {
  trainerId: number,
  trainerName: string,
}

/*
const emptyTrainer: EntityCrudTrainer = {
  trainerId: -1,
  trainerName: '',
};
*/

export const useStoreCrudTrainer = defineStore('crudTrainer', {
  state: () => ({
    rows: [] as EntityCrudTrainer[],
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
        const axiosResponse = await api.get('/api/trainer/list');
        console.log('Http response', axiosResponse);
        this.rows = axiosResponse.data as EntityCrudTrainer[];
        console.log('Rows received', this.rows);
      } catch (e) {
        console.log('Unexpected Http load Error', e);
        throw e;
      } finally {
        this.stopLoading(loadId);
      }
    },

    async delete(id: number) {
      const loadId = this.startLoading('delete');
      try {
        await api.delete(`/api/trainer/${id}`)
        this.rows = this.rows.filter(r => r.trainerId !== id);
      } catch (e) {
        console.log('Unexpected Http delete Error', e);
        throw e;
      } finally {
        this.stopLoading(loadId);
      }
    },

    async create(trainer: EntityCrudTrainer) {
      const loadId = this.startLoading('create');
      try {
        trainer.trainerId = (await api.post('/api/trainer', trainer)).data as number;
        this.rows.push(trainer);
      } catch (e) {
        console.log('Unexpected Http create Error', e);
        throw e;
      } finally {
        this.stopLoading(loadId);
      }
    },

    async update(trainer: EntityCrudTrainer) {
      const loadId = this.startLoading('update');
      try {
        await api.put('/api/trainer', trainer);
        const index = this.rows.findIndex(r => r.trainerId == trainer.trainerId);
        if (index > 0) {
          this.rows[index] = trainer;
        }
      } catch (e) {
        console.log('Unexpected Http update Error', e);
        throw e;
      } finally {
        this.stopLoading(loadId);
      }
    },

  },
});
