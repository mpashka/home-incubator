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
import { defineComponent, ref } from 'vue';
import { useStore } from 'src/store';

let windowObjectReference: Window | null = null;

function openLoginWindow(event: Event, type: string) {
  event.preventDefault();
  event.stopPropagation();
  try {
    const url = `http://localhost:8080/login/init/${type}`;

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

declare global {
  interface Window {
    onLoginCompleted(sessionId: string): void;
  }
}


export default {
  name: 'PopupTest',
  props: {
    param1: {
      type: String,
      required: false
    }
  },
  setup() {
    const store = useStore();

    window.onLoginCompleted = async function (sessionId: string) {
      console.log(`Call parent from popup. SessionId: ${sessionId}`);
      windowObjectReference?.close();

      localStorage.setItem('session_id', sessionId);
      store.commit('login/sessionId', sessionId);

      // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access

/*
      const userLogin = (
        await api.get('/login/userInfo')
      ).data as UserLogin;
      store.commit('login/authorize', userLogin);
*/
    };

    return {
      windowObjectReference,
      openLoginWindow,
    }
  }
}

</script>
