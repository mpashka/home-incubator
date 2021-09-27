import { RouteRecordRaw } from 'vue-router';
import MainLayout from 'src/layouts/MainLayout.vue';
import Login from 'src/pages/Login.vue';

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: MainLayout,
    children: [
      { path: '', component: () => import('pages/Home.vue') },
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
  },

  // Always leave this as last one,
  // but you can also remove it
  {
    path: '/:catchAll(.*)*',
    component: () => import('pages/Error404.vue'),
  },
];

export default routes;
