<template>
  <div class="absolute-center">
    <q-card style="width: 406px;">
      <q-card-section>
        <q-img src="~assets/photo.png"/>
      </q-card-section>
      <!--    <div class="text-h4 q-gutter-lg">Totem FTC</div>-->
      <q-card-section class="q-gutter-lg">
        <!--      v-if="socialNetwork.loginScreen" -->
        <q-btn v-for="socialNetwork in socialNetworks" :key="socialNetwork.name" round @click.stop="openLoginWindow(socialNetwork.name, 'login')">
          <q-icon :color="socialNetwork.iconColor" :name="socialNetwork.icon" />
        </q-btn>
      </q-card-section>
    </q-card>
  </div>
</template>

<script lang="ts">
import {computed, defineComponent} from 'vue';
import { useStoreLogin } from 'src/store/store_login';
import { useRouter } from 'vue-router';
import {windowObjectReference, openLoginWindow, LoginUserType, socialNetworks} from 'pages/login/login';

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

    window.onLoginCompleted = async function (sessionId: string, user: LoginUserType) {
      console.log(`Call parent from popup. SessionId: ${sessionId}. User type ${user}`);
      windowObjectReference?.close();

      await storeLogin.authenticate(sessionId);

      if (storeLogin.isAuthenticated) {
        if (props.inProgress) {
          router.back();
        } else {
          await router.replace({path: user === 'newUser' ? '/settings' : '/'});
        }
      }
    };

    return {
      windowObjectReference,
      openLoginWindow,
      socialNetworks: computed(() => socialNetworks.filter(s => s.loginScreen)),
    }
  }
});
</script>
