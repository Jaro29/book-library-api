# PROGRESS.md — rest-api-workshop

## Aktualny etap
POST /books zamknięty ✅. Wybór kolejnego endpointu — patrz "Następne kroki".

## Zamknięte specy (kontrakt = źródło prawdy)

### POST /books
- Request: BookCreateRequest, query param `allowDuplicate` (bool, default false)
- 400: walidacja DTO (w tym ISBN checksum, gdy ISBN podany)
- 409: duplikat title+author, gdy allowDuplicate=false
- 201: sukces, zwraca BookResponse
- Status: **zaimplementowane, przetestowane end-to-end (curl), zcommitowane i wypchnięte** na `feature/post-books-endpoint`

## Decyzje architektoniczne
- Raw JDBC (NamedParameterJdbcTemplate) zamiast JPA — celowo, pod kątem pełnej kontroli nad SQL w przyszłych projektach wymagających bezpieczeństwa
- Mapowanie DTO ↔ model: statyczne metody w `BookMapper` (nie BeanUtils/reflection)
- Błędy domenowe: `ApiException` (bez znajomości HTTP) + `GlobalExceptionHandler` (@RestControllerAdvice) mapuje na kod HTTP — repozytorium/serwis nie znają HttpStatus
- finishDate zawsze opcjonalne, niezależnie od statusu (reguła "FINISHED wymaga finishDate" odrzucona)
- Branch model: main → develop → feature/<verb-noun>-<what>, bez numeracji etapów (inaczej niż w SAPER)

## Następne kroki
- [ ] Decyzja: PR feature/post-books-endpoint → develop teraz, czy kolejny endpoint na tej samej gałęzi?
- [ ] GET /books/{id} lub GET /books (lista) — do ustalenia który pierwszy
- [ ] Rozszerzenie GlobalExceptionHandler o kolejne przypadki (np. 404 not found) gdy się pojawią
## Na przyszłość
- [ ] CORS config (Spring `@CrossOrigin` / `WebMvcConfigurer`) — dopiero gdy zacznie się praca nad frontendem w Angularze, nie wcześniej