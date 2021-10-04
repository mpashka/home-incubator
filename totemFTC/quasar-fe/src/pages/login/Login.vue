<template>
  <div class="absolute-center">
    <q-img src="~assets/photo.png" style="width: 100%"/>
    <div class="text-h4">Totem FTC</div>
    <div>
      <q-btn round icon="facebook" @click="openLoginWindow($event, 'facebook')"/>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useStoreLogin } from 'src/store/store_login';
import { useRouter } from 'vue-router'

let windowObjectReference: Window | null = null;

function openLoginWindow(event: Event, type: string) {
  event.preventDefault();
  event.stopPropagation();
  try {
    const url = `http://localhost:8080/login/init/${type}`;

    /*
https://stackoverflow.com/questions/3437786/get-the-size-of-the-screen-current-web-page-and-browser-window
window.innerHeight 1174
window.innerWidth 1158
window.screenLeft 0
window.screenTop 14
window.screenX 0
window.screenY 14
window.screen.height 1291
window.screen.width 2296
window.screen.availWidth 2297
window.screen.availHeight 1278
     */
    if (windowObjectReference == null || windowObjectReference.closed) {
      console.log('New window');
      //,resizable,scrollbars,status
      windowObjectReference = window.open(url, 'loginWindow', 'width=600,height=600,left=600,top=200');
      if (!windowObjectReference || windowObjectReference.closed || typeof windowObjectReference.closed=='undefined') {
        console.log('Window popup blocked');
      }

    } else {
      console.log('Old window');
      windowObjectReference.location.href = url;
      windowObjectReference.focus();
    }
  } catch (e) {
    console.log('Error', e);
  }
  return false;
}

type userType = 'new' | 'existing';

declare global {
  interface Window {
    onLoginCompleted(sessionId: string, user: userType): void;
  }
}

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

    window.onLoginCompleted = async function (sessionId: string, user: userType) {
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
