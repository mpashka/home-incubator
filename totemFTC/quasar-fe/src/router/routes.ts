import { RouteRecordRaw } from 'vue-router';
import MainLayout from 'src/pages/layouts/MainLayout.vue';
import Login from 'pages/login/Login.vue';

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: MainLayout,
    children: [
      { path: '', name: 'root', component: () => import('pages/Home.vue') },
      { path: 'schedule', component: () => import('pages/TableSchedule.vue') },
      { path: 'trainings', component: () => import('pages/TableTrainings.vue') },
      { path: 'tableUsers', component: () => import('pages/TableUsers.vue') },
      { path: 'tableVisits', component: () => import('pages/TableVisits.vue') },
      { path: 'settings', component: () => import('pages/Settings.vue') },
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
