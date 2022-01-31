<template>
  <div class="row no-wrap items-baseline" style="width: 100%">
    <div class="col-3">
      {{ ticketType.name }}
    </div>
    <div class="col-4 row">
      <q-chip dense v-for="trainingType in ticketType.trainingTypes" :key="trainingType.trainingType">
        {{ trainingType.trainingName }}
      </q-chip>
    </div>
    <div class="col-3" style="text-align: right;">
      посещений {{ ticketType.visits }}
    </div>
    <div class="col-2" style="text-align: right;">
      {{ ticketType.cost }} ₽
    </div>
  </div>
</template>

<script lang="ts">
import {defineComponent, PropType} from 'vue';
import {EntityCrudTicketType} from 'src/store/store_crud_ticket';

export default defineComponent({
  name: "TicketTypeLine",
  props: {
    ticketType: Object as PropType<EntityCrudTicketType>,
  },
  setup() {
    return {
    }
  }
});
</script>

<style scoped lang="scss">
</style>
