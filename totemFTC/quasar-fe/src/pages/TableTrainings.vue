<template>
  <q-page>
    <div class="row justify-end">
      <q-list bordered class="col-6 rounded-borders">
        <q-expansion-item expand-separator icon="list"
                          label="TODO List"
                          caption="Список доработок для страницы Тренировки"
        >

          <q-item>
            <q-item-section>
              <q-item-label>
                Редактирование тренировки
              </q-item-label>
              <q-item-label caption>
                Возможно стоит убрать возможность изменять время тренировки если оно в прошлом.
                Возможно стоит проверять, что новое время должно не быть в прошлом.
                Возможно стоит добавить возможность изменять не только время, но и дату при редактировании.
              </q-item-label>
            </q-item-section>
          </q-item>

          <q-item>
            <q-item-section>
              <q-item-label>
                Права на редактирование
              </q-item-label>
              <q-item-label caption>
                Сейчас у тренера нет возможности редактировать тренировку. Возможно стоит добавить
              </q-item-label>
            </q-item-section>
          </q-item>

          <q-item>
            <q-item-section>
              <q-item-label>
                Удаление тренировки
              </q-item-label>
              <q-item-label caption>
                Перед удалением надо проверять, что у тренировки нет посетителей.
              </q-item-label>
            </q-item-section>
          </q-item>

          <q-item>
            <q-item-section>
              <q-item-label>
                Просмотр тренировки
              </q-item-label>
              <q-item-label caption>
                Надо сделать страницу тренировки - с возможностью отмечать посетителей
              </q-item-label>
            </q-item-section>
          </q-item>

          <q-item>
            <q-item-section>
              <q-item-label>
                Пустые недели
              </q-item-label>
              <q-item-label caption>
                Надо показывать пустые недели и пустые дни - чтобы была возможность их заполнить
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
              <div class="text-h5">Расписание тренировок</div>
              <div class="text-subtitle2">
                с
                {{date.formatDate(storeTraining.trainingsInterval.from, dateFormat)}}
                по
                {{date.formatDate(storeTraining.trainingsInterval.to, dateFormat)}}
              </div>
            </div>
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

      <div v-for="week in storeTraining.trainingsByWeek" v-bind:key="'week-' + week.date">
        <q-card flat>
          <q-card-section>
            <div class="row no-wrap">
              <div class="col text-h6">{{ weekDateName(week.date) }}</div>
              <dev class="col-auto">
                <q-btn icon="mdi-table-arrow-down" @click="schedulePropagate(week.date)">
                  <q-tooltip>Заполнить расписание на неделю</q-tooltip>
                </q-btn>
              </dev>
            </div>
          </q-card-section>
        </q-card>

        <div class="row">
        <q-card class="q-ma-xs" v-for="day in week.dateTrainings" v-bind:key="'day-card-' + day.date">
          <q-card-section>
            <div class="row">
            <div class="col text-h6">{{ date.formatDate(day.date, 'dddd, D') }}
              <q-tooltip>{{ date.formatDate(day.date, 'D MMMM YYYY, ', formatGenitiveCase) + date.formatDate(day.date, 'dddd')}}</q-tooltip>
              <q-chip style="position: absolute; top: 0; right: 10em;" dense size="xs" class="self-end" :label="dateLabel(day.date)" v-if="dateLabel(day.date)"/>
            </div>
            <div class="col-auto" v-if="storeUser.isAdmin(storeLogin.user)">
              <q-btn round icon="add" @click="editRowStart(day.date, defaultTraining)" />
            </div>
            </div>
          </q-card-section>
          <q-card-section>
            <q-table hide-header hide-bottom :columns="trainingColumns" :rows="day.trainings" @row-click="onTrainingClick">
              <template v-slot:body-cell-actions="props">
                <q-td :props="props" v-if="storeUser.isAdmin(storeLogin.user)">
                  <q-btn round flat size="sm" icon="edit" @click.stop="editRowStart(day.date, props.row)"/>
                  <q-btn round flat size="sm" icon="delete" @click.stop="deleteRowStart(props.row)"/>
                </q-td>
              </template>
            </q-table>
          </q-card-section>
        </q-card>
        </div>

      </div>

      <div class="row justify-center">
        <q-btn class="q-my-lg" size="md" @click="loadNext()">
          <div style="width: 1em;" />
          <q-icon name="expand_more"/>
          <div style="width: 1em;" />
        </q-btn>
      </div>
  </q-page>


  <q-dialog v-model="isConfirmDelete">
    <q-card>
      <q-card-section class="row items-center">
        <q-avatar icon="delete_forever"/>
        <span class="q-ml-sm">Удалить тренировку {{ date.formatDate(deleteRowObj.time, 'HH:mm D, MMM') }} {{deleteRowObj.trainingType.trainingName}} {{storeUser.trainerNameString(deleteRowObj.trainer)}}</span>
      </q-card-section>

      <q-card-actions align="right">
        <q-btn flat label="Cancel" color="primary" v-close-popup />
        <q-btn flat label="Delete" color="primary" @click="deleteRowCommit()" />
      </q-card-actions>
    </q-card>
  </q-dialog>


  <q-dialog v-model="isConfirmAdd" persistent>
    <q-card class="q-gutter-md" style="width: 60%; max-width: 60%">
      <q-card-section>
        <div class="text-h6">{{ isRowAddOrEdit ? 'Добавить' : 'Редактировать' }}</div>
      </q-card-section>

      <q-card-section>
        <div class="row q-gutter-md">
          <q-input class="col-2" filled v-model="editRowObj.localPropertyTime" label="Время">
            <template v-slot:append>
              <q-icon name="access_time" class="cursor-pointer">
                <q-popup-proxy transition-show="scale" transition-hide="scale" v-model="uiTimePopup">
                  <q-time v-model="editRowObj.localPropertyTime" mask="HH:mm" :minute-options="[0, 15, 30, 45]" format24h @update:model-value="onTimeUpdate">
                    <div class="row items-center justify-end">
                      <q-btn v-close-popup label="Close" color="primary" flat />
                    </div>
                  </q-time>
                </q-popup-proxy>
              </q-icon>
            </template>
          </q-input>

          <q-select class="col-2" filled v-model="editRowObj.trainingType" label="Тренировка"
                    :options="storeTraining.trainingTypes" option-label="trainingName" @update:model-value="onTrainingTypeChange">
            <template v-slot:no-option>
              <q-item>
                <q-item-section class="text-grey">
                  Пожалуйста выберите тип тренировки
                </q-item-section>
              </q-item>
            </template>
          </q-select>

          <q-select class="col-4" filled v-model="editRowObj.trainer" label="Тренер"
                    :options="trainers" :option-label="storeUser.trainerNameString">
            <template v-slot:no-option>
              <q-item>
                <q-item-section class="text-grey">
                  Пожалуйста выберите тренера
                </q-item-section>
              </q-item>
            </template>
          </q-select>
        </div>
      </q-card-section>

      <q-card-actions align="right">
        <q-btn flat label="Ok" color="primary" @click="editRowCommit()" />
        <q-btn flat label="Cancel" color="primary" v-close-popup />
      </q-card-actions>
    </q-card>
  </q-dialog>

</template>


<script lang="ts">
import {computed, defineComponent, Ref, ref} from 'vue';
import {emptyTraining, EntityCrudTraining, EntityCrudTrainingType, useStoreCrudTraining} from 'src/store/store_crud_training';
import {dateFormat, dateLabel, DateValue, formatGenitiveCase, timeFormat, UiDate, weekDateName, weekStart} from 'src/store/store_utils';
import {date} from 'quasar';
import {EntityUser, useStoreCrudUser} from 'src/store/store_crud_user';
import {useStoreLogin} from 'src/store/store_login';
import router from 'src/router';

export default defineComponent({
  name: 'TableTrainings',
  setup () {
    const trainingColumns = [
      {name: 'time', required: true, label: 'Время', align: 'left', field: 'time', format: (val: string) => date.formatDate(val, timeFormat)},
      {name: 'type', required: true, label: 'Тренировка', align: 'left', field: 'trainingType', format: (val: EntityCrudTrainingType) => `${val.trainingName}`,},
      {name: 'trainer', required: true, label: 'Тренер', align: 'left', field: 'trainer', format: (val: EntityUser) => `${val.nickName}`},
      {name: 'actions', label: 'Actions'}
    ];

    const now = new Date();
    const start = date.subtractFromDate(weekStart(now), {days: 7});
    const end = date.addToDate(start, {days: 20});
    const interval: Ref<{from: string, to: string}> = ref({from: date.formatDate(start, dateFormat), to: date.formatDate(end, dateFormat)});

    const storeTraining = useStoreCrudTraining();
    storeTraining.loadTrainings({from: start, to: end}).catch(e => console.log('Load trainings error', e));

    const trainers: Ref<EntityUser[]> = ref([]);
    storeTraining.loadTrainers().catch(e => console.log('Load trainers error', e));
    storeTraining.loadTrainingTypes().catch(e => console.log('Load training types error', e));

    const storeUser = useStoreCrudUser();
    const storeLogin = useStoreLogin();

    async function schedulePropagate(d: DateValue) {
      await storeTraining.schedulePropagate(d);
      await storeTraining.loadTrainings(storeTraining.trainingsInterval);
    }

    const deleteRowObj: Ref<EntityCrudTraining | null> = ref(null);

    function deleteRowStart(row: EntityCrudTraining) {
      deleteRowObj.value = row;
      console.log('Confirm delete row', row);
    }

    async function deleteRowCommit() {
      console.log('Delete row ', deleteRowObj.value);
      deleteRowObj.value && await storeTraining.deleteTraining(deleteRowObj.value.id);
      deleteRowObj.value = null;
    }


    const editRowObj: Ref<EntityCrudTraining | null> = ref(null);

    function editRowStart(d: string, training: EntityCrudTraining) {
      console.log('Start edit row', training);
      editRowObj.value = Object.assign({}, training);
      if (training.id === -1) {
        editRowObj.value.localPropertyTime = '00:00';
        editRowObj.value.time = d;
      } else {
        editRowObj.value.localPropertyTime = date.formatDate(training.time, timeFormat);
      }
      trainers.value = [];
    }

    async function editRowCommit() {
      console.log('Add row', editRowObj.value);
      const training = editRowObj.value as EntityCrudTraining;
      training.time = `${date.formatDate(training.time, dateFormat)} ${training.localPropertyTime as string}`;
      if (training.id === -1) {
        await storeTraining.createTraining(training);
      } else {
        await storeTraining.updateTraining(training);
      }
      editRowObj.value = null;
    }

    async function loadPrev() {
      await storeTraining.loadTrainingsPrev(date.subtractFromDate(storeTraining.trainingsInterval.from, {days: 7}));
    }

    async function loadNext() {
      await storeTraining.loadTrainingsNext(date.addToDate(storeTraining.trainingsInterval.to, {days: 7}));
    }

    const uiDatePopup: Ref<boolean> = ref(false);

    function beforeIntervalUpdate() {
      interval.value = {
        from: date.formatDate(storeTraining.trainingsInterval.from, dateFormat),
        to: date.formatDate(date.subtractFromDate(storeTraining.trainingsInterval.to, {days: 1}), dateFormat)
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
      await storeTraining.loadTrainings({from: interval.value.from, to: date.addToDate(interval.value.to, {days: 1})});
    }

    const uiTimePopup = ref(false);
    function onTimeUpdate(value:string) {
      console.log(`Time updated: ${value}`);
      uiTimePopup.value = false;
    }

    function onTrainingTypeChange(type: EntityCrudTrainingType) {
      trainers.value = storeTraining.trainers.filter(v => v.trainingTypes.indexOf(type.trainingType) > -1);
    }

    async function onTrainingClick(evt: Event, training: EntityCrudTraining) {
      await router.push({ name: 'visit', params: { trainingId: training.id } });
    }

    return {
      storeUser, storeLogin,
      trainingColumns,
      storeTraining,
      trainers,
      schedulePropagate,
      weekDateName,
      uiDatePopup,
      interval,
      loadPrev, loadNext,
      beforeIntervalUpdate,
      onIntervalUpdate,
      intervalSet,
      isConfirmDelete: computed({get: () => deleteRowObj.value !== null, set: () => deleteRowObj.value = null}),
      isConfirmAdd: computed({get: () => editRowObj.value !== null, set: () => editRowObj.value = null}),
      isRowAddOrEdit: computed(() => editRowObj.value?.id === -1),
      deleteRowObj,
      deleteRowStart,
      deleteRowCommit,
      editRowObj,
      editRowStart,
      editRowCommit,
      defaultTraining: emptyTraining,
      date,
      dateLabel, dateFormat, formatGenitiveCase,
      uiTimePopup, onTimeUpdate, onTrainingTypeChange,
      onTrainingClick,
    }
  }
});
</script>

<style lang="scss" scoped>
</style>
