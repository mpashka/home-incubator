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

interface CustomLoginScreen {
  name: string,
  url: string,
}

const customLoginScreens: CustomLoginScreen[] = [
  { name: 'google', url: '/login/google' },
  { name: 'facebook', url: '/login/facebook' },
  { name: 'instagram', url: '/login/instagram' },
  { name: 'twitter', url: '/login/twitter' },
  { name: 'apple', url: '/login/apple' },
];

export function openLoginWindow(provider:string, action: ProviderActionType) {
  const customLoginScreen = customLoginScreens.find(s => s.name === provider);

  let url = (customLoginScreen
      ? `${String(process.env.FrontendFullUrl)}${customLoginScreen.url}`
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
