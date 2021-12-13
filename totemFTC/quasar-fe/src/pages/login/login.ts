import { Dialog } from 'quasar';
import {randomString} from 'components/utils';

const windowObjectReferences: {[name: string]: WindowProxy | null} = {};

export function openPopupWindow(url: string, name: string) {
  try {
    const windowObjectReference = windowObjectReferences[name];
    /*
todo locate and resize
      var left = Math.max(0, (screen.width - width) / 2) + (screen.availLeft | 0),
          top = Math.max(0, (screen.height - height) / 2) + (screen.availTop | 0);


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
      windowObjectReferences[name] = window.open(url, 'loginWindow', 'width=600,height=600,left=600,top=200');
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
}

export type LoginType = 'normal' | 'warningRegister' | 'errorApple' | 'errorTwitter';

export interface LoginProvider {
  name: string,
  site: string,
  icon: string,
  iconColor?: string,
  authorizationEndpoint: string,
  clientId: string,
  clientIdProd?: string,
  loginType: LoginType,
}

// import instagram_icon from 'src/assets/Instagram_logo_2016.svg'
/* eslint @typescript-eslint/no-var-requires: "off" */
const instagram_icon=require('src/assets/Instagram_logo_2016.svg') as string;

export const loginProviders: LoginProvider[] = [
  {
    name: 'facebook',
    site: 'facebook.com',
    icon: 'fab fa-facebook-f',
    iconColor: 'blue-10',
    authorizationEndpoint: 'https://www.facebook.com/dialog/oauth?scope=openid+email+public_profile+user_gender+user_link+user_birthday+user_location',
    clientId: '558989508631164',
    //'220460853218319' - prod app,
    loginType: 'warningRegister',
  },
  {
    name: 'google',
    site: 'google.com',
    icon: 'fab fa-google',
    iconColor: 'red-8',
    authorizationEndpoint: 'https://accounts.google.com/o/oauth2/v2/auth?prompt=consent+select_account&scope=openid+email+profile',
    clientId: '393082269793-6em2i881catbcbfgij4m96a7br1n4sse.apps.googleusercontent.com',
    loginType: 'warningRegister',
  },
  {
    name: 'apple',
    site: 'apple.com',
    icon: 'fab fa-apple',
    iconColor: 'grey-13',
    authorizationEndpoint: '',
    clientId: '',
    loginType: 'errorApple',
  },
  {
    name: 'instagram',
    site: 'instagram.com',
    // icon: 'fab fa-instagram',
    // iconColor: 'orange-8',
    icon: `img:${instagram_icon}`,
    // icon: `img:${String(require('src/assets/Instagram_logo_2016.svg'))}`,
    // icon: 'img:./assets/Instagram_logo_2016.svg',
    authorizationEndpoint: 'https://api.instagram.com/oauth/authorize?scope=openid,user_profile',
    clientId: '534785450961419',
    // '2076313149192518' - prod app,
    loginType: 'warningRegister',
  },
  {
    name: 'twitter',
    site: 'twitter.com',
    icon: 'fab fa-twitter',
    iconColor: 'light-blue-7',
    authorizationEndpoint: '',
    clientId: '',
    loginType: 'errorTwitter',
  },
  {
    name: 'github',
    site: 'github.com',
    icon: 'fab fa-github',
    iconColor: 'black',
    authorizationEndpoint: 'https://github.com/login/oauth/authorize?scope=read:user+user:email',
    clientId: '1010b07145f48c457922',
    clientIdProd: '146aaca397f8fc44399e',
    loginType: 'normal',
  },
  {
    name: 'vk',
    site: 'vk.com',
    icon: 'fab fa-vk',
    iconColor: 'blue-2',
    authorizationEndpoint: 'https://oauth.vk.com/authorize?scope=email&v=5.131',
    clientId: '7929993',
    loginType: 'normal',
  },
  {
    name: 'mailru',
    site: 'mail.ru',
    icon: 'fas fa-at',
    iconColor: 'deep-orange',
    authorizationEndpoint: 'https://oauth.mail.ru/login?scope=openid+userinfo+email+profile+offline_access',
    clientId: 'a63ce5684a1a4b61a4cb741ba072b57e',
    loginType: 'normal',
  },
  {
    name: 'okru',
    site: 'ok.ru',
    icon: 'fab fa-odnoklassniki',
    iconColor: 'orange',
    authorizationEndpoint: 'https://connect.ok.ru/oauth/authorize?scope=VALUABLE_ACCESS;GET_EMAIL;LONG_ACCESS_TOKEN',
    clientId: '512000823728',
    loginType: 'normal',
  },
  {
    name: 'yandex',
    site: 'yandex.ru',
    icon: 'fab fa-yandex',
    iconColor: 'red',
    authorizationEndpoint: 'https://oauth.yandex.ru/authorize?scope=login:birthday+login:email+login:info+login:avatar&force_confirm=yes',
    clientId: '23d6b1950e904067af78ed108d33d11d',
    loginType: 'normal',
  },
]

const loginWindowPopupName = 'loginWindow';
/**
 * See login-callback.html
 * See quasar.conf.js for FrontendUrl
 */
const loginEventName = 'loginCompleted';
let prevLoginEventListener: EventListener | null;
export async function openLoginWindow(provider:LoginProvider, action: (callbackParameters: string) => Promise<void>) {
  removeLoginEventListener();
  if (provider.loginType != 'normal' && !await showLoginProviderDialog(provider)) {
    return;
  }

  const clientId = process.env.PROD ? (provider.clientIdProd || provider.clientId) : provider.clientId;
  const redirectUri = `${String(process.env.FrontendUrl)}/login-callback.html`;
  const url = `${provider.authorizationEndpoint}&client_id=${clientId}&redirect_uri=${redirectUri}&state=state_client_quasar&response_type=code&nonce=${randomString(10)}`;
  const onLoginEventListener = prevLoginEventListener = ((ev: CustomEvent) => {
    let callbackParameters = ev.detail as string;
    console.log(`Call parent from popup. Callback parameters: ${callbackParameters}`);
    windowObjectReferences[loginWindowPopupName]?.close();
    if (callbackParameters.startsWith('?')) {
      callbackParameters = callbackParameters.substr(1);
    }
    if (callbackParameters.indexOf('error') == 0) {
      showErrorDialog(provider, callbackParameters);
    } else {
      void action(callbackParameters);
    }
    removeLoginEventListener();
  }) as EventListener;
  window.addEventListener(loginEventName, onLoginEventListener, {once: true});
/*
  window.onLoginCompleted = async function (callbackParameters: string) {
    console.log(`Call parent from popup. Callback parameters: ${callbackParameters}`);
    windowObjectReferences[loginWindowPopupName]?.close();
    action(callbackParameters);
    window.onLoginCompleted = null;
  };
*/

  openPopupWindow(url, loginWindowPopupName);
}

function removeLoginEventListener() {
  if (prevLoginEventListener != null) {
    window.removeEventListener(loginEventName, prevLoginEventListener);
  }
  prevLoginEventListener = null;
}


interface LoginProviderMessage {
  text: string,
  icon: string,
  iconColor: string,
  error: boolean
}

const emptyLoginProviderMessage: LoginProviderMessage = {
  text: '',
  icon: '',
  iconColor: '',
  error: false
}

const loginProviderMessages: {[name in LoginType]: LoginProviderMessage} = {
  normal: emptyLoginProviderMessage,
  warningRegister: {
    text: 'Интеграция с ${provider} находится в тестовом режиме. ' +
      'Для того чтобы заработал вход через ${provider} необходимо сначала ' +
      'написать разработчикам ' +
      '(<a href="https://telegram.me/M_pashka" target="_blank"><i class="fab fa-telegram"></i></a>)' +
      ', они должны добавить тебя в тестовые пользователи ' +
      'приложения TotemFTC на платформе ${provider}. И после соответствующих манипуляций ' +
      'можно будет пользоваться входом. Жми "Ok" если тебя уже добавили.',
    icon: 'fas fa-exclamation-triangle',
    iconColor: 'warning',
    error: false
  },
  errorApple: {
    text: 'Apple просит $99 в год за интеграцию. Данное приложение бесплатное ' +
      'и не принесло прямой прибыли. Так что пока не вижу финансовой возможности ' +
      'реализовать вход через Apple.',
    icon: 'fas fa-exclamation-circle',
    iconColor: 'negative',
    error: true
  },
  errorTwitter: {
    text: 'Интеграция с Twitter не работает. Twitter ' +
      'отклонил мой запрос и навсегда забанил меня. Сука. Надо создать нового пользователя '+
      'и написать им еще раз.',
    icon: 'fas fa-exclamation-circle',
    iconColor: 'negative',
    error: true
  },
};

function showErrorDialog(loginProvider: LoginProvider, callbackParameters: string) {
  Dialog.create({
    title: `<i class="fas fa-exclamation-circle negative"></i> Login through ${loginProvider.name} error`,
    message: `Login via provider ${loginProvider.name} error: ${callbackParameters}`,
    html: true,
  });
}

async function showLoginProviderDialog(loginProvider: LoginProvider): Promise<boolean> {
  const message = loginProviderMessages[loginProvider.loginType];
  return new Promise<boolean>(resolve => {
    Dialog.create({
      title: `<i class="${message.icon} text-${message.iconColor}"></i> Login through ${loginProvider.name}`,
      cancel: !message.error,
      message: message.text.replace(/\${provider}/g, loginProvider.name),
      html: true,
    }).onOk(() => {
      resolve(!message.error);
    }).onCancel(() => {
      resolve(false);
    }).onDismiss(() => {
      resolve(false);
    });
  });
}

/**
 * @see public/login-callback.html
 */
/*
declare global {
  interface Window {
    onLoginCompleted(callbackParameters: string): void;
  }
}
*/
