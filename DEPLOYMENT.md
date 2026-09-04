# DEPLOYMENT.md - book-library-api

## Cel
Wdrożenie aplikacji (backend + frontend + MariaDB) na Oracle Cloud Free Tier, jeden VM, Docker Compose.

## Stan serwera (Oracle Cloud)
- Instancja: Ampere A1, 1 OCPU / 6 GB RAM, Ubuntu 24.04, AD-2
- Publiczny IP: 141.147.39.244 (**Reserved**, nie Ephemeral)
- Domena: **https://afterword.coffe.ink** (FreeDNS, rekord A → 141.147.39.244)
- Dostęp: `ssh -i ~/.ssh/id_ed25519_oracle ubuntu@141.147.39.244`
- Klucz SSH: dedykowany, `~/.ssh/id_ed25519_oracle`; desktop i laptop mają osobne klucze w `authorized_keys`
- System: zaktualizowany, Docker 29.7.1, Docker Compose v5.4.0 (plugin)
- Docker + Docker Compose zainstalowane też **lokalnie na laptopie i desktopie**
- GitHub: dostęp do prywatnego repo na serwerze przez dedykowany **deploy key** (read-only)

## Zrobione ✅

### Backend
- [x] `backend/Dockerfile` (multi-stage: `maven:3.9-eclipse-temurin-25` → `eclipse-temurin:25-jre`)

### Frontend
- [x] `frontend/Dockerfile` (multi-stage: `node:22-alpine` → `nginx:alpine`)
- [x] `frontend/nginx.conf` - SPA fallback, reverse proxy `/api/` → `http://backend:8080/`
- [x] Angular `environment.ts`/`environment.prod.ts`, `BookService` używa `environment.apiUrl`

### Pełny stos ✅
- [x] `docker-compose.yml`: `mariadb` (wolumen `mariadb-data`), `backend`, `frontend`, `certbot`, `certbot-renew`
- [x] `.env` lokalny i produkcyjny - osobne, wygenerowane hasła (`openssl rand -base64 24`), zapisane w KeePassXC; zmienne: `DB_ROOT_PASSWORD`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`

### Wdrożenie na serwer ✅
- [x] `git clone` przez deploy key
- [x] `docker compose up -d --build` - wszystkie kontenery działają
- [x] Port 80 otwarty w Security List Oracle
- [x] Naprawiony i wdrożony bug: normalizacja pustego ISBN (`''` → `null`)

### HTTPS ✅
- [x] Serwis `certbot` w `docker-compose.yml`, wolumeny `certbot-etc`/`certbot-www` współdzielone z `frontend`
- [x] `nginx.conf`: `location /.well-known/acme-challenge/` dodana **przed** wdrożeniem certyfikatu (Etap A)
- [x] Certyfikat wygenerowany: `docker compose run --rm certbot certonly --webroot -w /var/www/certbot -d afterword.coffe.ink --email ... --agree-tos --no-eff-email` - ważny do **2026-11-04**
- [x] `nginx.conf` (Etap B): drugi `server` blok na porcie 443 z `ssl_certificate`/`ssl_certificate_key`; port 80 przekierowuje (`301`) na HTTPS, poza ścieżką ACME
- [x] Port 443 otwarty w Security List Oracle (ten sam wzorzec co port 80)
- [x] Serwis `certbot-renew` - pętla `certbot renew` co 12h (odnawia tylko, gdy zostało <30 dni do wygaśnięcia)
- [x] CORS: `allowedOrigins` zaktualizowane z `http://` na **`https://afterword.coffe.ink`** - HTTPS zmienia `Origin`, więc stary wpis przestał pasować

### Autoryzacja ✅
- [x] Spring Security + JWT (nie Basic Auth) - rejestracja, logowanie, per-user izolacja danych
- [x] CORS w `SecurityConfig` (`CorsConfigurationSource` bean)
- [x] Frontend: `AuthService`, `LoginForm`, `RegisterForm`, interceptor z `Bearer <token>`
- [x] Produkcyjne dane (68 książek) bezpiecznie zmigrowane do modelu multi-user, zero strat
- [x] Rate limit logowania (5 prób na parę email+IP, 20 na sam email, okno 15 minut). **Licznik żyje w pamięci aplikacji** - `docker compose up -d --build backend` kasuje wszystkie blokady, a przy skalowaniu na wiele instancji każda liczyłaby osobno. Przy jednej instancji bez znaczenia; przy więcej niż jednej trzeba by współdzielonego magazynu (Redis)
- [x] Adres IP klienta pobierany z nagłówka `X-Real-IP` ustawianego przez nginx - **rate limit działa poprawnie tylko za proxy**. Przy bezpośrednim wystawieniu backendu wszyscy użytkownicy wyglądaliby jak jeden adres

### Wyszukiwanie zewnętrzne ✅
- [x] BN Data (`data.bn.org.pl`) jako główne źródło - bez klucza API, bez limitów, bez blokad regionalnych, więc **nie wymaga żadnej konfiguracji na serwerze**
- [x] Google Books usunięte (2026-09-04) - zwracało z tego serwera katalog niemiecki, więc było bezużyteczne. `GOOGLE_BOOKS_API_KEY` usunięty z produkcyjnego `.env`, a sam klucz unieważniony w Google Cloud Console
- [x] BN Data ma timeout 3s i zwraca pustą listę przy awarii - niedostępność zewnętrznego katalogu nigdy nie psuje aplikacji

## Lokalne środowisko deweloperskie
- Zamiast H2 (in-memory), lokalny development używa **trwałej** MariaDB w Dockerze (`dev-mariadb`, port 3307) - eliminuje powtarzające się niekompatybilności H2/MariaDB przy migracjach (składnia `ALTER TABLE`, nazwy tabel systemowych, automatyczne nazewnictwo ograniczeń)
- `application-dev.yml` wskazuje na tę bazę zamiast H2
- Testy `@JdbcTest` też skonfigurowane na realną MariaDB przez `@AutoConfigureTestDatabase(replace = Replace.NONE)`, dla spójności z produkcją

## Do zrobienia
- [ ] `healthcheck` na MariaDB + `condition: service_healthy` w compose
- [ ] Rozważyć budowanie lokalne + rejestr obrazów, jeśli budowanie na serwerze okaże się zbyt wolne
- [ ] CI/CD: automatyczne wdrażanie przez GitHub Actions (trigger na push do `develop`, SSH do serwera przez sekret, `git pull` + `docker compose up -d --build`) - obecnie proces w pełni ręczny

## Standardowa procedura aktualizacji zdeployowanej aplikacji

### 1. Zmiana lokalnie
```bash
git checkout develop
git pull
```

### 2. Test lokalny
Backend: `cd backend && ./mvnw test`

### 3. Commit, PR, merge
```bash
git checkout -b fix/<opis>
git add .
git commit -m "..."
git push -u origin fix/<opis>
gh pr create --base develop --head fix/<opis> --title "..." --body "..."
gh pr merge --squash --delete-branch
```

### 4. Wdrożenie na serwer
```bash
ssh -i ~/.ssh/id_ed25519_oracle ubuntu@141.147.39.244
cd book-library-api
git pull
```

### 5. Przebuduj tylko to, co się zmieniło
- Backend: `docker compose up -d --build backend`, **potem** `docker compose restart frontend` (nginx cache'uje adres IP backendu przy własnym starcie - bez restartu dostaniesz 502 Bad Gateway po każdej przebudowie samego backendu)
- Frontend: `docker compose up -d --build frontend`
- Zmiana w `docker-compose.yml` (nowy serwis, nowy port): pełne `docker compose up -d --build` (albo precyzyjnie: `docker compose up -d --build <nowe-serwisy>`, jeśli chcesz uniknąć dotykania niezmienionych)
- Zmiana **tylko** w `docker-compose.yml` bez zmiany kodu: `docker compose up -d` (bez `--build`)

### 6. Weryfikacja
```bash
docker compose logs backend --tail=20
docker compose ps
```
Test na żywo: `https://afterword.coffe.ink`

### Kluczowa zasada: dane przetrwają aktualizacje
`mariadb-data` to nazwany wolumen - `docker compose up -d --build` nigdy go nie usuwa. Jedyny sposób utraty danych: `docker compose down -v` - **nigdy na serwerze produkcyjnym**.

## Aktualizacja systemu na serwerze

```bash
sudo apt update && sudo apt upgrade -y
sudo reboot
```

Przy pytaniu o pliki konfiguracyjne (np. `sshd_config`) wybrać **"keep the local version currently installed"** - inaczej można nadpisać ustawienia SSH, przez które trwa połączenie.

Po restarcie **zawsze sprawdzić `docker compose ps -a`**, nie tylko `docker compose ps`. Kontenery bez `restart: unless-stopped` **nie wstaną same** i będą widoczne tylko z flagą `-a` (tak właśnie wyszło na jaw, że `certbot-renew` nie miał tej polityki - patrz Incydent #7 w `PROGRESS.md`).

## Decyzje podjęte po drodze
- Budowanie obrazów: na serwerze (opcja A)
- Klucz SSH osobny od GitHuba; osobny deploy key (read-only) do klonowania
- Publiczny IP: Reserved (darmowe, limit 1/konto)
- Domena: darmowa subdomena FreeDNS zamiast płatnej własnej domeny na start
- HTTPS: Let's Encrypt/Certbot, metoda `webroot`, wdrożone w dwóch etapach (najpierw ścieżka weryfikacji, potem sam SSL) - bo Certbot potrzebuje działającego HTTP, zanim może wystawić certyfikat dla HTTPS

## Notatki / rzeczy do pamiętania
- `DevDataSeeder`/`data-dev.sql` **usunięte** (2026-08-08) - bez sensu w świecie multi-user (brak domyślnego właściciela); dane testowe dodaje się teraz przez zarejestrowanie konta i UI
- `host.docker.internal` na natywnym Linuksie wymaga `--add-host=host.docker.internal:host-gateway` (dotyczy tylko ręcznych testów pojedynczych kontenerów)
- `ufw` blokuje domyślnie ruch z kontenerów do hosta (dotyczy tylko scenariusza kontener→host, nie `docker-compose.yml`)
- **Incydent `.env`:** przypadkiem scommitowany przez błąd w `.gitignore` (`echo >>` sklejony z poprzednią linią). Naprawione, `git status` warto sprawdzać uważnie po zmianie `.gitignore`
- Konflikt portu 3306 przy testach lokalnych z systemową MariaDB - użyj innego portu hosta (`-p 3307:3306`)
- Oracle "Out of capacity" dla Ampere A1 - częsty, nie błąd konfiguracji; ręczne ponawianie zwykle wystarcza
- FreeDNS wymaga logowania kilka razy w roku, żeby subdomena nie wygasła
- **Docker Compose `entrypoint` jako string vs lista:** zwykły string w `entrypoint` jest dzielony na "słowa" i pierwsze traktowane jako nazwa programu - polecenia powłoki (`trap`, `while`) trzeba jawnie owinąć: `["/bin/sh", "-c", "..."]`. W tej samej składni `$$!` (nie `$${!}`) daje poprawny, dosłowny `$!` wewnątrz kontenera
- **CORS a HTTPS:** `allowedOrigins` porównuje **cały** origin, łącznie z protokołem - `http://` i `https://` to dwa różne originy. Po migracji na HTTPS trzeba zaktualizować `WebConfig`, inaczej wszystkie żądania z frontendu dostają 403 "Invalid CORS request"
- **Basic Auth w SPA wymaga własnej obsługi** - natywne okienko przeglądarki działa tylko przy nawigacji, nie przy AJAX - SPA musi ręcznie doklejać nagłówek `Authorization` (interceptor)
- **Nowe zmienne env w `docker-compose.yml` trzeba jawnie dopisać w `environment:`** - samo dodanie do `.env` nie wystarczy, `.env` tylko dostarcza wartości dla `${...}` **już użytych** w compose
- **502 Bad Gateway po `docker compose up -d --build backend`** - nginx nie odświeża automatycznie adresu IP kontenera backendu. Zawsze `docker compose restart frontend` po samodzielnej przebudowie backendu
- **Bezpieczne wdrażanie migracji NOT NULL na istniejące dane:** rozbij na dwa wdrożenia - (1) nullable kolumna + kod aplikacji, (2) backfill danych przez `UPDATE`, (3) osobna migracja `NOT NULL`, dopiero gdy backfill potwierdzony (np. `SELECT COUNT(*) vs COUNT(kolumna)`)
- **`restart: unless-stopped` na każdym serwisie, który ma działać ciągle** - brak tej polityki przy `certbot-renew` sprawił, że po restarcie serwera odnawianie certyfikatu cicho przestało działać. Awaria tego typu nie daje żadnego sygnału aż do momentu, gdy jest już za późno
- **Zewnętrzne API może zwracać inne dane w zależności od adresu IP serwera** - Google Books z serwera Oracle (Niemcy) zwraca katalog niemiecki, mimo `langRestrict=pl` i `country=PL`. Co działa lokalnie, nie musi działać na produkcji - warto testować zewnętrzne integracje **z serwera**, nie tylko z lokalnej maszyny