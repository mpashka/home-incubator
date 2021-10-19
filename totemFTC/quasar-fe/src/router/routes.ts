import { RouteRecordRaw } from 'vue-router';
import MainLayout from 'src/layouts/MainLayout.vue';
import Login from 'pages/login/Login.vue';

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: MainLayout,
    children: [
      { path: '', component: () => import('pages/Home.vue') },
      { path: 'schedule', component: () => import('pages/TableSchedule.vue') },
      { path: 'tableUsers', component: () => import('pages/TableUsers.vue') },
      { path: 'tableVisits', component: () => import('pages/TableVisits.vue') },
      { path: 'settings', component: () => import('pages/Settings.vue') },
    ],
    meta: {
      requiresAuth: true
    }
  },

  { path: '/login', name: 'login', component: Login, props: (route) => ({ inProgress: (route.query.inProgress === 'true') }), },
  { path: '/login/ok', name: 'login-ok', component: () => import('pages/login/LoginOk.vue'), },
  { path: '/login/google', name: 'login-google', component: () => import('pages/login/LoginGoogle.vue'), },
  { path: '/login/facebook', name: 'login-facebook', component: () => import('pages/login/LoginFacebook.vue'), },
  { path: '/login/twitter', name: 'login-twitter', component: () => import('pages/login/LoginTwitter.vue'), },
  { path: '/login/instagram', name: 'login-instagram', component: () => import('pages/login/LoginInstagram.vue'), },
  { path: '/login/apple', name: 'login-apple', component: () => import('pages/login/LoginApple.vue'), },

  // Always leave this as last one,
  // but you can also remove it
  {
    path: '/:catchAll(.*)*',
    component: () => import('pages/Error404.vue'),
  },
];

export default routes;
