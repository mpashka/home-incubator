<script setup>
import { computed } from 'vue'
import { grammarHelpOpen, grammarTopic, ruleForm, ruleWord } from '../grammar-help.js'
import { ruleFor, breakdown, CASE_ORDER, CASE_USE } from '../grammar-rules.js'
import { formName, formNameSerbian } from '../labels.js'
import { ruleParadigm, ruleExamples, ruleExceptions, ruleWordBreakdown } from '../settings.js'
import Accented from './Accented.vue'

/** Правило, породившее форму, из-за которой справку открыли. */
const rule = computed(() => ruleFor(ruleForm.value?.rule, ruleForm.value?.ruleType))
/** Разбор этой формы: основа и окончание по той же таблице, что показана рядом. */
const parts = computed(() => breakdown(rule.value, ruleForm.value))

/**
 * Парадигма конкретного слова — из его же карточки, а не пересчитанная заново:
 * второй расчёт разошёлся бы с первым, и словарь начал бы спорить сам с собой.
 */
const paradigm = computed(() => {
  const cells = {}
  for (const form of ruleWord.value?.forms ?? []) {
    const [caseKey, number] = (form.grammar ?? '').split('.')
    if (!CASE_ORDER.includes(caseKey) || !['sg', 'pl'].includes(number)) continue
    cells[caseKey] ??= { sg: [], pl: [] }
    if (!cells[caseKey][number].includes(form.form)) cells[caseKey][number].push(form.form)
  }
  return cells
})

const cellText = (caseKey, number) => (paradigm.value[caseKey]?.[number] ?? []).join(' / ')

const topics = [
  ['nouns', 'Падежи'], ['verbs', 'Глаголы'], ['pronouns', 'Местоимения'], ['adjectives', 'Прилагательные']
]
const cases = [
  ['Nominativ', 'Ko? Šta?', 'Подлежащее и называние предмета.', 'Student uči gramatiku.'],
  ['Genitiv', 'Koga? Čega?', 'Принадлежность, количество и многие предлоги.', 'Bez rečnika; odgovor od studenta.'],
  ['Dativ', 'Kome? Čemu?', 'Косвенное дополнение: адресат, направление.', 'Profesor odgovara studentu.'],
  ['Akuzativ', 'Koga? Šta?', 'Прямое дополнение; направление движения.', 'Profesor pita studenta.'],
  ['Vokativ', '—', 'Обращение; у многих существительных имеет особое окончание.', 'Gde si, brate!'],
  ['Instrumental', 'Kim? Čim?', 'Средство или совместность.', 'Student piše olovkom.'],
  ['Lokativ', 'Kome? Čemu?', 'Место или тема; употребляется с предлогом.', 'Student studira na univerzitetu.']
]
const nounEndings = [
  ['Nom.', '—', '-i'],
  ['Gen.', '-a', '-a'],
  ['Dat.', '-u', '-ima'],
  ['Akuz.', '= Nom. / Gen.*', '-e'],
  ['Vok.', '-e / -u', '= Nom.'],
  ['Inst.', '-om / -em', '-ima'],
  ['Lok.', '-u', '-ima']
]
</script>

<template>
  <div v-if="grammarHelpOpen" class="grammar-overlay" @click.self="grammarHelpOpen = false">
    <section class="grammar-dialog" role="dialog" aria-modal="true" aria-label="Грамматика сербского языка">
      <header>
        <h2>Грамматика</h2>
        <button class="grammar-close" type="button" aria-label="Закрыть" @click="grammarHelpOpen = false">×</button>
      </header>
      <nav class="grammar-tabs" aria-label="Раздел грамматики">
        <button v-if="ruleForm" type="button" :class="{ active: grammarTopic === 'rule' }" @click="grammarTopic = 'rule'">Правило формы</button>
        <button v-for="[key, title] in topics" :key="key" type="button" :class="{ active: grammarTopic === key }" @click="grammarTopic = key">{{ title }}</button>
      </nav>

      <!-- Правило, по которому получена форма в карточке: панель разделов такая же,
           как у карточки, — что показывать, решает читающий. -->
      <div v-if="grammarTopic === 'rule' && rule" class="grammar-content">
        <h3 class="rule-title">
          {{ rule.title }}
          <small v-if="ruleForm?.grammar">— {{ formNameSerbian(ruleForm.grammar) }}, {{ formName(ruleForm.grammar) }}</small>
        </h3>

        <p v-if="rule.kind === 'absent'" class="rule-absent">{{ rule.reason }}</p>

        <template v-else>
          <p>{{ rule.summary }}</p>
          <p class="grammar-source">
            Окончания — те самые, по которым словарь построил форму (правило
            <code>{{ ruleForm.rule }}</code>). Сверено с О. А. Просвириной, §5.2.3–5;
            употребление падежей — по В. Краишник, «Научимо падеже».
            <b>Ударение правилом не выводится</b>: у порождённых форм его нет.
          </p>

          <nav class="quick-toggles rule-toggles" aria-label="Разделы правила">
            <button type="button" :class="{ active: ruleWordBreakdown === 'true' }" @click="ruleWordBreakdown = ruleWordBreakdown === 'true' ? 'false' : 'true'">Разбор слова</button>
            <button type="button" :class="{ active: ruleParadigm === 'true' }" @click="ruleParadigm = ruleParadigm === 'true' ? 'false' : 'true'">Парадигма</button>
            <button type="button" :class="{ active: ruleExamples === 'true' }" @click="ruleExamples = ruleExamples === 'true' ? 'false' : 'true'">Примеры</button>
            <button type="button" :class="{ active: ruleExceptions === 'true' }" @click="ruleExceptions = ruleExceptions === 'true' ? 'false' : 'true'">Исключения</button>
          </nav>

          <section v-if="ruleWordBreakdown === 'true'" class="rule-block">
            <h4>Разбор слова</h4>
            <p>{{ rule.stemHint }}</p>
            <p v-if="parts" class="rule-breakdown">
              <Accented :text="ruleWord?.headword || ''" />
              → основа <b>{{ parts.stem }}</b> + окончание <b>{{ parts.ending }}</b>
              = <Accented :text="ruleForm.form" />
            </p>
            <p v-else class="rule-note">
              Эта форма правилу не отвечает: она пришла из словаря, а не выведена окончанием.
            </p>
          </section>

          <section v-if="ruleParadigm === 'true'" class="rule-block">
            <h4>Окончания и формы слова</h4>
            <table>
              <thead><tr><th>Падеж</th><th>Ед. ч.</th><th>Слово</th><th>Мн. ч.</th><th>Слово</th></tr></thead>
              <tbody>
                <tr v-for="key in CASE_ORDER" :key="key" :class="{ 'rule-current': ruleForm?.grammar?.startsWith(key + '.') }">
                  <th>{{ formNameSerbian(key + '.sg').split(' ')[0] }}</th>
                  <td>{{ rule.endings[key][0] }}</td>
                  <td><Accented :text="cellText(key, 'sg')" /></td>
                  <td>{{ rule.endings[key][1] }}</td>
                  <td><Accented :text="cellText(key, 'pl')" /></td>
                </tr>
              </tbody>
            </table>
            <p class="rule-note">Образец: {{ rule.sample.word }} «{{ rule.sample.translation }}», основа {{ rule.sample.stem }}-.</p>
          </section>

          <section v-if="ruleExamples === 'true'" class="rule-block">
            <h4>Когда какой падеж</h4>
            <article v-for="key in CASE_ORDER" :key="key" class="grammar-case">
              <h3>{{ formNameSerbian(key + '.sg').split(' ')[0] }} <small>{{ CASE_USE[key][0] }}</small></h3>
              <p>{{ CASE_USE[key][1] }}</p>
              <p class="grammar-example">{{ CASE_USE[key][2] }}</p>
            </article>
          </section>

          <section v-if="ruleExceptions === 'true'" class="rule-block">
            <h4>Чего правило не делает</h4>
            <ul class="rule-exceptions">
              <li v-for="(item, index) in rule.exceptions" :key="index">{{ item }}</li>
            </ul>
          </section>
        </template>
      </div>

      <div v-else-if="grammarTopic === 'nouns'" class="grammar-content">
        <p class="grammar-source">По О. А. Просвириной, §5.2.3–5; употребление падежей дополнено пособием В. Краишник «Научимо падеже».</p>
        <p>Склоняются существительные, прилагательные, местоимения и некоторые числительные. В сербском семь падежей: добавлен вокатив.</p>
        <article v-for="item in cases" :key="item[0]" class="grammar-case">
          <h3>{{ item[0] }} <small>{{ item[1] }}</small></h3>
          <p>{{ item[2] }}</p>
          <p class="grammar-example">{{ item[3] }}</p>
        </article>
        <h3>Первое склонение: мужской род на согласный</h3>
        <table><thead><tr><th>Падеж</th><th>Единственное</th><th>Множественное</th></tr></thead><tbody>
          <tr v-for="row in nounEndings" :key="row[0]"><th>{{ row[0] }}</th><td>{{ row[1] }}</td><td>{{ row[2] }}</td></tr>
        </tbody></table>
        <p>* В аккузативе единственного числа неодушевлённое существительное совпадает с номинативом, одушевлённое — с генитивом: <i>vidim rečnik / studenta</i>.</p>
        <p>Это образец, не универсальный алгоритм: чередования и исключения (например, беглое <i>a</i>) словарь показывает готовой формой из источника.</p>
      </div>

      <div v-else-if="grammarTopic === 'verbs'" class="grammar-content">
        <p class="grammar-source">По О. А. Просвириной, §5.6.1–2 и §5.6.17.</p>
        <p>Форму нельзя надёжно получить только из инфинитива: в учебнике тип спряжения определяется соотношением основ инфинитива и презенса. Поэтому парадигма конкретного глагола в словаре хранится отдельно.</p>
        <p>Образец второго спряжения: <b>učiti</b> («учить»).</p>
        <table><tbody>
          <tr><th>ja</th><td>učim</td><th>mi</th><td>učimo</td></tr>
          <tr><th>ti</th><td>učiš</td><th>vi</th><td>učite</td></tr>
          <tr><th>on / ona / ono</th><td>uči</td><th>oni / one / ona</th><td>uče</td></tr>
        </tbody></table>
        <p>Три практических ориентира: <i>pitati → pitam</i>, <i>nositi → nosim</i>, <i>pisati → pišem</i>. Чередование в третьем типе существенно; его нельзя подменять одним окончанием.</p>
        <p><i>Biti</i> — отдельный глагол: <i>sam, si, je, smo, ste, su</i>; отрицание пишется слитно: <i>nisam, nisi, nije…</i>. После модальных глаголов допустимы инфинитив и <i>da + prezent</i>: <i>moram raditi / moram da radim</i>.</p>
      </div>

      <div v-else-if="grammarTopic === 'pronouns'" class="grammar-content">
        <p class="grammar-source">По О. А. Просвириной, §5.4.1.</p>
        <p>В генитиве, дативе и аккузативе есть полные ударные и краткие безударные формы. Полная обязательна после предлога, в начале фразы и при логическом выделении; краткая обычно стоит после первого ударного слова.</p>
        <table><thead><tr><th>Падеж</th><th>Я</th><th>Ты</th><th>Он</th><th>Она</th><th>Мы</th><th>Они</th></tr></thead><tbody>
          <tr><th>Nominativ</th><td>ja</td><td>ti</td><td>on</td><td>ona</td><td>mi</td><td>oni</td></tr>
          <tr><th>Genitiv</th><td>mene / me</td><td>tebe / te</td><td>njega / ga</td><td>nje / je</td><td>nas</td><td>njih / ih</td></tr>
          <tr><th>Dativ</th><td>meni / mi</td><td>tebi / ti</td><td>njemu / mu</td><td>njoj / joj</td><td>nama / nam</td><td>njima / im</td></tr>
          <tr><th>Akuzativ</th><td>mene / me</td><td>tebe / te</td><td>njega / ga</td><td>nju / je</td><td>nas</td><td>njih / ih</td></tr>
          <tr><th>Instrumental</th><td>mnom</td><td>tobom</td><td>njim</td><td>njom</td><td>nama</td><td>njima</td></tr>
          <tr><th>Lokativ</th><td>meni</td><td>tebi</td><td>njemu</td><td>njoj</td><td>nama</td><td>njima</td></tr>
        </tbody></table>
        <p>Сравните: <i>Volim ga</i>, но <i>Mislim na njega</i>. Возвратное <i>sebe / se</i> относится к подлежащему и не имеет номинатива.</p>
      </div>

      <div v-else-if="grammarTopic === 'adjectives'" class="grammar-content">
        <p class="grammar-source">По О. А. Просвириной, §5.3.1–2.</p>
        <p>Прилагательное согласуется с существительным в роде, числе и падеже. Качественные прилагательные имеют неопределённый и определённый вид: <i>On je dobar</i>, но <i>taj dobri mladić</i>. Относительные имеют только определённую форму.</p>
        <table><thead><tr><th>Падеж</th><th>м. р.</th><th>ж. р.</th><th>ср. р.</th></tr></thead><tbody>
          <tr><th>Nominativ</th><td>dobar čovek</td><td>dobra žena</td><td>dobro dete</td></tr>
          <tr><th>Genitiv</th><td>dobrog čoveka</td><td>dobre žene</td><td>dobrog deteta</td></tr>
          <tr><th>Dativ</th><td>dobrom čoveku</td><td>dobroj ženi</td><td>dobrom detetu</td></tr>
          <tr><th>Akuzativ</th><td>dobrog čoveka / dobar sto</td><td>dobru ženu</td><td>dobro dete</td></tr>
          <tr><th>Instrumental</th><td>s dobrim čovekom</td><td>s dobrom ženom</td><td>s dobrim detetom</td></tr>
          <tr><th>Lokativ</th><td>o dobrom čoveku</td><td>o dobroj ženi</td><td>o dobrom detetu</td></tr>
        </tbody></table>
        <p>В аккузативе единственного числа мужского рода форма зависит от одушевлённости: <i>vidim debelog mačka</i>, но <i>vidim debeli rečnik</i>. После <i>dva, tri, četiri, oba</i> появляется окончание <i>-a</i>: <i>dva dobra studenta</i>.</p>
      </div>
    </section>
  </div>
</template>
