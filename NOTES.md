# Roadmap / Backlog

## Model Book — planowane rozszerzenia
- [ ] coverUrl – adres okładki
- [ ] dateAdded – data dodania do biblioteki
- [ ] favorite – oznaczenie ulubionych
- [ ] rereadCount – liczba ponownych przeczytań
- [ ] tags – lista tagów (String) lub osobna encja Tag (relacja many-to-many)
- [ ] private String publisher;
- [ ] private Integer publishYear;
- [ ] private String language;
- [ ] private String category;

- [ ] private String series;
- [ ] private Integer seriesNumber;

- [ ] private Integer pages;
- [ ] private Duration duration;
- [ ] private String notes;

- [ ] private BookOwnership ownership;
- [ ] private String source;

## Planowanye klasy do rozszerzeń

package pl.jaro.restapiworkshop.model;

public enum BookOwnership {
OWNED,
PUBLIC_LIBRARY,
FRIEND,
FAMILY,
SUBSCRIPTION,
OTHER
}

package pl.jaro.restapiworkshop.model;

public enum BookType {
PAPER,
EBOOK,
AUDIOBOOK
}

## Reguły biznesowe do zaimplementowania
- [ ] status == TO_READ → finishDate musi być null // TODO: Add business validation - finishDate must be null when status is TO_READ
- [ ] status == FINISHED → finishDate nie może być null // TODO: Add business validation - finishDate must not be null when status is FINISHED


## jeśli kiedyś dodam dynamiczne komunikaty błędów w walidatorach, trzeba zmienić testy na Mockito
