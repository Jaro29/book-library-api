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

## Zrobione ✅
- [x] Instancja VM utworzona i dostępna przez SSH
- [x] Docker + Docker Compose zainstalowane na serwerze
- [x] `backend/Dockerfile` napisany (multi-stage: maven build → eclipse-temurin JRE)
- [x] Zweryfikowano lokalnie: Flyway migracja (`V1__create_books_table.sql`) działa poprawnie na prawdziwej MariaDB (test przez `docker run mariadb:11`, port 3307, profil `prod`)

## Do zrobienia — kolejność

### 1. Lokalne testy Dockera (przed wysłaniem na serwer)
- [ ] `docker build -t book-library-backend ./backend` — zbudować i sprawdzić obraz backendu
- [ ] `frontend/Dockerfile` — multi-stage (node build → nginx serwujący pliki statyczne)
- [ ] `docker build -t book-library-frontend ./frontend` — zbudować i sprawdzić obraz frontendu
- [ ] `docker-compose.yml` w korzeniu repo — spina backend + frontend + MariaDB
- [ ] `.env` (lokalnie, **w `.gitignore`**) — hasła do bazy, sekrety
- [ ] `docker compose up` lokalnie — sprawdzić, że cała trójka działa razem

### 2. Konfiguracja produkcyjna
- [ ] Sprawdzić/dopasować `application-prod.yml` do nazw serwisów z `docker-compose.yml` (np. `jdbc:mariadb://mariadb:3306/bookdb`, host = nazwa serwisu, nie `localhost`)
- [ ] CORS w `WebConfig` — dodać origin produkcyjny (adres/domenę, pod którą będzie dostępny frontend)

### 3. Transfer i uruchomienie na serwerze (opcja A: build na serwerze)
- [ ] `git clone` repo na serwerze
- [ ] Stworzyć `.env` **na serwerze** (osobno, nigdy nie przez git)
- [ ] `docker compose up -d --build` na serwerze
- [ ] Sprawdzić logi (`docker compose logs -f`) — Flyway, start aplikacji

### 4. Sieć / firewall
- [ ] Otworzyć port 80 (frontend) w Security List / NSG na Oracle
- [ ] Zdecydować: czy port 8080 (backend) ma być publicznie dostępny, czy tylko przez frontend/nginx proxy
- [ ] Test z zewnątrz: `curl http://141.147.39.244` z innej maszyny (nie z samego serwera)

### 5. Później (nie teraz)
- [ ] Domena + HTTPS (np. Let's Encrypt/Certbot)
- [ ] Rozważenie przejścia z "build na serwerze" na "build lokalnie + rejestr obrazów" (GitHub Container Registry), jeśli budowanie na serwerze okaże się zbyt wolne

## Decyzje podjęte po drodze
- Budowanie obrazów: **na serwerze** (opcja A) na start — prostsze, można przejść na rejestr obrazów później bez zmian w Dockerfile
- Klucz SSH: osobny od GitHuba (dobra praktyka bezpieczeństwa)
- MariaDB: potwierdzona kompatybilność z istniejącą migracją Flyway — brak potrzeby poprawek SQL

## Notatki / rzeczy do pamiętania
- Test lokalny robiliśmy na porcie **3307** (bo systemowa MariaDB na laptopie zajmowała 3306) — w `docker-compose.yml` **nie** będzie tego konfliktu, bo kontenery mają własną sieć
- `DevDataSeeder` (dane testowe) uruchamia się **tylko** w profilu `dev` — na serwerze (`prod`) baza startuje pusta, trzeba będzie ręcznie dodać pierwsze książki przez UI/API