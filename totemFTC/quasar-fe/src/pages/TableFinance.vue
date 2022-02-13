<template>
  <q-page>
    <div class="row justify-end">
      <q-list bordered class="col-6 rounded-borders">
        <q-expansion-item expand-separator icon="list"
                          label="TODO List"
                          caption="Список доработок для страницы Финансы"
        >

          <q-item>
            <q-item-section>
              <q-item-label>
                ...
              </q-item-label>
              <q-item-label caption>
                ...
              </q-item-label>
            </q-item-section>
          </q-item>

        </q-expansion-item>
      </q-list>
    </div>

    <q-card bordered>
      <q-card-section>
        <div class="row no-wrap">
          <div class="col">
            <div class="text-h5">Доход</div>
            <div class="text-subtitle2">
              с
              {{date.formatDate(storeFinance.interval.from, dateFormat)}}
              по
              {{date.formatDate(storeFinance.interval.to, dateFormat)}}
            </div>
          </div>
          <div style="width: 9em;" v-if="storeUser.isAdmin(storeLogin.user)">
            <q-select v-model="incomeType" :options="incomeTypeOptions" @update:model-value="reloadIncome()"
                      borderless
            />
          </div>
          <div style="width: 1em;"/>
          <div style="width: 6em;">
            <q-select v-model="incomeInterval" :options="incomeIntervalOptions" @update:model-value="reloadIncome()"
                      borderless
            />
          </div>
          <div style="width: 1em;"/>
          <div class="col-auto self-center">
            <q-icon size="md" name="calendar_month" class="cursor-pointer">
              <q-popup-proxy v-model="uiDatePopup" cover transition-show="scale" transition-hide="scale" @before-show="beforeIntervalUpdate">
                <q-date v-model="interval" :mask="dateFormat" @update:model-value="onIntervalUpdate" range>
                  <div class="row items-center justify-end">
                    <q-btn v-close-popup label="Ok" color="primary" flat @click="intervalSet"/>
                    <q-btn v-close-popup label="Cancel" color="primary" flat />
                  </div>
                </q-date>
              </q-popup-proxy>
            </q-icon>
          </div>
        </div>
      </q-card-section>
    </q-card>

    <div class="row justify-center">
      <q-btn class="q-my-sm" size="md" @click="loadPrev()">
        <div style="width: 1em;" />
        <q-icon name="expand_less"/>
        <div style="width: 1em;" />
      </q-btn>
    </div>

    <q-table :rows="storeFinance.income" :columns="incomeColumns"
             :visible-columns="storeFinance.incomeType === 'trainerIncome' ? ['trainer'] : []"
             :loading="storeUtils.loading" :row-key="row => row.date + String(row.trainer?.userId)"
             :pagination="{rowsPerPage: 0}" hide-pagination
    />

    <div class="row justify-center" v-if="date.getDateDiff(storeFinance.interval.to, Date.now(), 'days') < 1">
      <q-btn class="q-my-lg" size="md" @click="loadNext()">
        <div style="width: 1em;" />
        <q-icon name="expand_more"/>
        <div style="width: 1em;" />
      </q-btn>
    </div>
  </q-page>

</template>

<script lang="ts">
import {defineComponent, Ref, ref} from 'vue';
import {dateFormat, UiDate, useStoreUtils, weekDateName, weekStart} from 'src/store/store_utils';
import {date} from 'quasar';
import {EntityUser, useStoreCrudUser} from 'src/store/store_crud_user';
import {useStoreLogin} from 'src/store/store_login';
import {IncomeInterval, IncomeType, useStoreFinance} from 'src/store/store_crud_finance';


export default defineComponent({
  name: 'TableFinance',
  setup () {

    const storeUser = useStoreCrudUser();
    const storeLogin = useStoreLogin();
    const storeFinance = useStoreFinance();

    const incomeColumns = [
      { name: 'date', required: true, label: 'Дата', align: 'left', field: 'date', sortable: true, format: displayDate },
      { name: 'trainer', required: false, label: 'Тренер', align: 'left', field: 'trainer', format: (val?: EntityUser) => storeUser.trainerNameString(val), sortable: true },
      { name: 'trainings', required: true, label: 'Тренировки', align: 'left', field: 'trainings', sortable: true },
      { name: 'visits', required: true, label: 'Посещения', align: 'left', field: 'visits', sortable: true },
      { name: 'income', required: true, label: 'Доход', align: 'left', field: 'income', sortable: true },
    ];

    const incomeIntervalOptions: {label: string, value: IncomeInterval}[] = [
      { label: 'Неделя', value: 'week' },
      { label: 'Месяц', value: 'month' },
    ];
    const incomeTypeOptions: {label: string, value: IncomeType}[] = [
      { label: 'Твой доход', value: 'currentTrainerIncome' },
      { label: 'Все тренеры', value: 'trainerIncome' },
      { label: 'Общий доход', value: 'totalIncome' }
    ];

    const incomeInterval = ref(incomeIntervalOptions[0]);
    const incomeType = ref(incomeTypeOptions[0]);

    const now = new Date();
    const start = date.subtractFromDate(weekStart(now), {days: 7});
    const end = date.addToDate(date.startOfDate(now, 'day'), {days: 1});

    incomeType.value = incomeTypeOptions[storeUser.isAdmin(storeLogin.user) ? 2 : 0];
    storeFinance.incomeType = incomeType.value.value;

    storeFinance.loadIncome({from: start, to: end}).catch(e => console.log('Load error', e));
    const storeUtils = useStoreUtils();

    async function reloadIncome() {
      storeFinance.incomeType = incomeType.value.value;
      storeFinance.incomeInterval = incomeInterval.value.value;
      await storeFinance.loadIncome(storeFinance.interval);
    }

    function displayDate(d: string) {
      switch (storeFinance.incomeInterval) {
        case 'month':
          return date.formatDate(d, 'MMMM YYYY');
        case 'week':
          return weekDateName(d);
      }
    }

    const interval: Ref<{from: string, to: string}> = ref({from: date.formatDate(start, dateFormat), to: date.formatDate(end, dateFormat)});
    const uiDatePopup: Ref<boolean> = ref(false);
    function beforeIntervalUpdate() {
      interval.value = {
        from: date.formatDate(storeFinance.interval.from, dateFormat),
        to: date.formatDate(date.subtractFromDate(storeFinance.interval.to, {days: 1}), dateFormat)
      };
    }

    function onIntervalUpdate(value: string | [] | unknown | null, reason: string,
                              details: UiDate & { from: UiDate, to: UiDate }) {
      console.log('::onIntervalUpdate(). Date updated:', value, reason, details);
      interval.value = {
        from: date.formatDate(weekStart(interval.value.from), dateFormat),
        to: date.formatDate(date.addToDate(weekStart(interval.value.to), {days: 6}), dateFormat)
      };
    }

    async function intervalSet() {
      console.log('::intervalSet(). Date:', interval.value);
      await storeFinance.loadIncome({from: interval.value.from, to: date.addToDate(interval.value.to, {days: 1})});
    }

    async function loadPrev() {
      await storeFinance.loadIncomePrev(date.subtractFromDate(storeFinance.interval.from, {days: 7}));
    }

    async function loadNext() {
      await storeFinance.loadIncomeNext(date.addToDate(storeFinance.interval.to, {days: 7}));
    }

    return {
      storeFinance, storeUser, storeLogin, storeUtils,
      incomeType, incomeInterval, reloadIncome, incomeColumns, incomeTypeOptions, incomeIntervalOptions,
      date, dateFormat,
      uiDatePopup, interval, beforeIntervalUpdate, onIntervalUpdate, intervalSet, loadPrev, loadNext
    }
  }
});
</script>

<style lang="scss" scoped>
</style>
