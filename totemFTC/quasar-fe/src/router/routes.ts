import { RouteRecordRaw } from 'vue-router';
import MainLayout from 'src/layouts/MainLayout.vue';
import Login from 'pages/login/Login.vue';
import LoginOk from 'pages/login/LoginOk.vue';

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: MainLayout,
    children: [
      { path: '', component: () => import('pages/Home.vue') },
      { path: 'tableTrainers', component: () => import('pages/TableTrainers.vue') },
      { path: 'tableVisits', component: () => import('pages/TableVisits.vue') },
      { path: 'settings', component: () => import('pages/Settings.vue') },
    ],
    meta: {
      requiresAuth: true
    }
  },

  {
    path: '/login',
    name: 'login',
    component: Login,
    props: (route) => ({ inProgress: (route.query.inProgress === 'true') }),
  },

  {
    path: '/login-ok',
    name: 'login-ok',
    component: LoginOk,
  },

  // Always leave this as last one,
  // but you can also remove it
  {
    path: '/:catchAll(.*)*',
    component: () => import('pages/Error404.vue'),
  },
];

export default routes;
