<template>
  Settings.
  <div>
    <p>SessionId: {{ storeLogin.sessionId }}</p>
    <p>User: {{ storeLogin.user }}</p>
    <p>Name: {{ storeLogin.user.firstName }}</p>
    <p>Last: {{ storeLogin.user.lastName }}</p>
    <p>Nick: {{ storeLogin.user.nickName }}</p>
    <div v-for="phone in storeLogin.user.phones" :key="phone.phone">
      <p>Phone: {{ phone.phone }} ({{ phone.confirmed }})</p>
    </div>
    <div v-for="email in storeLogin.user.emails" :key="email.email">
      <p>Phone: {{email.email }} ({{ email.confirmed }})</p>
    </div>
  </div>
</template>

<script lang="ts">
import { useStoreLogin } from 'src/store/store_login';
import {defineComponent} from 'vue';

export default defineComponent({
  name: 'Settings',
  setup() {
    const storeLogin = useStoreLogin();
    return {
      storeLogin,
    };
  }
});
</script>

<style scoped>

</style>
