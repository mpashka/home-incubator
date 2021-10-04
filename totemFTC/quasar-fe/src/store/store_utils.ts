import axios from 'axios';
import {useRouter} from 'vue-router';

export const SESSION_ID_STORAGE_KEY = 'session_id';

/* eslint-disable @typescript-eslint/no-explicit-any*/
export function processLoadError(method: string, err: any) {
  // todo handle error here - show message, add to table, e.t.c.
  if (axios.isAxiosError(err)) {
    if (err.response?.status === 403) {
      // This is ok - server reports there is no appropriate session - e.g. it was restarted
      localStorage.removeItem(SESSION_ID_STORAGE_KEY)
      const router = useRouter();
      router && router.push({ name: 'login', params: { inProgress: 'true' } })
        .catch(e => console.log('Error push to login page', e));
    }
  } else {
    console.log(`Unexpected Http ${method} Error`, err);
    throw err;
  }

}
