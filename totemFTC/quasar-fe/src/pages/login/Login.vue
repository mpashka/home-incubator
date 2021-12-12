<template>
  <div class="absolute-center">
    <q-card style="width: 406px;">
      <q-card-section>
        <q-img src="~assets/photo.png"/>
      </q-card-section>
      <!--    <div class="text-h4 q-gutter-lg">Totem FTC</div>-->
      <q-card-section class="q-gutter-lg">
        <!--      v-if="socialNetwork.loginScreen" -->
        <q-btn v-for="loginProvider in loginProviders" :key="loginProvider.name" round @click.stop="login(loginProvider)">
          <q-icon :color="loginProvider.iconColor" :name="loginProvider.icon" />
          <q-icon v-if="loginProvider.loginType === 'warningRegister'" name="fas fa-exclamation-triangle" color="warning" style="position: absolute; left: 10px; top: 10px; font-size: 9px; "/>
          <q-icon v-else-if="loginProvider.loginType.startsWith('error')" name="fas fa-exclamation-circle" color="negative" style="position: absolute; left: 10px; top: 10px; font-size: 9px; "/>
        </q-btn>
      </q-card-section>
    </q-card>
  </div>
</template>

<script lang="ts">
import {defineComponent} from 'vue';
import {LoginResult, useStoreLogin} from 'src/store/store_login';
import { useRouter } from 'vue-router';
import {LoginProvider, loginProviders, openLoginWindow} from 'pages/login/login';


export default defineComponent({
  name: 'Login',
  props: {
    inProgress: {
      type: Boolean,
      default: false,
    }
  },
  setup(props) {
    const storeLogin = useStoreLogin();
    const router = useRouter();

    const login = function (loginProvider: LoginProvider) {
      void openLoginWindow(loginProvider, async callbackParameters => {
        console.log(`Call login. Callback: ${callbackParameters}`);
        const loginResult: LoginResult = await storeLogin.login(loginProvider, callbackParameters);

        if (storeLogin.isAuthenticated) {
          if (props.inProgress) {
            router.back();
          } else {
            await router.replace({path: loginResult.userType === 'newUser' ? '/settings' : '/'});
          }
        }
      });
    }

    return {
      login,
      loginProviders,
    }
  }
});
</script>
