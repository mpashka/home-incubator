<template>
  <q-table title="Посещения" :rows="store.rows" :columns="columns" :filter="filter" :loading="store.isLoading" row-key="id">
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
        <span class="q-ml-sm">Удалить посещение </span>
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
          <q-input filled v-model="editRowObj.visitDate" label="Дата" hint="Дата посещения">
            <template v-slot:prepend>
              <q-icon name="event" class="cursor-pointer">
                <q-popup-proxy transition-show="scale" transition-hide="scale" ref="datePopup">
                  <q-date v-model="editRowObj.visitDate" mask="YYYY-MM-DD HH:mm" @update:model-value="onDateUpdate">
                    <!--div class="row items-center justify-end">
                      <q-btn v-close-popup label="Close" color="primary" flat />
                    </div-->
                  </q-date>
                </q-popup-proxy>
              </q-icon>
            </template>

            <template v-slot:append>
              <q-icon name="access_time" class="cursor-pointer">
                <q-popup-proxy transition-show="scale" transition-hide="scale">
                  <q-time v-model="editRowObj.visitDate" mask="YYYY-MM-DD HH:mm" format24h>
                    <div class="row items-center justify-end">
                      <q-btn v-close-popup label="Close" color="primary" flat />
                    </div>
                  </q-time>
                </q-popup-proxy>
              </q-icon>
            </template>
          </q-input>
        </div>
        <div class="row">
          <q-input filled v-model="editRowObj.visitComment" label="Примечание"/>
        </div>
        <div class="row">
          <q-select filled v-model="editRowObj.trainer" use-input
                    input-debounce="0" label="Тренер" hint="Тренер"
                    :options="trainers" option-label="trainerName"
                    @filter="trainerFilter"
          >
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
import {EntityCrudTrainer, useStoreCrudTrainer} from 'src/store/store_crud_trainers'
import {EntityCrudVisit, useStoreCrudVisit, emptyVisit} from 'src/store/store_crud_visits';
import {QPopupProxy} from "quasar";

export default {
  name: 'TableVisits',
  setup () {
    let filter = ref('');
    const store = useStoreCrudVisit();
    store.load()
      .then(() => console.log('Loaded successfully'), e => console.log('Load error', e));

    async function deleteRowCommit() {
      console.log('Delete row ', deleteRowObj);
      deleteRowObj.value && await store.delete(deleteRowObj.value.visitId);
      deleteRowObj.value = null;
    }

    async function editRowCommit() {
      console.log('Add row', editRowObj);
      const newValue = editRowObj.value as EntityCrudVisit;
      if (newValue.visitId === -1) {
        await store.create(newValue);
      } else {
        await store.update(newValue);
      }
      editRowObj.value = null;
    }

    const columns = [
      { name: 'date', required: true, label: 'Дата', align: 'left', field: 'visitDate', sortable: true },
      { name: 'trainer', required: true, label: 'Тренер', align: 'left', field: 'trainer', format: (val: EntityCrudTrainer) => val.trainerName, sortable: true },
      { name: 'comment', required: false, label: 'Примечание', align: 'left', field: 'visitComment', sortable: false },
      { name: 'actions', label: 'Actions'}
    ]

    const deleteRowObj :Ref<EntityCrudVisit | null> = ref(null);

    function deleteRowStart(row: EntityCrudVisit) {
      deleteRowObj.value = row;
      console.log('Confirm delete row', row);
    }

    const editRowObj :Ref<EntityCrudVisit | null> = ref(null);

    function editRowStart(row: EntityCrudVisit) {
      console.log('Start edit row', row);
      editRowObj.value = Object.assign({}, row);
    }

    const storeTrainers = useStoreCrudTrainer();
    const trainers = ref(storeTrainers.rows);
    storeTrainers.load()
      .then(() => {
        console.log('Loaded successfully');
        trainers.value = storeTrainers.rows;
      }, e => console.log('Load error', e));

    function trainerFilter(val: string, update: (fn: () => void) => void) {
      if (!val || val.length === 0) {
        update(() => {
          trainers.value = storeTrainers.rows;
        });
      } else {
        const valL = val.toLowerCase();
        update(() => {
          trainers.value = storeTrainers.rows.filter(v => v.trainerName.toLowerCase().indexOf(valL) > -1);
        });
      }
    }

    const datePopup = ref(null);
    function onDateUpdate(value:string, reason:string, details: any) {
      console.log(`Date updated: ${value}`);
      (datePopup.value as unknown as QPopupProxy).hide();
    }

    return {
      columns,
      store,
      filter,
      trainers,
      trainerFilter,
      deleteRowStart,
      deleteRowCommit,
      deleteRowObj,
      editRowStart,
      editRowCommit,
      editRowObj,
      defaultRow: emptyVisit,
      confirmDelete: computed({get: () => deleteRowObj.value !== null, set: () => deleteRowObj.value = null}),
      confirmAdd: computed({get: () => editRowObj.value !== null, set: () => editRowObj.value = null}),
      isRowAddOrEdit: computed(() => editRowObj.value?.visitId === -1),
      datePopup,
      onDateUpdate,
    }
  }
}
</script>

<style scoped>

</style>
