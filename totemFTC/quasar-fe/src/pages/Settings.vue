<template>
  <q-card>
    <q-card-section class="text-h5">Имя</q-card-section>
    <q-separator />
    <q-card-section>
      <p>{{ storeUser.user.firstName }}</p>
      <p>{{ storeUser.user.lastName }}</p>
      <p v-if="storeUser.user.nickName">({{ storeUser.user.nickName }})</p>
    </q-card-section>
  </q-card>
  <q-card>
    <q-card-section class="text-h5">Телефон</q-card-section>
    <q-separator />
    <q-card-section>
      <p v-for="phone in storeUser.user.phones" :key="phone.phone">{{ phone.phone }} ({{ phone.confirmed }})</p>
    </q-card-section>
  </q-card>
  <q-card>
    <q-card-section class="text-h5">E-Mail</q-card-section>
    <q-separator />
    <q-card-section>
      <p v-for="email in storeUser.user.emails" :key="email.email">{{ email.email }} ({{ email.confirmed }})</p>
    </q-card-section>
  </q-card>

  <q-card>
    <q-card-section class="text-h5">Социальные сети</q-card-section>
    <q-separator />
    <q-card-section>
      <div>
        <q-item clickable v-ripple v-for="socialNetwork in uiSocialNetworks" :key="socialNetwork.loginProvider.name" @click="onClickSocialNetwork(socialNetwork)">
          <q-item-section avatar>
            <q-icon :color="socialNetwork.loginProvider.iconColor" :name="socialNetwork.loginProvider.icon"/>
            <q-icon v-if="socialNetwork.loginProvider.loginType === 'warningRegister'" name="fas fa-exclamation-triangle" color="warning" style="position: absolute; left: 10px; top: 10px; font-size: 9px; "/>
            <q-icon v-else-if="socialNetwork.loginProvider.loginType.startsWith('error')" name="fas fa-exclamation-circle" color="negative" style="position: absolute; left: 10px; top: 10px; font-size: 9px; "/>
          </q-item-section>

          <q-item-section>
            <q-item-label>{{socialNetwork.label}}</q-item-label>
            <q-item-label caption>{{socialNetwork.loginProvider.site}}</q-item-label>
          </q-item-section>

          <q-item-section side v-if="socialNetwork.user" @click.stop="disconnectNetworkStart(socialNetwork)">
            <q-icon name="fas fa-times"/>
          </q-item-section>
        </q-item>
      </div>
    </q-card-section>
  </q-card>

  <q-dialog v-model="isDisconnectingNetwork">
    <q-card>
      <q-card-section class="row items-center">
        <q-avatar icon="signal_wifi_off" color="primary" text-color="white" />
        <span class="q-ml-sm">Удалить вход через {{ disconnectingNetwork.name }}?</span>
      </q-card-section>

      <q-card-actions align="right">
        <q-btn flat label="Cancel" color="primary" v-close-popup />
        <q-btn flat label="Delete" color="primary" @click="disconnectNetworkCommit()" />
      </q-card-actions>
    </q-card>
  </q-dialog>


</template>

<script lang="ts">
import {EntityUserSocialNetwork, useStoreCrudUser} from 'src/store/store_crud_user';
import {computed, defineComponent, ref} from 'vue';
import {
  LoginProvider,
  openLoginWindow,
  openPopupWindow,
  loginProviders,
} from 'pages/login/login';
import {useStoreLogin} from 'src/store/store_login';

/**
 * Used to display social network (SN) connections in UI
 * Contains meta info about SN and this user SN connection
 */
interface UiSocialNetwork {
  loginProvider: LoginProvider,
  label: string,
  user?: EntityUserSocialNetwork,
}

export default defineComponent({
  name: 'Settings',
  setup() {
    const storeUser = useStoreCrudUser();
    const storeLogin = useStoreLogin();

    const disconnectingNetwork = ref<UiSocialNetwork | null>(null);

    const uiSocialNetworks = computed<UiSocialNetwork[]>(() => {
      return loginProviders.map(loginProvider => {
        // console.log(`Social network ${s.name}`, 'User netw', storeUser.user.socialNetworks)
        const userNetworkInfo = storeUser.user.socialNetworks.find((u: EntityUserSocialNetwork) => u.networkName === loginProvider.name);
        return {loginProvider: loginProvider, user: userNetworkInfo, label: userNetworkInfo ? 'вход подключен' : 'настроить вход'};
      });
      // return r;
    });

    function disconnectNetworkStart(socialNetwork: UiSocialNetwork) {
      disconnectingNetwork.value = socialNetwork;
    }

    async function disconnectNetworkCommit() {
      const network = disconnectingNetwork.value as UiSocialNetwork;
      await storeUser.deleteSocialNetwork(network.loginProvider.name);
      disconnectingNetwork.value = null;
    }

    // todo add wait indicator
    function onClickSocialNetwork(socialNetwork: UiSocialNetwork) {
      if (socialNetwork.user) {
        // Social network is already linked
        const url = socialNetwork.user.link ? socialNetwork.user.link : `https://${socialNetwork.loginProvider.site}`;
        openPopupWindow(url, 'userLink');
      } else {
        void openLoginWindow(socialNetwork.loginProvider, async callbackParameters => {
          console.log(`Link login provider ${socialNetwork.loginProvider.name} with parameters ${callbackParameters}`);
          await storeLogin.link(socialNetwork.loginProvider, callbackParameters);
        });
      }
    }

    return {
      storeUser,
      uiSocialNetworks,
      disconnectingNetwork,
      isDisconnectingNetwork: computed({get:() => disconnectingNetwork.value !== null, set: () => disconnectingNetwork.value = null}),
      disconnectNetworkStart,
      disconnectNetworkCommit,
      onClickSocialNetwork,
    };
  }
});
</script>

<style scoped>
.icon-svg:before{
  /*content : url("/icons/Instagram_logo_2016.svg");*/
  content : url("/src/assets/Instagram_logo_2016.svg");
}
</style>
