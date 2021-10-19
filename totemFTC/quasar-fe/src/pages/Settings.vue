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
      <p v-for="email in storeUser.user.emails" :key="email.email">{{email.email }} ({{ email.confirmed }})</p>
    </q-card-section>
  </q-card>

  <q-card>
    <q-card-section class="text-h5">Социальные сети</q-card-section>
    <q-separator />
    <q-card-section>
      <div>
        <q-item clickable v-ripple v-for="socialNetwork in uiSocialNetworks" :key="socialNetwork.name" @click="onClickSocialNetwork(socialNetwork)">
          <q-item-section avatar>
            <q-icon :color="socialNetwork.iconColor" :name="socialNetwork.icon" />
            <q-icon v-if="socialNetwork.iconExclamation" name="fas fa-exclamation-triangle" color="yellow" style="position: absolute; left: 10px; top: 10px; font-size: 9px; "/>
          </q-item-section>

          <q-item-section>
            <q-item-label>{{socialNetwork.label}}</q-item-label>
            <q-item-label caption>{{socialNetwork.site}}</q-item-label>
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
import {LoginUserType, openLoginWindow, openPopupWindow, windowObjectReference} from 'components/login';

interface SocialNetwork {
  name: string,
  icon: string,
  iconColor?: string,
  iconExclamation?: boolean,
  site: string,
  link: string, // This is used if social network doesn't have user link
}

interface UiSocialNetwork extends SocialNetwork {
  label: string,
  user?: EntityUserSocialNetwork,
}

const socialNetworks: SocialNetwork[] = [
  {
    name: 'facebook',
    site: 'facebook.com',
    icon: 'fab fa-facebook-f',
    iconColor: 'blue-10',
    iconExclamation: true,
    link: 'https://facebook.com'
  },
  {
    name: 'google',
    site: 'google.com',
    icon: 'fab fa-google',
    iconColor: 'red-8',
    iconExclamation: true,
    link: 'https://google.com'
  },
  {
    name: 'apple',
    site: 'apple.com',
    icon: 'fab fa-apple',
    iconColor: 'grey-13',
    iconExclamation: true,
    link: 'https://apple.com'
  },
  {
    name: 'instagram',
    site: 'instagram.com',
    // icon: 'fab fa-instagram',
    // iconColor: 'orange-8',
    icon: `img:${require('src/assets/Instagram_logo_2016.svg')}`,
    // icon: 'svguse:/icons/Instagram_logo_2016.svg',
    iconExclamation: true,
    link: 'https://instagram.com'
  },
  {
    name: 'twitter',
    site: 'twitter.com',
    icon: 'fab fa-twitter',
    iconColor: 'light-blue-7',
    iconExclamation: true,
    link: 'https://twitter.com'
  },
  {
    name: 'github',
    site: 'github.com',
    icon: 'fab fa-github',
    iconColor: 'black',
    link: 'https://github.com'
  },
  {
    name: 'vk',
    site: 'vk.com',
    icon: 'fab fa-vk',
    iconColor: 'blue-2',
    link: 'https://vk.com'
  },
  {
    name: 'mailru',
    site: 'mail.ru',
    icon: 'fas fa-at',
    iconColor: 'deep-orange',
    link: 'https://mail.ru'
  },
  {
    name: 'okru',
    site: 'ok.ru',
    icon: 'fab fa-odnoklassniki',
    iconColor: 'orange',
    link: 'https://ok.ru'
  },
]

export default defineComponent({
  name: 'Settings',
  setup() {
    const storeUser = useStoreCrudUser();

    const disconnectingNetwork = ref<UiSocialNetwork | null>(null);

    window.onLoginCompleted = async function (sessionId: string, user: LoginUserType) {
      console.log(`Call parent from popup. SessionId: ${sessionId}. User type ${user}`);
      windowObjectReference?.close();
      await storeUser.loadUser();
    };

    const uiSocialNetworks = computed<UiSocialNetwork[]>(() => {
      const r:UiSocialNetwork[] = [];
      socialNetworks.forEach(s => {
        // console.log(`Social network ${s.name}`, 'User netw', storeUser.user.socialNetworks)
        const userNetworkInfo = storeUser.user.socialNetworks.find((u: EntityUserSocialNetwork) => u.networkName === s.name);
        if (userNetworkInfo) {
          r.push({...s, user: userNetworkInfo, label: 'вход подключен'});
        } else {
          r.push({...s, label: 'настроить вход'});
        }
      });
      return r;
    });

    function disconnectNetworkStart(socialNetwork: UiSocialNetwork) {
      disconnectingNetwork.value = socialNetwork;
    }

    async function disconnectNetworkCommit() {
      const network = disconnectingNetwork.value as UiSocialNetwork;
      await storeUser.deleteSocialNetwork(network.name);
      disconnectingNetwork.value = null;
    }

    function onClickSocialNetwork(socialNetwork: UiSocialNetwork) {
      if (socialNetwork.user) {
        const url = socialNetwork.user.link ? socialNetwork.user.link : socialNetwork.link;
        openPopupWindow(url);
      } else {
        openLoginWindow(socialNetwork.name, 'link');
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
