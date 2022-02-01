import {defineStore} from "pinia";
import {date} from 'quasar';

export type DateValue = string | Date | number;

export interface DateInterval {
  from: DateValue,
  to: DateValue,
}

export interface UiDate {
  year: number, month: number, day: number,
}

export interface UiLabel {label: string, value: string}

export const timeFormat = 'HH:mm';
export const dateFormat = 'YYYY-MM-DD';
export const dateTimeFormat = 'YYYY-MM-DD HH:mm';

export const monthsGenitiveCase = [
  'января', 'февраля', 'марта', 'апреля', 'мая', 'июня', 'июля', 'августа', 'сентября', 'октября', 'ноября', 'декабря'
];
export const formatGenitiveCase = {
  months: monthsGenitiveCase,
};

export function weekDateName(inDate: DateValue) {
  const startDate = new Date(inDate);
  const endDate = date.addToDate(startDate, {days: 6});
  const startStr = date.formatDate(startDate, startDate.getMonth() === endDate.getMonth() ? 'D' : 'D MMMM', formatGenitiveCase);
  const endStr = date.formatDate(endDate, 'D MMMM', formatGenitiveCase);
  return `${startStr} - ${endStr}`;
}

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

const weekStartDay = 1;  // Monday

export function weekStart(inDate: Date | number | string) {
  const day = date.startOfDate(inDate, 'day');
  const weekMinus = (date.getDayOfWeek(day) - weekStartDay) % 7;
  return date.subtractFromDate(day, {days: weekMinus});
}

export function dateLabel(d: DateValue): string {
  switch (date.getDateDiff(d, Date.now(), 'days')) {
    case 0: return 'Сегодня';
    case 1: return 'Завтра';
    case 2: return 'Послезавтра';
    case 7: return 'Через неделю';
    case -1: return 'Вчера';
    case -2: return 'Позавчера';
    case -7: return 'Неделю назад';
  }
  return '';
}

export type EditType = 'add' | 'edit' | undefined;

export const useStoreUtils = defineStore('storeUtils', {
  state: () => ({
    loading: false,
  }),
});
