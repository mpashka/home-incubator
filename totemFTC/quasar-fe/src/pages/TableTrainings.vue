<template>
  <div class="row">
    <div style="display: contents">
      <q-btn icon="fas fa-caret-left"/>
      <q-btn :label="storeVisit.date">
        <q-chip style="position: absolute; top: 0; right: 1em;" dense size="xs" class="self-end" :label="visitDateLabel()"/>

        <q-popup-proxy transition-show="scale" transition-hide="scale" ref="uiDatePopup">
          <q-date v-model="storeVisit.date" :mask="dateFormat" @update:model-value="onVisitDateUpdate">
            <div class="row items-center justify-end">
              <q-btn label="Сегодня" color="primary" flat @click="onVisitDateUpdate(date.formatDate(new Date(), dateFormat))"/>
              <q-btn label="Вчера" color="primary" flat @click="onVisitDateUpdate(date.formatDate(date.subtractFromDate(new Date(), {days: 1}), dateFormat))"/>
            </div>
          </q-date>
        </q-popup-proxy>
      </q-btn>
      <q-btn icon="fas fa-caret-right"/>
    </div>

    <div style="width: 3em;"></div>

    <q-select class="col-auto" v-model="storeVisit.training" :options="storeVisit.trainings" option-label="displayTraining"
              @update:model-value="storeVisit.reloadVisits()" filled >

      <template v-slot:before-options>
        Тренировки ...
      </template>

      <template v-slot:selected-item="props">
        <!--{{displayTraining(props.opt)}}-->
        <div class="q-gutter-x-sm" style="display: flex">
        <training-line :training="props.opt"/>
        </div>
      </template>

      <template v-slot:option="props">
        <q-item @click="props.toggleOption(props.opt)" clickable :active="props.selected">
          <q-item-section>
            <div class="row" >
              <training-line :training="props.opt"/>
            </div>
          </q-item-section>
        </q-item>
      </template>

    </q-select>
  </div>

  <q-table :rows="storeVisit.visits" :columns="visitColumns"
           :loading="storeUtils.loading" :row-key="visitId"
    title="Посещения">

    <template v-slot:top-right>
      <q-btn round icon="fas fa-plus" @click="editRowStart(emptyVisit)"/>
    </template>


    <template v-slot:body-cell-actions="props">
      <q-td :props="props">
        <q-btn round flat size="sm" @click="rowMark(props.row)">
          <q-icon name="fas fa-check" :class="props.row.markMaster ? 'text-green' : null"/>
        </q-btn>
        <q-btn round flat size="sm" icon="edit" @click="editRowStart(props.row)"/>
        <q-btn round flat size="sm" icon="delete" @click="deleteRowStart(props.row)"/>
      </q-td>
    </template>

  </q-table>

  <q-dialog v-model="isConfirmDelete">
    <q-card>
      <q-card-section class="row items-center">
        <q-avatar icon="signal_wifi_off" color="primary" text-color="white" />
        <span class="q-ml-sm">Удалить посещение {{ storeUser.userNameString(deleteRowObj.user) }}</span>
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
        <q-select v-if="isRowAddOrEdit"
                  filled v-model="editRowObj.user" use-input
                  input-debounce="0" label="Работяга"
                  :options="storeUser.filteredRows" :option-label="storeUser.userNameString"
                  @filter="(f, u) => u(() => storeUser.setFilterByName(f))"
        >
          <template v-slot:no-option>
            <q-item>
              <q-item-section class="text-grey">
                Пожалуйста выберите жертву
              </q-item-section>
            </q-item>
          </template>
        </q-select>
        <q-input v-model="editRowObj.comment" label="Комментарий"/>
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
import {dateFormat} from 'src/store/store_crud_training'
import {EntityCrudVisit, useStoreCrudVisit, emptyVisit} from 'src/store/store_crud_visits';
import {useStoreUtils} from 'src/store/store_utils';
import {date, QPopupProxy} from 'quasar'
import {useStoreCrudUser, EntityUser} from "src/store/store_crud_user";
import TrainingLine from "pages/components/TrainingLine.vue";

const visitColumns = [
  { name: 'userFirstName', required: true, label: 'Ф', align: 'left', field: 'user', format: (val: EntityUser) => val.firstName, sortable: true },
  { name: 'userLastName', required: true, label: 'И', align: 'left', field: 'user', format: (val: EntityUser) => val.lastName, sortable: true },
  { name: 'userNickName', required: true, label: 'Имя', align: 'left', field: 'user', format: (val: EntityUser) => val.nickName, sortable: true },
  { name: 'comment', required: false, label: 'Примечание', align: 'left', field: 'comment', sortable: false },
  { name: 'actions', label: 'Actions'},
]

export default defineComponent({
  name: 'TableTrainings',
  components: {TrainingLine},
  setup () {

    const storeVisit = useStoreCrudVisit();
    const storeUser = useStoreCrudUser();
    const storeUtils = useStoreUtils();
    storeVisit.reloadTrainings().catch(e => console.log('Load error', e));
    storeUser.load().catch(e => console.log('Load error', e));

    const deleteRowObj :Ref<EntityCrudVisit | null> = ref(null);

    function deleteRowStart(row: EntityCrudVisit) {
      deleteRowObj.value = row;
      console.log('Confirm delete row', row);
    }

    async function deleteRowCommit() {
      console.log('Delete row ', deleteRowObj);
      deleteRowObj.value && await storeVisit.deleteVisit(deleteRowObj.value);
      deleteRowObj.value = null;
    }


    const editRowObj :Ref<EntityCrudVisit | null> = ref(null);

    function editRowStart(row: EntityCrudVisit) {
      console.log('Start edit row', row);
      editRowObj.value = Object.assign({}, row);
    }

    async function editRowCommit() {
      console.log('Add row', editRowObj, 'Selected training id', storeVisit.training.id);
      const newValue = editRowObj.value as EntityCrudVisit;
      if (newValue.trainingId === -1) {
        newValue.trainingId = storeVisit.training.id;
        newValue.markMaster = true;
        await storeVisit.createVisit(newValue);
      } else {
        await storeVisit.updateComment(newValue);
      }
      editRowObj.value = null;
    }

    async function rowMark(row: EntityCrudVisit) {
      await storeVisit.update(row, 'Master', !row.markMaster);
    }

    const uiDatePopup = ref(null);
    async function onVisitDateUpdate(value:string) {
      console.log(`Date updated: ${value}`);
      (uiDatePopup.value as unknown as QPopupProxy).hide();
      await storeVisit.setDate(value);
    }

    function visitId(visit: EntityCrudVisit): string {
      return String(visit.trainingId) + '_' + String(visit.user.userId);
    }

    function visitDateLabel(): string {
      switch (date.getDateDiff(storeVisit.date, new Date().toDateString(), 'days')) {
        case 0: return 'Сегодня';
        case 1: return 'Завтра';
        case 2: return 'Послезавтра';
        case 7: return 'Через неделю';
        case -1: return 'Вчера';
        case -2: return 'Позавчера';
        case -7: return 'Неделю назад';
      }
      return '';
    }

    return {
      visitColumns,
      storeVisit,
      storeUtils,
      storeUser,
      emptyVisit,
      visitDateLabel,
      visitId,
      dateFormat,
      uiDatePopup,
      onVisitDateUpdate,
      isConfirmDelete: computed({get: () => deleteRowObj.value !== null, set: () => deleteRowObj.value = null}),
      isConfirmAdd: computed({get: () => editRowObj.value !== null, set: () => editRowObj.value = null}),
      isRowAddOrEdit: computed(() => editRowObj.value?.trainingId === -1),
      deleteRowObj,
      deleteRowStart,
      deleteRowCommit,
      editRowObj,
      editRowStart,
      editRowCommit,
      rowMark,
      date,
    }
  }
});
</script>

<style lang="scss" scoped>
</style>
