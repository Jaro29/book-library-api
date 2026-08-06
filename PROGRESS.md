# PROGRESS.md — rest-api-workshop (book-library-api)

## Aktualny etap
Backend: pełny CRUD + walidacja biznesowa + testy Mockito + Flyway ✅.
Frontend: pełny CRUD z UI (Angular 22, Signal Forms) + stylowanie + environments (dev/prod) ✅.
Docker: backend + frontend (nginx reverse proxy) + docker-compose (z MariaDB) ✅.
**Deployment: KOMPLETNY — HTTPS, aplikacja żyje pod https://afterword.coffe.ink ✅**

---

## Kontrakty endpointów (źródło prawdy dla API)

### POST /books
- Request: `BookCreateRequest` (title, author, isbn, status, startDate, finishDate, timesRead, notes), query param `allowDuplicate` (bool, default false)
- 400: walidacja DTO (ISBN checksum, timesRead ujemny, FINISHED z timesRead<=0)
- 409: `DuplicateBookException` — duplikat title+author, gdy allowDuplicate=false
- 201: sukces, zwraca `BookResponse`
- Status: **zaimplementowane, przetestowane end-to-end i jednostkowo, zmergowane, działa na produkcji**

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
- `WebMvcConfigurer`, `/**`, allowedOrigins `http://localhost:4200`, `https://afterword.coffe.ink`, metody GET/POST/PATCH/DELETE
- Status: **skonfigurowane i zgodne z produkcją (HTTPS)** — naprawiony bug: pierwotny wpis miał `http://`, po migracji na HTTPS dawał 403 (Origin się nie zgadzał)

### Frontend — UI
- `BookList`: lista + paginacja, delete inline, edit inline (`EditBookForm`)
- `AddBookForm` / `EditBookForm`: Signal Forms, pełny komplet pól, obsługa 409
- Stan współdzielony przez `BookService` (signals)
- Stylowanie: ciemny motyw "biblioteka"
- `environment.ts`/`environment.prod.ts` — `apiUrl` przełączany przez `fileReplacements` w `angular.json` (dev: `http://localhost:8080`, prod: `/api`)
- Status: **zaimplementowane, zmergowane, działa na produkcji**

---

## Docker / Infrastruktura

### backend/Dockerfile
- Multi-stage: `maven:3.9-eclipse-temurin-25` (build) → `eclipse-temurin:25-jre` (finalny)
- Status: **zbudowany, przetestowany, działa na serwerze produkcyjnym**

### frontend/Dockerfile + nginx.conf
- Multi-stage: `node:22-alpine` (build) → `nginx:alpine` (serwowanie statycznych plików)
- `nginx.conf`: `location /` → SPA fallback (`try_files ... /index.html`); `location /api/` → `proxy_pass http://backend:8080/`
- Status: **zbudowany, przetestowany, działa na serwerze produkcyjnym**

### docker-compose.yml
- Trzy serwisy: `mariadb` (z nazwanym wolumenem `mariadb-data` dla trwałości danych), `backend`, `frontend`
- Tylko `frontend` ma wystawiony port (80) na zewnątrz — `backend`/`mariadb` dostępne tylko wewnątrz sieci Compose
- `restart: unless-stopped` na wszystkich serwisach
- Status: **działa na produkcji od kilku dni bez przerw**

### .env
- Zmienne: `DB_ROOT_PASSWORD`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`
- Lokalny (laptop) i produkcyjny (serwer) — **osobne, różne** hasła, wygenerowane przez `openssl rand -base64 24`, zapisane w KeePassXC
- **Incydent (naprawiony):** `.env` z placeholderami przypadkiem scommitowany przez błąd w `.gitignore` (linie sklejone przez `echo >>`). Usunięty ze śledzenia, `.gitignore` naprawiony — szczegóły w `DEPLOYMENT.md`

### Infrastruktura sieciowa i dostępowa
- Publiczny IP: **141.147.39.244**, zamieniony z Ephemeral na **Reserved** (darmowe w Free Tier, nie zmieni się przy restarcie instancji)
- Domena: **http://afterword.coffe.ink** (darmowa subdomena przez FreeDNS afraid.org, rekord A)
- Dostęp SSH do serwera: osobne klucze z desktopa i laptopa, oba dodane do `authorized_keys`
- Dostęp do prywatnego repo GitHub z serwera: dedykowany **deploy key** (read-only), wygenerowany bezpośrednio na serwerze

---

## Incydent produkcyjny — naprawiony ✅
**Bug:** pusty string w polu ISBN (`''`, domyślna wartość formularza) łamał ograniczenie `UNIQUE` w MariaDB (puste stringi liczą się jako równe, w przeciwieństwie do `NULL`) — druga książka bez ISBN dawała 500.
**Fix:** `normalizeIsbn()` w `BookMapper` (pusty/blank → `null`), przetestowane lokalnie, zmergowane, wdrożone na serwer (`docker compose up -d --build backend`, ~36s), zweryfikowane na żywo.

## Incydent #2 — Certbot entrypoint (naprawiony przed wdrożeniem) ✅
**Bug:** `entrypoint` w `certbot-renew` jako zwykły string zamiast listy — Docker próbował uruchomić `trap` jako osobny program.
**Fix:** `entrypoint: ["/bin/sh", "-c", "trap exit TERM; while :; do certbot renew; sleep 12h & wait $$!; done;"]`.

## Incydent #3 — CORS po migracji HTTPS (naprawiony) ✅
**Bug:** `allowedOrigins` miał `http://afterword.coffe.ink`, ale po włączeniu HTTPS przeglądarka wysyła `Origin: https://...` — 403 na każdym żądaniu z frontendu.
**Fix:** zmiana na `https://afterword.coffe.ink` w `WebConfig`, wdrożone (`docker compose up -d --build backend`).

---

## Backlog / Deployment — WSZYSTKO ZROBIONE ✅
- [x] Instancja Ampere A1, klucz SSH, Docker na serwerze
- [x] Pełny stos zweryfikowany lokalnie i na serwerze
- [x] Prawdziwe hasła produkcyjne w `.env`
- [x] Transfer repo, uruchomienie na serwerze
- [x] Port 80 otwarty, test z zewnątrz
- [x] Domena skonfigurowana (`afterword.coffe.ink`)
- [x] Publiczny IP zamieniony na Reserved

## Backlog / Deployment — pozostałe
- [ ] `healthcheck` na MariaDB + `condition: service_healthy` w compose
- [ ] Rozważyć przejście na budowanie lokalne + rejestr obrazów, jeśli budowanie na serwerze okaże się zbyt wolne
- [ ] Pamiętać o logowaniu na FreeDNS co kilka miesięcy (inaczej subdomena może wygasnąć)

## Backlog / Migracje bazy danych
- [x] Flyway wdrożony, `V1__create_books_table.sql`, zweryfikowany na H2 i prawdziwej MariaDB
- [ ] Kolejne zmiany schematu = nowy plik `V<n>__opis.sql`, nigdy edycja użytej migracji

## Backlog / Model Book — planowane rozszerzenia
- [ ] coverUrl, dateAdded, favorite, tags
- [ ] publisher, publishYear, language, category
- [ ] series, seriesNumber
- [ ] pages, duration
- [ ] ownership (enum), source

## Backlog / Wielu użytkowników (duża zmiana architektoniczna, na przyszłość)
- [ ] Uwierzytelnianie — logowanie/rejestracja (Spring Security)
- [ ] Decyzja podjęta: **jedna, wspólna baza**, nowa kolumna `user_id` na `books` (nowa migracja Flyway) — nie osobna baza per user (zbyt złożone jak na potrzeby tej aplikacji: dynamiczny routing datasource, migracje per-tenant)
- [ ] Wszystkie metody repozytorium będą wymagały jawnego parametru `userId` (spójne z istniejącym stylem raw JDBC/named params)
- [ ] To wymaga osobnego planowania (mini-spec, jak przy każdym endpoincie) — nie robić przy okazji mniejszych poprawek

## Backlog / Techniczne
- [ ] `IsbnValidatorTest` — przepisać na Mockito, jeśli dodane zostaną dynamiczne komunikaty błędów
- [ ] PATCH: rozróżnienie "pole pominięte" vs "pole = null" (np. `JsonNullable`) — dopiero jeśli pojawi się potrzeba
- [ ] `GlobalExceptionHandler`: rozszerzyć o kolejne przypadki, jeśli się pojawią
- [ ] Testy dla `searchBooks`/`countBySearch` w `BookRepositoryImplTest` (fragment w tytule/autorze, case-insensitive, brak wyników, zgodność count z wynikami)
- [ ] Ujednolicić konwencję nazewnictwa metod serwis/repo (obecnie niespójne: część metod dodaje jawne "Book"/"Books" w serwisie, część nie)

## Następny krok
- [ ] Poprawki UI (kolejność): 4) wyszukiwarka + zwijany formularz dodawania, 1) potwierdzenie usuwania, 2) status jako kolorowa plakietka, 3) ikonki na przyciskach Edytuj/Usuń — wdrożenie razem, po kilku zmianach, nie po każdej osobno
