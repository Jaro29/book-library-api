# CONVENTIONS.md - konwencje projektu AfterWord

Spisane po fakcie, na podstawie tego, co faktycznie stosowaliśmy przez cały projekt. Nie jest to formalny standard branżowy, tylko nasz własny, spójny sposób pracy - i to jest jego zaletą: nie wymaga dodatkowej dyscypliny, bo już tak pracujemy.

---

## Gałęzie

```
main → develop → <typ>/<opis>
```

`main` to stan wdrożony, `develop` to gałąź integracyjna, praca idzie na gałęziach tematycznych odbijanych od `develop`.

### Prefiksy

- `feature/` - nowa funkcjonalność (`feature/https-setup-stage-a`, `feature/certbot-auto-renew`, `feature/multi-user-jwt-wip`)
- `fix/` - poprawka błędu (`fix/cors-https-origin`, `fix/isbn-empty-string-duplicate`)

### Reszta nazwy

Krótki, opisowy slug w formie `<czasownik>-<rzeczownik>-<czego dotyczy>`: czasownik na początku (`add`, `fix`, `remove`), potem kontekst. Nazwa ma od razu mówić, co gałąź robi, bez czytania opisu PR-a - `fix/add-cors-production-origin` tłumaczy się sam.

**Bez numeracji etapów.** Styl `etap-01`, `etap-02` (z projektu SAPER) został tu świadomie odrzucony: pracujemy iteracyjnie, funkcja po funkcji i endpoint po endpoincie, a nie według liniowego planu, więc numer etapu nic by nie znaczył.

---

## Commity

Konwencja opisowa, nie formalna - świadomie **nie** używamy Conventional Commits (`feat:`, `fix:`, `docs:`).

### Wzorzec

```
<Czasownik w trybie rozkazującym> <co> (opcjonalnie: dlaczego / kontekst)
```

### Przykłady z historii repo

```
Add production origin (afterword.coffe.ink) to CORS allowedOrigins
Fix CORS: use https:// for production origin (was http://, causing 403 after HTTPS migration)
Add certbot service and ACME challenge location to nginx (HTTPS stage A: prepare for certificate generation)
Remove dead BookUpdateRequest DTO; complete shouldReturnZeroTotalPagesWhenNoElements test
```

### Zasady

1. **Czasownik na początku, w trybie rozkazującym** - "Add", "Fix", "Remove", nie "Added"/"Fixed". Commit *robi* coś, jest instrukcją, nie sprawozdaniem
2. **Pierwsza linia to zwięzłe podsumowanie** tego, co się zmieniło
3. **Kontekst w nawiasie albo po dwukropku**, gdy trzeba wyjaśnić *dlaczego*, nie tylko *co*. Szczególnie przy poprawkach błędów - za pół roku ma być wiadomo, co dokładnie było zepsute
4. **Body PR-a niesie pełniejsze wyjaśnienie.** Tytuł to nagłówek, `--body` to szczegóły i decyzje
5. **Język: angielski**, w tytule i w body. Dokumentacja projektu (`PROGRESS.md`, ten plik) jest po polsku, ale historia gita jest angielska w całości - mieszanie języków w jednym commicie wygląda niechlujnie

### Dlaczego nie Conventional Commits

Dodałyby ustandaryzowane prefiksy pasujące do nazw gałęzi (`feature/` → `feat:`, `fix/` → `fix:`) i umożliwiły automatyczne generowanie changelogów. To jednak dodatkowa dyscyplina, której solo-projekt nie potrzebuje - obecny zapis jest czytelny i działa. Do rozważenia, gdyby pojawiło się CI/CD z automatycznym changelogiem.

---

## Nazewnictwo metod: repozytorium vs serwis

### Repozytorium - krótkie nazwy w CRUD, opisowe w wyszukiwaniu

```java
findAll, findById, create, update, delete, countAll      // BookRepository - generyczne
searchBooks, getBooksByTitle, existsByTitleAndAuthor     // BookSearchRepository - opisowe
```

`BookRepository` (podstawowy CRUD) ma **krótkie** nazwy, bo kontekst - że chodzi o książki - wynika z nazwy klasy. `BookSearchRepository` ma nazwy **opisowe** (`searchBooks`, nie `search`), bo to osobny interfejs i nazwa metody powinna sama mówić, co robi.

### Serwis - zawsze `<Czasownik><Book/Books>`

```java
createBook       // repo: create
findBookById     // repo: findById
findAllBooks     // repo: findAll
updateBook       // repo: update
deleteBook       // repo: delete
```

Celowo **inaczej** niż w repozytorium: w kodzie wołającym (kontroler) widać wtedy od razu, na której warstwie się jest.

**Znane odstępstwo:** obecnie nie wszystkie metody serwisu trzymają ten wzorzec - część nie dodaje jawnego "Book"/"Books". Ujednolicenie jest w backlogu technicznym w `PROGRESS.md`.

---

## Praca z GitHubem (gh)

PR zakładany z terminala, zawsze na `develop`:

```bash
gh pr create --base develop --head feature/multi-user-jwt-wip \
  --title "Add multi-user support (JWT auth, per-user book isolation)" \
  --body "Adds users table, registration, JWT-based login/auth, and full user_id filtering across all book operations (backend + frontend). V5 (NOT NULL on user_id) intentionally excluded from this PR - will be added in a follow-up deployment after production data is backfilled to avoid breaking the migration on existing rows."
```

Merge zawsze ze spłaszczeniem historii i usunięciem gałęzi:

```bash
gh pr merge --squash --delete-branch
```

`--body` to miejsce na decyzje i świadome pominięcia (jak wyłączenie migracji V5 z powyższego PR-a) - to samo, czego szuka się potem w historii.
