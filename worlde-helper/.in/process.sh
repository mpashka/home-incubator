sed 's/Ё/Е/' f_out1.txt| sed 's/[\.12":;-\)]*$//' | grep "^.....$">f_out2.txt
sed 's/[УДЯЮШЗЭ]//g' f_out2.txt |awk '{ print length() " - "  $0 }'|sort|head
grep '^[УДЯЮШЗЭЬЕ]*$' f_out2.txt


tr '[:blank:][:cntrl:][:punct:][:space:][:digit:]' '\n' <eng_in1.txt  |tr '[:upper:]' '[:lower:]'|grep '^.....$'| grep '^[a-z]*$'|sort|uniq>eng_out1.txt

2 - ЬЕ
3 - АВЬ
3 - АГО
3 - АНА
3 - АТЬ
3 - БАР
3 - БНК
3 - БРК
3 - ВГЛ
3 - ВЕА

_[кз][он]о[а]!итлемурвс
"[a]_[a][n][!yt]!rosepldick"
"[!нa][!ар]р__!ктеп"