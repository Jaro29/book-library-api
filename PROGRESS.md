# PROGRESS.md — rest-api-workshop (book-library-api)

## Aktualny etap
Backend: pełny CRUD + walidacja biznesowa + testy Mockito ✅.
Frontend: pełny CRUD z UI (Angular 22, Signal Forms) + stylowanie ✅.
Sprzątanie: usunięto martwy kod (`BookUpdateRequest`, `BookStatus.ABANDONED`) ✅.
Otwarte: migracje (Flyway) i deployment (Oracle Cloud, zablokowany "Out of capacity") — do wyboru na następnej sesji.

---

## Kontrakty endpointów (źródło prawdy dla API)

### POST /books
- Request: `BookCreateRequest` (title, author, isbn, status, startDate, finishDate, timesRead, notes), query param `allowDuplicate` (bool, default false)
- 400: walidacja DTO (ISBN checksum, timesRead ujemny, FINISHED z timesRead<=0)
- 409: `DuplicateBookException` — duplikat title+author, gdy allowDuplicate=false
- 201: sukces, zwraca `BookResponse`
- Status: **zaimplementowane, przetestowane end-to-end i jednostkowo, zmergowane**

### GET /books/{id}
- 200: zwraca `BookResponse`
- 404: `BookNotFoundException`, gdy id nie istnieje
- Status: **zaimplementowane, zmergowane**

### GET /books (lista + paginacja)
- Query params: `page` (default 0, `@Min(0)`), `pageSize` (default 20, `@Min(1)`)
- 400: `ConstraintViolationException` przy niepoprawnych wartościach
- Response: `PageResponse<BookResponse>` (content, page, pageSize, totalElements, totalPages)
- Status: **zaimplementowane, przetestowane (Mockito: totalPages, edge case 0 elementów), zmergowane**

### PATCH /books/{id}
- Request: `BookPatchRequest`, wszystkie pola opcjonalne; `null` = "nie zmieniaj" (świadome uproszczenie, patrz Backlog)
- 400: walidacja ISBN/timesRead na finalnym, zmergowanym obiekcie
- 404: `BookNotFoundException`
- 409: `DuplicateBookException` — sprawdzane z pominięciem własnego id (`existsByTitleAndAuthorExcludingId`)
- 200: zwraca zaktualizowany `BookResponse`
- Status: **zaimplementowane, zmergowane** (naprawiony bug: brak `.id(...)` w mapperze powodował update z `id: null`)

### DELETE /books/{id}
- 204: sukces, bez body
- 404: `BookNotFoundException`
- Status: **zaimplementowane, zmergowane** (repo/service: `void` zamiast `boolean` — boolean nigdy realnie nie zwracał false)

### CORS
- `WebMvcConfigurer`, `/**`, allowedOrigins `http://localhost:4200`, metody GET/POST/PATCH/DELETE
- Status: **skonfigurowane** — do rozszerzenia o origin produkcyjny przy deploymencie

### Frontend — UI
- `BookList`: lista + paginacja (prev/next, zachowanie strony po add/edit/delete), delete inline, edit inline (`EditBookForm` jako dziecko via `@Input`/`@Output`)
- `AddBookForm` / `EditBookForm`: Signal Forms, pełny komplet pól (title, author, isbn, status-select, dates, timesRead z `min(0)`, notes), obsługa 409 z opcją "Dodaj mimo to"/Anuluj
- Stan współdzielony przez `BookService` (signals: `books`, `currentPage`, `totalPages`) — komponenty nie trzymają własnych kopii listy
- Stylowanie: ciemny motyw "biblioteka" (Fraunces/Lora/IBM Plex Mono, brąz/marigold/ember/szałwia), kolorowy pasek statusu z lewej strony wiersza
- Status: **zaimplementowane, zmergowane**

---

## Decyzje architektoniczne i biznesowe

### Backend — ogólne
- Raw JDBC (`NamedParameterJdbcTemplate`) zamiast JPA — celowo, pod kątem pełnej kontroli nad SQL w przyszłych projektach wymagających bezpieczeństwa
- Mapowanie DTO ↔ model: statyczne metody w `BookMapper` (nie BeanUtils/reflection)
- Błędy domenowe — trzy osobne wyjątki bez znajomości HTTP: `BookNotFoundException` (404), `DuplicateBookException` (409), `ApiException` (500, tylko dla realnych awarii infrastruktury/bazy) — `GlobalExceptionHandler` mapuje na kody HTTP
- Metody repo/service zwracające `void` zamiast `boolean`, gdy jedyna droga niesukcesu to wyjątek (delete)
- `data-dev.sql` odseparowany od kontekstu testowego (ładowany tylko przez `application-dev.yml`)
- Branch model: `main → develop → feature/<verb-noun>-<what>`, bez numeracji etapów

### Duplikaty książek
- Duplikat = ten sam tytuł i autor, case-insensitive; sprawdzany w serwisie przed create i update (update z pominięciem własnego id)
- `POST` pozwala świadomie pominąć blokadę przez `allowDuplicate=true`
- Brak `UNIQUE(title, author)` w bazie — celowe, aplikacja jednoosobowa, duplikat czasem pożądany
- ISBN nie definiuje duplikatu biznesowego — może być pusty, nie blokuje innego wydania tej samej książki

### Reguły biznesowe — rozstrzygnięte
- `TO_READ` → `finishDate` musi być null / `FINISHED` → `finishDate` nie może być null — **ODRZUCONE**. `finishDate` zawsze opcjonalne, niezależnie od statusu
- `FINISHED` wymaga `timesRead > 0`, `timesRead` nigdy ujemne — **PRZYJĘTE** i zaimplementowane (`InvalidTimesReadException`, 400)

### Sprzątanie / usunięty kod
- `BookUpdateRequest` — usunięty, pozostałość po planowanym `PUT`, który nigdy nie powstał
- `BookStatus.ABANDONED` — usunięty, nigdy nie było wpięte we frontend ani w walidację; zweryfikowane grepem w całym repo (poza `node_modules`) i w danych dev

### Frontend
- Monorepo: `backend/` + `frontend/` w jednym repo (jeden VM na Oracle Cloud = jeden docker-compose)
- Angular 22, standalone components, Signal Forms (`form()`/`FormField`/`required()`/`min()`), sygnały jako domyślny mechanizm stanu (nie Reactive Forms)
- Komunikacja rodzic-dziecko: `input.required<T>()` / `output<void>()` (nowoczesny `@Input`/`@Output`)
- `:host { display: contents; }` na komponentach-formularzach osadzonych w `<li>` — unika dodatkowego inline-wrappera
- CORS wymagany do komunikacji z `localhost:4200`

---

## Backlog / Model Book — planowane rozszerzenia
- [ ] coverUrl, dateAdded, favorite, tags (String lub encja Tag)
- [ ] publisher, publishYear, language, category
- [ ] series, seriesNumber
- [ ] pages, duration
- [ ] ownership (enum `BookOwnership`), source

## Backlog / Migracje bazy danych
- [x] Wdrożyć Flyway zamiast `schema.sql` (app + test)
- [x] `V1__create_books_table.sql` jako pierwsza migracja
- [x] Usunąć `spring.sql.init.*` i pliki `schema.sql` po wdrożeniu
- [x] `data-dev.sql` zostaje wyłącznie jako seed dla profilu `dev`
- [ ] Kolejne zmiany schematu = nowy plik `V<n>__opis.sql`, nigdy edycja użytej migracji

## Backlog / Deployment (Oracle Cloud Free Tier)
- [ ] Instancja Ampere A1 (1 OCPU/6GB, Ubuntu 24.04) — zablokowana "Out of capacity" we wszystkich AD
- [ ] Klucz SSH dedykowany (`~/.ssh/id_ed25519_oracle`) gotowy do wgrania
- [ ] Do rozstrzygnięcia: ręczne ponawianie vs skrypt w Cloud Shell
- [ ] CORS: dodać origin produkcyjny po deploymencie

## Backlog / Techniczne
- [ ] Testy service — brakuje: `updateBook` gdy książka nie istnieje → `BookNotFoundException` (propagacja z repo)
- [ ] Duplikacja try/catch w `BookRepositoryImpl` (identyczny blok w ~10 metodach) — opcje: metoda pomocnicza `execute(Supplier<T>)` vs AOP; decyzja odłożona
- [ ] `IsbnValidatorTest` — jeśli dodane zostaną dynamiczne komunikaty błędów w walidatorach, trzeba przepisać na Mockito
- [ ] PATCH: rozróżnienie "pole pominięte" vs "pole = null" (np. `JsonNullable`) — dopiero jeśli pojawi się potrzeba czyszczenia pól
- [ ] `GlobalExceptionHandler`: rozszerzyć o kolejne przypadki, jeśli się pojawią

## Backlog / Techniczne
- [x] Naprawiono zepsute testy frontendowe: martwy saveEdit() w BookList, zła nazwa klasy w book.spec.ts, nieaktualny placeholder w app.spec.ts, brakujący required input w EditBookForm spec, testy komponentów strzelające do prawdziwego backendu (dodano provideHttpClientTesting())

## Następny krok
- [ ] Decyzja: Flyway teraz, czy wracamy do deploymentu na Oracle Cloud?