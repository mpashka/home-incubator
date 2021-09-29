<template>
  <q-table title="Тренер" :rows="store.rows" :columns="columns" :filter="filter" :loading="store.isLoading" row-key="id">
    <template v-slot:top-right>
      <q-input borderless dense debounce="300" v-model="filter" placeholder="Search">
        <template v-slot:append>
          <q-icon name="search" />
        </template>
      </q-input>
      <q-btn round icon="plus" @click="editRowStart(defaultRow)"/>
    </template>

    <template v-slot:body-cell-actions="props">
      <q-td :props="props">
        <q-btn round flat size="sm" icon="edit" @click="editRowStart(props.row)"/>
        <q-btn round flat size="sm" icon="delete" @click="deleteRowStart(props.row)"/>
      </q-td>
    </template>
  </q-table>

  <q-dialog v-model="confirmDelete">
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

  <q-dialog v-model="confirmAdd" persistent>
    <q-card class="q-gutter-md" style="width: 60%; max-width: 60%">
      <q-card-section>
        <div class="text-h6">{{ isRowAddOrEdit ? 'Добавить' : 'Редактировать' }}</div>
      </q-card-section>

      <q-card-section>
        <div class="row">
          <q-input filled v-model="editRowObj.trainerName" label="ФИО" hint="Name and surname"
                   lazy-rules
                   :rules="[ val => val && val.length > 0 || 'Please type something']"
          />
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
import {EntityCrudTrainer, useStoreCrudTrainer} from 'src/store/store_crud_trainers'

export default {
  name: 'TableVisits',
  setup () {
    let filter = ref('');
    const store = useStoreCrudTrainer();
    store
      .load()
      .then(() => console.log('Loaded successfully'));

    async function deleteRowCommit() {
      console.log('Delete row ', deleteRowObj);
      deleteRowObj.value && await store.delete(deleteRowObj.value.trainerId);
      deleteRowObj.value = null;
    }

    async function editRowCommit() {
      console.log('Add row', editRowObj.value);
      const newValue = editRowObj.value as EntityCrudTrainer;
      if (newValue.trainerId === -1) {
        await store.create(newValue);
      } else {
        await store.update(newValue);
      }
      editRowObj.value = null;
    }

    const defaultRow: EntityCrudTrainer = {
      trainerId: -1,
      trainerName: '',
    }

    const columns = [
      { name: 'trainerName', required: true, label: 'ФИО', align: 'left', field: 'trainerName', sortable: true },
      { name: 'actions', label: 'Actions'}
    ]

    const deleteRowObj :Ref<EntityCrudTrainer | null> = ref(null);

    function deleteRowStart(row: EntityCrudTrainer) {
      deleteRowObj.value = row;
      console.log(`Confirm delete row ${row}`);
    }

    const editRowObj :Ref<EntityCrudTrainer | null> = ref(null);

    function editRowStart(row: EntityCrudTrainer) {
      console.log('Start edit row', row);
      editRowObj.value = Object.assign({}, row);
    }

    return {
      columns,
      store,
      filter,
      deleteRowStart,
      deleteRowCommit,
      deleteRowObj,
      editRowStart,
      editRowCommit,
      editRowObj,
      defaultRow,
      confirmDelete: computed({get: () => deleteRowObj.value !== null, set: () => deleteRowObj.value = null}),
      confirmAdd: computed({get: () => editRowObj.value !== null, set: () => editRowObj.value = null}),
      isRowAddOrEdit: computed(() => editRowObj.value?.trainerId === -1),
    }
  }
}
</script>

<style scoped>

</style>
