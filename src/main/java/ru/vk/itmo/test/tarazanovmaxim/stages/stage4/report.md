# Таразанов Максим, ИТМО ФИТИП, М4139, Stage 4

## Репликация
На данной стадии реализовывалось горизонтальное масштабирование в виде
репликационного кластера с целью повышения отказоустойчивости.

Хеш-функция была изменена c SHA-256 на Murmur3 (соответственно предложению
из ревью взять более простую функцию, хотя на общем фоне это по-прежнему не так заметно)

Результаты предыдущего этапа оказались несовсем легитимными,
об этом описано [в конце отчета](#PS). Коротко о нелегитимных результатах:
на get-запросах 30000rps вместо 12000.

## GET
Тестирование происходило на скриптах из предыдущих этапов.
При условии, что нам необходимо получить ответы от более чем
половины, ask считался через ```quorum(from) = from / 2 + 1```.
ask и from не заполнялись в скрипте и были равны соответственно 2 и 3.

В своей совокупности латенси не изменилось, просел rps.
Он сократился на 15% до 25000rps.
Связано с увеличением количества обрабатываемых запросов (в виде редиректов),
а также необходимости поддержки согласованности данных.

## PUT
Условия тестирования аналогичны get-запросам. Явное задание значений
ack и from никак не влияют на итоговые результаты.

Почти на треть сократилось количество запросов в секунду.
Это сокращение количества стабильно-обрабатывающихся запросов
обусловлено тем, что нам также надо поддерживать согласованность,
а для этого нам также надо переспрашивать другие узлы.
В итоге система чуть медленнее работает, но гораздо эффективнее
в плане получения итогового результата при неожиданных отключениях узлов.

## Asprof

### CPU
* PUT: Узел при получении запроса говорит создать реплику, и в результате
работа самого узла и узла для реплики не отличается: "просто записать";
* GET: Огромная часть процессорного времени идёт на сбор ответов реплик;
такова цена за согласованность данных. В частности увеличелась нагрузка на ResponceReader,
из-за редиректов надо больше отвечать.

### Alloc
Значительных зменений обнаружено не было.

### Lock
Изменений обнаружено не было, разве что в виде погрешности в 0.5-1%.
В целом результаты не отличаются

#### PS
Я нашел причину переодических сбоев системы, о наличии которых упоминал ранее.
В отчете к стадии 3 я писал, что иногда моё железо странно работает,
и я нашёл причину -- зарядка меньшей мощности чем родная от ноутбука.
Странное поведение напрямую коррелирует с использованием неродной зарядки.
Я связываю это с тем, что ноутбуки делают "умом", и при сигнале о зарядке,
чтобы не использовать мимолетный ресурс батареи, питание элементов происходит
напрямую от розетки. Неродная зарядка расчитана на 120Вт, родная -- на 180Вт.
В результате просадки напряжения и нестабильная работа (в частности процессора и памяти).
В целом я посмотрел результаты предыдущей стадии и они всё же оказались примерно
такими же, что и описаны в отчете, только работает сервер чуть стабильнее
(графики почти идентичны, латенси в пределах погрешности, rps на put не изменяется,
на get стабильная работа поддерживается при увеличении rps в 2.5 раза, порядка 30000rps).

В итоге помимо горизонтального масштабирования у меня произошло ещё и вертикальное)