<template>
  <q-page>
    <div class="row justify-end">
    <q-list class="col-8">
      <q-item>
        <q-item-label header>
          TODO List
        </q-item-label>
      </q-item>

      <q-item>
        <q-item-section>
          <q-item-label>
            <q-icon name="merge_type" />
            Слияние пользователей
          </q-item-label>
          <q-item-label caption>
            Если мы заведем пользователей вручную, но не укажем их e-mail,
            или соц сеть не предоставит e-mail пользователя, или e-mail не совпадет
            с существующим,
            то при первом логине аккаунт не будет связан с существующим. Вместо этого будет создан
            новый аккаунт. Для таких случаев надо добавить способ объединения аккаунтов.
          </q-item-label>
        </q-item-section>
      </q-item>

      <q-item>
        <q-item-section>
          <q-item-label>
            <q-icon name="person" />
            Страница пользователя
          </q-item-label>
          <q-item-label caption>
            При клике на пользователя надо показывать его страницу где будут отображаться
            все его абонементы и посещения и можно будет редактировать ФИО, картинку и т.д.
          </q-item-label>
        </q-item-section>
      </q-item>
    </q-list>
    </div>

  <q-table title="Пользователи" :rows="storeUser.filteredRows" :columns="columns" :filter="filterName" :loading="storeUtils.loading"
           :pagination="pagination"
           row-key="id">
    <template v-slot:top-right>
      <q-input borderless dense debounce="300" v-model="filterName" placeholder="Search">
        <template v-slot:append>
          <q-icon name="search" />
        </template>
      </q-input>
      <q-toggle icon="fas fa-filter" :model-value="storeUser.isFiltered()" @click="filterDialogShow"/>
      <div class="row no-wrap justify-end">
        <q-btn round icon="fas fa-plus" @click="editRowStart(defaultRow)"/>
      </div>
    </template>

    <template v-slot:body-cell-types="props">
      <q-td>
        <q-icon name="fas fa-question" size="sm" v-if="storeUser.isGuest(props.row)" />
        <q-icon name="person" size="sm" v-if="storeUser.isUser(props.row)" />
        <q-icon name="sports" size="sm" v-if="storeUser.isTrainer(props.row)" />
        <q-icon name="manage_accounts" size="sm" v-if="storeUser.isAdmin(props.row)" />
      </q-td>
    </template>

    <template v-slot:body-cell-actions="props">
      <q-td>
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
        <span class="q-ml-sm">Удалить пользователя {{ deleteRowObj.trainerName }}</span>
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
        </div>
      </q-card-section>

      <q-card-section>
        <q-checkbox v-for="userType in userTypes"
                    v-model="editRowObj.types"
                    :val="userType.type"
                    :label="userType.label"
                    :key="'user-type-' + userType.type"
        />
      </q-card-section>

      <q-card-section v-if="editRowObj.types.includes('trainer')">
        <div class="text-h5">Тренер</div>
        <q-checkbox v-for="training in storeCrudTraining.trainingTypes"
                    v-model="editRowObj.trainingTypes"
                    :val="training.trainingType"
                    :label="training.trainingName"
                    :key="'training-type-' + training.trainingType"
        />
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
        <q-list>
          <q-item><q-radio v-model="filterType" val="all" label="Все" /></q-item>
          <q-item><q-radio v-model="filterType" val="guest" label="Гости" /></q-item>
          <q-item><q-radio v-model="filterType" val="user" label="Посетители" /></q-item>
          <q-item><q-radio v-model="filterType" val="trainer" label="Тренеры" /></q-item>
          <q-item><q-radio v-model="filterType" val="admin" label="Администраторы" /></q-item>
        </q-list>
      </q-card-section>
      <q-card-section>
        <q-btn flat label="Ok" color="primary" @click="filterDialogCommit()"/>
        <q-btn flat label="Сбросить" color="primary" @click="storeUser.disableFilter()" v-close-popup />
        <q-btn flat label="Cancel" color="primary" v-close-popup/>
      </q-card-section>
    </q-card>
  </q-dialog>
</template>

<script lang="ts">
import { ref, computed, Ref } from 'vue';
import {useStoreCrudUser, EntityUser, EntityUserType, emptyUser, EntityUserTypeFilter} from 'src/store/store_crud_user';
import {useStoreUtils} from "src/store/store_utils";
import {useStoreCrudTraining} from "src/store/store_crud_training";

interface UserType {
  type: EntityUserType,
  label: string,
}

const userTypes: UserType[] = [
  {type: 'user'    , label: 'Посетитель'},
  {type: 'trainer' , label: 'Тренер'},
  {type: 'admin'   , label: 'Администратор'},
]

const columns = [
  { name: 'id', required: true, label: 'Id', align: 'left', field: 'userId', sortable: false, headerClasses: 'column-class-id' },
  { name: 'firstName', required: true, label: 'Имя', align: 'left', field: 'firstName', sortable: true },
  { name: 'lastName', required: true, label: 'Фамилия', align: 'left', field: 'lastName', sortable: true },
  { name: 'nickName', required: true, label: 'Ник', align: 'left', field: 'nickName', sortable: true },
  // { name: 'images', required: false, label: 'картинка', align: 'left', field: 'images', sortable: false },
  // { name: 'phones', required: false, label: 'телефон', align: 'left', field: 'phones', sortable: false },
  // { name: 'emails', required: false, label: 'e-mail', align: 'left', field: 'emails', sortable: false },
  // { name: 'emails', required: false, label: 'e-mail', align: 'left', field: 'emails', sortable: false },
  { name: 'types', label: 'Тип', align: 'left', field: 'types', sortable: false },
  { name: 'actions', label: 'Actions', align: 'left' },
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
    storeUser.load().catch(e => console.log('Load users error', e));
    storeUser.loadTrainers().catch(e => console.log('Load trainers error', e));

    const filterName = ref<string>('');
    const filterType = ref<EntityUserTypeFilter>('all');

    function filterDialogShow() {
      isFilterDialogVisible.value = true;
      filterType.value = storeUser.filterVal.type ? storeUser.filterVal.type : 'all';
    }

    function filterDialogCommit() {
      isFilterDialogVisible.value = false;
      storeUser.setFilterByType(filterType.value);
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
      editRowObj, editRowStart, editRowCommit,
      deleteRowObj, deleteRowStart, deleteRowCommit,
      defaultRow: emptyUser,
      isConfirmDelete: computed({get: () => deleteRowObj.value !== null, set: () => deleteRowObj.value = null}),
      isConfirmAdd: computed({get: () => editRowObj.value !== null, set: () => editRowObj.value = null}),
      isRowAddOrEdit: computed(() => editRowObj.value?.userId === -1),
      userTypes,
      isFilterDialogVisible, filterDialogShow, filterDialogCommit, filterType, filterName,
    }
  }
}
</script>

<style scoped lang="scss">
column-class-id {
  width: 5em;
}
</style>
