<template>
  Closing window...
  <q-btn icon="times" round @click="window.close()"></q-btn>
</template>

<script lang="ts">
import {LoginUserType} from 'pages/login/login';

export default {
  name: 'LoginOk',
  setup() {
    const urlParams = new URLSearchParams(window.location.search);
    const session_id = urlParams.get('session_id') || '';
    const userType = urlParams.get('user_type') as LoginUserType;

    if (opener) {
      (opener as Window).parent.onLoginCompleted(session_id, userType);
    }

    return {
    }
  }
}
</script>

<style scoped>

</style>
