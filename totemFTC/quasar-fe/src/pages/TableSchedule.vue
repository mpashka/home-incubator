<template>
  <div class="row">
  <q-card v-for="day in 7" v-bind:key="'day-card-' + day">
    <q-card-section>
      <div class="text-h6">{{ weekDayName(day) }}</div>
    </q-card-section>
    <q-card-section>
      <q-table hide-header hide-bottom :columns="columns" :rows="selectSchedule(day)">
        <template v-slot:body-cell-actions="props">
          <q-td :props="props">
            <q-btn round flat size="sm" icon="edit" @click="editRowStart(day, props.row)"/>
            <q-btn round flat size="sm" icon="delete" @click="deleteRowStart(props.row)"/>
          </q-td>
        </template>
      </q-table>
    </q-card-section>
    <q-card-actions align="right">
      <q-btn round icon="plus" @click="editRowStart(day, defaultRow)" />
    </q-card-actions>
  </q-card>
  </div>

  <q-dialog v-model="confirmDelete">
    <q-card>
      <q-card-section class="row items-center">
        <q-avatar icon="signal_wifi_off" color="primary" text-color="white" />
        <span class="q-ml-sm">Удалить запись?</span>
      </q-card-section>

      <q-card-actions align="right">
        <q-btn flat label="Cancel" color="primary" v-close-popup />
        <q-btn flat label="Delete" color="primary" @click="deleteRowCommit()" />
      </q-card-actions>
    </q-card>
  </q-dialog>

  <q-dialog v-model="confirmAdd" persistent>
    <q-card class="q-gutter-md" style="width: 60%; max-width: 60%">
      <q-card-section>
        <div class="text-h6">{{ isRowAddOrEdit ? 'Добавить' : 'Редактировать' }}</div>
      </q-card-section>

      <q-card-section>
        <div class="row q-gutter-md">
        <q-input class="col-1" filled v-model="editRowObj.time" label="Время">
          <template v-slot:append>
            <q-icon name="access_time" class="cursor-pointer">
              <q-popup-proxy transition-show="scale" transition-hide="scale" ref="timePopup">
                <q-time v-model="editRowObj.time" mask="HH:mm" :minute-options="[0, 15, 30, 45]" format24h @update:model-value="onTimeUpdate">
                  <div class="row items-center justify-end">
                    <q-btn v-close-popup label="Close" color="primary" flat />
                  </div>
                </q-time>
              </q-popup-proxy>
            </q-icon>
          </template>
        </q-input>

          <q-select class="col-2" filled v-model="editRowObj.trainingType" label="Тренировка"
                    :options="store.trainingTypes" option-label="trainingName" @update:model-value="onTrainingTypeChange">
            <template v-slot:no-option>
              <q-item>
                <q-item-section class="text-grey">
                  Пожалуйста выберите тип тренировки
                </q-item-section>
              </q-item>
            </template>
          </q-select>

          <q-select class="col-4" filled v-model="editRowObj.trainer" label="Тренер"
                    :options="trainers" option-label="nickName">
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
import { ref, computed, Ref } from 'vue';
import {
  emptySchedule,
  EntityCrudSchedule, EntityCrudTrainer,
  EntityCrudTrainingType,
  useStoreCrudSchedule
} from 'src/store/store_crud_schedule';
import {QPopupProxy} from "quasar";

export default {
  name: 'TableSchedule',
  setup () {
    const store = useStoreCrudSchedule();
    const trainers = ref(store.trainers);
    store.load().catch(e => console.log('Load error', e));
    store.loadTrainers()
      .then(() => trainers.value = store.trainers)
      .catch(e => console.log('Load error', e));
    store.loadTrainingTypes().catch(e => console.log('Load error', e));

    async function deleteRowCommit() {
      console.log('Delete row ', deleteRowObj);
      deleteRowObj.value && await store.delete(deleteRowObj.value?.id);
      deleteRowObj.value = null;
    }

    async function editRowCommit() {
      console.log('Add row', editRowObj.value);
      const newValue = editRowObj.value as EntityCrudSchedule;
      if (newValue.id === -1) {
        await store.create(newValue);
      } else {
        await store.update(newValue);
      }
      editRowObj.value = null;
    }

    const columns = [
      { name: 'time', required: true, label: 'Время', align: 'left', field: 'time' },
      { name: 'type', required: true, label: 'Тренировка', align: 'left', field: 'trainingType', format: (val: EntityCrudTrainingType) => `${val.trainingName}`,},
      { name: 'trainer', required: true, label: 'Тренер', align: 'left', field: 'trainer', format: (val: EntityCrudTrainer) => `${val.nickName}`},
      { name: 'actions', label: 'Actions'}
    ]

    const deleteRowObj :Ref<EntityCrudSchedule | null> = ref(null);

    function deleteRowStart(row: EntityCrudSchedule) {
      deleteRowObj.value = row;
      console.log('Confirm delete row', row);
    }

    const editRowObj :Ref<EntityCrudSchedule | null> = ref(null);

    function editRowStart(day: number, row: EntityCrudSchedule) {
      console.log('Start edit row', row);
      editRowObj.value = Object.assign({}, row);
      editRowObj.value.day = day;
    }

    function weekDayName(weekDay: number) {
      return new Date(1971, 1, weekDay).toLocaleDateString("ru-RU", { weekday: 'long' });
    }

    function selectSchedule(weekDay: number): EntityCrudSchedule[] {
      return store.schedule.filter(s => s.day == weekDay);
    }

    function onTrainingTypeChange(type: EntityCrudTrainingType) {
      trainers.value = store.trainers.filter(v => v.trainingTypes.indexOf(type.trainingType) > -1);
    }

    const timePopup = ref(null);
    function onTimeUpdate(value:string) {
      console.log(`Time updated: ${value}`);
      (timePopup.value as unknown as QPopupProxy).hide();
    }

    return {
      columns,
      store,
      weekDayName,
      selectSchedule,
      deleteRowStart,
      deleteRowCommit,
      deleteRowObj,
      editRowStart,
      editRowCommit,
      editRowObj,
      trainers,
      onTrainingTypeChange,
      timePopup,
      onTimeUpdate,
      defaultRow: emptySchedule,
      confirmDelete: computed({get: () => deleteRowObj.value !== null, set: () => deleteRowObj.value = null}),
      confirmAdd: computed({get: () => editRowObj.value !== null, set: () => editRowObj.value = null}),
      isRowAddOrEdit: computed(() => editRowObj.value?.id === -1),
    }
  }
}
</script>

<style scoped>

</style>
