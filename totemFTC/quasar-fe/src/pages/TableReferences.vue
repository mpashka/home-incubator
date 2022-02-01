<template>
  <q-page>
    <div class="row justify-end">
      <q-list bordered class="col-6 rounded-borders">
        <q-expansion-item expand-separator icon="list"
                          label="TODO List"
                          caption="Список доработок для страницы Справочные данные"
        >
          <q-item>
            <q-item-section>
              <q-item-label>
                <q-icon name="" />
                ...
              </q-item-label>
              <q-item-label caption>
                ...
              </q-item-label>
            </q-item-section>
          </q-item>

        </q-expansion-item>
      </q-list>
    </div>

    <q-table class="q-ma-lg table-training-types" title="Тренировки" :rows="storeTraining.trainingTypes" :columns="trainingTypeColumns"
             :filter="trainingTypeFilterName" :loading="storeUtils.loading"
             :pagination="{rowsPerPage: 0}" hide-pagination
             row-key="trainingType"
    >
      <template v-slot:top-right>
        <q-input borderless dense debounce="300" v-model="trainingTypeFilterName" placeholder="Search">
          <template v-slot:append>
            <q-icon name="search" />
          </template>
        </q-input>
        <div class="row no-wrap justify-end">
          <q-btn round icon="fas fa-plus" @click="editTrainingTypeStart(defaultTrainingType)"/>
        </div>
      </template>

      <template v-slot:body-cell-actions="props">
        <q-td auto-width>
          <q-btn round flat size="sm" icon="edit" @click.stop="editTrainingTypeStart(props.row)"/>
          <q-btn round flat size="sm" icon="delete" @click.stop="deleteTrainingTypeStart(props.row)"/>
        </q-td>
      </template>
    </q-table>

    <q-table class="q-ma-lg table-ticket-types" title="Абонементы" :rows="storeTicket.ticketTypes" :columns="ticketTypeColumns"
             :filter="ticketTypeFilterName" :loading="storeUtils.loading"
             :pagination="{rowsPerPage: 0}" hide-pagination
             row-key="id"
    >
      <template v-slot:top-right>
        <q-input borderless dense debounce="300" v-model="ticketTypeFilterName" placeholder="Search">
          <template v-slot:append>
            <q-icon name="search" />
          </template>
        </q-input>
        <div class="row no-wrap justify-end">
          <q-btn round icon="fas fa-plus" @click="editTicketTypeStart(defaultTicketType)"/>
        </div>
      </template>

      <template v-slot:body-cell-actions="props">
        <q-td auto-width>
          <q-btn round flat size="sm" icon="edit" @click.stop="editTicketTypeStart(props.row)"/>
          <q-btn round flat size="sm" icon="delete" @click.stop="deleteTicketTypeStart(props.row)"/>
        </q-td>
      </template>
    </q-table>
  </q-page>


  <q-dialog v-model="isTrainingTypeConfirmDelete">
    <q-card>
      <q-card-section class="row items-center">
        <q-avatar icon="delete_forever"/>
        <span class="q-ml-sm">Удалить тренировку {{ deleteTrainingTypeObj.trainingName }}</span>
      </q-card-section>

      <q-card-actions align="right">
        <q-btn flat label="Cancel" color="primary" v-close-popup />
        <q-btn flat label="Delete" color="primary" @click="deleteTrainingTypeCommit()" />
      </q-card-actions>
    </q-card>
  </q-dialog>


  <q-dialog v-model="isTrainingTypeConfirmAdd" persistent>
    <q-card class="q-gutter-md" style="width: 60%; max-width: 60%">
      <q-card-section>
        <div class="text-h6">{{ editTrainingTypeObj.localPropertyEdit === 'add' ? 'Добавить' : 'Редактировать' }} тренировку</div>
      </q-card-section>

      <q-card-section>
        <div class="column q-gutter-lg">
          <q-input filled v-model="editTrainingTypeObj.trainingType" label="Тип"
                   :rules="[ val => val && val.length > 0 || 'Please type something']"
                   v-if="editTrainingTypeObj.localPropertyEdit === 'add'"
          />
          <q-input filled v-model="editTrainingTypeObj.trainingName" label="Название"
                   :rules="[ val => val && val.length > 0 || 'Please type something']"
          />
          <q-input filled v-model="editTrainingTypeObj.defaultCost" label="Стоимость" type="number"
          />
        </div>
      </q-card-section>

      <q-card-actions align="right">
        <q-btn flat label="Ok" color="primary" @click="editTrainingTypeCommit()" />
        <q-btn flat label="Cancel" color="primary" v-close-popup />
      </q-card-actions>
    </q-card>
  </q-dialog>


  <q-dialog v-model="isTicketTypeConfirmDelete">
    <q-card>
      <q-card-section class="row items-center">
        <q-avatar icon="delete_forever"/>
        <span class="q-ml-sm">Удалить абонемент {{ deleteTicketTypeObj.name }}</span>
      </q-card-section>

      <q-card-actions align="right">
        <q-btn flat label="Cancel" color="primary" v-close-popup />
        <q-btn flat label="Delete" color="primary" @click="deleteTicketTypeCommit()" />
      </q-card-actions>
    </q-card>
  </q-dialog>


  <q-dialog v-model="isTicketTypeConfirmAdd" persistent>
    <q-card class="q-gutter-md" style="width: 60%; max-width: 60%">
      <q-card-section>
        <div class="text-h6">{{ editTicketTypeObj.id === -1 ? 'Добавить' : 'Редактировать' }} абонемент</div>
      </q-card-section>

      <q-card-section>
        <div class="column q-gutter-lg">
          <q-input filled v-model="editTicketTypeObj.name" label="Название" />
          <q-input filled v-model="editTicketTypeObj.cost" label="Стоимость" type="number" />
          <q-input filled v-model="editTicketTypeObj.visits" label="Посещения" type="number" />
          <q-input filled v-model="editTicketTypeObj.days" label="Дни" type="number" />
        </div>
      </q-card-section>

      <q-card-section>

        <q-checkbox v-for="trainingType in storeTraining.trainingTypes"
                    v-model="editTicketTypeObj.trainingTypes"
                    :val="trainingType"
                    :label="trainingType.trainingName"
                    :key="'training-type-' + trainingType.trainingType"
        />
      </q-card-section>

      <q-card-actions align="right">
        <q-btn flat label="Ok" color="primary" @click="editTicketTypeCommit()" />
        <q-btn flat label="Cancel" color="primary" v-close-popup />
      </q-card-actions>
    </q-card>
  </q-dialog>

</template>

<script lang="ts">
import {computed, defineComponent, Ref, ref} from 'vue';
import {useStoreUtils} from 'src/store/store_utils';
import {emptyTrainingType, EntityCrudTrainingType, useStoreCrudTraining} from 'src/store/store_crud_training';
import {emptyTicketType, EntityCrudTicketType, useStoreCrudTicket} from 'src/store/store_crud_ticket';

export default defineComponent({
  name: 'TableUsers',
  setup () {

    const storeUtils = useStoreUtils();

    const storeTicket = useStoreCrudTicket();
    storeTicket.loadTicketTypes().catch(e => console.log('Load ticket types error', e));

    const ticketTypeColumns = [
      { name: 'name', required: true, label: 'Название', align: 'left', field: 'name', sortable: true },
      { name: 'trainingTypes', required: true, label: 'Тренировки', align: 'left', field: 'trainingTypes', sortable: true, format: (trainingTypes: EntityCrudTrainingType[]) => trainingTypes.map(t => t.trainingName).join(', ') },
      { name: 'cost', required: true, label: 'Стоимость', align: 'left', field: 'cost', sortable: true },
      { name: 'visits', label: 'Посещений', align: 'left', field: 'visits', sortable: true },
      { name: 'days', label: 'Дней', align: 'left', field: 'days', sortable: true },
      { name: 'actions', label: 'Actions', align: 'right' },
    ];

    const editTicketTypeObj :Ref<EntityCrudTicketType | null> = ref(null);

    function editTicketTypeStart(ticketType: EntityCrudTicketType) {
      console.log('Start edit ticketType', ticketType);
      editTicketTypeObj.value = Object.assign({}, ticketType);
      // Use store objects for training types to implement q-checkbox
      editTicketTypeObj.value.trainingTypes = ticketType.trainingTypes.map(ticket => storeTraining.trainingTypes.find(store => store.trainingType == ticket.trainingType) as EntityCrudTrainingType);
    }

    async function editTicketTypeCommit() {
      console.log('Commit ticketType', editTicketTypeObj.value);
      const newValue = editTicketTypeObj.value as EntityCrudTicketType;
      if (newValue.id === -1) {
        await storeTicket.createTicketType(newValue);
      } else {
        await storeTicket.updateTicketType(newValue);
      }
      editTicketTypeObj.value = null;
    }

    const deleteTicketTypeObj :Ref<EntityCrudTicketType | null> = ref(null);

    function deleteTicketTypeStart(ticketType: EntityCrudTicketType) {
      deleteTicketTypeObj.value = ticketType;
      console.log('Confirm delete ticketType', ticketType);
    }

    async function deleteTicketTypeCommit() {
      console.log('Delete ticketType ', deleteTicketTypeObj);
      deleteTicketTypeObj.value && await storeTicket.deleteTicketType(deleteTicketTypeObj.value);
      deleteTicketTypeObj.value = null;
    }

    //
    //
    //

    const storeTraining = useStoreCrudTraining();
    storeTraining.loadTrainingTypes().catch(e => console.log('Load training types error', e));

    const trainingTypeColumns = [
      { name: 'type', required: true, label: 'Тип', align: 'left', field: 'trainingType', sortable: true },
      { name: 'name', required: true, label: 'Название', align: 'left', field: 'trainingName', sortable: true },
      { name: 'сost', required: true, label: 'Стоимость', align: 'left', field: 'defaultCost', sortable: true },
      { name: 'actions', label: 'Actions', align: 'right' },
    ];

    const editTrainingTypeObj :Ref<EntityCrudTrainingType | null> = ref(null);

    function editTrainingTypeStart(trainingType: EntityCrudTrainingType) {
      console.log('Start edit trainingType', trainingType);
      editTrainingTypeObj.value = Object.assign({localPropertyEdit: trainingType === emptyTrainingType ? 'add' : 'edit'}, trainingType);
    }

    async function editTrainingTypeCommit() {
      console.log('Add trainingType', editTrainingTypeObj.value);
      const trainingType = editTrainingTypeObj.value as EntityCrudTrainingType;
      if (trainingType.localPropertyEdit === 'add') {
        await storeTraining.createTrainingType(trainingType);
      } else {
        await storeTraining.updateTrainingType(trainingType);
      }
      editTrainingTypeObj.value = null;
    }

    const deleteTrainingTypeObj :Ref<EntityCrudTrainingType | null> = ref(null);

    function deleteTrainingTypeStart(trainingType: EntityCrudTrainingType) {
      deleteTrainingTypeObj.value = trainingType;
      console.log('Confirm delete trainingType', trainingType);
    }

    async function deleteTrainingTypeCommit() {
      console.log('Delete trainingType ', deleteTrainingTypeObj);
      deleteTrainingTypeObj.value && await storeTraining.deleteTrainingType(deleteTrainingTypeObj.value);
      deleteTrainingTypeObj.value = null;
    }


    return {
      storeUtils, storeTraining, storeTicket,
      ticketTypeColumns, ticketTypeFilterName: ref(''),
      editTicketTypeObj, editTicketTypeStart, editTicketTypeCommit,
      deleteTicketTypeObj, deleteTicketTypeStart, deleteTicketTypeCommit,
      defaultTicketType: emptyTicketType,
      isTicketTypeConfirmDelete: computed({get: () => deleteTicketTypeObj.value !== null, set: () => deleteTicketTypeObj.value = null}),
      isTicketTypeConfirmAdd: computed({get: () => editTicketTypeObj.value !== null, set: () => editTicketTypeObj.value = null}),

      trainingTypeColumns, trainingTypeFilterName: ref(''),
      editTrainingTypeObj, editTrainingTypeStart, editTrainingTypeCommit,
      deleteTrainingTypeObj, deleteTrainingTypeStart, deleteTrainingTypeCommit,
      defaultTrainingType: emptyTrainingType,
      isTrainingTypeConfirmDelete: computed({get: () => deleteTrainingTypeObj.value !== null, set: () => deleteTrainingTypeObj.value = null}),
      isTrainingTypeConfirmAdd: computed({get: () => editTrainingTypeObj.value !== null, set: () => editTrainingTypeObj.value = null}),
    }
  }
});
</script>

<style lang="sass">
.table-training-types
  height: 356px

.table-ticket-types
  height: 503px

.table-training-types, .table-ticket-types
  .q-table__top,
  .q-table__bottom,
  thead tr:first-child th
    /* bg color is important for th; just specify one */
    background-color: #c1f4cd

  thead tr th
    position: sticky
    z-index: 1
  /* this will be the loading indicator */
  thead tr:last-child th
    /* height of all previous header rows */
    top: 48px
  thead tr:first-child th
    top: 0
</style>
