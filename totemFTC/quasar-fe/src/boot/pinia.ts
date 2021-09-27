import {boot} from 'quasar/wrappers'
import {createPinia} from 'pinia'

// more info on params: https://v2.quasar.dev/quasar-cli/boot-files
export default boot( ( { app} ) => {
  app.use(createPinia());
})
