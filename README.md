# Проблемы с отображением маркеров
Изначальная идея использования `iconOpacity` заключалась в том, чтобы покрыть большинство кейсов.

В данном проекте выявлены следующие проблемы, связанные с отображением маркеров и текста на карте:

## 1. Маркеры иногда не возвращаются  
Иногда маркеры не возвращаются в видимую область после скрытия

### Видео:  
[Проблема с возвратом маркеров](https://github.com/user-attachments/assets/103b48f5-83db-4113-a9c5-0fb00e401e09)

---

## 2. Использование `iconOpacity`  
Попытка сочетания маркеров с параметром `iconOpacity` частично решает проблему. Однако:  
- Иногда текст маркера остаётся видимым.  
- Установка параметра текста в `transparent` делает его только полупрозрачным, но не скрывает полностью.

---

## 3. Проблемы с `visible`  
При отключении `iconOpacity` и использовании только параметра `visible`, иногда маркер не скрывается.  
На примере в видео видно, что при нажатии на **Marker 0** он не исчезает:  

### Видео:  
[Проблема с параметром `visible`](https://github.com/user-attachments/assets/57c9b2ae-ac19-4877-af3e-7449096f5523)  
[Ещё один пример проблемы с `visible`](https://github.com/user-attachments/assets/a26c9673-2b48-4712-9762-4a10360a990e)

---
## 4. Проблема с возвратом маркеров после `visible = false`  
При использовании `visible = false` и масштабировании карты (отдалении и приближении) маркеры могут неожиданно возвращаться в видимую область.

### Видео:  
[Рабочий кейс с `iconOpacity`](https://github.com/user-attachments/assets/a7f4724f-a605-42e9-bde6-a46db1820159)  


## У нас в балади есть еще проблема что если маркер visible = false, то соседний рядом появляется, пока на демо не могу это воспроизвести но видео прикреплю с проекта балади


https://github.com/user-attachments/assets/72ed7677-c377-42c2-9450-1432605d8195


