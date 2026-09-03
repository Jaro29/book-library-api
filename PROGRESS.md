# PROGRESS.md - rest-api-workshop (book-library-api)

## Aktualny etap
Backend: pełny CRUD + walidacja biznesowa + testy Mockito + Flyway ✅.
Frontend: pełny CRUD z UI (Angular 22, Signal Forms) + stylowanie + environments (dev/prod) ✅.
Docker: backend + frontend (nginx reverse proxy) + docker-compose (z MariaDB) ✅.
**Deployment: KOMPLETNY - HTTPS, aplikacja żyje pod https://afterword.coffe.ink ✅**
**Multi-user: KOMPLETNY - JWT, pełna izolacja danych per-user, wdrożone bezpiecznie na produkcji (68/68 książek zachowanych) ✅**
**Wyszukiwanie zewnętrzne: KOMPLETNE - BN Data (główne źródło) + Google Books (dla wydań obcojęzycznych), wsadowe dodawanie z zaznaczaniem ✅**

---

## Kontrakty endpointów (źródło prawdy dla API)

### POST /register
- Request: `RegisterRequest` (displayName, email, password min. 8 znaków)
- 409: `EmailAlreadyExistsException`, gdy email zajęty
- 201: `RegisterResponse` (id, displayName, email - bez hasła)
- Status: **zaimplementowane, przetestowane, działa na produkcji**

### POST /login
- Request: `LoginRequest` (email, password)
- 401: `InvalidCredentialsException` (ten sam komunikat dla "brak konta" i "złe hasło" - celowo, żeby nie zdradzać które adresy są zarejestrowane)
- 429: `TooManyLoginAttemptsException` - 5 nieudanych prób blokuje konto na 15 minut
- 200: `LoginResponse` (token, displayName)
- Status: **zaimplementowane, przetestowane, działa na produkcji**

### POST /books
- Request: `BookCreateRequest` (title, author, isbn, status, startDate, finishDate, timesRead, notes), query param `allowDuplicate` (bool, default false)
- 400: walidacja DTO (ISBN checksum, timesRead ujemny, FINISHED z timesRead<=0)
- 409: `DuplicateBookException` - duplikat title+author, gdy allowDuplicate=false
- 201: sukces, zwraca `BookResponse`
- Status: **zaimplementowane, przetestowane end-to-end i jednostkowo, zmergowane, działa na produkcji**

### GET /books/{id}
- 200: zwraca `BookResponse`
- 404: `BookNotFoundException`, gdy id nie istnieje
- Status: **zaimplementowane, zmergowane**

### GET /books (lista + paginacja + wyszukiwanie)
- Query params: `page` (default 0, `@Min(0)`), `pageSize` (default 20, `@Min(1)`, `@Max(100)`), `search` (opcjonalny, filtruje po title/author, case-insensitive, fragment w dowolnym miejscu; znaki `%`, `_`, `!` escapowane przez `ESCAPE '!'`)
- 400: `ConstraintViolationException` przy niepoprawnych wartościach
- Response: `PageResponse<BookResponse>` (content, page, pageSize, totalElements, totalPages)
- Status: **zaimplementowane, przetestowane (Mockito: totalPages, edge case 0 elementów), zmergowane**

### PATCH /books/{id}
- Request: `BookPatchRequest`, wszystkie pola opcjonalne; `null` = "nie zmieniaj"
- 400: walidacja ISBN/timesRead na finalnym, zmergowanym obiekcie
- 404: `BookNotFoundException`
- 409: `DuplicateBookException` - sprawdzane z pominięciem własnego id (`existsByTitleAndAuthorExcludingId`)
- 200: zwraca zaktualizowany `BookResponse`
- Status: **zaimplementowane, zmergowane**

### DELETE /books/{id}
- 204: sukces, bez body
- 404: `BookNotFoundException`
- Status: **zaimplementowane, zmergowane**

### GET /books/suggestions (podpowiedzi z zewnętrznych katalogów)
- Query params: `author` i/lub `title` (przynajmniej jeden wymagany), `source` (`bn` domyślnie, albo `google`), `lang` (używany tylko przy `source=google`)
- 400: `ResponseStatusException`, gdy ani `title` ani `author` nie podano
- 200: `List<BookSuggestion>` (title, author, isbn, coverUrl, publicationYear, publisher)
- Wymaga zalogowania (jak reszta `/books/*`) - żeby nie robić z API darmowego proxy do zewnętrznych katalogów
- Awaria/timeout zewnętrznego źródła = pusta lista + `WARN` w logach, nigdy błąd dla usera
- Status: **zaimplementowane, działa na produkcji**

### CORS
- Przeniesione z `WebMvcConfigurer` (`WebConfig`, usunięty) do `CorsConfigurationSource` bean w `SecurityConfig` - Spring Security musi znać CORS **przed** swoim filtrem, inaczej blokuje nawet poprawne żądania
- `allowedOrigins`: `http://localhost:4200`, `https://afterword.coffe.ink`, metody GET/POST/PATCH/DELETE
- Status: **skonfigurowane i zgodne z produkcją**

### Frontend - UI
- **Strona powitalna** (przed zalogowaniem): dwie kolumny - branding (nazwa AfterWord, opis, trzy punkty, notka o fleuronie) i karta z formularzem. Poniżej 860px składa się do jednej kolumny. Wcześniej był tam sam, nieostylowany formularz przyklejony do lewej górnej krawędzi, bez żadnego brandingu
- **Motyw graficzny: fleuron** (❦, drukarski ornament roślinny) - znaczniki listy i notka na stronie powitalnej, pod nagłówkiem aplikacji, w pustym stanie listy, na końcu ostatniej strony katalogu (jak ornament kończący rozdział), oraz jako favicon. Świadomie **nie** przy każdej książce ani w przyciskach - ornament działa, dopóki jest rzadki
- **Style formularzy logowania i rejestracji** w globalnym `styles.css` (klasa `.auth-form`) - oba mają identyczną strukturę, więc dublowanie w dwóch plikach komponentów nie miałoby sensu. Wcześniej `login-form.css` był **pusty**, przez co etykiety nigdy nie dostały stylu
- **Puste stany listy** z dwoma wariantami treści: inna dla pustej biblioteki (zachęta do dodania), inna dla wyszukiwania bez wyników. Paginacja **znika** przy zerowych wynikach zamiast pokazywać "Strona 1 z 0"
- `BookList`: lista + paginacja (20 pozycji na stronę), wyszukiwanie (pasek w `App`, dzielony przez `bookService.searchQuery`), delete inline z dwuetapowym potwierdzeniem i stanem "Usuwanie...", edit inline (`EditBookForm`)
- `AddBookForm` / `EditBookForm`: Signal Forms, pełny komplet pól, obsługa 409 i ogólnych błędów 400 (`generalError`), domyślny status `FINISHED`/`timesRead=1`
- Status wyświetlany jako kolorowa plakietka (`color-mix()` z istniejących zmiennych CSS), akcje edit/delete jako ikony SVG (`stroke=currentColor`, bez dodatkowej biblioteki)
- Formularz dodawania zwijany (`App.showAddForm` signal), pasek wyszukiwania + przycisk "Dodaj" w jednym wierszu
- `AuthorSearch` (osobny panel, otwierany przyciskiem "Szukaj po autorze w sieci"): checkbox przełącza źródło (BN / Google), wyniki jako siatka kart z checkboxami, "zaznacz/odznacz wszystko", "Dodaj zaznaczone (N)", etykieta źródła, podsumowanie po dodaniu. Wsadowe dodawanie przez `forkJoin` z `catchError` na każdym żądaniu (duplikat nie przerywa reszty). Dodawane pozycje dostają `FINISHED`/`timesRead=1`
- Stan współdzielony przez `BookService` (signals)
- Stylowanie: ciemny motyw "biblioteka"
- `environment.ts`/`environment.prod.ts` - `apiUrl` przełączany przez `fileReplacements` w `angular.json` (dev: `http://localhost:8080`, prod: `/api`)
- Status: **zaimplementowane, zmergowane, działa na produkcji**

### Autoryzacja - JWT + wielu użytkowników
- **Backend:** `users` (id, display_name, email UNIQUE, password hashed), `books.user_id` (FK, NOT NULL od V5). `POST /register` (hashowanie przez `PasswordEncoder`), `POST /login` (zwraca JWT). `JwtService` (jjwt 0.13.0, HMAC, userId jako subject, ważność 24h), `JwtAuthFilter` (`OncePerRequestFilter`, czyta `Bearer <token>`, ustawia `SecurityContextHolder`). `SecurityConfig`: `httpBasic`/`InMemoryUserDetailsManager` usunięte, `addFilterBefore(jwtAuthFilter, ...)`, `/register`/`/login` `permitAll`, reszta `authenticated()`
- **Izolacja danych:** `userId` wyciągany server-side przez `@AuthenticationPrincipal Long userId` (nigdy z ciała żądania) i wymagany w **każdej** metodzie `BookRepository`/`BookService`/`BookController`. `UPDATE`/`DELETE` filtrowane przez `id AND user_id` - próba modyfikacji cudzej książki po zgadniętym `id` zwraca 404, nie 403 (baza po prostu nie znajduje pasującego wiersza)
- **ISBN unikalny per-user**, nie globalnie (`UNIQUE(user_id, isbn)`, migracja V4) - różni userzy mogą mieć tę samą książkę
- **Frontend:** `AuthService` (HTTP-based `login`/`register`, `token`/`displayName` signals w `sessionStorage`), interceptor wysyła `Authorization: Bearer <token>` **i przechwytuje 401** - wygasły token czyści sesję, co automatycznie przerzuca `App` na ekran logowania. Żądania bez tokenu pomijają tę logikę, więc nieudane logowanie pokazuje własny komunikat zamiast wylogowywać. `LoginForm` (email/hasło + komunikat z backendu), `RegisterForm`, przycisk wylogowania w nagłówku
- **401 zamiast 403:** Spring Security bez `AuthenticationEntryPoint` odpowiada domyślnie **403** na żądania nieuwierzytelnione. Semantycznie błędne (403 = "zalogowany, ale bez uprawnień") i uniemożliwiało frontendowi rozpoznanie wygasłego tokenu. Naprawione przez `HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)`
- **Rate limit logowania:** `LoginRateLimiter` - 5 nieudanych prób blokuje konto na 15 minut, udane logowanie kasuje licznik. Kluczowane po **znormalizowanym emailu**, nie po IP: zagrożeniem jest zgadywanie hasła do konkretnego konta, a klucz emailowy nie da się obejść zmianą adresu, nie wymaga zaufania nagłówkom proxy i działa tak samo lokalnie (bez nginx). Zablokowane konto jest odrzucane **przed** dotknięciem bazy. Licznik w pamięci - restart aplikacji go czyści
- Status: **w pełni zaimplementowane, przetestowane end-to-end lokalnie i na produkcji**

### Wyszukiwanie zewnętrzne - BN Data + Google Books
- **BN Data (`BnDataService`)** - główne źródło, `https://data.bn.org.pl/api/institutions/bibs.json`. Bez klucza API, bez limitów, bez blokad regionalnych. Czyste wartości brane z bloku `marc` (`100$a` autor główny, `245$a` tytuł, `245$n` numer tomu, `020$a` ISBN, `260$b` wydawca), nie z płaskich pól - te są sklejone (autor + wydawca + współtwórcy, tytuł + podtytuł + seria). Filtr `language=polski` w zapytaniu **oraz** własny filtr po polu `language`. Dopasowanie autora po **wszystkich tokenach** zapytania w `100$a`, dzięki czemu `"Andrzej Sapkowski"` znajduje `"Sapkowski, Andrzej"`, a pozycje, gdzie autor napisał tylko przedmowę (pole `700`), odpadają
- **Tomy i wydania:** `245$n` doklejany do tytułu, więc "Galeony Wojny T. 1" i "T. 2" są rozróżnialne (wcześniej wyglądały na duplikaty i kolidowały ze sobą przy dodawaniu). Wyniki deduplikowane po tytule - zostaje **najstarsze wydanie**, bo kilkanaście wydań tego samego tytułu zajmowało cały limit i wypychało inne książki autora. Limit (50 pozycji ze 100 pobranych) stosowany **po** deduplikacji, więc liczy różne tytuły
- **Świadomy kompromis:** przy deduplikacji ISBN pochodzi z pierwszego wydania, niekoniecznie z egzemplarza na półce. Akceptowalne, bo roku i wydawcy i tak nie zapisujemy - służą tylko do rozróżnienia wydań na ekranie wyboru
- **Google Books (`GoogleBooksService`)** - drugie źródło, dla wydań obcojęzycznych. Wymaga `GOOGLE_BOOKS_API_KEY` (bez klucza dzielony, bardzo niski limit anonimowy). Ma okładki, których BN nie ma
- **Znane ograniczenie Google Books:** wyniki są dobierane według regionu adresu IP żądania. Z serwera Oracle zapytanie o Sapkowskiego zwraca katalog niemiecki (18 `de`, 1 `en`, 1 `pt-BR`, zero `pl`) - ani `langRestrict=pl`, ani `country=PL` tego nie zmienia. Stąd BN Data jako źródło domyślne
- Status: **zaimplementowane, wdrożone, działa na produkcji**
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
- Tylko `frontend` ma wystawiony port (80) na zewnątrz - `backend`/`mariadb` dostępne tylko wewnątrz sieci Compose
- `restart: unless-stopped` na wszystkich serwisach
- Status: **działa na produkcji od kilku dni bez przerw**

### .env
- Zmienne: `DB_ROOT_PASSWORD`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`
- Lokalny (laptop) i produkcyjny (serwer) - **osobne, różne** hasła, wygenerowane przez `openssl rand -base64 24`, zapisane w KeePassXC
- **Incydent (naprawiony):** `.env` z placeholderami przypadkiem scommitowany przez błąd w `.gitignore` (linie sklejone przez `echo >>`). Usunięty ze śledzenia, `.gitignore` naprawiony - szczegóły w `DEPLOYMENT.md`

### Infrastruktura sieciowa i dostępowa
- Publiczny IP: **141.147.39.244**, zamieniony z Ephemeral na **Reserved** (darmowe w Free Tier, nie zmieni się przy restarcie instancji)
- Domena: **https://afterword.coffe.ink** (darmowa subdomena przez FreeDNS afraid.org, rekord A)
- Dostęp SSH do serwera: osobne klucze z desktopa i laptopa, oba dodane do `authorized_keys`
- Dostęp do prywatnego repo GitHub z serwera: dedykowany **deploy key** (read-only), wygenerowany bezpośrednio na serwerze

---

## Incydent produkcyjny - naprawiony ✅
**Bug:** pusty string w polu ISBN (`''`, domyślna wartość formularza) łamał ograniczenie `UNIQUE` w MariaDB (puste stringi liczą się jako równe, w przeciwieństwie do `NULL`) - druga książka bez ISBN dawała 500.
**Fix:** `normalizeIsbn()` w `BookMapper` (pusty/blank → `null`), przetestowane lokalnie, zmergowane, wdrożone na serwer (`docker compose up -d --build backend`, ~36s), zweryfikowane na żywo.

## Incydent #2 - Certbot entrypoint (naprawiony przed wdrożeniem) ✅
**Bug:** `entrypoint` w `certbot-renew` jako zwykły string zamiast listy - Docker próbował uruchomić `trap` jako osobny program.
**Fix:** `entrypoint: ["/bin/sh", "-c", "trap exit TERM; while :; do certbot renew; sleep 12h & wait $$!; done;"]`.

## Incydent #3 - CORS po migracji HTTPS (naprawiony) ✅
**Bug:** `allowedOrigins` miał `http://afterword.coffe.ink`, ale po włączeniu HTTPS przeglądarka wysyła `Origin: https://...` - 403 na każdym żądaniu z frontendu.
**Fix:** zmiana na `https://afterword.coffe.ink` w `WebConfig`, wdrożone (`docker compose up -d --build backend`).

## Incydent #4 - brakujące zmienne APP_USERNAME/APP_PASSWORD w docker-compose.yml (naprawiony) ✅
**Bug:** `docker-compose.yml` nie przekazywał `APP_USERNAME`/`APP_PASSWORD` do kontenera `backend` - mimo ustawienia ich w `.env` na serwerze, Spring cicho używał wartości domyślnych z `application.yaml` (`admin`/`admin123`). Produkcja faktycznie działała na domyślnych danych logowania.
**Fix:** dodanie `APP_USERNAME: ${APP_USERNAME}` / `APP_PASSWORD: ${APP_PASSWORD}` do sekcji `environment` serwisu `backend`, wdrożone (`docker compose up -d`, bez przebudowy - zmiana tylko w compose).

## Incydent #5 - 502 Bad Gateway po selektywnym przebudowaniu backendu (naprawiony) ✅
**Bug:** po `docker compose up -d --build backend`, nginx (`frontend`) nadal wskazywał na stary, nieistniejący już adres IP kontenera backendu (nginx cache'uje adres przy własnym starcie, nie odświeża go automatycznie) - `POST /api/login` i inne żądania kończyły się 502.
**Fix:** `docker compose restart frontend` po każdym przebudowaniu **samego** backendu. Udokumentowane jako stały krok w procedurze wdrożenia.

## Incydent #6 - wyszukiwanie w liście książek całkowicie zepsute (naprawiony) ✅
**Bug:** poprawka z code review dodająca escapowanie wildcardów w `LIKE` użyła backslasha: `ESCAPE '\\'` w Javowym text blocku daje w SQL `ESCAPE '\'`, a MariaDB traktuje backslash jako znak ucieczki **wewnątrz literałów** - widzi więc niedomknięty string. Zapytanie się nie parsowało, `execute()` łapało wyjątek i zwracało 500. Poprawka mająca **zabezpieczyć** wyszukiwanie **rozbiła** je całkowicie.
**Fix:** zmiana znaku ucieczki na `!` (`ESCAPE '!'`), który nie wymaga podwójnego escapowania w żadnej warstwie; escapowanie w Javie wyciągnięte do wspólnej metody `toLikePattern`.
**Dlaczego przeszło niezauważone:** brak jakiegokolwiek testu dla `searchBooks`/`countBySearch` - pozycja z backlogu, której nie zrobiono. Bug przeleżał na produkcji kilka tygodni.

## Incydent #7 - `certbot-renew` nie wstaje po restarcie serwera (naprawiony) ✅
**Bug:** `certbot-renew` jako jedyny serwis w `docker-compose.yml` nie miał `restart: unless-stopped`. Po restarcie serwera (aktualizacja jądra) został w stanie `Exited (143)`, podczas gdy pozostałe wstały same. Odnawianie certyfikatu cicho przestało działać - zauważone przypadkiem, objawiłoby się dopiero wygasnięciem certyfikatu.
**Fix:** dodanie `restart: unless-stopped`, wdrożone przez `docker compose up -d`.

---

## Backlog / Deployment - WSZYSTKO ZROBIONE ✅
- [x] Instancja Ampere A1, klucz SSH, Docker na serwerze
- [x] Pełny stos zweryfikowany lokalnie i na serwerze
- [x] Prawdziwe hasła produkcyjne w `.env`
- [x] Transfer repo, uruchomienie na serwerze
- [x] Port 80 otwarty, test z zewnątrz
- [x] Domena skonfigurowana (`afterword.coffe.ink`)
- [x] Publiczny IP zamieniony na Reserved

## Backlog / Deployment - pozostałe
- [ ] `healthcheck` na MariaDB + `condition: service_healthy` w compose
- [ ] Rozważyć przejście na budowanie lokalne + rejestr obrazów, jeśli budowanie na serwerze okaże się zbyt wolne
- [ ] Pamiętać o logowaniu na FreeDNS co kilka miesięcy (inaczej subdomena może wygasnąć)

## Backlog / Migracje bazy danych
- [x] Flyway wdrożony, `V1__create_books_table.sql`
- [x] `V2__create_users_table.sql`, `V3__add_user_id_to_books.sql` (nullable na start), `V4__make_isbn_unique_per_user.sql`, `V5__make_user_id_not_null.sql`, `V6__add_cover_url_to_books.sql` - wszystkie zweryfikowane na lokalnej i produkcyjnej MariaDB
- [x] **Wzorzec bezpiecznego wdrożenia migracji łamiącej istniejące dane:** gdy nowa kolumna musi być `NOT NULL`, a stare wiersze nie mają wartości - migracja `NOT NULL` idzie **osobno, po** wdrożeniu nullable wersji i **ręcznym backfillu** danych (`UPDATE ... WHERE ... IS NULL`), nie razem z pierwszym wdrożeniem
- [ ] Kolejne zmiany schematu = nowy plik `V<n>__opis.sql`, nigdy edycja użytej migracji

## Backlog / Model Book - planowane rozszerzenia
- [x] coverUrl (V6, wypełniany z Google Books; BN Data nie ma okładek)
- [ ] dateAdded, favorite, tags
- [ ] publisher, publishYear, language, category (BN Data **zwraca** rok i wydawcę - obecnie tylko wyświetlane przy wyborze wydania, nie zapisywane)
- [ ] series, seriesNumber
- [ ] pages, duration
- [ ] ownership (enum), source

## Wielu użytkowników - zrobione ✅ (2026-08-08)
- [x] Uwierzytelnianie JWT (rejestracja + logowanie)
- [x] Jedna wspólna baza, `user_id` na `books`, `NOT NULL` po backfillu
- [x] Pełna izolacja - wszystkie metody repo/serwis/kontroler wymagają `userId`
- [x] Frontend: RegisterForm, przepisany LoginForm/AuthService/interceptor na JWT, wylogowanie
- [x] Lokalny dev przeniesiony z H2 na trwałą, lokalną MariaDB (Docker, `dev-mariadb`) - koniec niekompatybilności H2/MariaDB przy migracjach; `DevDataSeeder`/`data-dev.sql` usunięte (bez sensu bez domyślnego właściciela w świecie multi-user)
- [x] Bezpieczne, dwuetapowe wdrożenie na produkcję - 68/68 istniejących książek przypisanych do konta bez utraty danych

## Backlog / Techniczne
- [x] Testy dla `searchBooks`/`countBySearch` (fragment w tytule/autorze, case-insensitive, brak wyników, zgodność count z wynikami, izolacja per-user, regresja na wildcardy `%`/`_`)
- [x] Testy `JwtService` (round trip, token wygasły, podpisany innym sekretem, zmanipulowany, śmieci zamiast tokenu)
- [x] Testy logowania (`UserServiceImplTest`: nieistniejący email, błędne hasło, sukces, hasło nigdy nie trafia do repozytorium jawnym tekstem, konto zablokowane nie dotyka bazy)
- [x] Usuwanie: stan "w trakcie" (wyłączony przycisk + "Usuwanie...") i obsługa błędu
- [ ] Testy izolacji multi-tenant na poziomie **serwisu** - świadomie **odrzucone**: `BookServiceImpl` tylko przekazuje `userId`, więc taki test asertowałby na mockach. Realną izolację wymusza SQL i pokrywa `BookRepositoryImplTest` na prawdziwej bazie
- [ ] `IsbnValidatorTest` - przepisać na Mockito, jeśli dodane zostaną dynamiczne komunikaty błędów
- [ ] PATCH: rozróżnienie "pole pominięte" vs "pole = null" (np. `JsonNullable`) - dopiero jeśli pojawi się potrzeba
- [ ] `GlobalExceptionHandler`: rozszerzyć o kolejne przypadki, jeśli się pojawią
- [ ] Ujednolicić konwencję nazewnictwa metod serwis/repo (obecnie niespójne: część metod dodaje jawne "Book"/"Books" w serwisie, część nie)

## Znaleziska z drugiego code review - zamknięte ✅ (2026-09-02)
- [x] `.headers().disable()` usunięte - relikt po H2 Console; domyślne nagłówki (`X-Frame-Options`, `X-Content-Type-Options`, `Cache-Control`) wróciły
- [x] Obsługa 401 na froncie - interceptor czyści sesję i przerzuca na ekran logowania; backend zwraca 401 zamiast domyślnego 403
- [x] Błędy PATCH/DELETE widoczne w UI zamiast `console.error`
- [x] `DuplicateKeyException` zamiast szerokiego `DataIntegrityViolationException` - naruszenie klucza obcego czy `NOT NULL` nie udaje już "duplikatu", tylko trafia do ogólnej obsługi z pełnym stack trace w logach
- [x] `secret.getBytes(StandardCharsets.UTF_8)` w `JwtService`
- [x] Rate limit na `/login`
- [ ] Filtrowanie autora po stronie klienta dla Google Books (`inauthor:` jest nieprecyzyjne - zwraca np. książki, do których autor napisał tylko wstęp). BN Data ma to już zrobione, więc dotyczy tylko drugorzędnego źródła

## Poprawki UI - zrobione ✅ (2026-08-07)
- [x] Wyszukiwanie po tytule/autorze (backend: `search` param + frontend: pasek w `App`, live filtering, przycisk czyszczenia)
- [x] Zwijany formularz dodawania (przycisk "+ Dodaj książkę" / "Zwiń formularz")
- [x] Dwuetapowe potwierdzenie usuwania ("Usuń" → "Na pewno?" / "Tak, usuń" / "Anuluj")
- [x] Status jako kolorowa plakietka (pill badge)
- [x] Ikonki SVG zamiast tekstu na przyciskach Edytuj/Usuń
- [x] Przy okazji: domyślny status nowej książki zmieniony na `FINISHED`/`timesRead=1`, dodana ogólna obsługa błędów 400 w `AddBookForm`
- Wszystko wdrożone razem, jednym `docker compose up -d --build frontend` na serwerze, zweryfikowane na żywo

## Wyszukiwanie zewnętrzne - zrobione ✅ (2026-09-02)
- [x] `GET /books/suggestions` z przełącznikiem źródła (`bn` / `google`)
- [x] `BnDataService` - mapowanie MARC, filtr języka i autora głównego
- [x] `GoogleBooksService` - klucz API, filtr języka po stronie klienta, bezpieczny fallback na pustą listę
- [x] `AuthorSearch` - panel z checkboxami, wsadowe dodawanie, karty bibliograficzne (rok + wydawca) przy braku okładki
- [x] `coverUrl` przeprowadzony przez cały stos (V6, model, DTO, mapper, SQL, frontend)

## Następny krok
- [ ] Nowa funkcja z Backlog / Model Book (dateAdded, tagi, favorite)
- [ ] Albo pozycja z Backlog / Techniczne / Deployment (healthcheck MariaDB, CI/CD)

