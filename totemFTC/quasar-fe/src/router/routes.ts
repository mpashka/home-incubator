import {RouteRecordRaw} from 'vue-router';
import MainLayout from 'src/pages/layouts/MainLayout.vue';
import Login from 'pages/login/Login.vue';
import Home from 'pages/Home.vue';
import TableSchedule from 'pages/TableSchedule.vue';
import TableTrainings from 'pages/TableTrainings.vue';
import TableUsers from 'pages/TableUsers.vue';
import TableUser from 'pages/TableUser.vue';
import TableVisits from 'pages/TableVisits.vue';
import TableFinance from 'pages/TableFinance.vue';

// Note: route protection is implemented in {@link boot/auth_check}

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: MainLayout,
    children: [
      { name: 'root',       path: '',           component: Home },
      { name: 'schedule',   path: 'schedule',   component: TableSchedule },
      { name: 'trainings',  path: 'trainings',  component: TableTrainings },
      { name: 'users',      path: 'users',      component: TableUsers },
      { name: 'user',       path: 'user/:userId(\\d+)', component: TableUser },
      { name: 'visits',     path: 'visits',     component: TableVisits },
      { name: 'finance',    path: 'finance',    component: TableFinance },
      { name: 'settings',   path: 'settings',   component: () => import('pages/Settings.vue') },
      { name: 'references', path: 'references', component: () => import('pages/TableReferences.vue') },
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
