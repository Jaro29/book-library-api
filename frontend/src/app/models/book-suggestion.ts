export interface BookSuggestion {
  title: string;
  author: string;
  isbn: string | null;
  coverUrl: string | null;
  publicationYear: string | null;
  publisher: string | null;
}
