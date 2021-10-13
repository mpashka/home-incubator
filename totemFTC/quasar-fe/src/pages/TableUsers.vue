<template>
  <q-table title="Пользователи" :rows="storeUser.filteredRows" :columns="columns" :filter="nameFilter" :loading="storeUtils.loading"
           :pagination="pagination"
           row-key="id">
    <template v-slot:top-right>
      <q-input borderless dense debounce="300" v-model="nameFilter" placeholder="Search">
        <template v-slot:append>
          <q-icon name="search" />
        </template>
      </q-input>
      <q-toggle icon="fas fa-filter" :model-value="storeUser.isFiltered()" @click="filterDialogShow"/>
    </template>

    <template v-slot:body-cell-actions="props">
      <q-td :props="props">
        <q-btn round flat size="sm" icon="edit" @click="editRowStart(props.row)"/>
        <q-btn round flat size="sm" icon="delete" @click="deleteRowStart(props.row)"/>
      </q-td>
    </template>
  </q-table>
  <div class="row no-wrap justify-end">
    <q-btn round icon="fas fa-plus" @click="editRowStart(defaultRow)"/>
  </div>

  <q-dialog v-model="isConfirmDelete">
    <q-card>
      <q-card-section class="row items-center">
        <q-avatar icon="signal_wifi_off" color="primary" text-color="white" />
        <span class="q-ml-sm">Удалить тренера {{ deleteRowObj.trainerName }}</span>
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
        <div class="column">
          <q-input filled v-model="editRowObj.firstName" label="Имя"
                   :rules="[ val => val && val.length > 0 || 'Please type something']"
          />
          <q-input filled v-model="editRowObj.lastName" label="Фамилия"
                   :rules="[ val => val && val.length > 0 || 'Please type something']"
          />
          <q-input filled v-model="editRowObj.nickName" label="Ник"/>
          <q-select v-model="editRowObj.type"
                    :options="userTypes" option-label="label" option-value="type" emit-value
                    :display-value="userTypesMap.get(editRowObj.type)"
          />
          <div class="column" v-if="editRowObj.type === 'trainer' || editRowObj.type === 'admin'">
            <div class="text-h4">Тренер по</div>
            <div v-for="tt in editRowObj.trainingTypes">{{tt}}</div>

            <q-checkbox v-for="training in storeCrudTraining.trainingTypes"
              v-model="editRowObj.trainingTypes" :val="training.trainingType" :label="training.trainingName"
                        :key="'training-type-' + training.trainingType"
            />
          </div>
        </div>
      </q-card-section>

      <q-card-actions align="right">
        <q-btn flat label="Ok" color="primary" @click="editRowCommit()" />
        <q-btn flat label="Cancel" color="primary" v-close-popup />
      </q-card-actions>
    </q-card>
  </q-dialog>

  <q-dialog v-model="isFilterDialogVisible">
    <q-card>
      <q-card-section>
        <div class="text-h4">Показать</div>
      </q-card-section>
      <q-card-section>
        <q-radio v-model="filterType" val="all" label="Все" />
        <q-radio v-model="filterType" val="trainers" label="Тренеры" />
      </q-card-section>
      <q-card-section>
        <q-btn flat label="Ok" color="primary" @click="storeUser.setFilterByType(filterType)" v-close-popup />
        <q-btn flat label="Сбросить" color="primary" @click="storeUser.disableFilter()" v-close-popup />
        <q-btn flat label="Cancel" color="primary" v-close-popup />
      </q-card-section>
    </q-card>
  </q-dialog>
</template>

<script lang="ts">
import { ref, computed, Ref } from 'vue';
import {useStoreCrudUser, EntityUser, EntityUserType, emptyUser, EntityUserFilterTypes} from 'src/store/store_crud_user'
import {useStoreUtils} from "src/store/store_utils";
import {useStoreCrudTraining} from "src/store/store_crud_training";

interface UserType {
  type: EntityUserType,
  label: string,
}

const userTypes: UserType[] = [
  {type: 'guest'   , label: 'Гость'},
  {type: 'user'    , label: 'Посетитель'},
  {type: 'trainer' , label: 'Тренер'},
  {type: 'admin'   , label: 'Администратор'},
]

const userTypesMap = new Map<string, string>();
userTypes.forEach(e => userTypesMap.set(e.type, e.label));

const columns = [
  { name: 'id', required: true, label: 'Id', align: 'left', field: 'userId', sortable: false, headerClasses: 'column-class-id' },
  { name: 'firstName', required: true, label: 'Имя', align: 'left', field: 'firstName', sortable: true },
  { name: 'lastName', required: true, label: 'Фамилия', align: 'left', field: 'lastName', sortable: true },
  { name: 'nickName', required: true, label: 'Ник', align: 'left', field: 'nickName', sortable: true },
  // { name: 'images', required: false, label: 'картинка', align: 'left', field: 'images', sortable: false },
  // { name: 'phones', required: false, label: 'телефон', align: 'left', field: 'phones', sortable: false },
  // { name: 'emails', required: false, label: 'e-mail', align: 'left', field: 'emails', sortable: false },
  // { name: 'emails', required: false, label: 'e-mail', align: 'left', field: 'emails', sortable: false },
  { name: 'type', required: true, label: 'Тип', align: 'left', field: 'type', format: (val: EntityUserType) => `${String(userTypesMap.get(val))}`, sortable: false, },
  { name: 'actions', label: 'Actions'}
]

export default {
  name: 'TableUsers',
  setup () {
    const isFilterDialogVisible = ref(false);
    const storeUtils = useStoreUtils();
    const storeUser = useStoreCrudUser();
    const storeCrudTraining = useStoreCrudTraining();
    const pagination = {sortBy: 'lastName'};
    storeUser.disableFilter();

    storeCrudTraining.loadTrainingTypes().catch(e => console.log('Load error', e));
    storeUser.load().catch(e => console.log('Load error', e));

    const filterType = ref<EntityUserFilterTypes>('all');

    function filterDialogShow() {
      isFilterDialogVisible.value = true;
      filterType.value = storeUser.filterVal.type;
    }

    const editRowObj :Ref<EntityUser | null> = ref(null);

    function editRowStart(row: EntityUser) {
      console.log('Start edit row', row);
      editRowObj.value = Object.assign({}, row);
      if (!editRowObj.value.trainingTypes) {
        editRowObj.value.trainingTypes = [];
      }
    }

    async function editRowCommit() {
      console.log('Add row', editRowObj.value);
      const newValue = editRowObj.value as EntityUser;
      if (newValue.userId === -1) {
        await storeUser.create(newValue);
      } else {
        await storeUser.update(newValue);
      }
      editRowObj.value = null;
    }

    const deleteRowObj :Ref<EntityUser | null> = ref(null);

    function deleteRowStart(row: EntityUser) {
      deleteRowObj.value = row;
      console.log('Confirm delete row', row);
    }

    async function deleteRowCommit() {
      console.log('Delete row ', deleteRowObj);
      deleteRowObj.value && await storeUser.delete(deleteRowObj.value);
      deleteRowObj.value = null;
    }

    return {
      columns,
      pagination,
      storeUser,
      storeUtils,
      storeCrudTraining,
      nameFilter: ref(''),
      editRowObj, editRowStart, editRowCommit,
      deleteRowObj, deleteRowStart, deleteRowCommit,
      defaultRow: emptyUser,
      isConfirmDelete: computed({get: () => deleteRowObj.value !== null, set: () => deleteRowObj.value = null}),
      isConfirmAdd: computed({get: () => editRowObj.value !== null, set: () => editRowObj.value = null}),
      isRowAddOrEdit: computed(() => editRowObj.value?.userId === -1),
      userTypes,
      userTypesMap,
      isFilterDialogVisible, filterDialogShow, filterType,
    }
  }
}
</script>

<style scoped lang="scss">
column-class-id {
  width: 5em;
}
</style>
