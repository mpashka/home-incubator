import { RouteRecordRaw } from 'vue-router';
import MainLayout from 'src/pages/layouts/MainLayout.vue';
import Login from 'pages/login/Login.vue';
import Home from 'pages/Home.vue';
import TableSchedule from 'pages/TableSchedule.vue';
import TableTrainings from 'pages/TableTrainings.vue';
import TableUsers from 'pages/TableUsers.vue';
import TableVisits from 'pages/TableVisits.vue';
import Settings from 'pages/Settings.vue';

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: MainLayout,
    children: [
      { path: '', name: 'root', component: Home },
      { path: 'schedule', component: TableSchedule },
      { path: 'trainings', component: TableTrainings },
      { path: 'tableUsers', component: TableUsers },
      { path: 'tableVisits', component: TableVisits },
      { path: 'settings', component: Settings },
    ],
    meta: {
      requiresAuth: true
    }
  },

  { path: '/login', name: 'login', component: Login, props: (route) => ({ inProgress: (route.query.inProgress === 'true') }), },

  // Always leave this as last one,
  // but you can also remove it
  {
    path: '/:catchAll(.*)*',
    component: () => import('pages/Error404.vue'),
  },
];

export default routes;
