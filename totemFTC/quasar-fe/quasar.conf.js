/*
 * This file runs in a Node context (it's NOT transpiled by Babel), so use only
 * the ES6 features that are supported by your Node version. https://node.green/
 */

// Configuration for your app
// https://v2.quasar.dev/quasar-cli/quasar-conf-js

/* eslint-env node */
/* eslint-disable @typescript-eslint/no-var-requires */
const { configure } = require('quasar/wrappers');
const { GitRevisionPlugin } = require('git-revision-webpack-plugin');
const gitRevisionPlugin = new GitRevisionPlugin();

// See lib/helpers/get-quasar-ctx.js
module.exports = configure(function (ctx) {
  const envObj = {
    RunProfile: process.env.RUN_PROFILE ? process.env.RUN_PROFILE : 'unknown',
  };

  if (ctx.dev) {
    envObj.BackendUrl = 'http://localhost:8080';
    envObj.FrontendUrl = 'http://localhost:8081';
    if (!process.env.RUN_PROFILE) envObj.RunProfile = 'dev';
  } else if (process.env.RUN_PROFILE === 'demo') {
    envObj.BackendUrl = 'https://api.ticket-demo.ml';
    envObj.FrontendUrl = 'https://ticket-demo.ml';
  } else if (process.env.RUN_PROFILE === 'totem' || ctx.prod) {
    envObj.BackendUrl = 'https://api.totemftc.ga';
    envObj.FrontendUrl = 'https://totemftc.ga';
  }
  envObj.ClientId = `quasar-${process.env.MODE}-${process.env.NODE_ENV}-${envObj.RunProfile}`;
  envObj.BuildInfo = `${process.env.USER} ${process.env.HOSTNAME} ${envObj.RunProfile} ${new Date().toISOString()} ${gitRevisionPlugin.branch()} ${gitRevisionPlugin.version()} ${gitRevisionPlugin.commithash()}`;

  return {
    // https://v2.quasar.dev/quasar-cli/supporting-ts
    supportTS: {
      tsCheckerConfig: {
        eslint: {
          enabled: true,
          files: './src/**/*.{ts,tsx,js,jsx,vue}',
        },
      }
    },

    // https://v2.quasar.dev/quasar-cli/prefetch-feature
    // preFetch: true,

    // app boot file (/src/boot)
    // --> boot files are part of "main.js"
    // https://v2.quasar.dev/quasar-cli/boot-files
    boot: [
      'pinia',
      'axios',
      'auth_check',
    ],

    // https://v2.quasar.dev/quasar-cli/quasar-conf-js#Property%3A-css
    css: [
      'app.scss'
    ],

    // https://github.com/quasarframework/quasar/tree/dev/extras
    extras: [
      // 'ionicons-v4',
      'mdi-v6',
      'fontawesome-v5',
      // 'eva-icons',
      // 'themify',
      // 'line-awesome',
      // 'roboto-font-latin-ext', // this or either 'roboto-font', NEVER both!

      'roboto-font', // optional, you are not bound to it
      'material-icons', // optional, you are not bound to it
    ],

    // Full list of options: https://v2.quasar.dev/quasar-cli/quasar-conf-js#Property%3A-build
    // For ctx description see totemFTC/quasar-fe/node_modules/@quasar/app/lib/helpers/get-quasar-ctx.js
    build: {
      // htmlFilename: "true" === process.env.DEMO_BUILD ? 'quasar.html' : 'index.html',
      htmlFilename: 'admin.html',

      //vueRouterMode: 'hash', // available values: 'hash', 'history'
      vueRouterMode: 'history',

      // transpile: false,

      // Add dependencies for transpiling with Babel (Array of string/regex)
      // (from node_modules, which are by default not transpiled).
      // Applies only if "transpile" is set to true.
      // transpileDependencies: [],

      // rtl: true, // https://v2.quasar.dev/options/rtl-support
      // preloadChunks: true,
      // showProgress: false,
      // gzip: true,
      // analyze: true,

      // Options below are automatically set depending on the env, set them if you want to override
      // extractCSS: false,

      // https://v2.quasar.dev/quasar-cli/handling-webpack
      // "chain" is a webpack-chain object https://github.com/neutrinojs/webpack-chain
      chainWebpack (/* chain */) {
        //
      },

      // distDir: ctx.modeName === 'spa' && ctx.prod ? 'target/classes/META-INF/resources' : `dist/${ctx.modeName}`,
      // distDir: "true" === process.env.DEMO_BUILD ? '../target/web' : `dist/${ctx.modeName}`,

      distDir: ctx.modeName === 'spa' ? `dist/${ctx.modeName}-${process.env.RUN_PROFILE}` : `dist/${ctx.modeName}`,

      env: envObj,
/*
      env: {
        // Used to make backend api calls. See axios.ts
        BackendUrl: ctx.dev ? 'http://localhost:8080' : 'https://api.totemftc.ga',
        FrontendUrl: ctx.dev ? 'http://localhost:8081' : 'https://totemftc.ga',
        demo: "true" === process.env.DEMO_BUILD,
        BuildInfo: `${process.env.USER} ${process.env.HOSTNAME} ${new Date().toISOString()} ${gitRevisionPlugin.branch()} ${gitRevisionPlugin.version()}`,
      }
*/
    },

    // Full list of options: https://v2.quasar.dev/quasar-cli/quasar-conf-js#Property%3A-devServer
    devServer: {
      https: false,
      port: 8081,
      open: false, // opens browser window automatically
    },

    // https://v2.quasar.dev/quasar-cli/quasar-conf-js#Property%3A-framework
    framework: {
      lang: 'ru',
      config: {},

      // iconSet: 'material-icons', // Quasar icon set
      iconSet: 'fontawesome-v5', // Quasar icon set
      // lang: 'en-US', // Quasar language pack

      // For special cases outside of where the auto-import strategy can have an impact
      // (like functional components as one of the examples),
      // you can manually specify Quasar components/directives to be available everywhere:
      //
      // components: [],
      // directives: [],

      // Quasar plugins
      plugins: [
        'Dialog'
      ]
    },

    // animations: 'all', // --- includes all animations
    // https://v2.quasar.dev/options/animations
    animations: [],

    // https://v2.quasar.dev/quasar-cli/developing-ssr/configuring-ssr
    ssr: {
      pwa: false,

      // manualStoreHydration: true,
      // manualPostHydrationTrigger: true,

      prodPort: 3000, // The default port that the production server should use
                      // (gets superseded if process.env.PORT is specified at runtime)

      maxAge: 1000 * 60 * 60 * 24 * 30,
        // Tell browser when a file from the server should expire from cache (in ms)

      chainWebpackWebserver (/* chain */) {
        //
      },

      middlewares: [
        ctx.prod ? 'compression' : '',
        'render' // keep this as last one
      ]
    },

    // https://v2.quasar.dev/quasar-cli/developing-pwa/configuring-pwa
    pwa: {
      workboxPluginMode: 'GenerateSW', // 'GenerateSW' or 'InjectManifest'
      workboxOptions: {}, // only for GenerateSW

      // for the custom service worker ONLY (/src-pwa/custom-service-worker.[js|ts])
      // if using workbox in InjectManifest mode
      chainWebpackCustomSW (/* chain */) {
        //
      },

      manifest: {
        name: 'Totem FTC',
        short_name: 'Totem FTC',
        description: 'Totem FTC Management tool',
        display: 'standalone',
        orientation: 'portrait',
        background_color: '#ffffff',
        theme_color: '#027be3',
        icons: [
          {
            src: 'icons/icon-128x128.png',
            sizes: '128x128',
            type: 'image/png'
          },
          {
            src: 'icons/icon-192x192.png',
            sizes: '192x192',
            type: 'image/png'
          },
          {
            src: 'icons/icon-256x256.png',
            sizes: '256x256',
            type: 'image/png'
          },
          {
            src: 'icons/icon-384x384.png',
            sizes: '384x384',
            type: 'image/png'
          },
          {
            src: 'icons/icon-512x512.png',
            sizes: '512x512',
            type: 'image/png'
          }
        ]
      }
    },

    // Full list of options: https://v2.quasar.dev/quasar-cli/developing-cordova-apps/configuring-cordova
    cordova: {
      // noIosLegacyBuildFlag: true, // uncomment only if you know what you are doing
    },

    // Full list of options: https://v2.quasar.dev/quasar-cli/developing-capacitor-apps/configuring-capacitor
    capacitor: {
      hideSplashscreen: true
    },

    // Full list of options: https://v2.quasar.dev/quasar-cli/developing-electron-apps/configuring-electron
    electron: {
      bundler: 'packager', // 'packager' or 'builder'

      packager: {
        // https://github.com/electron-userland/electron-packager/blob/master/docs/api.md#options

        // OS X / Mac App Store
        // appBundleId: '',
        // appCategoryType: '',
        // osxSign: '',
        // protocol: 'myapp://path',

        // Windows only
        // win32metadata: { ... }
      },

      builder: {
        // https://www.electron.build/configuration/configuration

        appId: 'quasar-fe'
      },

      // "chain" is a webpack-chain object https://github.com/neutrinojs/webpack-chain
      chainWebpack (/* chain */) {
        // do something with the Electron main process Webpack cfg
        // extendWebpackMain also available besides this chainWebpackMain
      },

      // "chain" is a webpack-chain object https://github.com/neutrinojs/webpack-chain
      chainWebpackPreload (/* chain */) {
        // do something with the Electron main process Webpack cfg
        // extendWebpackPreload also available besides this chainWebpackPreload
      },
    }
  }
});
