import {boot} from 'quasar/wrappers'
import {useStore} from 'src/store';

// "async" is optional;
// more info on params: https://v2.quasar.dev/quasar-cli/boot-files
export default boot(async ( { redirect } ) => {
  const store = useStore();
  await store.dispatch("login/authenticate");
  if (!store.getters["login/isAuthenticated"]) {
    redirect('/login');
  }
})
