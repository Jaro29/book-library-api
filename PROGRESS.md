# PROGRESS.md — rest-api-workshop

## Aktualny etap
Backend: pełny CRUD + CORS zamknięte ✅. Następny krok: start frontendu w Angularze (decyzja o strukturze repo w toku).

## Zamknięte specy (kontrakt = źródło prawdy)

### POST /books
- Request: BookCreateRequest, query param `allowDuplicate` (bool, default false)
- 400: walidacja DTO (w tym ISBN checksum, gdy ISBN podany)
- 409: duplikat title+author, gdy allowDuplicate=false
- 201: sukces, zwraca BookResponse
- Status: **zaimplementowane, przetestowane end-to-end, zmergowane do develop**

### GET /books/{id}
- 200: zwraca BookResponse
- 404: BookNotFoundException, gdy id nie istnieje
- Status: **zaimplementowane, przetestowane end-to-end, zmergowane do develop**

### GET /books (lista + paginacja)
- Query params: `page` (default 0), `pageSize` (default 20)
- Response: `PageResponse<BookResponse>` (content, page, pageSize, totalElements, totalPages)
- Status: **zaimplementowane, przetestowane end-to-end (2 strony, 15 rekordów seed), zmergowane do develop**

### PATCH /books/{id}
- Request: BookPatchRequest, wszystkie pola opcjonalne
- Uproszczenie: pole `null` = "nie zmieniaj" (nie da się wyczyścić pola przez PATCH) — świadomie odłożone, patrz "Na przyszłość"
- 404: BookNotFoundException, gdy id nie istnieje
- 200: sukces, zwraca zaktualizowany BookResponse
- Status: **zaimplementowane, przetestowane end-to-end, zmergowane do develop**
- Naprawiony bug: BookMapper.toBook(patchRequest, book) nie ustawiał `.id(...)`, co powodowało błędny update z `id: null`

### DELETE /books/{id}
- 204: sukces, bez body
- 404: BookNotFoundException, gdy id nie istnieje
- Status: **zaimplementowane, przetestowane end-to-end, zmergowane do develop**
- Repository/Service zmienione z `boolean` na `void` (boolean nigdy realnie nie zwracał false — zawsze wyjątek albo true)

### CORS
- WebMvcConfigurer, `/**`, allowedOrigins `http://localhost:4200`, metody GET/POST/PATCH/DELETE
- Status: **skonfigurowane, zmergowane do develop**

## Decyzje architektoniczne
- Raw JDBC (NamedParameterJdbcTemplate) zamiast JPA — celowo, pod kątem pełnej kontroli nad SQL w przyszłych projektach wymagających bezpieczeństwa
- Mapowanie DTO ↔ model: statyczne metody w `BookMapper` (nie BeanUtils/reflection)
- Błędy domenowe: dwa osobne wyjątki bez znajomości HTTP — `ApiException` (409, duplikat/błąd ogólny) i `BookNotFoundException` (404) — `GlobalExceptionHandler` (@RestControllerAdvice) mapuje je na kody HTTP; repozytorium/serwis nie znają HttpStatus
- finishDate zawsze opcjonalne, niezależnie od statusu (reguła "FINISHED wymaga finishDate" odrzucona)
- Branch model: main → develop → feature/<verb-noun>-<what>, bez numeracji etapów (inaczej niż w SAPER)
- Metody repo/service zwracające `void` zamiast `boolean`, gdy jedyna droga niesukcesu to wyjątek (delete)
- data-dev.sql (seed danych) odseparowany od kontekstu testowego — ładowany tylko przez application-dev.yml (`spring.sql.init.data-locations`), nie przez domyślną konwencję `data.sql`

## Na przyszłość
- [ ] CORS: rozszerzyć o origin produkcyjny, gdy powstanie deployment
- [ ] PATCH: rozważyć rozróżnienie "pole pominięte" vs "pole = null" (np. JsonNullable), jeśli pojawi się potrzeba czyszczenia pól
- [ ] GlobalExceptionHandler: rozszerzyć o kolejne przypadki błędów, jeśli się pojawią

## Następne kroki
- [ ] Decyzja: frontend Angular w tym samym repo (folder `frontend/`) czy osobne repo?
- [ ] Rozpoczęcie pracy nad frontendem w Angularze