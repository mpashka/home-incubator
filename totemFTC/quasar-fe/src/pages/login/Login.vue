<template>
  <div class="absolute-center">
    <q-img src="~assets/photo.png" style="width: 100%"/>
    <div class="text-h4">Totem FTC</div>
    <div>
      <q-btn round icon="facebook" @click.stop="openLoginWindow('facebook', 'login')"/>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useStoreLogin } from 'src/store/store_login';
import { useRouter } from 'vue-router';
import {windowObjectReference, openLoginWindow, LoginUserType} from 'src/components/login';

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
          await router.replace({path: user === 'new' ? '/settings' : '/'});
        }
      }
    };

    return {
      windowObjectReference,
      openLoginWindow,
    }
  }
});
</script>
