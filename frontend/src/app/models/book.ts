export interface Book {
  id: number;
  title: string;
  author: string;
  isbn: string | null;
  status: string;
  startDate: string | null;
  finishDate: string | null;
  timesRead: number;
  notes: string | null;
}
