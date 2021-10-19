import {useStoreLogin} from 'src/store/store_login';

export let windowObjectReference: Window | null = null;

export function openPopupWindow(url: string) {
  try {

    /*
todo locate and resize
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
}

export type ProviderActionType = 'login' | 'link';

/**
 * todo [!] put warning screen text here (or into asset/resource and load here) and use single login warning page
 */
export interface SocialNetwork {
  name: string,
  icon: string,
  iconColor?: string,
  site: string,
  link: string, // This is used if social network doesn't have user link
  /** Used to show info page before default SN login screen */
  customLoginUrl?: string,
  loginScreen: boolean,
}

// import instagram_icon from 'src/assets/Instagram_logo_2016.svg'
/* eslint @typescript-eslint/no-var-requires: "off" */
const instagram_icon=require('src/assets/Instagram_logo_2016.svg') as string;

export const socialNetworks: SocialNetwork[] = [
  {
    name: 'facebook',
    site: 'facebook.com',
    icon: 'fab fa-facebook-f',
    iconColor: 'blue-10',
    link: 'https://facebook.com',
    customLoginUrl: '/login/facebook',
    loginScreen: true,
  },
  {
    name: 'google',
    site: 'google.com',
    icon: 'fab fa-google',
    iconColor: 'red-8',
    link: 'https://google.com',
    customLoginUrl: '/login/google',
    loginScreen: true,
  },
  {
    name: 'apple',
    site: 'apple.com',
    icon: 'fab fa-apple',
    iconColor: 'grey-13',
    link: 'https://apple.com',
    customLoginUrl: '/login/apple',
    loginScreen: true,
  },
  {
    name: 'instagram',
    site: 'instagram.com',
    // icon: 'fab fa-instagram',
    // iconColor: 'orange-8',
    icon: `img:${instagram_icon}`,
    // icon: `img:${String(require('src/assets/Instagram_logo_2016.svg'))}`,
    // icon: 'img:./assets/Instagram_logo_2016.svg',
    link: 'https://instagram.com',
    customLoginUrl: '/login/instagram',
    loginScreen: false,
  },
  {
    name: 'twitter',
    site: 'twitter.com',
    icon: 'fab fa-twitter',
    iconColor: 'light-blue-7',
    link: 'https://twitter.com',
    customLoginUrl: '/login/twitter',
    loginScreen: false,
  },
  {
    name: 'github',
    site: 'github.com',
    icon: 'fab fa-github',
    iconColor: 'black',
    link: 'https://github.com',
    loginScreen: true,
  },
  {
    name: 'vk',
    site: 'vk.com',
    icon: 'fab fa-vk',
    iconColor: 'blue-2',
    link: 'https://vk.com',
    loginScreen: true,
  },
  {
    name: 'mailru',
    site: 'mail.ru',
    icon: 'fas fa-at',
    iconColor: 'deep-orange',
    link: 'https://mail.ru',
    loginScreen: true,
  },
  {
    name: 'okru',
    site: 'ok.ru',
    icon: 'fab fa-odnoklassniki',
    iconColor: 'orange',
    link: 'https://ok.ru',
    loginScreen: true,
  },
]



export function openLoginWindow(provider:string, action: ProviderActionType) {
  const socialNetwork = socialNetworks.find(s => s.name === provider);

  let url = (socialNetwork && socialNetwork.customLoginUrl
      ? `${String(process.env.FrontendFullUrl)}${socialNetwork.customLoginUrl}`
      : `${String(process.env.BackendFullUrl)}/api/login/init/${provider}`)
    + `?action=${action}`;

  if (action === 'link') {
    const storeLogin = useStoreLogin();
    url += `&sessionId=${storeLogin.sessionId}`;
  }
  openPopupWindow(url);
  return false;
}

export type LoginUserType = 'new' | 'existing';

declare global {
  interface Window {
    onLoginCompleted(sessionId: string, user: LoginUserType): void;
  }
}
