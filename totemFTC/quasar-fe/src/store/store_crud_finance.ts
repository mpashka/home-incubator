import {defineStore} from 'pinia';
import {api} from 'boot/axios';
import {dateFormat, DateInterval, DateValue} from 'src/store/store_utils';
import {date} from 'quasar';
import {EntityUser} from 'src/store/store_crud_user';

export interface EntityIncome {
  date: string,
  trainings: number,
  visits: number,
  ticketIncome: number,
  income: number,
  trainer: EntityUser,
}

export type IncomeType = 'currentTrainerIncome' | 'trainerIncome' | 'totalIncome';
export type IncomeInterval = 'week' | 'month';

export const useStoreFinance = defineStore('crudFinance', {
  state: () => ({
    interval: {from: '', to: ''} as DateInterval,
    income: [] as EntityIncome[],
    incomeType: 'trainerIncome' as IncomeType,
    incomeInterval: 'week' as IncomeInterval,
  }),

  actions: {
    async loadIncomeByDate(interval: DateInterval): Promise<EntityIncome[]> {
      return (await api.get<EntityIncome[]>(`/api/finance/${this.incomeType}/${this.incomeInterval}?from=${date.formatDate(interval.from, dateFormat)}&to=${date.formatDate(interval.to, dateFormat)}`)).data;
    },

    async loadIncome(interval: DateInterval) {
      // console.log('Loading income', interval);
      this.income = await this.loadIncomeByDate(interval);
      this.interval = interval;
      // console.log('Income: ', this.income);
    },

    async loadIncomePrev(from: DateValue) {
      const prevIncome = await this.loadIncomeByDate({from: from, to: this.interval.from});
      this.interval.from = from;
      this.income = prevIncome.concat(this.income);
    },

    async loadIncomeNext(to: DateValue) {
      const nextTrainings = await this.loadIncomeByDate({from: this.interval.to, to: to});
      this.interval.to = to;
      this.income = this.income.concat(nextTrainings);
      // this.trainings.sort((a,b) => a.time.localeCompare(b.time));
    },
  },
});
