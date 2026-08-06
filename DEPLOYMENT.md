# DEPLOYMENT.md — book-library-api

## Cel
Wdrożenie aplikacji (backend + frontend + MariaDB) na Oracle Cloud Free Tier, jeden VM, Docker Compose.

## Stan serwera (Oracle Cloud)
- Instancja: Ampere A1, 1 OCPU / 6 GB RAM, Ubuntu 24.04, AD-2
- Publiczny IP: 141.147.39.244 (**Reserved**, nie Ephemeral — nie zmieni się po zatrzymaniu instancji)
- Domena: **http://afterword.coffe.ink** (FreeDNS, subdomena, rekord A → 141.147.39.244)
- Dostęp: `ssh -i ~/.ssh/id_ed25519_oracle ubuntu@141.147.39.244`
- Klucz SSH: dedykowany, `~/.ssh/id_ed25519_oracle` (osobny od klucza GitHub); zarówno desktop, jak i laptop mają własny klucz dodany do `authorized_keys` na serwerze
- System: zaktualizowany (`apt update && upgrade`)
- Docker: zainstalowany (29.7.1), użytkownik `ubuntu` w grupie `docker` (bez sudo)
- Docker Compose: wbudowany jako plugin (v5.4.0)
- Docker + Docker Compose zainstalowane też **lokalnie na laptopie i desktopie** (do testów przed wysłaniem na serwer)
- GitHub: dostęp do prywatnego repo na serwerze przez dedykowany **deploy key** (tylko do odczytu, wygenerowany bezpośrednio na serwerze, dodany w Settings → Deploy keys repo)

## Zrobione ✅

### Backend
- [x] `backend/Dockerfile` (multi-stage: `maven:3.9-eclipse-temurin-25` → `eclipse-temurin:25-jre`)
- [x] Zbudowany lokalnie, uruchomiony jako pojedynczy kontener z testową MariaDB — Flyway zastosował migrację, `GET /books` → 200

### Frontend
- [x] `frontend/Dockerfile` (multi-stage: `node:22-alpine` → `nginx:alpine`)
- [x] `frontend/nginx.conf` — SPA fallback (`try_files ... /index.html`) + reverse proxy `/api/` → `http://backend:8080/`
- [x] Angular `environment.ts` (dev: `apiUrl: http://localhost:8080`) / `environment.prod.ts` (`apiUrl: /api`), przełączane przez `fileReplacements` w `angular.json`
- [x] `BookService` używa `environment.apiUrl` zamiast zaszytego na sztywno adresu

### Pełny stos — zweryfikowany lokalnie ✅
- [x] `docker-compose.yml` w korzeniu repo: serwisy `mariadb` (wolumen `mariadb-data` dla trwałości danych), `backend`, `frontend` (jedyny z wystawionym portem 80)
- [x] `.env` lokalny z wygenerowanymi hasłami (`openssl rand -base64 24`), zapisanymi w KeePassXC
- [x] `docker compose up --build` — wszystkie trzy kontenery wstają poprawnie
- [x] `curl http://localhost/api/books` → **200**

### Wdrożenie na serwer ✅
- [x] `git clone` przez dedykowany deploy key (`git config core.sshCommand`)
- [x] `.env` produkcyjny stworzony ręcznie na serwerze, hasła **inne** niż lokalne, zapisane w KeePassXC
- [x] `docker compose up -d --build` na serwerze — wszystkie trzy kontenery działają
- [x] Port 80 otwarty w Security List Oracle (Ingress, `0.0.0.0/0`, TCP, destination port 80)
- [x] Test z zewnątrz — aplikacja działa pod `http://afterword.coffe.ink`
- [x] Naprawiony i wdrożony bug produkcyjny: normalizacja pustego ISBN (`''` → `null`) — patrz `PROGRESS.md`

## Do zrobienia

- [ ] CORS w `WebConfig` — dodać `http://afterword.coffe.ink` do `allowedOrigins` (na razie działa, bo cały ruch idzie przez jeden origin/nginx, ale warto dodać jawnie zamiast polegać na tym zbiegu okoliczności)
- [ ] HTTPS (Let's Encrypt/Certbot) — teraz, gdy jest domena, to naturalny kolejny krok
- [ ] `healthcheck` na MariaDB + `condition: service_healthy` w compose (uniknięcie ewentualnego restartu backendu przy pierwszym starcie)
- [ ] Rozważyć przejście z "build na serwerze" na "build lokalnie + rejestr obrazów" (GitHub Container Registry), jeśli budowanie na serwerze okaże się zbyt wolne

## Standardowa procedura aktualizacji zdeployowanej aplikacji

Gdy zmieniasz kod i chcesz wdrożyć poprawkę na już działający serwer:

### 1. Zmiana lokalnie (desktop/laptop)
```bash
git checkout develop
git pull
# ...wprowadź zmianę w kodzie...
```

### 2. Test lokalny
Backend: `cd backend && ./mvnw test`
Frontend: build/testy odpowiednie do zmiany

### 3. Commit, PR, merge (jak zwykle)
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
- Zmiana tylko w backendzie: `docker compose up -d --build backend`
- Zmiana tylko we frontendzie: `docker compose up -d --build frontend`
- Zmiana w obu / niepewność: `docker compose up -d --build`

**Dlaczego przebudowywać selektywnie:** szybsze (np. ~36s dla samego backendu vs kilka minut dla całego stosu), i **nie dotyka** kontenera `mariadb` — dane w wolumenie `mariadb-data` pozostają nienaruszone niezależnie od tego, co przebudowujesz.

### 6. Weryfikacja
```bash
docker compose logs backend --tail=20   # albo frontend, zależnie co przebudowane
```
Szukaj: poprawny start (`Started RestApiWorkshopApplication`), oraz przy zmianie schematu: `Schema ... is up to date` lub `Successfully applied N migration(s)` — **nigdy** błędu Flyway o checksumie (oznaczałby edycję już zastosowanej migracji).

Na koniec: test na żywo w przeglądarce pod `http://afterword.coffe.ink`.

### Kluczowa zasada: dane przetrwają aktualizacje
Ponieważ `mariadb-data` to nazwany wolumen Dockera (nie część kontenera), `docker compose up -d --build` na dowolnym serwisie **nigdy** nie usuwa danych z bazy — jedyny sposób ich utraty to jawne `docker compose down -v` (flaga `-v` usuwa wolumeny) — **nigdy nie używać tej flagi na serwerze produkcyjnym**, tylko lokalnie do testów.

## Decyzje podjęte po drodze
- Budowanie obrazów: **na serwerze** (opcja A) na start
- Klucz SSH: osobny od GitHuba; osobny **deploy key** (read-only) do klonowania repo
- MariaDB: potwierdzona kompatybilność z migracją Flyway
- Adres API we frontendzie: `environment.apiUrl` (nie zaszyty na sztywno) — `/api` w prod, przekierowywane przez nginx do `backend:8080` wewnątrz sieci Compose
- Tylko `frontend` wystawiony na zewnątrz w `docker-compose.yml` — `backend`/`mariadb` osiągalne wyłącznie wewnątrz sieci Dockera
- Publiczny IP zamieniony na **Reserved** (darmowe w Free Tier, limit 1 na konto) — gwarantuje, że adres się nie zmieni
- Domena: darmowa subdomena przez FreeDNS (`afterword.coffe.ink`) zamiast płatnej własnej domeny na start

## Notatki / rzeczy do pamiętania
- `DevDataSeeder` uruchamia się **tylko** w profilu `dev` — na serwerze (`prod`) baza startuje pusta
- **`host.docker.internal` na natywnym Linuksie** wymaga `--add-host=host.docker.internal:host-gateway` przy `docker run` — dotyczyło tylko naszych **ręcznych** testów pojedynczych kontenerów; w `docker-compose.yml` ten problem nie występuje
- **`ufw` blokuje ruch z kontenerów do hosta domyślnie** — dotyczy tylko scenariusza "kontener → usługa na hoście", nie `docker-compose.yml`
- **Incydent bezpieczeństwa (naprawiony):** `.env` przez błąd w `.gitignore` (`echo ... >> .gitignore` sklejony z poprzednią linią) trafił do commita. Naprawione: `git rm --cached .env`, `.gitignore` poprawiony. Nauka: zawsze sprawdzać `git status` uważnie po zmianie `.gitignore`
- **Konflikt portu 3306 przy testach lokalnych** — jeśli masz systemową MariaDB, zmapuj testowy kontener na inny port (`-p 3307:3306`). Nie dotyczy `docker-compose.yml`
- **Oracle Cloud Free Tier — "Out of capacity" dla Ampere A1** — częsty problem, nie błąd konfiguracji. Rozwiązania: ręczne ponawianie prób (zadziałało tu), automatyzacja przez OCI Cloud Shell, albo fallback na `VM.Standard.E2.1.Micro`
- **FreeDNS wymaga logowania kilka razy w roku**, żeby konto/subdomena nie wygasły — automatyczne przypomnienia mailem, ale warto ustawić sobie osobne przypomnienie w kalendarzu