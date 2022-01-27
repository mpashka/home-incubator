import {boot} from 'quasar/wrappers'
import {createPinia} from 'pinia'
import {useStoreClientConfig} from 'src/store/store_config';

// more info on params: https://v2.quasar.dev/quasar-cli/boot-files
export default boot( ( { app} ) => {
  app.use(createPinia());

  useStoreClientConfig().init();
});
