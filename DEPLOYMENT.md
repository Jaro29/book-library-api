# DEPLOYMENT.md — book-library-api

## Cel
Wdrożenie aplikacji (backend + frontend + MariaDB) na Oracle Cloud Free Tier, jeden VM, Docker Compose.

## Stan serwera (Oracle Cloud)
- Instancja: Ampere A1, 1 OCPU / 6 GB RAM, Ubuntu 24.04, AD-2
- Publiczny IP: 141.147.39.244
- Dostęp: `ssh -i ~/.ssh/id_ed25519_oracle ubuntu@141.147.39.244`
- Klucz SSH: dedykowany, `~/.ssh/id_ed25519_oracle` (osobny od klucza GitHub)
- System: zaktualizowany (`apt update && upgrade`)
- Docker: zainstalowany (29.7.1), użytkownik `ubuntu` w grupie `docker` (bez sudo)
- Docker Compose: wbudowany jako plugin (v5.4.0)
- Docker + Docker Compose zainstalowane też **lokalnie na laptopie** (do testów przed wysłaniem na serwer)

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
- [x] `.env` lokalny z placeholderami (`DB_ROOT_PASSWORD`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`)
- [x] `docker compose up --build` — wszystkie trzy kontenery wstają poprawnie (`Up`, bez restartów w pętli po pierwszym "wyścigu" backend↔mariadb)
- [x] `curl http://localhost/api/books` → **200**, `{"content":[],...}`
- [x] Frontend w przeglądarce (`http://localhost`) ładuje się i łączy z API przez nginx proxy

## Do zrobienia — kolejność

### 1. Bezpieczeństwo przed wysłaniem na serwer
- [x] Wygenerować **prawdziwe, silne** hasła do `.env` produkcyjnego (obecne to placeholdery `zmien_to_haslo`)

### 2. Konfiguracja produkcyjna
- [ ] CORS w `WebConfig` — dodać origin produkcyjny (adres/domenę, pod którą będzie dostępny frontend na Oracle)

### 3. Transfer i uruchomienie na serwerze (opcja A: build na serwerze)
- [x] `git clone` repo na serwerze
- [x] Stworzyć `.env` **na serwerze** (ręcznie, z prawdziwymi hasłami, nigdy przez git)
- [x] `docker compose up -d --build` na serwerze
- [x] Sprawdzić logi (`docker compose logs -f`) — Flyway, start aplikacji, nginx

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
Szukaj: poprawny start (`Started RestApiWorkshopApplication`), oraz przy zmianie schematu: `Schema ... is up to date` lub `Successfully applied N migration(s)` — **nigdy** błędu Flyway o checksumie (oznaczałby edycję już zastosowanej migracji, patrz zasada w sekcji "Migracje").

Na koniec: test na żywo w przeglądarce pod `http://141.147.39.244`.

### Kluczowa zasada: dane przetrwają aktualizacje
Ponieważ `mariadb-data` to nazwany wolumen Dockera (nie część kontenera), `docker compose up -d --build` na dowolnym serwisie **nigdy** nie usuwa danych z bazy — jedyny sposób ich utraty to jawne `docker compose down -v` (flaga `-v` usuwa wolumeny) — **nigdy nie używać tej flagi na serwerze produkcyjnym**, tylko lokalnie do testów.

### 4. Sieć / firewall
- [ ] Otworzyć port 80 w Security List / NSG na Oracle (backend/MariaDB **nie** wystawione na zewnątrz — tylko frontend/nginx, zgodnie z `docker-compose.yml`)
- [ ] Test z zewnątrz: `curl http://141.147.39.244` z innej maszyny (nie z samego serwera)

### 5. Później (nie teraz)
- [ ] Domena + HTTPS (Let's Encrypt/Certbot)
- [ ] `healthcheck` na MariaDB + `condition: service_healthy` w compose (uniknięcie restartu backendu przy pierwszym starcie)
- [ ] Rozważenie przejścia z "build na serwerze" na "build lokalnie + rejestr obrazów" (GitHub Container Registry), jeśli budowanie na serwerze okaże się zbyt wolne

## Decyzje podjęte po drodze
- Budowanie obrazów: **na serwerze** (opcja A) na start
- Klucz SSH: osobny od GitHuba
- MariaDB: potwierdzona kompatybilność z migracją Flyway
- Adres API we frontendzie: `environment.apiUrl` (nie zaszyty na sztywno) — `/api` w prod, przekierowywane przez nginx do `backend:8080` wewnątrz sieci Compose
- Tylko `frontend` wystawiony na zewnątrz w `docker-compose.yml` — `backend`/`mariadb` osiągalne wyłącznie wewnątrz sieci Dockera

## Notatki / rzeczy do pamiętania
- `DevDataSeeder` uruchamia się **tylko** w profilu `dev` — na serwerze (`prod`) baza startuje pusta
- **`host.docker.internal` na natywnym Linuksie** wymaga `--add-host=host.docker.internal:host-gateway` przy `docker run` — dotyczyło tylko naszych **ręcznych** testów pojedynczych kontenerów; w `docker-compose.yml` ten problem nie występuje (komunikacja przez nazwy serwisów)
- **`ufw` blokuje ruch z kontenerów do hosta domyślnie** — jeśli kontener łączy się z usługą **na hoście** (poza Dockerem), trzeba dodać `sudo ufw allow from <podsieć-dockera> to any port <port> proto tcp`. Nie dotyczy `docker-compose.yml` (wszystko w kontenerach, ta sama sieć Compose)
- **Incydent bezpieczeństwa (naprawiony):** `.env` z placeholderami przypadkiem trafił do commita — przyczyna: `echo ".env" >> .gitignore` sklejił się z poprzednią linią pliku (brak znaku nowej linii na końcu), tworząc błędny wzorzec `*.iml.env`. Naprawione: `git rm --cached .env`, `.gitignore` poprawiony na osobne linie. Placeholdery, nie prawdziwe hasła — ryzyko minimalne (repo prywatne), ale nauka na przyszłość: **zawsze sprawdzać `git status` uważnie** po zmianie `.gitignore`, nie zakładać że zadziałało
- **Konflikt portu 3306 przy testach lokalnych** — jeśli masz zainstalowaną systemową MariaDB/MySQL (sprawdź: `sudo ss -tlnp | grep 3306`), kontener testowy nie może zająć tego samego portu. Rozwiązanie: zmapuj kontener na inny port hosta (`-p 3307:3306`). Nie dotyczy `docker-compose.yml` — tam MariaDB jest osobnym kontenerem w wewnętrznej sieci, żaden port nie musi być wystawiony na hosta
- **Oracle Cloud Free Tier — "Out of capacity" dla Ampere A1** — bardzo częsty problem, nie błąd konfiguracji: popyt na darmowe zasoby ARM przewyższa podaż w wielu regionach/AD. Opcje: (1) ręczne ponawianie prób co jakiś czas — zadziałało w tym projekcie po kilku próbach na różnych AD; (2) automatyzacja przez OCI Cloud Shell z pętlą ponawiającą `oci compute instance launch`; (3) fallback na gwarantowany, ale znacznie mniejszy `VM.Standard.E2.1.Micro` (x86, 1 OCPU/1GB) jeśli czas naciska bardziej niż potrzeba pełnych zasobów