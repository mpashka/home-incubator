<template>
  <q-page>
    <div class="row justify-end">
    <q-list bordered class="col-6 rounded-borders">
      <q-expansion-item expand-separator icon="list"
                        label="TODO List"
                        caption="Список доработок для страницы Пользователь"
      >
      <q-item>
        <q-item-section>
          <q-item-label>
            <q-icon name="" />
            Редактирование абонемента
          </q-item-label>
          <q-item-label caption>
            Стоит добавить редактирование типа абонемента для пользователя. Сделать возможным только если доступное
            число посещений нового абонемента не меньше, чем число посещений по старому и тренировки соответствуют
            посещениям.
          </q-item-label>
        </q-item-section>
      </q-item>

      <q-item>
        <q-item-section>
          <q-item-label>
            <q-icon name="" />
            Переиспользование компонента имя посещения
          </q-item-label>
          <q-item-label caption>
            Добавить компонент, отображающий посещение. Параметрами указывать что отображать -
            например дату, посетителя, тренера, тренировку
          </q-item-label>
        </q-item-section>
      </q-item>
      </q-expansion-item>
    </q-list>
    </div>

    <q-card flat>
      <q-card-section>
        <div class="row no-wrap">
          <div class="col">
            <div class="text-h4">{{ storeUser.userNameString(storeUser.user) }}</div>
            <div class="text-subtitle2">

            </div>
          </div>
        </div>
      </q-card-section>
    </q-card>

    <q-card class="q-ma-sm">
      <q-card-section>
        <div class="row">
          <div class="col text-h6">Абонементы</div>
          <div class="col-auto">
            <q-btn round icon="add" @click="editTicketStart(emptyTicket)" />
          </div>
        </div>
      </q-card-section>
      <q-separator inset />

      <q-card-section v-if="storeTicket.userTickets.length === 0">
        У пользователя нет действующих абонементов
      </q-card-section>

      <q-card-section v-if="storeTicket.userTickets.length > 0">
        <q-table :rows="storeTicket.userTickets" :columns="ticketColumns" :row-key="row => row.id"
                 selection="single" v-model:selected="selectedTicket"
                 @selection="onTicketSelectionChanges"
        >

          <template v-slot:body-cell-actions="props">
            <q-td :props="props" v-if="storeUser.isAdmin(storeLogin.user)">
              <!--q-btn round flat size="sm" icon="edit" @click.stop="editTicketStart(props.row)"/-->
              <q-btn round flat size="sm" icon="delete" @click.stop="deleteTicketStart(props.row)"/>
            </q-td>
          </template>

        </q-table>
      </q-card-section>
    </q-card>

    <q-card class="q-ma-sm">
      <q-card-section>
        <div class="text-h6">
          <span v-if="storeVisit.userSelectedTicket === null">
            Все посещения с {{date.formatDate(storeVisit.userVisitsInterval.from, 'D MMMM', formatGenitiveCase)}}
          </span>
          <span v-else>
            Посещения по абонементу {{storeVisit.userSelectedTicket.ticketType.name}} от
            {{date.formatDate(storeVisit.userSelectedTicket.buy, 'D MMMM', formatGenitiveCase)}}
          </span>
        </div>
      </q-card-section>

      <q-separator inset />

      <q-card-section v-if="storeVisit.visits.length === 0">
        Нет посещений
      </q-card-section>

      <q-card-section v-if="storeVisit.visits.length > 0">
        <q-table :rows="storeVisit.visits" :columns="visitColumns"
                 :loading="storeUtils.loading"
                 :row-key="row => row.trainingId"
        >
        <!-- :row-key="row => row.trainingId + '_' + row.user.userId" -->

          <template v-slot:body-cell-actions="props">
            <q-td :props="props" v-if="storeUser.isAdmin(storeLogin.user)">
              <q-btn round flat size="sm" @click="visitMark(props.row)">
                <q-icon name="schedule" size="sm" v-if="props.row.markSchedule"/>
                <q-icon name="mdi-account-check" size="sm" v-if="props.row.markSelf === 'on'"/>
                <q-icon name="mdi-account-cancel" size="sm" v-if="props.row.markSelf === 'off'"/>
                <q-icon name="mdi-shield-check" class="text-green" size="sm" v-if="props.row.markMaster === 'on'"/>
                <q-icon name="mdi-shield-remove-outline" class="text-red" size="sm" v-if="props.row.markMaster === 'off'"/>
                <q-icon name="mdi-help-rhombus-outline" size="sm" v-if="props.row.markMaster === 'unmark'"/>
              </q-btn>
              <q-btn round flat size="sm" icon="edit" @click="editVisitStart(props.row)"/>
              <q-btn round flat size="sm" icon="delete" @click="deleteVisitStart(props.row)"/>
            </q-td>
          </template>

        </q-table>
      </q-card-section>
    </q-card>

  </q-page>


  <q-dialog v-model="isConfirmTicketDelete">
    <q-card>
      <q-card-section class="row items-center">
        <q-avatar icon="delete_forever"/>
        <span class="q-ml-sm">Удалить абонемент '{{deleteTicketObj.ticketType.name}}' на
          {{ deleteTicketObj.ticketType.visits }} посещений
          от {{ date.formatDate(deleteTicketObj.buy, 'D MMMM', formatGenitiveCase) }}?
        </span>
        <div v-if="deleteTicketObj.visited > 0">
          <q-card rounded>
            <q-card-section>
              <q-icon name="warning" class="text-red" size="sm"/>
              {{ storeUser.userNameString(storeUser.user) }} уже посетил {{ deleteTicketObj.visited }} занятий по абонементу
            </q-card-section>
          </q-card>
        </div>
      </q-card-section>

      <q-card-actions align="right">
        <q-btn flat label="Cancel" color="primary" v-close-popup />
        <q-btn flat label="Delete" color="primary" @click="deleteTicketCommit()" />
      </q-card-actions>
    </q-card>
  </q-dialog>


  <q-dialog v-model="isConfirmTicketAdd" persistent>
    <q-card class="q-gutter-md" style="width: 60%; max-width: 60%">

      <q-card-section>
        <div class="row q-gutter-md">
          <div class="text-h6 col-3 self-center">Добавить абонемент</div>
          <q-select class="col" filled v-model="editTicketObj.ticketType"
                    :options="storeTicket.ticketTypes" option-label="name">

            <template v-slot:selected-item="props">
              <ticket-type-line :ticket-type="props.opt" v-if="props.opt.id !== -1"/>
              <q-item-section class="text-grey" v-else>
                Пожалуйста выберите
              </q-item-section>
            </template>

            <template v-slot:option="props">
              <q-item @click="props.toggleOption(props.opt)" clickable :active="props.selected">
                <q-item-section>
                  <ticket-type-line :ticket-type="props.opt"/>
                </q-item-section>
              </q-item>
            </template>

          </q-select>
        </div>
      </q-card-section>

      <q-card-actions align="right">
        <q-btn flat label="Ok" color="primary" @click="editTicketCommit()" />
        <q-btn flat label="Cancel" color="primary" v-close-popup />
      </q-card-actions>
    </q-card>
  </q-dialog>


  <q-dialog v-model="isConfirmVisitDelete">
    <q-card>
      <q-card-section class="row items-center">
        <q-avatar icon="signal_wifi_off" color="primary" text-color="white" />
        <span class="q-ml-sm">
          Удалить посещение {{ deleteVisitObj.training.trainingType.trainingName }}
          c тренером {{ storeUser.trainerNameString(deleteVisitObj.training.trainer) }}
          {{ date.formatDate(deleteVisitObj.training.time, 'HH:mm от D MMMM', formatGenitiveCase) }}
        </span>
      </q-card-section>

      <q-card-actions align="right">
        <q-btn flat label="Cancel" color="primary" v-close-popup />
        <q-btn flat label="Delete" color="primary" @click="deleteVisitCommit()" />
      </q-card-actions>
    </q-card>
  </q-dialog>

  <q-dialog v-model="isConfirmVisitAdd" persistent>
    <q-card class="q-gutter-md" style="width: 60%; max-width: 60%">
      <q-card-section>
        <div class="text-h6">
          {{ isVisitAddOrEdit ? 'Добавить' : 'Редактировать' }}
          посещение {{ editVisitObj.training.trainingType.trainingName }}
          c тренером {{ storeUser.trainerNameString(editVisitObj.training.trainer) }}
          {{ date.formatDate(editVisitObj.training.time, 'HH:mm от D MMMM', formatGenitiveCase) }}
        </div>
      </q-card-section>

      <q-card-section>
        <q-input v-model="editVisitObj.comment" label="Комментарий"/>
      </q-card-section>

      <q-card-actions align="right">
        <q-btn flat label="Ok" color="primary" @click="editVisitCommit()" />
        <q-btn flat label="Cancel" color="primary" v-close-popup />
      </q-card-actions>
    </q-card>
  </q-dialog>


</template>

<script lang="ts">
import {computed, defineComponent, ref, Ref} from 'vue';
import {useStoreCrudUser} from 'src/store/store_crud_user';
import {emptyTicket, EntityCrudTicket, EntityCrudTicketType} from 'src/store/store_crud_ticket';
import {dateFormat, dateTimeFormat, formatGenitiveCase, useStoreUtils, weekStart} from 'src/store/store_utils';
import {useRoute} from 'vue-router';
import {date} from 'quasar';
import TicketTypeLine from 'pages/components/TicketTypeLine.vue';
import {useStoreLogin} from 'src/store/store_login';
import {EntityCrudVisit, nextMark, useStoreCrudVisit} from 'src/store/store_crud_visit';
import {EntityCrudTraining} from 'src/store/store_crud_training';
import {useStoreCrudTicket} from 'src/store/store_crud_ticket';

export default defineComponent({
  name: 'TableUser',
  components: {TicketTypeLine},
  setup () { //props, context
    const ticketColumns = [
      // { name: 'id', required: true, label: 'Id', align: 'left', field: 'id', visible: false },
      { name: 'buy', required: true, label: 'Куплен', align: 'left', field: 'buy', format: (val: string) => date.formatDate(val, dateFormat), sortable: true },
      { name: 'name', required: true, label: 'Абонемент', align: 'left', field: 'ticketType', format: (val: EntityCrudTicketType) => val.name, sortable: true },
      { name: 'visits', required: true, label: 'Доступно', align: 'left', field: 'ticketType', format: (val: EntityCrudTicketType) => val.visits, sortable: true },
      { name: 'visited', required: true, label: 'Сходил', align: 'left', field: 'visited', sortable: true },
      { name: 'actions', label: 'Actions'},
    ];

    const visitColumns = [
      { name: 'date', required: true, label: 'Дата', align: 'left', field: 'training', sortable: true, format: (val: EntityCrudTraining) => date.formatDate(val.time, 'HH:mm, dddd, D MMMM') },
      { name: 'trainingName', required: true, label: 'Тренировка', align: 'left', field: 'training', sortable: true, format: (val: EntityCrudTraining) => val.trainingType.trainingName },
      { name: 'trainerName', required: true, label: 'Тренер', align: 'left', field: 'training', sortable: true, format: (val: EntityCrudTraining) => storeUser.trainerNameString(val.trainer) },
      { name: 'comment', required: false, label: 'Примечание', align: 'left', field: 'comment', sortable: false },
      { name: 'actions', label: 'Actions'},
    ];

    const route = useRoute();
    const storeUtils = useStoreUtils();
    const storeLogin = useStoreLogin();
    const storeUser = useStoreCrudUser();
    const storeTicket = useStoreCrudTicket();
    const storeVisit = useStoreCrudVisit();

    storeUser.loadUserById(Number(route.params.userId)).catch(e => console.log('Error loading user', e));
    storeTicket.loadUserTickets().catch(e => console.log('Error loading user tickets', e));
    storeTicket.loadTicketTypes().catch(e => console.log('Error loading user ticket types', e));
    const now = date.startOfDate(Date.now(), 'day');
    const start = date.subtractFromDate(weekStart(now), {days: 7*2});
    const end = date.addToDate(now, {days: 1});
    storeVisit.userVisitsInterval = {from: start, to: end};
    storeVisit.loadUserVisits().catch(e => console.log('Error loading user ticket types', e));

    //
    // Ticket
    //
    const selectedTicket: Ref<EntityCrudTicket[]> = ref([]);

    async function onTicketSelectionChanges(details: {rows: EntityCrudTicket[], keys: [], added: boolean, evt: Event}) {
      storeVisit.userSelectedTicket = details.added ? details.rows[0] : null;
      await storeVisit.loadUserVisits();
      // console.log('Selection details', details, 'Model', selectedTicket);
    }

    const deleteTicketObj: Ref<EntityCrudTicket | null> = ref(null);

    function deleteTicketStart(row: EntityCrudTicket) {
      deleteTicketObj.value = row;
      console.log('Confirm delete row', row);
    }

    async function deleteTicketCommit() {
      console.log('Delete row ', deleteTicketObj.value);
      deleteTicketObj.value && await storeTicket.deleteTicket(deleteTicketObj.value.id);
      deleteTicketObj.value = null;
    }


    const editTicketObj: Ref<EntityCrudTicket | null> = ref(null);

    function editTicketStart(ticket: EntityCrudTicket) {
      console.log('Start edit ticket', ticket);
      editTicketObj.value = Object.assign({}, ticket);
    }

    async function editTicketCommit() {
      console.log('Add row', editTicketObj.value);
      const ticket = editTicketObj.value as EntityCrudTicket;
      ticket.user = storeUser.user;
      ticket.buy = date.formatDate(Date.now(), dateTimeFormat);
      ticket.visited = 0;
      await storeTicket.createTicket(ticket);
      editTicketObj.value = null;
    }


    //
    // Visit
    //

    const deleteVisitObj :Ref<EntityCrudVisit | null> = ref(null);

    function deleteVisitStart(row: EntityCrudVisit) {
      deleteVisitObj.value = row;
      console.log('Confirm delete row', row);
    }

    async function deleteVisitCommit() {
      console.log('Delete row ', deleteVisitObj);
      deleteVisitObj.value && await storeVisit.deleteVisit(deleteVisitObj.value);
      deleteVisitObj.value = null;
    }


    const editVisitObj :Ref<EntityCrudVisit | null> = ref(null);

    function editVisitStart(row: EntityCrudVisit) {
      console.log('Start edit row', row);
      storeUser.setFilterByType('user');
      storeUser.setFilterExclude(storeVisit.visits.map(v => v.user.userId));
      editVisitObj.value = Object.assign({}, row);
    }

    async function editVisitCommit() {
      console.log('Add row', editVisitObj, 'Selected training id', storeVisit.training.id);
      const newValue = editVisitObj.value as EntityCrudVisit;
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
      editVisitObj.value = null;
    }

    async function visitMark(row: EntityCrudVisit) {
      row.user = storeUser.user;
      await storeVisit.updateMaster(row, nextMark.get(row.markMaster));
    }

    return {
      storeUser, storeVisit, storeTicket, storeUtils, storeLogin,
      ticketColumns, selectedTicket, onTicketSelectionChanges,
      isConfirmTicketDelete: computed({get: () => deleteTicketObj.value !== null, set: () => deleteTicketObj.value = null}),
      deleteTicketObj, deleteTicketStart, deleteTicketCommit,
      isConfirmTicketAdd: computed({get: () => editTicketObj.value !== null, set: () => editTicketObj.value = null}),
      editTicketObj, editTicketStart, editTicketCommit, emptyTicket,
      visitColumns,
      deleteVisitObj, deleteVisitStart, deleteVisitCommit,
      editVisitObj, editVisitStart, editVisitCommit, visitMark,
      isConfirmVisitDelete: computed({get: () => deleteVisitObj.value !== null, set: () => deleteVisitObj.value = null}),
      isConfirmVisitAdd: computed({get: () => editVisitObj.value !== null, set: () => editVisitObj.value = null}),
      isVisitAddOrEdit: computed(() => editVisitObj.value?.trainingId === -1),
      date, formatGenitiveCase,
    }
  }
});
</script>

<style scoped lang="scss">
</style>
