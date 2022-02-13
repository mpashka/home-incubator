<template>
  <div class="row">
  <q-card v-for="day in 7" v-bind:key="'day-card-' + day">
    <q-card-section>
      <div class="text-h6">{{ weekDayName(day) }}</div>
    </q-card-section>
    <q-card-section>
      <q-table hide-header hide-bottom :columns="columns" :rows="selectSchedule(day)">
        <template v-slot:body-cell-actions="props">
          <q-td :props="props" v-if="storeUser.isAdmin(storeLogin.user)">
            <q-btn round flat size="sm" icon="edit" @click="editRowStart(day, props.row)"/>
            <q-btn round flat size="sm" icon="delete" @click="deleteRowStart(props.row)"/>
          </q-td>
        </template>
      </q-table>
    </q-card-section>
    <q-card-actions align="right" v-if="storeUser.isAdmin(storeLogin.user)">
      <q-btn round icon="add" @click="editRowStart(day, defaultRow)" />
    </q-card-actions>
  </q-card>
  </div>

  <q-dialog v-model="isConfirmDelete">
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

  <q-dialog v-model="isConfirmAdd" persistent>
    <q-card class="q-gutter-md" style="width: 60%; max-width: 60%">
      <q-card-section>
        <div class="text-h6">{{ isRowAddOrEdit ? 'Добавить' : 'Редактировать' }}</div>
      </q-card-section>

      <q-card-section>
        <div class="row q-gutter-md">
        <q-input class="col-2" filled v-model="editRowObj.time" label="Время">
          <template v-slot:append>
            <q-icon name="access_time" class="cursor-pointer">
              <q-popup-proxy transition-show="scale" transition-hide="scale" v-model="uiTimePopup">
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
import {ref, computed, Ref, defineComponent} from 'vue';
import {
  emptySchedule,
  EntityCrudSchedule,
  useStoreCrudSchedule
} from 'src/store/store_crud_schedule';
import {EntityCrudTrainingType, useStoreCrudTraining} from 'src/store/store_crud_training';
import {useStoreCrudUser, EntityUser} from 'src/store/store_crud_user';
import {useStoreLogin} from 'src/store/store_login';

export default defineComponent({
  name: 'TableSchedule',
  setup () {
    const storeUser = useStoreCrudUser();
    const storeLogin = useStoreLogin();
    const storeSchedule = useStoreCrudSchedule();
    const storeTraining = useStoreCrudTraining();
    const trainers:Ref<EntityUser[]> = ref([]);
    storeSchedule.load().catch(e => console.log('Load error', e));
    storeTraining.loadTrainers().catch(e => console.log('Load error', e));
    storeTraining.loadTrainingTypes().catch(e => console.log('Load error', e));

    const columns = [
      { name: 'time', required: true, label: 'Время', align: 'left', field: 'time' },
      { name: 'type', required: true, label: 'Тренировка', align: 'left', field: 'trainingType', format: (val: EntityCrudTrainingType) => `${val.trainingName}`,},
      { name: 'trainer', required: true, label: 'Тренер', align: 'left', field: 'trainer', format: (val: EntityUser) => storeUser.trainerNameString(val)},
      { name: 'actions', label: 'Actions'}
    ]


    const deleteRowObj :Ref<EntityCrudSchedule | null> = ref(null);

    function deleteRowStart(row: EntityCrudSchedule) {
      deleteRowObj.value = row;
      console.log('Confirm delete row', row);
    }

    async function deleteRowCommit() {
      console.log('Delete row ', deleteRowObj);
      deleteRowObj.value && await storeSchedule.delete(deleteRowObj.value?.id);
      deleteRowObj.value = null;
    }


    const editRowObj :Ref<EntityCrudSchedule | null> = ref(null);

    function editRowStart(day: number, row: EntityCrudSchedule) {
      console.log('Start edit row', row);
      editRowObj.value = Object.assign({}, row);
      editRowObj.value.day = day;
      trainers.value = [];
    }

    async function editRowCommit() {
      console.log('Add row', editRowObj.value);
      const newValue = editRowObj.value as EntityCrudSchedule;
      if (newValue.id === -1) {
        await storeSchedule.create(newValue);
      } else {
        await storeSchedule.update(newValue);
      }
      editRowObj.value = null;
    }


    function weekDayName(weekDay: number) {
      return new Date(1971, 1, weekDay).toLocaleDateString("ru-RU", { weekday: 'long' });
    }

    function selectSchedule(weekDay: number): EntityCrudSchedule[] {
      return storeSchedule.schedule.filter(s => s.day == weekDay);
    }

    function onTrainingTypeChange(type: EntityCrudTrainingType) {
      trainers.value = storeTraining.trainers.filter(v => v.trainingTypes.indexOf(type.trainingType) > -1);
    }

    const uiTimePopup = ref(false);
    function onTimeUpdate(value:string) {
      console.log(`Time updated: ${value}`);
      uiTimePopup.value = false;
    }

    return {
      storeUser, storeLogin, storeTraining,
      columns,
      weekDayName,
      selectSchedule,
      deleteRowObj, deleteRowStart, deleteRowCommit,
      editRowObj, editRowStart, editRowCommit,
      trainers, onTrainingTypeChange,
      uiTimePopup, onTimeUpdate,
      defaultRow: emptySchedule,
      isConfirmDelete: computed({get: () => deleteRowObj.value !== null, set: () => deleteRowObj.value = null}),
      isConfirmAdd: computed({get: () => editRowObj.value !== null, set: () => editRowObj.value = null}),
      isRowAddOrEdit: computed(() => editRowObj.value?.id === -1),
    }
  }
});
</script>

<style scoped>

</style>
