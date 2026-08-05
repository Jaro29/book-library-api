# PROGRESS.md — rest-api-workshop (book-library-api)

## Aktualny etap
Backend: pełny CRUD + walidacja biznesowa + testy Mockito + Flyway ✅.
Frontend: pełny CRUD z UI (Angular 22, Signal Forms) + stylowanie + environments (dev/prod) ✅.
Docker: backend + frontend (nginx reverse proxy) + docker-compose (z MariaDB) — **zweryfikowane end-to-end lokalnie** ✅.
Deployment: instancja Oracle Cloud żyje, SSH działa, Docker zainstalowany na serwerze — **transfer i uruchomienie na serwerze jeszcze do zrobienia**.

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
- Request: `BookPatchRequest`, wszystkie pola opcjonalne; `null` = "nie zmieniaj"
- 400: walidacja ISBN/timesRead na finalnym, zmergowanym obiekcie
- 404: `BookNotFoundException`
- 409: `DuplicateBookException` — sprawdzane z pominięciem własnego id (`existsByTitleAndAuthorExcludingId`)
- 200: zwraca zaktualizowany `BookResponse`
- Status: **zaimplementowane, zmergowane**

### DELETE /books/{id}
- 204: sukces, bez body
- 404: `BookNotFoundException`
- Status: **zaimplementowane, zmergowane**

### CORS
- `WebMvcConfigurer`, `/**`, allowedOrigins `http://localhost:4200`, metody GET/POST/PATCH/DELETE
- Status: **skonfigurowane** — do rozszerzenia o origin produkcyjny przy finalnym deploymencie

### Frontend — UI
- `BookList`: lista + paginacja, delete inline, edit inline (`EditBookForm`)
- `AddBookForm` / `EditBookForm`: Signal Forms, pełny komplet pól, obsługa 409
- Stan współdzielony przez `BookService` (signals)
- Stylowanie: ciemny motyw "biblioteka"
- `environment.ts`/`environment.prod.ts` — `apiUrl` przełączany przez `fileReplacements` w `angular.json` (dev: `http://localhost:8080`, prod: `/api`)
- Status: **zaimplementowane, zmergowane**

---

## Docker / Infrastruktura

### backend/Dockerfile
- Multi-stage: `maven:3.9-eclipse-temurin-25` (build) → `eclipse-temurin:25-jre` (finalny)
- Status: **zbudowany, przetestowany jako pojedynczy kontener z MariaDB (test), zweryfikowany w docker-compose**

### frontend/Dockerfile + nginx.conf
- Multi-stage: `node:22-alpine` (build) → `nginx:alpine` (serwowanie statycznych plików)
- `nginx.conf`: `location /` → SPA fallback (`try_files ... /index.html`); `location /api/` → `proxy_pass http://backend:8080/`
- Status: **zbudowany, przetestowany, reverse proxy działa poprawnie w docker-compose**

### docker-compose.yml
- Trzy serwisy: `mariadb` (z nazwanym wolumenem `mariadb-data` dla trwałości danych), `backend`, `frontend`
- Tylko `frontend` ma wystawiony port (80) na zewnątrz — `backend`/`mariadb` dostępne tylko wewnątrz sieci Compose
- `restart: unless-stopped` na wszystkich serwisach — backend automatycznie restartuje się, jeśli MariaDB nie zdąży wystartować pierwsza (znany "wyścig" przy pierwszym starcie, nieblokujący)
- Status: **zweryfikowany end-to-end lokalnie — `GET http://localhost/api/books` zwraca 200, cały stos żyje i się komunikuje**

### .env
- Zmienne: `DB_ROOT_PASSWORD`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`
- **WAŻNE — incydent i naprawa:** `.env` z placeholderami (`zmien_to_haslo`) został przypadkiem scommitowany (błąd w `.gitignore` — dwie linie sklejone przez `echo >>` bez nowej linii na końcu pliku). Usunięty ze śledzenia (`git rm --cached`), `.gitignore` naprawiony. Placeholdery, nie prawdziwe hasła — ryzyko minimalne, repo prywatne, ale **prawdziwe hasła produkcyjne trzeba wygenerować od nowa, nigdy nie commitować**

---

## Backlog / Deployment (Oracle Cloud Free Tier) — plan w DEPLOYMENT.md

- [x] Instancja Ampere A1 (1 OCPU/6GB, Ubuntu 24.04, AD-2) — utworzona po kilku próbach ("Out of capacity" — częsty problem Free Tier)
- [x] Klucz SSH dedykowany, dostęp do serwera potwierdzony
- [x] System zaktualizowany, Docker + Docker Compose zainstalowane na serwerze
- [x] Docker + Docker Compose zainstalowane też na laptopie (do lokalnych testów)
- [x] Cały stos (`docker compose up --build`) zweryfikowany lokalnie — działa poprawnie
- [ ] Wygenerować prawdziwe, silne hasła do `.env` (produkcyjne)
- [ ] Transfer repo na serwer (`git clone`), `.env` stworzony ręcznie na serwerze
- [ ] `docker compose up -d --build` na serwerze
- [ ] Otworzyć port 80 w Security List / NSG na Oracle
- [ ] Test z zewnątrz (z innej maszyny niż serwer)
- [ ] CORS: dodać origin produkcyjny w `WebConfig`
- [ ] Później: domena + HTTPS, rozważenie przejścia na budowanie lokalne + rejestr obrazów (jeśli budowanie na serwerze okaże się zbyt wolne)

## Backlog / Migracje bazy danych
- [x] Flyway wdrożony, `V1__create_books_table.sql`, zweryfikowany na H2 i prawdziwej MariaDB
- [ ] Kolejne zmiany schematu = nowy plik `V<n>__opis.sql`, nigdy edycja użytej migracji

## Backlog / Model Book — planowane rozszerzenia
- [ ] coverUrl, dateAdded, favorite, tags
- [ ] publisher, publishYear, language, category
- [ ] series, seriesNumber
- [ ] pages, duration
- [ ] ownership (enum), source

## Backlog / Techniczne
- [ ] `IsbnValidatorTest` — przepisać na Mockito, jeśli dodane zostaną dynamiczne komunikaty błędów
- [ ] PATCH: rozróżnienie "pole pominięte" vs "pole = null" (np. `JsonNullable`) — dopiero jeśli pojawi się potrzeba
- [ ] `GlobalExceptionHandler`: rozszerzyć o kolejne przypadki, jeśli się pojawią
- [ ] docker-compose: rozważyć `healthcheck` na MariaDB + `condition: service_healthy`, żeby uniknąć restartu backendu przy pierwszym starcie

## Następny krok
- [ ] Deployment na serwer Oracle (transfer, `.env` produkcyjny, uruchomienie, firewall, test z zewnątrz)