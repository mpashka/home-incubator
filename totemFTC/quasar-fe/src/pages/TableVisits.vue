<template>
  <div class="row">
    <div class="row">
      <q-btn icon="caret-circle-left"/>
      <q-btn class="column">
        <div class="column">
          <q-chip dense size="xs" class="self-end" :label="visitDateLabel()"/>
          <p>{{storeVisit.date}}</p>
        </div>

        <q-popup-proxy transition-show="scale" transition-hide="scale" ref="datePopup">
          <q-date v-model="storeVisit.date" :mask="dateFormat" @update:model-value="onVisitDateUpdate" ref="uiDatePopup">
            <div class="row items-center justify-end">
              <q-btn label="Сегодня" color="primary" flat @click="onVisitDateUpdate(date.formatDate(new Date(), dateFormat))"/>
              <q-btn label="Вчера" color="primary" flat @click="onVisitDateUpdate(date.formatDate(date.subtractFromDate(new Date(), {days: 1}), dateFormat))"/>
            </div>
          </q-date>
        </q-popup-proxy>
      </q-btn>
      <q-btn icon="caret-circle-right"/>
    </div>

    <q-list v-model="storeVisit.training" :options="storeVisit.trainings" @update:model-value="storeVisit.reloadVisits()"
            filled >

      <template v-slot:before-options>
        Тренировки ...
      </template>

      <template v-slot:option="props">
        <div class="row">
          <div class="col-1">
            {{props.opt.time}}
          </div>
          <div class="col-2">
            {{props.opt.trainer.nickName}}
          </div>
          <div class="col-1">
            {{props.opt.trainingType.trainingName}}
          </div>
          <div class="col-1">
            {{props.opt.comment}}
          </div>
        </div>
      </template>

    </q-list>

  </div>

  <q-table :rows="storeVisit.visits" :columns="visitColumns"
           :loading="storeUtils.loading" :row-key="visitId"
    title="Посещения">

    <template v-slot:body-cell-actions="props">
      <q-td :props="props">
        <q-btn round flat size="sm" @click="editRowStart(props.row)">
          <q-icon name="check" :class="props.row.markMaster ? 'text-green' : null"/>
        </q-btn>
        <q-btn round flat size="sm" icon="edit" @click="editRowStart(props.row)"/>
        <q-btn round flat size="sm" icon="delete" @click="deleteRowStart(props.row)"/>
      </q-td>
    </template>

  </q-table>

  <q-dialog v-model="confirmDelete">
    <q-card>
      <q-card-section class="row items-center">
        <q-avatar icon="signal_wifi_off" color="primary" text-color="white" />
        <span class="q-ml-sm">Удалить посещение {{ displayUserName(deleteRowObj.user) }}</span>
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
        <q-select filled v-model="editRowObj.user" use-input
                  input-debounce="0" label="Работяга"
                  :options="users" option-label="displayUserName"
                  @filter="userFilter"
        >
          <template v-slot:no-option>
            <q-item>
              <q-item-section class="text-grey">
                Пожалуйста выберите жертву
              </q-item-section>
            </q-item>
          </template>
        </q-select>
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
import {dateFormat, useStoreCrudTraining} from 'src/store/store_crud_training'
import {EntityCrudVisit, useStoreCrudVisit, emptyVisit} from 'src/store/store_crud_visits';
import {useStoreUtils} from 'src/store/store_utils';
import {date, QPopupProxy} from 'quasar'
import {useStoreCrudUser, EntityUser} from "src/store/store_crud_user";

const visitColumns = [
  { name: 'userFirstName', required: true, label: 'Ф', align: 'left', field: 'user', format: (val: EntityUser) => val.firstName, sortable: true },
  { name: 'userLastName', required: true, label: 'И', align: 'left', field: 'user', format: (val: EntityUser) => val.lastName, sortable: true },
  { name: 'userNickName', required: true, label: 'Имя', align: 'left', field: 'user', format: (val: EntityUser) => val.nickName, sortable: true },
  { name: 'comment', required: false, label: 'Примечание', align: 'left', field: 'comment', sortable: false },
  { name: 'actions', label: 'Actions'},
]

export default {
  name: 'TableVisits',
  async setup () {

    const storeVisit = useStoreCrudVisit();
    const storeTraining = useStoreCrudTraining();
    const storeUser = useStoreCrudUser();
    const storeUtils = useStoreUtils();
    storeTraining.loadTrainers().catch(e => console.log('Load error', e));
    storeTraining.loadTrainingTypes().catch(e => console.log('Load error', e));

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
      console.log('Add row', editRowObj);
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

    function displayUserName(user: EntityUser) {
      let result: string = '';
      const name = user.firstName || user.lastName;

      function addName() {
        if (user.firstName) {
          result += user.firstName;
        }
        if (user.lastName) {
          result += user.lastName;
        }
      }

      if (user.nickName) {
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
    }


    const users = ref(storeUser.rows);
    function userFilter(filter: string, update: (fn: () => void) => void) {
      if (!filter) {
        update(() => users.value = storeUser.rows)
      } else {
        /** Returns true if all filters are present in some user parts */
        function contains(user: EntityUser, filters: string[]): boolean {
          const userStrings = [user.firstName.toLowerCase(), user.lastName.toLowerCase(), user.nickName.toLowerCase()];
          for (let i = 0; i < filters.length; i++) {
            let notPresent = true;
            for (let j = 0; j < userStrings.length; j++) {
              if (userStrings[j].includes(filters[i])) {
                notPresent = false;
                break;
              }
            }
            if (notPresent) {
              return false;
            }
          }
          return true;
        }

        update(() => {
          const filters = filter.toLowerCase().split(/ +/);
          users.value = storeUser.rows.filter(u => contains(u, filters))
        })
      }
    }

    const uiDatePopup = ref(null);
    async function onVisitDateUpdate(value:string) {
      console.log(`Date updated: ${value}`);
      (uiDatePopup.value as unknown as QPopupProxy).hide();
      await storeVisit.setDate(value);
    }

    function visitId(visit: EntityCrudVisit): string {
      return visit.trainingId + '_' + visit.user.userId;
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
      confirmDelete: computed({get: () => deleteRowObj.value !== null, set: () => deleteRowObj.value = null}),
      confirmAdd: computed({get: () => editRowObj.value !== null, set: () => editRowObj.value = null}),
      isRowAddOrEdit: computed(() => editRowObj.value?.trainingId === -1),
      deleteRowObj,
      deleteRowStart,
      deleteRowCommit,
      editRowObj,
      editRowStart,
      editRowCommit,
      displayUserName,
      users,
      userFilter,
    }
  }
}
</script>

<style scoped>

</style>
