<template>
  <q-page>

  <div class="row">
    <div style="display: contents">
      <q-btn icon="fas fa-caret-left" @click="storeVisit.addDate(-1)"/>
      <q-btn :label="storeVisit.date">
        <q-chip style="position: absolute; top: 0; right: 1em;" dense size="xs" class="self-end" :label="visitDateLabel" v-if="visitDateLabel.length > 0"/>

        <q-popup-proxy transition-show="scale" transition-hide="scale">
          <q-date first-day-of-week="1" v-model="storeVisit.date" :mask="dateFormat" @update:model-value="onVisitDateUpdate">
            <div class="row items-center justify-end">
              <q-btn v-close-popup label="Вчера" color="primary" flat @click="onVisitDateUpdate(date.subtractFromDate(Date.now(), {days: 1}))"/>
              <q-btn v-close-popup label="Сегодня" color="primary" flat @click="onVisitDateUpdate(Date.now())"/>
              <q-btn v-close-popup label="Завтра" color="primary" flat @click="onVisitDateUpdate(date.addToDate(Date.now(), {days: 1}))"/>
            </div>
          </q-date>
        </q-popup-proxy>
      </q-btn>
      <q-btn icon="fas fa-caret-right" @click="storeVisit.addDate(1)"/>
    </div>

    <div style="width: 3em;"></div>

    <q-select class="col-auto" v-model="storeVisit.training"
              label="Тренировка"
              :options="storeVisit.trainings" option-label="displayTraining"
              @update:model-value="storeVisit.reloadVisits()" filled >

      <template v-slot:selected-item="props">
        <training-line :training="props.opt" v-if="props.opt.id !== -1"/>
        <q-item-section class="text-grey" v-else>
          Пожалуйста выберите тренировку
        </q-item-section>
      </template>

      <template v-slot:option="props">
        <q-item @click="props.toggleOption(props.opt)" clickable :active="props.selected">
          <q-item-section>
            <training-line :training="props.opt"/>
          </q-item-section>
        </q-item>
      </template>

    </q-select>
  </div>

  <q-table :rows="storeVisit.visits" :columns="visitColumns"
           :loading="storeUtils.loading" row-key="visitId"
           title="Посещения" v-if="storeVisit.training.id !== -1">

    <template v-slot:top-right>
      <q-btn round icon="fas fa-plus" @click="editRowStart(emptyVisit)" v-if="storeUser.isAdmin(storeLogin.user)"/>
    </template>


    <template v-slot:body-cell-actions="props">
      <q-td :props="props" v-if="storeUser.isAdmin(storeLogin.user)">
        <q-btn round flat size="sm" @click="rowMark(props.row)">
          <q-icon name="schedule" size="sm" v-if="props.row.markSchedule"/>
          <q-icon name="mdi-account-check" size="sm" v-if="props.row.markSelf === 'on'"/>
          <q-icon name="mdi-account-cancel" size="sm" v-if="props.row.markSelf === 'off'"/>
          <q-icon name="mdi-shield-check" class="text-green" size="sm" v-if="props.row.markMaster === 'on'"/>
          <q-icon name="mdi-shield-remove-outline" class="text-red" size="sm" v-if="props.row.markMaster === 'off'"/>
          <q-icon name="mdi-help-rhombus-outline" size="sm" v-if="props.row.markMaster === 'unmark'"/>
        </q-btn>
        <q-btn round flat size="sm" icon="edit" @click="editRowStart(props.row)"/>
        <q-btn round flat size="sm" icon="delete" @click="deleteRowStart(props.row)"/>
      </q-td>
    </template>

  </q-table>
  </q-page>

  <q-dialog v-model="isConfirmDelete">
    <q-card>
      <q-card-section class="row items-center">
        <q-avatar icon="signal_wifi_off" color="primary" text-color="white" />
        <span class="q-ml-sm">Удалить посещение {{ storeLogin.userNameString(deleteRowObj.user) }}</span>
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
                  input-debounce="0" label="Посетитель"
                  :options="storeUser.filteredRows" :option-label="storeLogin.userNameString"
                  @filter="(f, u) => u(() => storeUser.setFilterByName(f))"
        >
          <template v-slot:no-option>
            <q-item>
              <q-item-section class="text-grey">
                Пожалуйста выберите посетителя
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
import {computed, defineComponent, Ref, ref} from 'vue';
import {emptyVisit, EntityCrudVisit, nextMark, useStoreCrudVisit} from 'src/store/store_crud_visit';
import {dateFormat, dateLabel, DateValue, useStoreUtils} from 'src/store/store_utils';
import {date} from 'quasar';
import {EntityUser, useStoreCrudUser} from 'src/store/store_crud_user';
import {useStoreLogin} from 'src/store/store_login';
import TrainingLine from 'pages/components/TrainingLine.vue';

export default defineComponent({
  name: 'TableVisits',
  components: {TrainingLine},
  setup () {

    const visitColumns = [
      { name: 'userFirstName', required: true, label: 'Фамилия', align: 'left', field: 'user', format: (val: EntityUser) => val.firstName, sortable: true },
      { name: 'userLastName', required: true, label: 'Имя', align: 'left', field: 'user', format: (val: EntityUser) => val.lastName, sortable: true },
      { name: 'userNickName', required: true, label: 'Ник', align: 'left', field: 'user', format: (val: EntityUser) => val.nickName, sortable: true },
      { name: 'comment', required: false, label: 'Примечание', align: 'left', field: 'comment', sortable: false },
      { name: 'actions', label: 'Actions'},
    ];

    const storeVisit = useStoreCrudVisit();
    const storeUser = useStoreCrudUser();
    const storeLogin = useStoreLogin();
    const storeUtils = useStoreUtils();
    storeVisit.reloadTrainings().catch(e => console.log('Load error', e));
    storeUser.load().catch(e => console.log('Load error', e));
    storeUser.disableFilter();

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
      storeUser.setFilterByType('user');
      storeUser.setFilterExclude(storeVisit.visits.map(v => v.user.userId));
      editRowObj.value = Object.assign({}, row);
    }

    async function editRowCommit() {
      console.log('Add row', editRowObj, 'Selected training id', storeVisit.training.id);
      const newValue = editRowObj.value as EntityCrudVisit;
      if (newValue.trainingId === -1) {
        const presentUserVisits = storeVisit.visits.filter(v => v.user.userId == newValue.user.userId);
        if (presentUserVisits.length === 0) {
          // New user visit
          newValue.trainingId = storeVisit.training.id;
          newValue.markMaster = 'on';
          await storeVisit.createVisit(newValue);

        } else {
          // User is already present in visit list
          const presentVisit = presentUserVisits[0];
          await storeVisit.updateMaster(presentVisit, 'on');
          presentVisit.comment = newValue.comment;
          await storeVisit.updateComment(presentVisit);
        }

      } else {
        // For existing user just update comment
        await storeVisit.updateComment(newValue);
      }
      editRowObj.value = null;
    }

    async function rowMark(row: EntityCrudVisit) {
      await storeVisit.updateMaster(row, nextMark.get(row.markMaster));
    }

    async function onVisitDateUpdate(value: DateValue) {
      console.log('Date updated: ', value);
      await storeVisit.setDate(value);
    }

    function visitId(visit: EntityCrudVisit): string {
      return String(visit.trainingId) + '_' + String(visit.user.userId);
    }

    return {
      storeVisit, storeUtils, storeUser, storeLogin,
      visitColumns,
      emptyVisit,
      visitDateLabel: computed(() => dateLabel(storeVisit.date)),
      visitId,
      dateFormat,
      onVisitDateUpdate,
      isConfirmDelete: computed({get: () => deleteRowObj.value !== null, set: () => deleteRowObj.value = null}),
      isConfirmAdd: computed({get: () => editRowObj.value !== null, set: () => editRowObj.value = null}),
      isRowAddOrEdit: computed(() => editRowObj.value?.trainingId === -1),
      deleteRowObj, deleteRowStart, deleteRowCommit,
      editRowObj, editRowStart, editRowCommit,
      rowMark,
      date,
    }
  }
});
</script>

<style lang="scss" scoped>
</style>
