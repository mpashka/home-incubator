<template>
  <div class="row q-gutter-x-sm" >
    <div class="col-3" style="min-width: 3em;">
      {{date.formatDate(training.time, 'HH:mm')}}
    </div>
    <div class="col-3" style="min-width: 3em;">
      {{storeUser.trainerNameString(training.trainer)}}
    </div>
    <div class="col-4" style="min-width: 3em;">
      {{training.trainingType.trainingName}}
    </div>
    <div class="col-2" style="min-width: 3em;">
      {{training.comment}}
    </div>
  </div>
</template>

<script lang="ts">
import {EntityCrudTraining} from "src/store/store_crud_training";
import {defineComponent, PropType} from "vue";
import {date} from "quasar";
import {useStoreCrudUser} from 'src/store/store_crud_user';

export default defineComponent({
  name: "TrainingLine",
  props: {
    training: Object as PropType<EntityCrudTraining>,
  },
  setup() {
    const storeUser = useStoreCrudUser();
    return {
      storeUser,
      date,
    }
  }
});
</script>

<style scoped lang="scss">

</style>
