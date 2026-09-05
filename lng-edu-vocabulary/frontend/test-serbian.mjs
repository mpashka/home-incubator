import assert from 'node:assert/strict'
import { toLatin } from './src/serbian.js'

assert.equal(toLatin('Љу̏бав, џеп и њива'), 'Ljȕbav, džep i njiva')
