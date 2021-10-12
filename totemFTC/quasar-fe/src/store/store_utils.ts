import {defineStore} from "pinia";

export const useStoreUtils = defineStore('storeUtils', {
  state: () => ({
    loading: false,
  }),
});

/** Returns true if all filters are present in some obj parts */
export function contains(objStrings: string[], filters: string[]): boolean {
  for (let i = 0; i < objStrings.length; i++) {
    objStrings[i] = objStrings[i].toLowerCase();
  }
  for (let i = 0; i < filters.length; i++) {
    let notPresent = true;
    for (let j = 0; j < objStrings.length; j++) {
      if (objStrings[j].includes(filters[i])) {
        notPresent = false;
        break;
      }
    }
    if (notPresent) {
      return false;
    }
  }
  return true;
}
